package eu.greev.twofa.commands;

import eu.greev.twofa.Main;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.HashingUtils;
import eu.greev.twofa.utils.MySQLMethodes;
import eu.greev.twofa.utils.TwoFactorState;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.security.GeneralSecurityException;
import java.util.Set;

public class TwoFACommand extends Command {
    private final String helpMessage = Main.getInstance().getConfig().getString("messages.help").replace("&", "§");
    private final String alreadyActive = Main.getInstance().getConfig().getString("messages.alreadyactive").replace("&", "§");
    private final String activated = Main.getInstance().getConfig().getString("messages.activated").replace("&", "§");
    private final String removeAuth = Main.getInstance().getConfig().getString("messages.removeauth").replace("&", "§");
    private final String notLoggedIn = Main.getInstance().getConfig().getString("messages.notloggedin").replace("&", "§");
    private final String logoutMessage = Main.getInstance().getConfig().getString("messages.logoutmessage").replace("&", "§");
    private final String servername = Main.getInstance().getConfig().getString("servername").replace("&", "§");
    private final String missingCode = Main.getInstance().getConfig().getString("messages.missingcode").replace("&", "§");
    private final String errorOccurred = Main.getInstance().getConfig().getString("messages.errorocurred").replace("&", "§");
    private final String codeIsInvalid = Main.getInstance().getConfig().getString("messages.codeisinvalid").replace("&", "§");
    private final String successfulActivated = Main.getInstance().getConfig().getString("messages.successfulcctivated").replace("&", "§");
    private final String hovertext = Main.getInstance().getConfig().getString("messages.hovertext").replace("&", "§");
    private final String disableforforced = Main.getInstance().getConfig().getString("messages.disableforforced").replace("&", "§");

    public TwoFACommand() {
        super("2fa");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        if (args.length != 0) {
            switch (args[0].toLowerCase()) {
                case "enable":
                    enableTFA(player);
                    break;
                case "disable":
                    disableTFA(player);
                    break;
                case "logout":
                    logout(player);
                    break;
                case "activate":
                    if (args.length == 2) {
                        activate(player, args[1]);
                    } else {
                        player.sendMessage(new TextComponent(missingCode));
                    }
                    break;
                default:
                    player.sendMessage(new TextComponent(helpMessage));
            }
        } else {
            player.sendMessage(new TextComponent(helpMessage));
        }
    }

    private void activate(ProxiedPlayer player, String code) {
        String uuid = player.getUniqueId().toString();
        Spieler spieler = Spieler.get(player.getUniqueId());

        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (spieler.getTwoFactorState() != TwoFactorState.ACTIVATED) {
                player.sendMessage(new TextComponent(notLoggedIn));
                return;
            }

            try {
                String secret = spieler.getSecret();

                Set<String> validCodes = Main.getInstance()
                        .getTwoFactorAuthUtil()
                        .generateNumbersWithOffset(secret, Main.getMILLISECOND_TIMING_THRESHOLD());

                if (!validCodes.contains(code)) {
                    player.sendMessage(new TextComponent(codeIsInvalid));
                    return;
                }

                player.sendMessage(new TextComponent(successfulActivated));

                String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

                MySQLMethodes.setIP(player.getUniqueId().toString(), hashedIp);
                MySQLMethodes.setState(player.getUniqueId().toString(), TwoFactorState.ACTIVE);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(errorOccurred));
            }
        });
    }

    private void logout(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            Spieler spieler = Spieler.get(player.getUniqueId());
            if (spieler.getAuthState() == AuthState.AUTHENTICATED) {
                player.sendMessage(new TextComponent(logoutMessage));
                MySQLMethodes.setState(player.getUniqueId().toString(), TwoFactorState.LOGOUT);
            } else {
                player.sendMessage(new TextComponent(notLoggedIn));
            }
        });
    }

    private void disableTFA(ProxiedPlayer player) {
        if (player.hasPermission("2fa.forceenable")) {
            player.sendMessage(new TextComponent(disableforforced));
            return;
        }
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            Spieler spieler = Spieler.get(player.getUniqueId());
            if (spieler.getAuthState() == AuthState.AUTHENTICATED) {
                MySQLMethodes.removePlayer(player.getUniqueId().toString());
                player.sendMessage(new TextComponent(removeAuth));
            } else {
                player.sendMessage(new TextComponent(notLoggedIn));
            }
        });
    }

    private void enableTFA(ProxiedPlayer player) {
        Spieler spieler = Spieler.get(player.getUniqueId());
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (spieler.getAuthState() == AuthState.AUTHENTICATED || spieler.getAuthState() == AuthState.WAITING_FOR_AUTH) {
                player.sendMessage(new TextComponent(alreadyActive));
                return;
            }
            String secret = Main.getInstance().getTwoFactorAuthUtil().generateBase32Secret();

            spieler.setSecret(secret);

            String url = Main.getInstance().getTwoFactorAuthUtil().qrImageUrl(player.getName(), servername, secret);
            String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

            MySQLMethodes.addNewPlayer(
                    player.getUniqueId().toString(),
                    secret,
                    hashedIp,
                    TwoFactorState.ACTIVATED
            );

            spieler.setTwoFactorState(TwoFactorState.ACTIVATED);

            TextComponent message = new TextComponent(activated
                    .replace("%secret%", secret)
                    .replace("%link%", url)
            );

            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hovertext).create()));
            System.out.println(url);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

            player.sendMessage(message);
        });
    }
}

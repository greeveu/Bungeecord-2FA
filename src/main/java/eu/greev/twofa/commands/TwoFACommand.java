package eu.greev.twofa.commands;

import eu.greev.twofa.TwoFactorAuth;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.*;
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
    private final String helpMessage = TwoFactorAuth.getInstance().getConfig().getString("messages.help").replace("&", "§");
    private final String alreadyActive = TwoFactorAuth.getInstance().getConfig().getString("messages.alreadyactive").replace("&", "§");
    private final String activated = TwoFactorAuth.getInstance().getConfig().getString("messages.activated").replace("&", "§");
    private final String removeAuth = TwoFactorAuth.getInstance().getConfig().getString("messages.removeauth").replace("&", "§");
    private final String notLoggedIn = TwoFactorAuth.getInstance().getConfig().getString("messages.notloggedin").replace("&", "§");
    private final String logoutMessage = TwoFactorAuth.getInstance().getConfig().getString("messages.logoutmessage").replace("&", "§");
    private final String serverName = TwoFactorAuth.getInstance().getConfig().getString("servername").replace("&", "§");
    private final String missingCode = TwoFactorAuth.getInstance().getConfig().getString("messages.missingcode").replace("&", "§");
    private final String errorOccurred = TwoFactorAuth.getInstance().getConfig().getString("messages.errorocurred").replace("&", "§");
    private final String codeIsInvalid = TwoFactorAuth.getInstance().getConfig().getString("messages.codeisinvalid").replace("&", "§");
    private final String successfulActivated = TwoFactorAuth.getInstance().getConfig().getString("messages.successfulcctivated").replace("&", "§");
    private final String hoverText = TwoFactorAuth.getInstance().getConfig().getString("messages.hovertext").replace("&", "§");
    private final String disableforforced = TwoFactorAuth.getInstance().getConfig().getString("messages.disableforforced").replace("&", "§");

    public TwoFACommand() {
        super("2fa");
    }

    private void sendHelpMessage(ProxiedPlayer player) {
        player.sendMessage(new TextComponent(this.helpMessage.replace("&", "§")));
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

        if (args.length == 0) {
            player.sendMessage(new TextComponent(helpMessage));
            return;
        }

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
    }

    private void activate(ProxiedPlayer player, String code) {
        String uuid = player.getUniqueId().toString();
        Spieler spieler = Spieler.get(player.getUniqueId());

        ProxyServer.getInstance().getScheduler().runAsync(TwoFactorAuth.getInstance(), () -> {
            if (spieler.getTwoFactorState() != TwoFactorState.ACTIVATED) {
                player.sendMessage(new TextComponent(notLoggedIn));
                return;
            }

            try {
                String secret = spieler.getSecret();

                Set<String> validCodes = TwoFactorAuth.getInstance()
                        .getTwoFactorAuthUtil()
                        .generateNumbersWithOffset(secret, TwoFactorAuth.getMILLISECOND_TIMING_THRESHOLD());

                if (!validCodes.contains(code)) {
                    player.sendMessage(new TextComponent(codeIsInvalid));
                    return;
                }

                player.sendMessage(new TextComponent(successfulActivated));

                String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

                MySQLMethods.setIP(uuid, hashedIp);
                MySQLMethods.setState(uuid, TwoFactorState.ACTIVE);

                spieler.setAuthState(AuthState.AUTHENTICATED);
                spieler.setTwoFactorState(TwoFactorState.ACTIVE);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(errorOccurred));
            }
        });
    }

    private void logout(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(TwoFactorAuth.getInstance(), () -> {
            Spieler spieler = Spieler.get(player.getUniqueId());
            if (spieler.getAuthState() == AuthState.AUTHENTICATED) {
                player.sendMessage(new TextComponent(logoutMessage));
                MySQLMethods.setState(player.getUniqueId().toString(), TwoFactorState.LOGOUT);
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
        ProxyServer.getInstance().getScheduler().runAsync(TwoFactorAuth.getInstance(), () -> {
            Spieler spieler = Spieler.get(player.getUniqueId());
            if (spieler.getAuthState() == AuthState.AUTHENTICATED) {
                MySQLMethods.removePlayer(player.getUniqueId().toString());
                player.sendMessage(new TextComponent(removeAuth));
            } else {
                player.sendMessage(new TextComponent(notLoggedIn));
            }
        });
    }

    private void enableTFA(ProxiedPlayer player) {
        Spieler spieler = Spieler.get(player.getUniqueId());
        ProxyServer.getInstance().getScheduler().runAsync(TwoFactorAuth.getInstance(), () -> {
            if (spieler.getTwoFactorState() == TwoFactorState.ACTIVATED) {
                sendEnableMessage(player, spieler.getSecret());
                return;
            }
            if (spieler.getAuthState() == AuthState.AUTHENTICATED || spieler.getAuthState() == AuthState.WAITING_FOR_AUTH) {
                player.sendMessage(new TextComponent(alreadyActive));
                return;
            }
            String secret = TwoFactorAuth.getInstance().getTwoFactorAuthUtil().generateBase32Secret();

            spieler.setSecret(secret);

            String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

            MySQLMethods.addNewPlayer(
                    player.getUniqueId().toString(),
                    secret,
                    hashedIp,
                    TwoFactorState.ACTIVATED
            );

            spieler.setTwoFactorState(TwoFactorState.ACTIVATED);

            sendEnableMessage(player, secret);
        });
    }

    private void sendEnableMessage(ProxiedPlayer player, String secret) {
        String url = TwoFactorAuthUtil.qrImageUrl(serverName.trim() + ":" + player.getName(), serverName.trim(), secret, 6, 128);

        TextComponent message = new TextComponent(activated
                .replace("%secret%", secret)
                .replace("%link%", url)
        );

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        player.sendMessage(message);
    }
}

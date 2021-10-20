package eu.greev.twofa.commands;

import eu.greev.twofa.Main;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TwoFACommand extends Command {
    private final String helpMessage = Main.getInstance().getConfig().getString("messages.help");
    private final String alreadyActive = Main.getInstance().getConfig().getString("messages.alreadyactive");
    private final String activated = Main.getInstance().getConfig().getString("messages.activated");
    private final String removeAuth = Main.getInstance().getConfig().getString("messages.removeauth");
    private final String notLoggedIn = Main.getInstance().getConfig().getString("messages.notloggedin");
    private final String logoutMessage = Main.getInstance().getConfig().getString("messages.logoutmessage");
    private final String servername = Main.getInstance().getConfig().getString("servername");
    private final String missingCode = Main.getInstance().getConfig().getString("messages.missingcode");
    private final String errorOccurred = Main.getInstance().getConfig().getString("messages.errorocurred");
    private final String codeIsInvalid = Main.getInstance().getConfig().getString("messages.codeisinvalid");
    private final String successfulActivated = Main.getInstance().getConfig().getString("messages.successfulcctivated");
    private final String hovertext = Main.getInstance().getConfig().getString("messages.hovertext");

    public TwoFACommand() {
        super("2fa");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (player.hasPermission("2fa.use")) {
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
                                player.sendMessage(new TextComponent(missingCode.replace("&", "§")));
                            }
                            break;
                        default:
                            player.sendMessage(new TextComponent(helpMessage.replace("&", "§")));
                    }
                } else {
                    player.sendMessage(new TextComponent(helpMessage.replace("&", "§")));
                }
            }
        }
    }

    private void activate(ProxiedPlayer player, String code) {
        String uuid = player.getUniqueId().toString();
        Spieler spieler = Main.getSpieler(player);
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            Optional<String> lastIP = MySQLMethodes.getLastIP(uuid);

            if (!lastIP.isPresent()) {
                player.sendMessage(notLoggedIn.replace("&", "§"));
                return;
            }

            if (!lastIP.get().equals("just_activated")) {
                return;
            }

            try {
                String secret = spieler.getSecret();

                List<String> validCodes = Arrays.asList(
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret),
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret, System.currentTimeMillis() - 30000),
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret, System.currentTimeMillis() + 30000)
                );

                if (!validCodes.contains(code)) {
                    player.sendMessage(new TextComponent(codeIsInvalid.replace("&", "§")));
                    return;
                }

                player.sendMessage(new TextComponent(successfulActivated.replace("&", "§")));

                ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(),
                        () -> MySQLMethodes.setIP(player.getUniqueId().toString(), player.getPendingConnection().getAddress().getAddress().toString()));
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(errorOccurred.replace("&", "§")));
            }
        });
    }

    private void logout(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (MySQLMethodes.hasRecord(player.getUniqueId().toString())) {
                player.sendMessage(new TextComponent(logoutMessage.replace("&", "§")));
                MySQLMethodes.setIP(player.getUniqueId().toString(), "logout");
            } else {
                player.sendMessage(new TextComponent(notLoggedIn.replace("&", "§")));
            }
        });
    }

    private void disableTFA(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (MySQLMethodes.hasRecord(player.getUniqueId().toString())) {
                MySQLMethodes.removePlayer(player.getUniqueId().toString());
                player.sendMessage(new TextComponent(removeAuth.replace("&", "§")));
            } else {
                player.sendMessage(new TextComponent(notLoggedIn.replace("&", "§")));
            }
        });
    }

    private void enableTFA(ProxiedPlayer player) {
        Spieler spieler = Main.getSpieler(player);
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (MySQLMethodes.hasRecord(player.getUniqueId().toString())) {
                player.sendMessage(new TextComponent(alreadyActive.replace("&", "§")));
                return;
            }
            String secret = Main.getInstance().twoFactorAuthUtil.generateBase32Secret();

            spieler.setSecret(secret);
            String url = Main.getInstance().twoFactorAuthUtil.qrImageUrl(player.getName(), servername, secret);

            MySQLMethodes.addNewPlayer(player.getUniqueId().toString(), secret, "just_activated");
            TextComponent message = new TextComponent(activated
                    .replace("&", "§")
                    .replace("%secret%", secret)
                    .replace("%link%", url)
            );

            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hovertext.replace("&", "§")).create()));

            player.sendMessage(message);
        });
    }
}

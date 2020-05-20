package eu.greev.twofa.commands;

import eu.greev.twofa.Main;
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

public class TwoFACommand extends Command {
    String helpMessage = Main.getInstance().config.getString("messages.help");
    String alreadyActive = Main.getInstance().config.getString("messages.alreadyactive");
    String activated = Main.getInstance().config.getString("messages.activated");
    String removeAuth = Main.getInstance().config.getString("messages.removeauth");
    String notLoggedIn = Main.getInstance().config.getString("messages.notloggedin");
    String logoutMessage = Main.getInstance().config.getString("messages.logoutmessage");
    String servername = Main.getInstance().config.getString("servername");
    String missingCode = Main.getInstance().config.getString("messages.missingcode");
    String errorOccurred = Main.getInstance().config.getString("messages.errorocurred");
    String codeIsInvalid = Main.getInstance().config.getString("messages.codeisinvalid");
    String successfulActivated = Main.getInstance().config.getString("messages.successfulcctivated");
    String hovertext = Main.getInstance().config.getString("messages.hovertext");

    public TwoFACommand() {
        super("2fa");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
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
                            player.sendMessage(missingCode.replace("&", "§"));
                        }
                        break;
                }
            } else {
                player.sendMessage(helpMessage.replace("&", "§"));
            }
        }
    }

    private void activate(ProxiedPlayer player, String code) {
        String uuid = player.getUniqueId().toString();
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
                    if (MySQLMethodes.hasRecord(uuid)) {
                        if (MySQLMethodes.getLastIP(uuid).equalsIgnoreCase("just_activated")) {
                            try {
                                String secret = MySQLMethodes.getSecret(player.getUniqueId().toString());
                                if (Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret).equals(code) ||
                                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret, System.currentTimeMillis() - 30000).equals(code) || //-30 Seconds in case the users time isnt exactly correct and / or he wasnt fast enough
                                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret, System.currentTimeMillis() + 30000).equals(code)) { //+30 Seconds in case the users time isnt exactly correct and / or he wasnt fast enough
                                    MySQLMethodes.setIP(uuid, player.getPendingConnection().getAddress().getAddress().toString());
                                    player.sendMessage(successfulActivated.replace("&", "§"));
                                } else {
                                    player.sendMessage(codeIsInvalid.replace("&", "§"));
                                }
                            } catch (GeneralSecurityException e) {
                                e.printStackTrace();
                                player.sendMessage(errorOccurred.replace("&", "§"));
                            }
                        }
                    } else {
                        player.sendMessage(notLoggedIn.replace("&", "§"));
                    }
                }
        );
    }

    private void logout(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
                    if (MySQLMethodes.hasRecord(player.getUniqueId().toString()) && !MySQLMethodes.getLastIP(player.getUniqueId().toString()).equals("just_activated")) {
                        player.sendMessage(logoutMessage.replace("&", "§"));
                        MySQLMethodes.setIP(player.getUniqueId().toString(), "logout");
                    } else {
                        player.sendMessage(notLoggedIn.replace("&", "§"));
                    }
                }
        );
    }

    private void disableTFA(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
                    if (MySQLMethodes.hasRecord(player.getUniqueId().toString())) {
                        MySQLMethodes.removePlayer(player.getUniqueId().toString());
                        player.sendMessage(removeAuth.replace("&", "§"));
                    } else {
                        player.sendMessage(notLoggedIn.replace("&", "§"));
                    }
                }
        );
    }

    private void enableTFA(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
                    if (MySQLMethodes.hasRecord(player.getUniqueId().toString())) {
                        player.sendMessage(alreadyActive.replace("&", "§"));
                        return;
                    }
                    String secret = Main.getInstance().twoFactorAuthUtil.generateBase32Secret();
                    MySQLMethodes.addNewPlayer(player.getUniqueId().toString(), secret, "just_activated");
                    TextComponent message = new TextComponent(activated.replace("&", "§").replace("%secret%", secret).replace("%link%", Main.getInstance().twoFactorAuthUtil.qrImageUrl(player.getName(), servername, secret)));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Main.getInstance().twoFactorAuthUtil.qrImageUrl(player.getName(), servername, secret)));
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hovertext.replace("&", "§")).create()));
                    player.sendMessage(message);
                }
        );
    }
}

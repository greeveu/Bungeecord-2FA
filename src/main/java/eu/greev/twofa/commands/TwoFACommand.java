package eu.greev.twofa.commands;

import eu.greev.twofa.Main;
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
    private final String helpMessage = Main.getInstance().getConfig().getString("messages.help");
    private final String alreadyActive = Main.getInstance().getConfig().getString("messages.alreadyactive");
    private final String activated = Main.getInstance().getConfig().getString("messages.activated");
    private final String removeAuth = Main.getInstance().getConfig().getString("messages.removeauth");
    private final String notLoggedIn = Main.getInstance().getConfig().getString("messages.notloggedin");
    private final String logoutMessage = Main.getInstance().getConfig().getString("messages.logoutmessage");
    private final String serverName = Main.getInstance().getConfig().getString("servername");
    private final String missingCode = Main.getInstance().getConfig().getString("messages.missingcode");
    private final String errorOccurred = Main.getInstance().getConfig().getString("messages.errorocurred");
    private final String codeIsInvalid = Main.getInstance().getConfig().getString("messages.codeisinvalid");
    private final String successfulActivated = Main.getInstance().getConfig().getString("messages.successfulcctivated");
    private final String hoverText = Main.getInstance().getConfig().getString("messages.hovertext");

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
        if (args.length == 0) {
            this.sendHelpMessage(player);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "enable":
                this.enableTFA(player);
                break;
            case "disable":
                this.disableTFA(player);
                break;
            case "logout":
                this.logout(player);
                break;
            case "activate":
                if (args.length == 2) {
                    this.activate(player, args[1]);
                } else {
                    player.sendMessage(new TextComponent(this.missingCode.replace("&", "§")));
                }
                break;
            default:
                this.sendHelpMessage(player);
        }
    }

    private void activate(ProxiedPlayer player, String code) {
        String uuid = player.getUniqueId().toString();
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (!Main.getInstance().getMySQLMethods().hasRecord(uuid)) {
                player.sendMessage(new TextComponent(this.notLoggedIn.replace("&", "§")));
                return;
            }
            if (!Main.getInstance().getMySQLMethods().getLastIP(uuid).equalsIgnoreCase("just_activated")) {
                return;
            }

            try {
                String secret = Main.getInstance().getMySQLMethods().getSecret(player.getUniqueId().toString());
                if (Main.getInstance().getTwoFactorAuthUtil().generateCurrentNumber(secret).equals(code)
                    || Main.getInstance().getTwoFactorAuthUtil().generateCurrentNumber(secret, System.currentTimeMillis() - 30000).equals(code)  //-30 Seconds in case the users time isnt exactly correct and / or he wasnt fast enough
                    || Main.getInstance().getTwoFactorAuthUtil().generateCurrentNumber(secret, System.currentTimeMillis() + 30000).equals(code)) //+30 Seconds in case the users time isnt exactly correct and / or he wasnt fast enough
                {
                    Main.getInstance().getMySQLMethods().setIP(uuid, player.getPendingConnection().getAddress().getAddress().toString());
                    player.sendMessage(new TextComponent(this.successfulActivated.replace("&", "§")));
                } else {
                    player.sendMessage(new TextComponent(this.codeIsInvalid.replace("&", "§")));
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(this.errorOccurred.replace("&", "§")));
            }
        });
    }

    private void logout(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (Main.getInstance().getMySQLMethods().hasRecord(player.getUniqueId().toString())
                && !Main.getInstance().getMySQLMethods().getLastIP(player.getUniqueId().toString()).equals("just_activated")
            ) {
                player.sendMessage(new TextComponent(this.logoutMessage.replace("&", "§")));
                Main.getInstance().getMySQLMethods().setIP(player.getUniqueId().toString(), "logout");
            } else {
                player.sendMessage(new TextComponent(this.notLoggedIn.replace("&", "§")));
            }
        });
    }

    private void disableTFA(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (Main.getInstance().getMySQLMethods().hasRecord(player.getUniqueId().toString())) {
                Main.getInstance().getMySQLMethods().removePlayer(player.getUniqueId().toString());
                player.sendMessage(new TextComponent(this.removeAuth.replace("&", "§")));
            } else {
                player.sendMessage(new TextComponent(this.notLoggedIn.replace("&", "§")));
            }
        });
    }

    private void enableTFA(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            if (Main.getInstance().getMySQLMethods().hasRecord(player.getUniqueId().toString())) {
                player.sendMessage(new TextComponent(this.alreadyActive.replace("&", "§")));
                return;
            }
            String secret = Main.getInstance().getTwoFactorAuthUtil().generateBase32Secret();
            Main.getInstance().getMySQLMethods().addNewPlayer(player.getUniqueId().toString(), secret, "just_activated");
            TextComponent message = new TextComponent(this.activated.replace("&", "§").replace("%secret%", secret).replace("%link%", Main.getInstance().getTwoFactorAuthUtil().qrImageUrl(player.getName(), this.serverName, secret)));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Main.getInstance().getTwoFactorAuthUtil().qrImageUrl(player.getName(), this.serverName, secret)));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.hoverText.replace("&", "§")).create()));
            player.sendMessage(message);
        });
    }
}

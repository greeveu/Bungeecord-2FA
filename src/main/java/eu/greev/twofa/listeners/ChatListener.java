package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class ChatListener implements Listener {

    String waitingForAuthCode = Main.getInstance().config.getString("messages.waitingforauthcode");
    String errorOcurred = Main.getInstance().config.getString("messages.errorocurred");
    String loginSuccessful = Main.getInstance().config.getString("messages.loginsuccessful");
    String codeIsInvalid = Main.getInstance().config.getString("messages.codeisinvalid");

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        Spieler spieler = Main.getSpieler(player);
        String message = event.getMessage();

        if (spieler.isAuthenticated()) {
            return;
        }

        event.setCancelled(true);

        if (spieler.isWaitingForAuth() && message.length() == 6) {
            player.sendMessage(waitingForAuthCode.replace("&", "§"));

            try {
                String secret = spieler.getSecret();

                List<String> validCodes = Arrays.asList(
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret),
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret, System.currentTimeMillis() - 30000),
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret, System.currentTimeMillis() + 30000)
                );

                //The Code was Invalid
                if (!validCodes.contains(message)) {
                    player.sendMessage(codeIsInvalid.replace("&", "§"));
                    return;
                }

                spieler.setWaitingForAuth(false);
                spieler.setAuthenticated(true);
                player.sendMessage(loginSuccessful.replace("&", "§"));

                MySQLMethodes.setIP(player.getUniqueId().toString(), player.getPendingConnection().getAddress().getAddress().toString());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(errorOcurred.replace("&", "§"));
            }
        }
    }
}

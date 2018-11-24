package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.security.GeneralSecurityException;

public class ChatListener implements Listener {

    String waitingForAuthCode = Main.getInstance().config.getString("messages.waitingforauthcode");
    String errorOcurred = Main.getInstance().config.getString("messages.errorocurred");
    String loginSuccessful = Main.getInstance().config.getString("messages.loginsuccessful");

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();
        if (Main.getInstance().waitingForAuth.contains(player)) {
            if (message.length() == 6) {
                try {
                    if (Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(MySQLMethodes.getSecret(player.getUniqueId().toString())).equals(message)) {
                        Main.getInstance().waitingForAuth.remove(player);
                        MySQLMethodes.setIP(player.getUniqueId().toString(), player.getPendingConnection().getAddress().getAddress().toString());
                        player.sendMessage(loginSuccessful.replace("&", "§"));
                        event.setCancelled(true);
                        return;
                    } else {
                        player.sendMessage(waitingForAuthCode.replace("&", "§"));
                    }
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    player.sendMessage(errorOcurred.replace("&", "§"));
                }
            } else {
                player.sendMessage(waitingForAuthCode.replace("&", "§"));
            }
            event.setCancelled(true);
        }
    }
}

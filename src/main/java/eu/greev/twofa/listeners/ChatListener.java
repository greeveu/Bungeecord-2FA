package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.security.GeneralSecurityException;

public class ChatListener implements Listener {

    String waitingForAuthCode = Main.getInstance().getConfig().getString("messages.waitingforauthcode");
    String errorOcurred = Main.getInstance().getConfig().getString("messages.errorocurred");
    String loginSuccessful = Main.getInstance().getConfig().getString("messages.loginsuccessful");
    String codeIsInvalid = Main.getInstance().getConfig().getString("messages.codeisinvalid");

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();
        if (Main.getInstance().getWaitingForAuth().contains(player)) {
            event.setCancelled(true);
            if (message.length() != 6) {
                player.sendMessage(waitingForAuthCode.replace("&", "§"));
                return;
            }
            try {
                String secret = MySQLMethodes.getSecret(player.getUniqueId().toString());
                if (Main.getInstance().getTwoFactorAuthUtil().generateCurrentNumber(secret).equals(message) ||
                        Main.getInstance().getTwoFactorAuthUtil().generateCurrentNumber(secret, System.currentTimeMillis() - 30000).equals(message) || //-30 Seconds in case the users time isnt exactly correct and / or he wasnt fast enough
                        Main.getInstance().getTwoFactorAuthUtil().generateCurrentNumber(secret, System.currentTimeMillis() + 30000).equals(message)) { //+30 Seconds in case the users time isnt exactly correct and / or he wasnt fast enough
                    Main.getInstance().getWaitingForAuth().remove(player);
                    MySQLMethodes.setIP(player.getUniqueId().toString(), player.getPendingConnection().getAddress().getAddress().toString());
                    player.sendMessage(loginSuccessful.replace("&", "§"));
                } else {
                    player.sendMessage(codeIsInvalid.replace("&", "§"));
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(errorOcurred.replace("&", "§"));
            }
        }
    }
}

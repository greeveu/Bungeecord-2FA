package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ChatListener implements Listener {

    private final String waitingForAuthCode = Main.getInstance().getConfig().getString("messages.waitingforauthcode");
    private final String errorOcurred = Main.getInstance().getConfig().getString("messages.errorocurred");
    private final String loginSuccessful = Main.getInstance().getConfig().getString("messages.loginsuccessful");
    private final String codeIsInvalid = Main.getInstance().getConfig().getString("messages.codeisinvalid");

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        Spieler spieler = Main.getSpieler(player);
        String message = event.getMessage();

        if (spieler.getAuthState() != AuthState.WAITING_FOR_AUTH) {
            return;
        }

        event.setCancelled(true);

        if (message.length() == 6) {

            try {
                String secret = spieler.getSecret();

                List<String> validCodes = Arrays.asList(
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret),
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret, Instant.now().toEpochMilli() - 30000),
                        Main.getInstance().twoFactorAuthUtil.generateCurrentNumber(secret, Instant.now().toEpochMilli() + 30000)
                );

                //The Code was Invalid
                if (!validCodes.contains(message)) {
                    player.sendMessage(codeIsInvalid.replace("&", "§"));
                    return;
                }

                spieler.setAuthState(AuthState.AUTHENTICATED);
                player.sendMessage(loginSuccessful.replace("&", "§"));

                MySQLMethodes.setIP(player.getUniqueId().toString(), player.getPendingConnection().getAddress().getAddress().toString());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(errorOcurred.replace("&", "§"));
            }
        } else {
            player.sendMessage(waitingForAuthCode.replace("&", "§"));
        }
    }
}

package eu.greev.twofa.listeners;

import eu.greev.twofa.TwoFactorAuth;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.HashingUtils;
import eu.greev.twofa.utils.MySQLMethods;
import eu.greev.twofa.utils.TwoFactorState;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.security.GeneralSecurityException;
import java.util.Set;

public class ChatListener implements Listener {

    private final String waitingForAuthCode = TwoFactorAuth.getInstance().getConfig().getString("messages.waitingforauthcode").replace("&", "§");
    private final String errorOccurred = TwoFactorAuth.getInstance().getConfig().getString("messages.errorocurred").replace("&", "§");
    private final String loginSuccessful = TwoFactorAuth.getInstance().getConfig().getString("messages.loginsuccessful").replace("&", "§");
    private final String codeIsInvalid = TwoFactorAuth.getInstance().getConfig().getString("messages.codeisinvalid").replace("&", "§");
    private final String forceenable = TwoFactorAuth.getInstance().getConfig().getString("messages.forceenable").replace("&", "§");

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        Spieler spieler = Spieler.get(player.getUniqueId());
        String message = event.getMessage();

        if (spieler.getAuthState() == AuthState.AUTHENTICATED || spieler.getAuthState() == AuthState.NOT_ENABLED) {
            return;
        }

        event.setCancelled(true);

        //If message is not a 2fa token return
        if (message.length() != 6) {
            if (spieler.getAuthState() == AuthState.FORCED_ENABLE) { //TODO: Eww, format this if else
                if (message.toLowerCase().startsWith("/2fa")) {
                    event.setCancelled(false);
                    return;
                }
                player.sendMessage(new TextComponent(forceenable));
            } else {
                player.sendMessage(new TextComponent(waitingForAuthCode));
            }
            return;
        }

        try {
            String secret = spieler.getSecret();

            Set<String> validCodes = TwoFactorAuth.getInstance().getTwoFactorAuthUtil().generateNumbersWithOffset(secret, TwoFactorAuth.getMILLISECOND_TIMING_THRESHOLD());

            //The Code was Invalid
            if (!validCodes.contains(message)) {
                player.sendMessage(new TextComponent(codeIsInvalid));
                return;
            }

            spieler.setAuthState(AuthState.AUTHENTICATED);
            ProxyServer.getInstance().getScheduler()
                    .runAsync(TwoFactorAuth.getInstance(), () -> MySQLMethods.setState(player.getUniqueId().toString(), TwoFactorState.ACTIVE));

            player.sendMessage(new TextComponent(loginSuccessful));

            String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());
            MySQLMethods.setIP(player.getUniqueId().toString(), hashedIp);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            player.sendMessage(new TextComponent(errorOccurred));
        }
    }

    @EventHandler
    public void onTab(TabCompleteEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        Spieler spieler = Spieler.get(player.getUniqueId());

        if (spieler.getAuthState() == AuthState.WAITING_FOR_AUTH) {
            event.setCancelled(true);
        }
    }
}

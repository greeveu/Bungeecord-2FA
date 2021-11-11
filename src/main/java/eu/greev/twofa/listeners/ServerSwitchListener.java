package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.MySQLMethodes;
import eu.greev.twofa.utils.TwoFactorState;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Optional;

public class ServerSwitchListener implements Listener {
    private final String waitingForAuthCode = Main.getInstance().getConfig().getString("messages.waitingforauthcode");
    private final String authEnabled = Main.getInstance().getConfig().getString("messages.authenabled");
    private final String needToActivate = Main.getInstance().getConfig().getString("messages.needtoactivate");

    @EventHandler
    public void onSwitch(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        Spieler spieler = Spieler.get(player.getUniqueId());
        String uuid = player.getUniqueId().toString();

        if (spieler == null) {
            spieler = new Spieler(player);
            Spieler.add(spieler);
        }

        //TODO: If player has 2fa.forceenabled permission force him to activate 2fa

        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            asyncDatabaseAndPlayerUpdate(player, spieler, uuid);
            return;
        }

        if (spieler.getAuthState() != AuthState.WAITING_FOR_AUTH) {
            return;
        }

        player.sendMessage(new TextComponent(waitingForAuthCode.replace("&", "§")));
        event.setCancelled(true);
    }

    private void asyncDatabaseAndPlayerUpdate(ProxiedPlayer player, Spieler spieler, String uuid) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            boolean has2faEnables = MySQLMethodes.hasRecord(uuid);

            //Remove the player if he hasnt 2fa enabled
            if (!has2faEnables) {
                spieler.setAuthState(AuthState.NOT_ENABLED);
                return;
            }

            Optional<String> lastip = MySQLMethodes.getLastIP(uuid);
            Optional<String> secret = MySQLMethodes.getSecret(uuid);
            TwoFactorState twoFactorState = MySQLMethodes.getState(uuid); //TODO: One database call for all of them, if thats done I can also remove the has Record call and just check if there is data.

            secret.ifPresent(spieler::setSecret);
            spieler.setTwoFactorState(twoFactorState);

            if (!lastip.isPresent()) {
                return;
            }

            if (lastip.get().equals("just_activated")) {
                player.sendMessage(new TextComponent(needToActivate.replace("&", "§")));
                spieler.setAuthState(AuthState.NOT_ENABLED);
                return;
            }

            if (lastip.get().equals(player.getPendingConnection().getAddress().getAddress().toString())) {
                spieler.setAuthState(AuthState.AUTHENTICATED);
                return;
            }

            player.sendMessage(authEnabled.replace("&", "§"));
        });
    }
}

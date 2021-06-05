package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Optional;

public class ServerSwitchListener implements Listener {
    String waitingForAuthCode = Main.getInstance().config.getString("messages.waitingforauthcode");
    String authEnabled = Main.getInstance().config.getString("messages.authenabled");
    String needToActivate = Main.getInstance().config.getString("messages.needtoactivate");

    @EventHandler
    public void onSwitch(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        Spieler spieler = Main.getSpieler(player);
        String uuid = player.getUniqueId().toString();

        if (spieler == null) {
            spieler = new Spieler(player);
            Main.addSpieler(player, spieler);
        }

        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            asyncDatabaseAndPlayerUpdate(player, spieler, uuid);
            return;
        }

        if (spieler.isAuthenticated()) {
            return;
        }

        if (spieler.isWaitingForAuth()) {
            player.sendMessage(waitingForAuthCode.replace("&", "§"));
            event.setCancelled(true);
        }
    }

    private void asyncDatabaseAndPlayerUpdate(ProxiedPlayer player, Spieler spieler, String uuid) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            boolean has2faEnables = MySQLMethodes.hasRecord(uuid);

            //Remove the player if he hasnt 2fa enabled
            if (!has2faEnables) {
                spieler.setAuthenticated(true);
                spieler.setWaitingForAuth(false);
                return;
            }

            Optional<String> lastip = MySQLMethodes.getLastIP(uuid);
            Optional<String> secret = MySQLMethodes.getSecret(player.getUniqueId().toString());

            secret.ifPresent(spieler::setSecret);

            if (lastip.get().equals("just_activated")) {
                player.sendMessage(needToActivate.replace("&", "§"));
                spieler.setAuthenticated(true);
                spieler.setWaitingForAuth(false);
                return;
            }

            if (lastip.get().equals(player.getPendingConnection().getAddress().getAddress().toString())) {
                spieler.setAuthenticated(true);
                spieler.setWaitingForAuth(false);
                return;
            }

            player.sendMessage(authEnabled.replace("&", "§"));
        });
    }
}

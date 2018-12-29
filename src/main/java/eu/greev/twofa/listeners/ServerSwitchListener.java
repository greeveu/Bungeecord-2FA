package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitchListener implements Listener {
    String waitingForAuthCode = Main.getInstance().config.getString("messages.waitingforauthcode");
    String authEnabled = Main.getInstance().config.getString("messages.authenabled");
    String needToActivate = Main.getInstance().config.getString("messages.needtoactivate");

    @EventHandler
    public void onSwitch(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        if (!Main.getInstance().waitingForAuth.contains(player)) {
            Main.getInstance().waitingForAuth.add(player); //Add player directly and remove him later incase the database needs more time so the player cant execute any commands while waiting for the db
            ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
                if (MySQLMethodes.hasRecord(uuid)) {
                    String lastip = MySQLMethodes.getLastIP(uuid);
                    if (lastip.equals("just_activated")) {
                        player.sendMessage(needToActivate.replace("&", "§"));
                        return;
                    }
                    if (lastip.equals(player.getPendingConnection().getAddress().getAddress().toString())) {
                        return;
                    }
                    player.sendMessage(authEnabled.replace("&", "§"));
                } else {
                    Main.getInstance().waitingForAuth.remove(player); //Remove the player if he hasnt 2fa enabled
                }
                    }
            );
        } else {
            player.sendMessage(waitingForAuthCode.replace("&", "§"));
            event.setCancelled(true);
        }
    }
}

package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitchListener implements Listener {
    private final String waitingForAuthCode = Main.getInstance().getConfig().getString("messages.waitingforauthcode");
    private final String authEnabled = Main.getInstance().getConfig().getString("messages.authenabled");
    private final String needToActivate = Main.getInstance().getConfig().getString("messages.needtoactivate");

    @EventHandler
    public void onSwitch(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        if (!Main.getInstance().getWaitingForAuth().contains(player)) {
            Main.getInstance().getWaitingForAuth().add(player); //Add player directly and remove him later incase the database needs more time so the player cant execute any commands while waiting for the db
            ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
                        if (MySQLMethodes.hasRecord(uuid)) {
                            String lastip = MySQLMethodes.getLastIP(uuid);
                            if (lastip.equals("just_activated")) {
                                player.sendMessage(this.needToActivate.replace("&", "§"));
                                return;
                            }
                            if (lastip.equals(player.getPendingConnection().getAddress().getAddress().toString())) {
                                return;
                            }
                            player.sendMessage(this.authEnabled.replace("&", "§"));
                        } else {
                            Main.getInstance().getWaitingForAuth().remove(player); //Remove the player if he hasnt 2fa enabled
                        }
                    }
            );
        } else {
            player.sendMessage(this.waitingForAuthCode.replace("&", "§"));
            event.setCancelled(true);
        }
    }
}

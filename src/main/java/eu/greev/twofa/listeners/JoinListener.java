package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class JoinListener extends Plugin implements Listener {
    String authEnabled = Main.getInstance().config.getString("messages.authenabled");
    String needToActivate = Main.getInstance().config.getString("messages.needtoactivate");

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
                    if (MySQLMethodes.hasRecord(uuid)) {
                        String lastip = MySQLMethodes.getLastIP(uuid);
                        if(lastip.equals("just_activated")){
                            player.sendMessage(needToActivate.replace("&", "§"));
                            return;
                        }
                        if (lastip.equals(player.getPendingConnection().getAddress().getAddress().toString())) {
                            return;
                        }
                        Main.getInstance().firstLogin.add(player);
                        Main.getInstance().waitingForAuth.add(player);
                        player.sendMessage(authEnabled.replace("&", "§"));
                    }
                }
        );
    }
}

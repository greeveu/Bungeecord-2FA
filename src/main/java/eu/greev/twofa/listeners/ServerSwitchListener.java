package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitchListener implements Listener {
    String waitingForAuthCode = Main.getInstance().config.getString("messages.waitingforauthcode");

    @EventHandler
    public void onSwitch(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (Main.getInstance().waitingForAuth.contains(player)) {
            player.sendMessage(waitingForAuthCode.replace("&", "§"));
            event.setCancelled(true);
        }
    }
}

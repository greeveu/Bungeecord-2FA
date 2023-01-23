package eu.greev.twofa.listeners;

import eu.greev.twofa.entities.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        User.remove(player.getUniqueId());
    }
}

package eu.greev.twofa.listeners;

import eu.greev.twofa.entities.User;
import eu.greev.twofa.service.TwoFaService;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.Language;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@RequiredArgsConstructor
public class ChatListener implements Listener {
    private final TwoFaService service;
    private final Language language;

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user.getAuthState() == AuthState.AUTHENTICATED || user.getAuthState() == AuthState.NOT_ENABLED) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage();
        if (user.getAuthState() == AuthState.FORCED_ENABLE) {
            if (message.toLowerCase().startsWith("/2fa")) {
                event.setCancelled(false);
                return;
            }
            player.sendMessage(new TextComponent(language.getForceenable()));
        }

        service.verifySendOTP(user, message);
    }

    @EventHandler
    public void onTab(TabCompleteEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user.getAuthState() == AuthState.WAITING_FOR_AUTH) {
            event.setCancelled(true);
        }
    }
}

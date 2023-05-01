package eu.greev.twofa.listeners;

import eu.greev.twofa.entities.User;
import eu.greev.twofa.service.AuthServerService;
import eu.greev.twofa.service.TwoFaService;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.Language;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class ServerSwitchListener implements Listener {
    private final TwoFaService service;
    private final AuthServerService authServerService;
    private final Language language;

    @EventHandler
    public void onSwitch(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user == null) {
            user = new User(player);
            User.add(user);
        }

        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            CompletableFuture<User> future = service.asyncDatabaseAndPlayerUpdate(player, user);
            if (authServerService.isEnabled()) {
                handleAuthServer(event, player, future);
            }
            return;
        }

        if (authServerService.getAuthServer().isPresent() && authServerService.getAuthServer().get().equals(event.getTarget())) {
            return;
        }

        switch (user.getAuthState()) {
            case NOT_ENABLED:
            case AUTHENTICATED:
                //In case it's not enabled or the player is already authenticated return and don't prevent anything.
                return;
            case FORCED_ENABLE:
                player.sendMessage(new TextComponent(language.getForceenable()));
                break;
            case WAITING_FOR_AUTH:
                player.sendMessage(new TextComponent(language.getWaitingForAuthCode()));
                break;
        }

        event.setCancelled(true);
    }

    private void handleAuthServer(ServerConnectEvent event, ProxiedPlayer player, CompletableFuture<User> future) {
        if (authServerService.isForceBlockingJoin()) {
            User u = future.join();
            if (u.getAuthState() == AuthState.NOT_ENABLED || u.getAuthState() == AuthState.AUTHENTICATED) {
                return;
            }
            authServerService.getAuthServer().ifPresent(event::setTarget);
        } else {
            future.thenAccept(u -> {
                if (u.getAuthState() == AuthState.NOT_ENABLED || u.getAuthState() == AuthState.AUTHENTICATED) {
                    return;
                }
                authServerService.getAuthServer().ifPresent(player::connect);
            });
        }
    }
}

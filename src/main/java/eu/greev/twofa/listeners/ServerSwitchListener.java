package eu.greev.twofa.listeners;

import eu.greev.twofa.TwoFactorAuth;
import eu.greev.twofa.entities.User;
import eu.greev.twofa.entities.UserData;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.HashingUtils;
import eu.greev.twofa.utils.TwoFactorState;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitchListener implements Listener {
    private final String waitingForAuthCode;
    private final String authEnabled;
    private final String needToActivate;
    private final String forceenable;

    private final TwoFactorAuth main;

    public ServerSwitchListener(TwoFactorAuth main) {
        this.main = main;

        waitingForAuthCode = main.getConfig().getString("messages.waitingforauthcode").replace("&", "§");
        authEnabled = main.getConfig().getString("messages.authenabled").replace("&", "§");
        needToActivate = main.getConfig().getString("messages.needtoactivate").replace("&", "§");
        forceenable = main.getConfig().getString("messages.forceenable").replace("&", "§");
    }

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
            asyncDatabaseAndPlayerUpdate(player, user);
            return;
        }

        switch (user.getAuthState()) {
            case NOT_ENABLED:
            case AUTHENTICATED:
                //In case it's not enabled or the player is already authenticated return and don't prevent anything.
                return;
            case FORCED_ENABLE:
                player.sendMessage(new TextComponent(forceenable));
                break;
            case WAITING_FOR_AUTH:
                player.sendMessage(new TextComponent(waitingForAuthCode));
                break;
        }

        event.setCancelled(true);
    }

    private void asyncDatabaseAndPlayerUpdate(ProxiedPlayer player, User user) {
        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            UserData userData = main.getTwoFaDao().loadUserData(player.getUniqueId().toString());

            //Remove the player if he hasn't 2fa enabled
            if (userData == null) {
                user.setAuthState(player.hasPermission("2fa.forceenable") ? AuthState.FORCED_ENABLE : AuthState.NOT_ENABLED);
                return;
            }

            if (userData.getLastIpHash() == null || userData.getLastIpHash().isEmpty()) {
                return;
            }

            if (userData.getStatus() == TwoFactorState.ACTIVATED) {
                player.sendMessage(new TextComponent(needToActivate));
                user.setAuthState(player.hasPermission("2fa.forceenable") ? AuthState.FORCED_ENABLE : AuthState.NOT_ENABLED);
                return;
            }

            String hasedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());
            if (userData.getLastIpHash().equals(hasedIp) && userData.getStatus() == TwoFactorState.ACTIVE) {
                user.setAuthState(AuthState.AUTHENTICATED);
                return;
            }

            player.sendMessage(new TextComponent(authEnabled));
        });
    }
}

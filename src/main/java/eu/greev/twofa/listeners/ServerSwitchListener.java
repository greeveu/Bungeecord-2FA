package eu.greev.twofa.listeners;

import eu.greev.twofa.Main;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.HashingUtils;
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
    private final String waitingForAuthCode = Main.getInstance().getConfig().getString("messages.waitingforauthcode").replace("&", "§");
    private final String authEnabled = Main.getInstance().getConfig().getString("messages.authenabled").replace("&", "§");
    private final String needToActivate = Main.getInstance().getConfig().getString("messages.needtoactivate").replace("&", "§");
    private final String forceenable = Main.getInstance().getConfig().getString("messages.forceenable").replace("&", "§");

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

        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            asyncDatabaseAndPlayerUpdate(player, spieler, uuid);
            return;
        }

        if (spieler.getAuthState() == AuthState.NOT_ENABLED || spieler.getAuthState() == AuthState.AUTHENTICATED) {
            return;
        }

        if (spieler.getAuthState() == AuthState.FORCED_ENABLE) {
            player.sendMessage(new TextComponent(forceenable));
        } else {
            player.sendMessage(new TextComponent(waitingForAuthCode));
        }
        event.setCancelled(true);
    }

    private void asyncDatabaseAndPlayerUpdate(ProxiedPlayer player, Spieler spieler, String uuid) {
        ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
            boolean has2faEnables = MySQLMethodes.hasRecord(uuid);

            //Remove the player if he hasnt 2fa enabled
            if (!has2faEnables) {
                if (player.hasPermission("2fa.forceenable")) {
                    spieler.setAuthState(AuthState.FORCED_ENABLE);
                } else {
                    spieler.setAuthState(AuthState.NOT_ENABLED);
                }

                return;
            }

            //TODO: One database call for all of them, if thats done I can also remove the has Record call and just check if there is data.
            Optional<String> lastHashedIp = MySQLMethodes.getLastIP(uuid);
            Optional<String> secret = MySQLMethodes.getSecret(uuid);
            TwoFactorState twoFactorState = MySQLMethodes.getState(uuid);

            secret.ifPresent(spieler::setSecret);
            spieler.setTwoFactorState(twoFactorState);

            if (!lastHashedIp.isPresent()) {
                return;
            }

            if (spieler.getTwoFactorState() == TwoFactorState.ACTIVATED) {
                player.sendMessage(new TextComponent(needToActivate));

                if (player.hasPermission("2fa.forceenable")) {
                    spieler.setAuthState(AuthState.FORCED_ENABLE);
                } else {
                    spieler.setAuthState(AuthState.NOT_ENABLED);
                }

                return;
            }

            String hasedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());
            if (lastHashedIp.get().equals(hasedIp) && twoFactorState != TwoFactorState.LOGOUT) {
                spieler.setAuthState(AuthState.AUTHENTICATED);
                return;
            }

            player.sendMessage(new TextComponent(authEnabled));
        });
    }
}

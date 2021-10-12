package eu.greev.twofa.api.impl;

import eu.greev.twofa.api.API;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.MySQLMethodes;
import net.md_5.bungee.api.ProxyServer;

import java.util.UUID;

public class APIImpl implements API {

    @Override
    public boolean hasPlayer2FAEnabled(String uuid) {
        return MySQLMethodes.hasRecord(uuid);
    }

    @Override
    public boolean hasPlayer2FAEnabled(UUID uuid) {
        return hasPlayer2FAEnabled(String.valueOf(uuid));
    }

    @Override
    public AuthState getAuthStateOfPlayer(String uuid) {
        return getAuthStateOfPlayer(UUID.fromString(uuid));
    }

    @Override
    public AuthState getAuthStateOfPlayer(UUID uuid) {
        return Spieler.get(ProxyServer.getInstance().getPlayer(uuid).getUniqueId()).getAuthState();
    }

}

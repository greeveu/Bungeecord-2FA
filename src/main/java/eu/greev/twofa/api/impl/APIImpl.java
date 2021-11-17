package eu.greev.twofa.api.impl;

import eu.greev.twofa.api.API;
import eu.greev.twofa.entities.Spieler;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.MySQLMethods;

import java.util.UUID;

public class APIImpl implements API {

    @Override
    public boolean hasPlayer2FAEnabled(String uuid) {
        return MySQLMethods.hasRecord(uuid);
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
        Spieler spieler = Spieler.get(uuid);
        if (spieler == null) return null;
        return spieler.getAuthState();
    }

}

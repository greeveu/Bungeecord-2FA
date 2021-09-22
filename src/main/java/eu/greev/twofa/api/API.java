package eu.greev.twofa.api;

import eu.greev.twofa.utils.AuthState;

import java.util.UUID;

public interface API {

    boolean hasPlayer2FAEnabled(String uuid);

    boolean hasPlayer2FAEnabled(UUID uuid);

    AuthState getAuthStateOfPlayer(String uuid);

    AuthState getAuthStateOfPlayer(UUID uuid);

}

package eu.greev.twofa.api.impl;

import eu.greev.twofa.TwoFactorAuth;
import eu.greev.twofa.api.API;
import eu.greev.twofa.entities.User;
import eu.greev.twofa.utils.AuthState;

import java.util.UUID;

public class APIImpl implements API {

    private final TwoFactorAuth twoFactorAuth;

    public APIImpl(TwoFactorAuth twoFactorAuth) {
        this.twoFactorAuth = twoFactorAuth;
    }

    @Override
    public boolean hasPlayer2FAEnabled(String uuid) {
        return twoFactorAuth.getTwoFaDao().loadUserData(uuid) != null;
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
        User user = User.get(uuid);
        if (user == null) return null;
        return user.getAuthState();
    }

}

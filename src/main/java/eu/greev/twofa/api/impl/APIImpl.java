package eu.greev.twofa.api.impl;

import eu.greev.twofa.api.API;
import eu.greev.twofa.dao.TwoFaDao;
import eu.greev.twofa.entities.User;
import eu.greev.twofa.utils.AuthState;

import java.util.UUID;

public class APIImpl implements API {

    private final TwoFaDao dao;

    public APIImpl(TwoFaDao dao) {
        this.dao = dao;
    }

    @Override
    public boolean hasPlayer2FAEnabled(String uuid) {
        return dao.loadUserData(uuid) != null;
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

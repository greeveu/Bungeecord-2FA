package eu.greev.twofa.utils;

public enum AuthState {
    WAITING_FOR_AUTH,
    AUTHENTICATED,
    NOT_ENABLED,
    FORCED_ENABLE
}

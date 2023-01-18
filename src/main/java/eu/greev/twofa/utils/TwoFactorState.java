package eu.greev.twofa.utils;

public enum TwoFactorState {
    ACTIVATED,  //Player has just activated 2fa but hasn't verified the code yet
    ACTIVE,     //Player has 2fa active
    LOGOUT      //Player has logged out
}

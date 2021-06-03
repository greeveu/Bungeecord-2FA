package eu.greev.twofa.entities;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class Spieler {

    private ProxiedPlayer player;
    boolean waitingForAuth = true;
    boolean authenticated = false;
    private String secret;

    public Spieler(ProxiedPlayer player) {
        this.player = player;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public void setPlayer(ProxiedPlayer player) {
        this.player = player;
    }

    public boolean isWaitingForAuth() {
        return waitingForAuth;
    }

    public void setWaitingForAuth(boolean waitingForAuth) {
        this.waitingForAuth = waitingForAuth;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        System.out.println(player.getDisplayName() + ": " + this.authenticated);
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}

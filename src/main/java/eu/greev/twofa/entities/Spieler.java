package eu.greev.twofa.entities;

import eu.greev.twofa.utils.AuthState;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Spieler {
    private final static Map<UUID, Spieler> SPIELER_MAP = new HashMap<>();

    @Getter private final ProxiedPlayer player;
    @Getter @Setter AuthState authState;
    @Getter @Setter private String secret;

    public Spieler(ProxiedPlayer player) {
        this.player = player;

        SPIELER_MAP.put(player.getUniqueId(), this);
    }

    public static Spieler get(UUID uuid) {
        return SPIELER_MAP.get(uuid);
    }
}

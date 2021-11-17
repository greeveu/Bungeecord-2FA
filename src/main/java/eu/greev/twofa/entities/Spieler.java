package eu.greev.twofa.entities;

import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.TwoFactorState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class Spieler {
    private static final Map<UUID, Spieler> SPIELER_MAP = new HashMap<>();

    @Getter
    private final ProxiedPlayer player;

    @Getter
    @Setter
    private AuthState authState;

    @Getter
    @Setter
    private TwoFactorState twoFactorState;

    @Getter
    @Setter
    private String secret;

    public static Spieler get(UUID uuid) {
        return SPIELER_MAP.get(uuid);
    }

    public static void add(Spieler spieler) {
        SPIELER_MAP.put(spieler.getPlayer().getUniqueId(), spieler);
    }

    public static void remove(UUID uuid) {
        SPIELER_MAP.remove(uuid);
    }
}

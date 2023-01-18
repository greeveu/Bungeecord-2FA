package eu.greev.twofa.entities;

import eu.greev.twofa.utils.AuthState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class User {
    private static final Map<UUID, User> USER_MAP = new HashMap<>();

    @Getter
    private final ProxiedPlayer player;

    @Getter
    @Setter
    private AuthState authState;

    @Getter
    @Setter
    private UserData userData;

    public static User get(UUID uuid) {
        return USER_MAP.get(uuid);
    }

    public static void add(User user) {
        USER_MAP.put(user.getPlayer().getUniqueId(), user);
    }

    public static void remove(UUID uuid) {
        USER_MAP.remove(uuid);
    }
}

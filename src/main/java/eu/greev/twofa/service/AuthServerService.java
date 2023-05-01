package eu.greev.twofa.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;

@RequiredArgsConstructor
public class AuthServerService {

    @Getter private final boolean enabled;
    //TODO: Maybe authServer should always force a blocking join?
    @Getter private final boolean forceBlockingJoin;
    private final String authServer;
    @Getter private final String successCommand;

    private ServerInfo authServerCache;

    public Optional<ServerInfo> getAuthServer() {
        if (enabled) {
            if (authServerCache == null) {
                authServerCache = ProxyServer.getInstance().getServerInfo(authServer);
            }
            return Optional.ofNullable(authServerCache);
        }
        return Optional.empty();
    }

}

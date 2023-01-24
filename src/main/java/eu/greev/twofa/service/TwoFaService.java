package eu.greev.twofa.service;

import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import eu.greev.twofa.TwoFactorAuth;
import eu.greev.twofa.dao.TwoFaDao;
import eu.greev.twofa.entities.User;
import eu.greev.twofa.entities.UserData;
import eu.greev.twofa.entities.YubicoOtp;
import eu.greev.twofa.utils.*;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.security.GeneralSecurityException;
import java.util.Set;

@RequiredArgsConstructor
public class TwoFaService {
    private final TwoFactorAuth main;
    private final TwoFaDao database;
    private final YubicoClient yubicoClient;
    private final TwoFactorAuthUtil twoFactorAuthUtil;
    private final Language language;

    public void verifyYubiOtp(ProxiedPlayer player, User user, String message) {
        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            try {
                VerificationResponse response = yubicoClient.verify(message);
                String publicId = YubicoClient.getPublicId(message);

                if (response.isOk() && user.getUserData().getYubiOtp().stream().anyMatch(yubicoOtp -> yubicoOtp.publicId().equals(publicId))) {
                    saveUserAsAuthenticated(player, user);
                } else {
                    player.sendMessage(new TextComponent(language.getYubikeyCodeInvalid()));
                }
            } catch (YubicoVerificationException | YubicoValidationFailure e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(language.getErrorOccurred()));
            }
        });
    }

    public void verifyTotpCode(ProxiedPlayer player, User user, String message) {
        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            try {
                String secret = user.getUserData().getSecret();

                Set<String> validCodes = twoFactorAuthUtil.generateNumbersWithOffset(secret, TwoFactorAuthUtil.TIME_STEP_SECONDS);

                if (validCodes.contains(message)) {
                    saveUserAsAuthenticated(player, user);
                } else {
                    player.sendMessage(new TextComponent(language.getCodeIsInvalid()));
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(language.getErrorOccurred()));
            }
        });
    }

    public void asyncDatabaseAndPlayerUpdate(ProxiedPlayer player, User user) {
        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            UserData userData = database.loadUserData(player.getUniqueId().toString());

            //Remove the player if he hasn't 2fa enabled
            if (userData == null) {
                user.setAuthState(player.hasPermission("2fa.forceenable") ? AuthState.FORCED_ENABLE : AuthState.NOT_ENABLED);
                return;
            }

            user.setUserData(userData);

            if (userData.getLastIpHash() == null || userData.getLastIpHash().isEmpty()) {
                return;
            }

            if (userData.getStatus() == TwoFactorState.ACTIVATED) {
                player.sendMessage(new TextComponent(language.getNeedToActivate()));
                user.setAuthState(player.hasPermission("2fa.forceenable") ? AuthState.FORCED_ENABLE : AuthState.NOT_ENABLED);
                return;
            }

            String hasedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());
            if (userData.getLastIpHash().equals(hasedIp) && userData.getStatus() == TwoFactorState.ACTIVE) {
                user.setAuthState(AuthState.AUTHENTICATED);
                return;
            }

            player.sendMessage(new TextComponent(language.getAuthEnabled()));
        });
    }

    public void saveUserAsAuthenticated(ProxiedPlayer player, User user) {
        String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

        user.setAuthState(AuthState.AUTHENTICATED);
        user.getUserData().setStatus(TwoFactorState.ACTIVE);
        user.getUserData().setLastIpHash(hashedIp);

        ProxyServer.getInstance().getScheduler().runAsync(main, () -> database.saveUserData(player.getUniqueId().toString(), user.getUserData()));

        player.sendMessage(new TextComponent(language.getLoginSuccessful()));
    }

    public void listYubico(ProxiedPlayer player) {
        if (yubicoClient == null) {
            player.sendMessage(new TextComponent(language.getYubikeyDisabled()));
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user.getUserData().getYubiOtp().isEmpty()) {
            player.sendMessage(new TextComponent(language.getYubikeyNokeysinaccount()));
            return;
        }

        player.sendMessage(new TextComponent(language.getYubikeyKeysinaccount()));

        user.getUserData().getYubiOtp().forEach(yubicoOtp -> player.sendMessage(
                new TextComponent(language.getYubikeyKeylist()
                        .replace("%name%", yubicoOtp.name())
                        .replace("%publicId%", yubicoOtp.publicId())))
        );
    }

    public void removeYubico(ProxiedPlayer player, String name) {
        if (yubicoClient == null) {
            player.sendMessage(new TextComponent(language.getYubikeyDisabled()));
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user.getUserData().getYubiOtp().stream().anyMatch(yubicoOtp -> yubicoOtp.name().equals(name))) {
            ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
                user.getUserData().getYubiOtp().removeIf(yubicoOtp -> yubicoOtp.name().equals(name));
                database.saveUserData(player.getUniqueId().toString(), user.getUserData());
                player.sendMessage(new TextComponent(language.getYubikeyRemovedkey()));
            });
        } else {
            player.sendMessage(new TextComponent(language.getYubikeyRemovedkeynotfound()));
        }
    }

    public void enableYubico(ProxiedPlayer player, String yubiName, String otp) {
        if (yubicoClient == null) {
            player.sendMessage(new TextComponent(language.getYubikeyDisabled()));
            return;
        }

        String uuid = player.getUniqueId().toString();
        User user = User.get(player.getUniqueId());

        if (user.getUserData().getStatus() != TwoFactorState.ACTIVE || user.getAuthState() != AuthState.AUTHENTICATED) {
            player.sendMessage(new TextComponent(language.getYubikeyEnabletotp()));
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            try {
                VerificationResponse verify = yubicoClient.verify(otp);

                if (!verify.isOk()) {
                    player.sendMessage(new TextComponent(language.getYubikeyCodeInvalid()));
                    return;
                }

                String publicId = YubicoClient.getPublicId(otp);

                player.sendMessage(new TextComponent(language.getYubikeyActivated()));

                user.getUserData().getYubiOtp().add(new YubicoOtp(yubiName, publicId));

                database.saveUserData(uuid, user.getUserData());
            } catch (YubicoVerificationException | YubicoValidationFailure | IllegalArgumentException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(language.getErrorOccurred()));
            }
        });
    }

    public void activate(ProxiedPlayer player, String code) {
        String uuid = player.getUniqueId().toString();
        User user = User.get(player.getUniqueId());

        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            if (user.getUserData().getStatus() != TwoFactorState.ACTIVATED) {
                player.sendMessage(new TextComponent(language.getNotLoggedIn()));
                return;
            }

            try {
                String secret = user.getUserData().getSecret();

                Set<String> validCodes = twoFactorAuthUtil
                        .generateNumbersWithOffset(secret, TwoFactorAuthUtil.TIME_STEP_SECONDS);

                if (!validCodes.contains(code)) {
                    player.sendMessage(new TextComponent(language.getCodeIsInvalid()));
                    return;
                }

                player.sendMessage(new TextComponent(language.getSuccessfulActivated()));

                String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

                user.getUserData().setLastIpHash(hashedIp);
                user.getUserData().setStatus(TwoFactorState.ACTIVE);
                user.setAuthState(AuthState.AUTHENTICATED);

                database.saveUserData(uuid, user.getUserData());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(language.getErrorOccurred()));
            }
        });
    }

    public void logout(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            User user = User.get(player.getUniqueId());
            if (user.getAuthState() == AuthState.AUTHENTICATED) {
                user.setAuthState(AuthState.WAITING_FOR_AUTH);
                user.getUserData().setStatus(TwoFactorState.LOGOUT);

                database.saveUserData(player.getUniqueId().toString(), user.getUserData());

                player.sendMessage(new TextComponent(language.getLogoutMessage()));
            } else {
                player.sendMessage(new TextComponent(language.getNotLoggedIn()));
            }
        });
    }

    public void disableTFA(ProxiedPlayer player) {
        if (player.hasPermission("2fa.forceenable")) {
            player.sendMessage(new TextComponent(language.getDisableforforced()));
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            User user = User.get(player.getUniqueId());
            if (user.getAuthState() == AuthState.AUTHENTICATED) {
                database.deleteUser(player.getUniqueId().toString());
                player.sendMessage(new TextComponent(language.getRemoveAuth()));
            } else {
                player.sendMessage(new TextComponent(language.getNotLoggedIn()));
            }
        });
    }

    public void enableTFA(ProxiedPlayer player) {
        User user = User.get(player.getUniqueId());
        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            if (user.getUserData().getStatus() == TwoFactorState.ACTIVATED) {
                sendEnableMessage(player, user.getUserData().getSecret());
                return;
            }
            if (user.getAuthState() == AuthState.AUTHENTICATED || user.getAuthState() == AuthState.WAITING_FOR_AUTH) {
                player.sendMessage(new TextComponent(language.getAlreadyActive()));
                return;
            }

            String secret = twoFactorAuthUtil.generateBase32Secret();
            String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

            user.getUserData().setSecret(secret);
            user.getUserData().setLastIpHash(hashedIp);
            user.getUserData().setStatus(TwoFactorState.ACTIVATED);

            database.saveUserData(player.getUniqueId().toString(), user.getUserData());

            sendEnableMessage(player, secret);
        });
    }

    public void verifySendOTP(User user, String message) {
        if (user.getUserData() == null) {
            user.getPlayer().sendMessage(new TextComponent(language.getDataNull()));
            return;
        }

        //Verify send code
        if (message.length() == 6) {
            verifyTotpCode(user.getPlayer(), user, message);
        } else if (yubicoClient != null &&
                !user.getUserData().getYubiOtp().isEmpty() &&
                YubicoClient.isValidOTPFormat(message)) {
            verifyYubiOtp(user.getPlayer(), user, message);
        } else {
            user.getPlayer().sendMessage(new TextComponent(language.getWaitingForAuthCode()));
        }
    }

    private void sendEnableMessage(ProxiedPlayer player, String secret) {
        String url = TwoFactorAuthUtil.qrImageUrl(language.getServerName().trim() + ":" + player.getName(), language.getServerName().trim(), secret, 6, 128);

        TextComponent message = new TextComponent(language.getActivated()
                .replace("%secret%", secret)
                .replace("%link%", url)
        );

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(language.getHoverText()).create()));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        player.sendMessage(message);
    }

}

package eu.greev.twofa.listeners;

import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import eu.greev.twofa.TwoFactorAuth;
import eu.greev.twofa.entities.User;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.HashingUtils;
import eu.greev.twofa.utils.TwoFactorState;
import io.netty.util.internal.StringUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.security.GeneralSecurityException;
import java.util.Set;

public class ChatListener implements Listener {

    private final String waitingForAuthCode;
    private final String errorOccurred;
    private final String loginSuccessful;
    private final String codeIsInvalid;
    private final String yubicoCodeInvalid;
    private final String forceenable;

    private final TwoFactorAuth twoFactorAuth;

    public ChatListener(TwoFactorAuth twoFactorAuth) {
        this.twoFactorAuth = twoFactorAuth;

        waitingForAuthCode = twoFactorAuth.getConfig().getString("messages.waitingforauthcode").replace("&", "§");
        errorOccurred = twoFactorAuth.getConfig().getString("messages.errorocurred").replace("&", "§");
        loginSuccessful = twoFactorAuth.getConfig().getString("messages.loginsuccessful").replace("&", "§");
        codeIsInvalid = twoFactorAuth.getConfig().getString("messages.codeisinvalid").replace("&", "§");
        yubicoCodeInvalid = twoFactorAuth.getConfig().getString("messages.invalidcode").replace("&", "§");
        forceenable = twoFactorAuth.getConfig().getString("messages.forceenable").replace("&", "§");
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user.getAuthState() == AuthState.AUTHENTICATED || user.getAuthState() == AuthState.NOT_ENABLED) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage();
        if (user.getAuthState() == AuthState.FORCED_ENABLE) {
            if (message.toLowerCase().startsWith("/2fa")) {
                event.setCancelled(false);
                return;
            }
            player.sendMessage(new TextComponent(forceenable));
        }

        //Verify send code
        if (message.length() == 6) {
            verifyTotpCode(player, user, message);
        } else if (twoFactorAuth.getYubicoClient() != null &&
                !StringUtil.isNullOrEmpty(user.getUserData().getYubiOtp()) &&
                YubicoClient.isValidOTPFormat(message)) {
            verifyYubiOtp(player, user, message);
        } else {
            player.sendMessage(new TextComponent(waitingForAuthCode));
        }
    }

    private void verifyYubiOtp(ProxiedPlayer player, User user, String message) {
        try {
            VerificationResponse response = twoFactorAuth.getYubicoClient().verify(message);

            if (response.isOk() && YubicoClient.getPublicId(message).equals(user.getUserData().getYubiOtp())) {
                saveUserAsAuthenticated(player, user);
            } else {
                player.sendMessage(new TextComponent(yubicoCodeInvalid));
            }
        } catch (YubicoVerificationException | YubicoValidationFailure e) {
            e.printStackTrace();
            player.sendMessage(new TextComponent(errorOccurred));
        }
    }

    private void verifyTotpCode(ProxiedPlayer player, User user, String message) {
        try {
            String secret = user.getUserData().getSecret();

            Set<String> validCodes = twoFactorAuth.getTwoFactorAuthUtil().generateNumbersWithOffset(secret, TwoFactorAuth.getMILLISECOND_TIMING_THRESHOLD());

            if (validCodes.contains(message)) {
                saveUserAsAuthenticated(player, user);
            } else {
                player.sendMessage(new TextComponent(codeIsInvalid));
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            player.sendMessage(new TextComponent(errorOccurred));
        }
    }

    private void saveUserAsAuthenticated(ProxiedPlayer player, User user) {
        String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

        user.setAuthState(AuthState.AUTHENTICATED);
        user.getUserData().setStatus(TwoFactorState.ACTIVE);
        user.getUserData().setLastIpHash(hashedIp);

        ProxyServer.getInstance().getScheduler().runAsync(twoFactorAuth, () -> twoFactorAuth.getTwoFaDao().saveUserData(player.getUniqueId().toString(), user.getUserData()));

        player.sendMessage(new TextComponent(loginSuccessful));
    }

    @EventHandler
    public void onTab(TabCompleteEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user.getAuthState() == AuthState.WAITING_FOR_AUTH) {
            event.setCancelled(true);
        }
    }
}

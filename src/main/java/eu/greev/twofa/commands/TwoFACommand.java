package eu.greev.twofa.commands;

import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import eu.greev.twofa.TwoFactorAuth;
import eu.greev.twofa.entities.User;
import eu.greev.twofa.entities.YubicoOtp;
import eu.greev.twofa.utils.AuthState;
import eu.greev.twofa.utils.HashingUtils;
import eu.greev.twofa.utils.TwoFactorAuthUtil;
import eu.greev.twofa.utils.TwoFactorState;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.security.GeneralSecurityException;
import java.util.Set;

public class TwoFACommand extends Command {
    private final String helpMessage;
    private final String alreadyActive;
    private final String activated;
    private final String removeAuth;
    private final String notLoggedIn;
    private final String logoutMessage;
    private final String serverName;
    private final String missingCode;
    private final String errorOccurred;
    private final String codeIsInvalid;
    private final String successfulActivated;
    private final String hoverText;
    private final String disableforforced;
    private final String yubikeyEnabletotp;
    private final String yubikeyActivated;
    private final String yubikeyCodeInvalid;
    private final String yubikeyAddyubiusage;
    private final String yubikeyRemoveyubiusage;
    private final String yubikeyDisabled;
    private final String yubikeyRemovedkey;
    private final String yubikeyRemovedkeynotfound;
    private final String yubikeyKeysinaccount;
    private final String yubikeyKeylist;
    private final String yubikeyNokeysinaccount;


    private final TwoFactorAuth twoFactorAuth;

    public TwoFACommand(TwoFactorAuth twoFactorAuth) {
        super("2fa");

        this.twoFactorAuth = twoFactorAuth;

        helpMessage = twoFactorAuth.getConfig().getString("messages.help").replace("&", "§");
        alreadyActive = twoFactorAuth.getConfig().getString("messages.alreadyactive").replace("&", "§");
        activated = twoFactorAuth.getConfig().getString("messages.activated").replace("&", "§");
        removeAuth = twoFactorAuth.getConfig().getString("messages.removeauth").replace("&", "§");
        notLoggedIn = twoFactorAuth.getConfig().getString("messages.notloggedin").replace("&", "§");
        logoutMessage = twoFactorAuth.getConfig().getString("messages.logoutmessage").replace("&", "§");
        serverName = twoFactorAuth.getConfig().getString("servername").replace("&", "§");
        missingCode = twoFactorAuth.getConfig().getString("messages.missingcode").replace("&", "§");
        errorOccurred = twoFactorAuth.getConfig().getString("messages.errorocurred").replace("&", "§");
        codeIsInvalid = twoFactorAuth.getConfig().getString("messages.codeisinvalid").replace("&", "§");
        successfulActivated = twoFactorAuth.getConfig().getString("messages.successfulcctivated").replace("&", "§");
        hoverText = twoFactorAuth.getConfig().getString("messages.hovertext").replace("&", "§");
        disableforforced = twoFactorAuth.getConfig().getString("messages.disableforforced").replace("&", "§");
        yubikeyCodeInvalid = twoFactorAuth.getConfig().getString("messages.yubikey.invalidcode").replace("&", "§");
        yubikeyActivated = twoFactorAuth.getConfig().getString("messages.yubikey.activated").replace("&", "§");
        yubikeyEnabletotp = twoFactorAuth.getConfig().getString("messages.yubikey.enabletotp").replace("&", "§");
        yubikeyAddyubiusage = twoFactorAuth.getConfig().getString("messages.yubikey.addyubiusage").replace("&", "§");
        yubikeyRemoveyubiusage = twoFactorAuth.getConfig().getString("messages.yubikey.removeyubiusage").replace("&", "§");
        yubikeyDisabled = twoFactorAuth.getConfig().getString("messages.yubikey.disabled").replace("&", "§");
        yubikeyRemovedkey = twoFactorAuth.getConfig().getString("messages.yubikey.removedkey").replace("&", "§");
        yubikeyRemovedkeynotfound = twoFactorAuth.getConfig().getString("messages.yubikey.removedkeynotfound").replace("&", "§");
        yubikeyKeysinaccount = twoFactorAuth.getConfig().getString("messages.yubikey.keysinaccount").replace("&", "§");
        yubikeyKeylist = twoFactorAuth.getConfig().getString("messages.yubikey.keylist").replace("&", "§");
        yubikeyNokeysinaccount = twoFactorAuth.getConfig().getString("messages.yubikey.nokeysinaccount").replace("&", "§");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        if (args.length == 0) {
            player.sendMessage(new TextComponent(helpMessage));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "enable":
                enableTFA(player);
                break;
            case "disable":
                disableTFA(player);
                break;
            case "logout":
                logout(player);
                break;
            case "addyubikey":
                if (args.length == 3 && args[1].length() <= 24) {
                    enableYubico(player, args[1], args[2]);
                } else {
                    player.sendMessage(new TextComponent(yubikeyAddyubiusage)); //missingotp
                }
                break;
            case "removeyubikey":
                if (args.length == 2 && args[1].length() <= 24) {
                    removeYubico(player, args[1]);
                } else {
                    player.sendMessage(new TextComponent(yubikeyRemoveyubiusage));
                }
                break;
            case "listyubikey":
                listYubico(player);
                break;
            case "activate":
                if (args.length == 2) {
                    activate(player, args[1]);
                } else {
                    player.sendMessage(new TextComponent(missingCode));
                }
                break;
            default:
                player.sendMessage(new TextComponent(helpMessage));
        }
    }

    private void listYubico(ProxiedPlayer player) {
        if (twoFactorAuth.getYubicoClient() == null) {
            player.sendMessage(new TextComponent(yubikeyDisabled));
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user.getUserData().getYubiOtp().isEmpty()) {
            player.sendMessage(new TextComponent(yubikeyNokeysinaccount));
            return;
        }

        player.sendMessage(new TextComponent(yubikeyKeysinaccount));

        user.getUserData().getYubiOtp().forEach(yubicoOtp -> player.sendMessage(
                new TextComponent(yubikeyKeylist
                        .replace("%name%", yubicoOtp.getName())
                        .replace("%publicId%", yubicoOtp.getPublicId())))
        );
    }

    private void removeYubico(ProxiedPlayer player, String name) {
        if (twoFactorAuth.getYubicoClient() == null) {
            player.sendMessage(new TextComponent(yubikeyDisabled));
            return;
        }

        User user = User.get(player.getUniqueId());

        if (user.getUserData().getYubiOtp().stream().anyMatch(yubicoOtp -> yubicoOtp.getName().equals(name))) {
            ProxyServer.getInstance().getScheduler().runAsync(twoFactorAuth, () -> {
                user.getUserData().getYubiOtp().removeIf(yubicoOtp -> yubicoOtp.getName().equals(name));
                twoFactorAuth.getTwoFaDao().saveUserData(player.getUniqueId().toString(), user.getUserData());
                player.sendMessage(new TextComponent(yubikeyRemovedkey));
            });
        } else {
            player.sendMessage(new TextComponent(yubikeyRemovedkeynotfound));
        }
    }

    private void enableYubico(ProxiedPlayer player, String yubiName, String otp) {
        if (twoFactorAuth.getYubicoClient() == null) {
            player.sendMessage(new TextComponent(yubikeyDisabled));
            return;
        }

        String uuid = player.getUniqueId().toString();
        User user = User.get(player.getUniqueId());

        if (user.getUserData().getStatus() != TwoFactorState.ACTIVE || user.getAuthState() != AuthState.AUTHENTICATED) {
            player.sendMessage(new TextComponent(yubikeyEnabletotp));
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(twoFactorAuth, () -> {
            try {
                VerificationResponse verify = twoFactorAuth.getYubicoClient().verify(otp);

                if (!verify.isOk()) {
                    player.sendMessage(new TextComponent(yubikeyCodeInvalid));
                    return;
                }

                String publicId = YubicoClient.getPublicId(otp);

                player.sendMessage(new TextComponent(yubikeyActivated));

                user.getUserData().getYubiOtp().add(new YubicoOtp(yubiName, publicId));

                twoFactorAuth.getTwoFaDao().saveUserData(uuid, user.getUserData());
            } catch (YubicoVerificationException | YubicoValidationFailure | IllegalArgumentException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(errorOccurred));
            }
        });
    }

    private void activate(ProxiedPlayer player, String code) {
        String uuid = player.getUniqueId().toString();
        User user = User.get(player.getUniqueId());

        ProxyServer.getInstance().getScheduler().runAsync(twoFactorAuth, () -> {
            if (user.getUserData().getStatus() != TwoFactorState.ACTIVATED) {
                player.sendMessage(new TextComponent(notLoggedIn));
                return;
            }

            try {
                String secret = user.getUserData().getSecret();

                Set<String> validCodes = twoFactorAuth
                        .getTwoFactorAuthUtil()
                        .generateNumbersWithOffset(secret, TwoFactorAuth.getMILLISECOND_TIMING_THRESHOLD());

                if (!validCodes.contains(code)) {
                    player.sendMessage(new TextComponent(codeIsInvalid));
                    return;
                }

                player.sendMessage(new TextComponent(successfulActivated));

                String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

                user.getUserData().setLastIpHash(hashedIp);
                user.getUserData().setStatus(TwoFactorState.ACTIVE);
                user.setAuthState(AuthState.AUTHENTICATED);

                twoFactorAuth.getTwoFaDao().saveUserData(uuid, user.getUserData());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponent(errorOccurred));
            }
        });
    }

    private void logout(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(twoFactorAuth, () -> {
            User user = User.get(player.getUniqueId());
            if (user.getAuthState() == AuthState.AUTHENTICATED) {
                user.setAuthState(AuthState.WAITING_FOR_AUTH);
                user.getUserData().setStatus(TwoFactorState.LOGOUT);

                twoFactorAuth.getTwoFaDao().saveUserData(player.getUniqueId().toString(), user.getUserData());

                player.sendMessage(new TextComponent(logoutMessage));
            } else {
                player.sendMessage(new TextComponent(notLoggedIn));
            }
        });
    }

    private void disableTFA(ProxiedPlayer player) {
        if (player.hasPermission("2fa.forceenable")) {
            player.sendMessage(new TextComponent(disableforforced));
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(twoFactorAuth, () -> {
            User user = User.get(player.getUniqueId());
            if (user.getAuthState() == AuthState.AUTHENTICATED) {
                twoFactorAuth.getTwoFaDao().deleteUser(player.getUniqueId().toString());
                player.sendMessage(new TextComponent(removeAuth));
            } else {
                player.sendMessage(new TextComponent(notLoggedIn));
            }
        });
    }

    private void enableTFA(ProxiedPlayer player) {
        User user = User.get(player.getUniqueId());
        ProxyServer.getInstance().getScheduler().runAsync(twoFactorAuth, () -> {
            if (user.getUserData().getStatus() == TwoFactorState.ACTIVATED) {
                sendEnableMessage(player, user.getUserData().getSecret());
                return;
            }
            if (user.getAuthState() == AuthState.AUTHENTICATED || user.getAuthState() == AuthState.WAITING_FOR_AUTH) {
                player.sendMessage(new TextComponent(alreadyActive));
                return;
            }

            String secret = twoFactorAuth.getTwoFactorAuthUtil().generateBase32Secret();
            String hashedIp = HashingUtils.hashIp(player.getPendingConnection().getAddress().getAddress().toString());

            user.getUserData().setSecret(secret);
            user.getUserData().setLastIpHash(hashedIp);
            user.getUserData().setStatus(TwoFactorState.ACTIVATED);

            twoFactorAuth.getTwoFaDao().saveUserData(player.getUniqueId().toString(), user.getUserData());

            sendEnableMessage(player, secret);
        });
    }

    private void sendEnableMessage(ProxiedPlayer player, String secret) {
        String url = TwoFactorAuthUtil.qrImageUrl(serverName.trim() + ":" + player.getName(), serverName.trim(), secret, 6, 128);

        TextComponent message = new TextComponent(activated
                .replace("%secret%", secret)
                .replace("%link%", url)
        );

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        player.sendMessage(message);
    }
}

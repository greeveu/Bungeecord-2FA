package eu.greev.twofa.utils;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;

@Getter
public class Language {
    private final String alreadyActive;
    private final String activated;
    private final String removeAuth;
    private final String notLoggedIn;
    private final String logoutMessage;
    private final String serverName;
    private final String errorOccurred;
    private final String codeIsInvalid;
    private final String successfulActivated;
    private final String hoverText;
    private final String disableforforced;
    private final String yubikeyEnabletotp;
    private final String yubikeyActivated;
    private final String yubikeyCodeInvalid;
    private final String yubikeyDisabled;
    private final String yubikeyRemovedkey;
    private final String yubikeyRemovedkeynotfound;
    private final String yubikeyKeysinaccount;
    private final String yubikeyKeylist;
    private final String yubikeyNokeysinaccount;
    private final String helpMessage;
    private final String missingCode;
    private final String yubikeyAddyubiusage;
    private final String yubikeyRemoveyubiusage;
    private final String waitingForAuthCode;
    private final String authEnabled;
    private final String needToActivate;
    private final String forceenable;
    private final String loginSuccessful;
    private final String yubicoCodeInvalid;
    private final String dataNull;

    public Language(Configuration configuration) {
        helpMessage = configuration.getString("messages.help").replace("&", "§");
        alreadyActive = configuration.getString("messages.alreadyactive").replace("&", "§");
        activated = configuration.getString("messages.activated").replace("&", "§");
        removeAuth = configuration.getString("messages.removeauth").replace("&", "§");
        notLoggedIn = configuration.getString("messages.notloggedin").replace("&", "§");
        logoutMessage = configuration.getString("messages.logoutmessage").replace("&", "§");
        serverName = configuration.getString("servername").replace("&", "§");
        missingCode = configuration.getString("messages.missingcode").replace("&", "§");
        errorOccurred = configuration.getString("messages.errorocurred").replace("&", "§");
        codeIsInvalid = configuration.getString("messages.codeisinvalid").replace("&", "§");
        successfulActivated = configuration.getString("messages.successfulcctivated").replace("&", "§");
        hoverText = configuration.getString("messages.hovertext").replace("&", "§");
        disableforforced = configuration.getString("messages.disableforforced").replace("&", "§");
        yubikeyCodeInvalid = configuration.getString("messages.yubikey.invalidcode").replace("&", "§");
        yubikeyActivated = configuration.getString("messages.yubikey.activated").replace("&", "§");
        yubikeyEnabletotp = configuration.getString("messages.yubikey.enabletotp").replace("&", "§");
        yubikeyAddyubiusage = configuration.getString("messages.yubikey.addyubiusage").replace("&", "§");
        yubikeyRemoveyubiusage = configuration.getString("messages.yubikey.removeyubiusage").replace("&", "§");
        yubikeyDisabled = configuration.getString("messages.yubikey.disabled").replace("&", "§");
        yubikeyRemovedkey = configuration.getString("messages.yubikey.removedkey").replace("&", "§");
        yubikeyRemovedkeynotfound = configuration.getString("messages.yubikey.removedkeynotfound").replace("&", "§");
        yubikeyKeysinaccount = configuration.getString("messages.yubikey.keysinaccount").replace("&", "§");
        yubikeyKeylist = configuration.getString("messages.yubikey.keylist").replace("&", "§");
        yubikeyNokeysinaccount = configuration.getString("messages.yubikey.nokeysinaccount").replace("&", "§");
        waitingForAuthCode = configuration.getString("messages.waitingforauthcode").replace("&", "§");
        authEnabled = configuration.getString("messages.authenabled").replace("&", "§");
        needToActivate = configuration.getString("messages.needtoactivate").replace("&", "§");
        forceenable = configuration.getString("messages.forceenable").replace("&", "§");
        loginSuccessful = configuration.getString("messages.loginsuccessful").replace("&", "§");
        yubicoCodeInvalid = configuration.getString("messages.invalidcode").replace("&", "§");
        dataNull = configuration.getString("messages.datanull").replace("&", "§");
    }
}

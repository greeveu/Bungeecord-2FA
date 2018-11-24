package eu.greev.twofa.utils;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.IOException;

public class ConfigHelper {

    public YamlFile getConfig(String file) {
        YamlFile config = new YamlFile(file);
        if (!config.exists()) {
            generateConfig(config);
        }
        try {
            config.load();
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private void generateConfig(YamlFile config) {
        config.set("servername", "YourServer.com ");

        config.set("mysql.host", "localhost");
        config.set("mysql.username", "username");
        config.set("mysql.password", "password");
        config.set("mysql.database", "database");
        config.set("mysql.port", "3306");

        config.set("messages.authenabled", "&c[&42FA&c]&7 Deine Session ist abgelaufen. Daher musst du deine 2FA Code erneut bestätigen. &9Schreibe dafür deinen 6 Stelligen Code aus der App einfach in den Chat.&7 Keiner außer du wird ihn sehen.");
        config.set("messages.help", "&c[&42FA&c]&7 Mit 2FA kannst du deinen Account absichern und so verhindern da andere als du mit deinem Account hier Spielen können. Du benötigst dafür ein Handy mit Authy oder Google Authenticator. Wir helfen dir gerne bei einrichten, melde dich dafür einfach bei uns im TeamSpeak Support oder schreib uns auf Discord.");
        config.set("messages.activated", "&c[&42FA&c]&7 2FA wurde bei dir Aktiviert! Nutze diesen Code: &9&l%secret% &r oder diesen Link: %link% und aktiviere 2FA indem du /2fa activate <6 Stelliger Code aus der App> ausführst.");
        config.set("messages.alreadyactive", "&c[&42FA&c]&7 2FA ist bei dir bereits Aktiviert. Kein Grund es nochmals zu aktivieren.");
        config.set("messages.removeauth", "&c[&42FA&c]&7 2FA wurde von deinem Account entfernt.");
        config.set("messages.notloggedin", "&c[&42FA&c]&7 2FA ist bei dir nicht Aktiviert. Du kannst daher diese Aktion nicht ausführen.");
        config.set("messages.logoutmessage", "&c[&42FA&c]&7 Du wurdest abgemeldet. Sobald du dich erneut anmeldest wirst du erneut nach dem Code gefragt.");
        config.set("messages.missingcode", "&c[&42FA&c]&7 Es fehlt dein 6 stelliger Code. Nutze /2fa activate <6 stelliger Code>");
        config.set("messages.errorocurred", "&c[&42FA&c]&7 Es ist ein Fehler aufgetreten. Versuche es bitte erneut, sollte es dann immer noch nicht funktionieren melde dich bitte bei uns im TeamSpeak (greev.eu) oder im Discord (https://discord.greev.eu)");
        config.set("messages.codeisinvalid", "&c[&42FA&c]&7 Der angegebene Code ist ungültig! Bitte überprüfe ihn erneut. Bei Fehlern melde dich bitte bei uns im TeamSpeak (greev.eu) oder im Discord (https://discord.greev.eu)");
        config.set("messages.successfulcctivated", "&c[&42FA&c]&7 Du hast deinen Account erfolgreich bestätigt. 2FA ist bei dir nun aktiv.");
        config.set("messages.needtoactivate", "&c[&42FA&c]&7 Es scheint so als wenn du 2FA bereits aktviert jedoch noch nicht bestätigt hast. &c&lSo schützt es deinen Account noch nicht! &7Bestätige 2FA indem du /2fa activate <6 Stelliger Code aus der App> ausführst.");
        config.set("messages.loginsuccessful", "&c[&42FA&c]&7 Bestätigung erfolgreich! Du kannst nun normal Spielen.");
        config.set("messages.waitingforauthcode", "&c[&42FA&c]&7 Bitte gebe zuerst deinen 6 Stelligen Code im Chat ein, bevor du diese Aktion ausführst.");


        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package eu.greev.twofa;

import eu.greev.twofa.commands.TwoFACommand;
import eu.greev.twofa.listeners.ChatListener;
import eu.greev.twofa.listeners.QuitListener;
import eu.greev.twofa.listeners.ServerSwitchListener;
import eu.greev.twofa.utils.ConfigUtils;
import eu.greev.twofa.utils.MySQL;
import eu.greev.twofa.utils.MySQLMethods;
import eu.greev.twofa.utils.TwoFactorAuthUtil;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Main extends Plugin {
    @Getter private static Main instance;
    @Getter private Configuration config;
    @Getter private MySQL mySQL;
    @Getter private MySQLMethods mySQLMethods;
    @Getter private final List<ProxiedPlayer> waitingForAuth = new ArrayList<>();
    @Getter private final TwoFactorAuthUtil twoFactorAuthUtil = new TwoFactorAuthUtil();

    @Override
    public void onEnable() {
        instance = this;

        try {
            this.config = ConfigUtils.getCustomConfig("2FA_Config.yml");
        } catch (IOException e) {
            e.printStackTrace();
            this.onDisable();
            return;
        }

        this.mySQL = new MySQL(
            this.config.getString("mysql.host"),
            this.config.getString("mysql.port"),
            this.config.getString("mysql.username"),
            this.config.getString("mysql.password"),
            this.config.getString("mysql.database")
        );
        this.mySQL.connect();
        this.mySQLMethods = new MySQLMethods(this.mySQL);
        this.mySQLMethods.createTable();

        this.registerCommands();
        this.registerEvents();
    }

    private void registerEvents() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new QuitListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ServerSwitchListener());
    }

    private void registerCommands() {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new TwoFACommand());
    }

    @Override
    public void onDisable() {
    }
}

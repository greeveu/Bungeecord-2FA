package eu.greev.twofa;

import eu.greev.twofa.commands.TwoFACommand;
import eu.greev.twofa.listeners.ChatListener;
import eu.greev.twofa.listeners.QuitListener;
import eu.greev.twofa.listeners.ServerSwitchListener;
import eu.greev.twofa.utils.ConfigHelper;
import eu.greev.twofa.utils.MySQL;
import eu.greev.twofa.utils.MySQLMethodes;
import eu.greev.twofa.utils.TwoFactorAuthUtil;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.ArrayList;
import java.util.List;

public final class Main extends Plugin {
    private final ConfigHelper configHelper = new ConfigHelper();

    @Getter private static Main instance;
    @Getter private YamlFile config;
    @Getter private MySQL mySQL;
    @Getter private final List<ProxiedPlayer> waitingForAuth = new ArrayList<>();
    @Getter private final TwoFactorAuthUtil twoFactorAuthUtil = new TwoFactorAuthUtil();

    @Override
    public void onEnable() {
        instance = this;
        this.config = this.configHelper.getConfig("plugins/2FA_Config.yml");
        this.mySQL = new MySQL(
            this.config.getString("mysql.host"),
            this.config.getString("mysql.port"),
            this.config.getString("mysql.username"),
            this.config.getString("mysql.password"),
            this.config.getString("mysql.database")
        );
        this.mySQL.connect();
        MySQLMethodes.createTable();
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

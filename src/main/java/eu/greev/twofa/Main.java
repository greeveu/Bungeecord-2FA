package eu.greev.twofa;

import eu.greev.twofa.commands.TwoFACommand;
import eu.greev.twofa.listeners.ChatListener;
import eu.greev.twofa.listeners.QuitListener;
import eu.greev.twofa.listeners.ServerSwitchListener;
import eu.greev.twofa.utils.ConfigHelper;
import eu.greev.twofa.utils.MySQL;
import eu.greev.twofa.utils.MySQLMethodes;
import eu.greev.twofa.utils.TwoFactorAuthUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.ArrayList;
import java.util.List;

public final class Main extends Plugin {

    private static Main main;
    public MySQL mySQL;
    public List<ProxiedPlayer> waitingForAuth = new ArrayList<>();
    public YamlFile config;
    private ConfigHelper configHelper = new ConfigHelper();
    public TwoFactorAuthUtil twoFactorAuthUtil = new TwoFactorAuthUtil();

    public static Main getInstance() {
        return main;
    }

    @Override
    public void onEnable() {
        main = this;
        config = configHelper.getConfig("plugins/2FA_Config.yml");
        mySQL = new MySQL(config.getString("mysql.host"), config.getString("mysql.port"), config.getString("mysql.username"), config.getString("mysql.password"), config.getString("mysql.database"));
        mySQL.connect();
        MySQLMethodes.createTable();
        registerCommands();
        registerEvents();
    }

    private void registerEvents() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new QuitListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ServerSwitchListener());
    }

    private void registerCommands() {
        getProxy().getPluginManager().registerCommand(this, new TwoFACommand());
    }

    @Override
    public void onDisable() { }
}

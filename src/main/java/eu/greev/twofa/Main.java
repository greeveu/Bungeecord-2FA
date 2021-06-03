package eu.greev.twofa;

import eu.greev.twofa.commands.TwoFACommand;
import eu.greev.twofa.entities.Spieler;
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

import java.util.HashMap;
import java.util.Map;

public final class Main extends Plugin {

    private static Main main;
    public static final Map<ProxiedPlayer, Spieler> PP_TO_S = new HashMap<>();
    public final TwoFactorAuthUtil twoFactorAuthUtil = new TwoFactorAuthUtil();
    private final ConfigHelper configHelper = new ConfigHelper();
    public YamlFile config;
    public MySQL mySQL;

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

    public static Spieler getSpieler(ProxiedPlayer player) {
        return PP_TO_S.get(player);
    }

    public static void removeSpieler(ProxiedPlayer player) {
        PP_TO_S.remove(player);
    }

    public static void addSpieler(ProxiedPlayer player, Spieler spieler) {
        PP_TO_S.put(player, spieler);
    }
}

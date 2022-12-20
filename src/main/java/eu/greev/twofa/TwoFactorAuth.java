package eu.greev.twofa;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import eu.greev.twofa.api.API;
import eu.greev.twofa.api.impl.APIImpl;
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
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;

public final class TwoFactorAuth extends Plugin {

    @Getter
    public static TwoFactorAuth instance;
    @Getter
    private final TwoFactorAuthUtil twoFactorAuthUtil = new TwoFactorAuthUtil();
    @Getter
    private Configuration config;
    @Getter
    private HikariDataSource hikari;
    @Getter
    private static final int MILLISECOND_TIMING_THRESHOLD = 30000;
    @Getter
    public API twoFactorApi;

    @Override
    public void onEnable() {
        instance = this;

        twoFactorApi = new APIImpl();

        try {
            this.config = ConfigUtils.getCustomConfig("2FA_Config.yml");
        } catch (IOException e) {
            e.printStackTrace();
            this.onDisable();
            return;
        }

        HikariConfig newDbHikariConfig = new HikariConfig();
        newDbHikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", getConfig().getString("mysql.host"), getConfig().getInt("mysql.port"), getConfig().getString("mysql.database")));
        newDbHikariConfig.setUsername(getConfig().getString("mysql.username"));
        newDbHikariConfig.setPassword(getConfig().getString("mysql.password"));
        newDbHikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        newDbHikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        newDbHikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikari = new HikariDataSource(newDbHikariConfig);

        MySQLMethods.createTable();

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
        hikari.close();
    }
}

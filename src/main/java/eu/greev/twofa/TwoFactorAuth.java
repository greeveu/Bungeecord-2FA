package eu.greev.twofa;

import com.yubico.client.v2.YubicoClient;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.greev.twofa.api.API;
import eu.greev.twofa.api.impl.APIImpl;
import eu.greev.twofa.commands.TwoFACommand;
import eu.greev.twofa.dao.TwoFaDao;
import eu.greev.twofa.dao.impl.TwoFaDaoImpl;
import eu.greev.twofa.listeners.ChatListener;
import eu.greev.twofa.listeners.QuitListener;
import eu.greev.twofa.listeners.ServerSwitchListener;
import eu.greev.twofa.service.TwoFaService;
import eu.greev.twofa.utils.ConfigUtils;
import eu.greev.twofa.utils.Language;
import eu.greev.twofa.utils.TwoFactorAuthUtil;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;

public final class TwoFactorAuth extends Plugin {

    @Getter
    public static API twoFactorApi;

    @Getter
    private TwoFaService twoFaService;

    private TwoFaDao twoFaDao;
    private Configuration config;
    private TwoFactorAuthUtil twoFactorAuthUtil;
    private HikariDataSource hikariDataSource;
    private YubicoClient yubicoClient;
    private Language language;

    @Override
    public void onEnable() {
        try {
            this.config = new ConfigUtils(this).getCustomConfig("2FA_Config.yml");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        language = new Language(config);

        loadDatabase();

        if (config.getBoolean("yubico.enabled", false)) {
            yubicoClient = YubicoClient.getClient(config.getInt("yubico.clientId"), config.getString("yubico.secretKey"));
        }
        twoFactorAuthUtil = new TwoFactorAuthUtil();
        twoFactorApi = new APIImpl(twoFaDao);
        twoFaService = new TwoFaService(this, twoFaDao, yubicoClient, twoFactorAuthUtil, language);

        this.registerCommands();
        this.registerEvents();
    }

    private void loadDatabase() {
        HikariConfig newDbHikariConfig = new HikariConfig();
        newDbHikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", config.getString("mysql.host"), config.getInt("mysql.port"), config.getString("mysql.database")));
        newDbHikariConfig.setUsername(config.getString("mysql.username"));
        newDbHikariConfig.setPassword(config.getString("mysql.password"));
        newDbHikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        newDbHikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        newDbHikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariDataSource = new HikariDataSource(newDbHikariConfig);

        twoFaDao = new TwoFaDaoImpl(hikariDataSource);

        twoFaDao.createTables();
    }

    private void registerEvents() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListener(twoFaService, language));
        ProxyServer.getInstance().getPluginManager().registerListener(this, new QuitListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ServerSwitchListener(twoFaService, language));
    }

    private void registerCommands() {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new TwoFACommand(twoFaService, language));
    }

    @Override
    public void onDisable() {
        hikariDataSource.close();
    }
}

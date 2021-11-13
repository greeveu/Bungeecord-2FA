package eu.greev.twofa.utils;

import eu.greev.twofa.TwoFactorAuth;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigUtils {
    private ConfigUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get custom config and save default if not already exists
     *
     * @param configName Name of custom config (with suffix)
     * @return Configuration
     */
    public static Configuration getCustomConfig(String configName) throws IOException {
        saveCustomConfigIfNotExist(configName);

        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(TwoFactorAuth.getInstance().getDataFolder(), configName));
    }

    /**
     * Save default config if not exist
     *
     * @param configName Name of custom config (with suffix)
     */
    public static void saveCustomConfigIfNotExist(String configName) {
        Plugin plugin = TwoFactorAuth.getInstance();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File file = new File(plugin.getDataFolder(), configName);

        if (!file.exists()) {
            try (InputStream in = plugin.getResourceAsStream(configName)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

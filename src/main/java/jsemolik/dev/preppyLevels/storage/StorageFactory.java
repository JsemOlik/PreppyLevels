package jsemolik.dev.preppyLevels.storage;

import jsemolik.dev.preppyLevels.config.PluginConfig;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

public class StorageFactory {
    public static StorageProvider createStorageProvider(PluginConfig config, Plugin plugin, Path dataDirectory) {
        switch (config.getStorageType()) {
            case MYSQL:
                return new MySQLStorageProvider(config.getMysqlConfig(), plugin);
            case H2:
                return new H2StorageProvider(config.getH2Config(), plugin, dataDirectory);
            case SQLITE:
                return new SQLiteStorageProvider(config.getSqliteConfig(), plugin, dataDirectory);
            case YAML:
                return new YAMLStorageProvider(plugin, dataDirectory);
            case JSON:
                return new JSONStorageProvider(plugin, dataDirectory);
            default:
                plugin.getLogger().warning("Unknown storage type, defaulting to H2");
                return new H2StorageProvider(config.getH2Config(), plugin, dataDirectory);
        }
    }
}


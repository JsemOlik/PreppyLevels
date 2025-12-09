package jsemolik.dev.preppyLevels.storage;

import jsemolik.dev.preppyLevels.config.PluginConfig;
import org.slf4j.Logger;

import java.nio.file.Path;

public class StorageFactory {
    public static StorageProvider createStorageProvider(PluginConfig config, Logger logger, Path dataDirectory) {
        switch (config.getStorageType()) {
            case MYSQL:
                return new MySQLStorageProvider(config.getMysqlConfig(), logger);
            case H2:
                return new H2StorageProvider(config.getH2Config(), logger, dataDirectory);
            case SQLITE:
                return new SQLiteStorageProvider(config.getSqliteConfig(), logger, dataDirectory);
            case YAML:
                return new YAMLStorageProvider(logger, dataDirectory);
            case JSON:
                return new JSONStorageProvider(logger, dataDirectory);
            default:
                logger.warn("Unknown storage type, defaulting to H2");
                return new H2StorageProvider(config.getH2Config(), logger, dataDirectory);
        }
    }
}


package jsemolik.dev.preppyLevels.config;

import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {
    private final Logger logger;
    private final Path configPath;

    public ConfigLoader(Logger logger, Path configPath) {
        this.logger = logger;
        this.configPath = configPath;
    }

    public PluginConfig loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                logger.warn("Config file not found, creating default config...");
                createDefaultConfig();
            }

            Yaml yaml = new Yaml();
            Map<String, Object> data;
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                data = yaml.load(inputStream);
            }

            PluginConfig config = new PluginConfig();
            
            // Storage type
            String storageTypeStr = (String) data.getOrDefault("storage-type", "H2");
            config.setStorageType(PluginConfig.StorageType.valueOf(storageTypeStr.toUpperCase()));

            // MySQL config
            if (data.containsKey("mysql")) {
                Map<String, Object> mysqlData = (Map<String, Object>) data.get("mysql");
                PluginConfig.MySQLConfig mysqlConfig = new PluginConfig.MySQLConfig();
                mysqlConfig.setHost((String) mysqlData.getOrDefault("host", "localhost"));
                mysqlConfig.setPort((Integer) mysqlData.getOrDefault("port", 3306));
                mysqlConfig.setDatabase((String) mysqlData.getOrDefault("database", "preppylevels"));
                mysqlConfig.setUsername((String) mysqlData.getOrDefault("username", "root"));
                mysqlConfig.setPassword((String) mysqlData.getOrDefault("password", "password"));
                mysqlConfig.setPoolSize((Integer) mysqlData.getOrDefault("pool-size", 10));
                config.setMysqlConfig(mysqlConfig);
            }

            // H2 config
            if (data.containsKey("h2")) {
                Map<String, Object> h2Data = (Map<String, Object>) data.get("h2");
                PluginConfig.H2Config h2Config = new PluginConfig.H2Config();
                h2Config.setFile((String) h2Data.getOrDefault("file", "preppylevels.db"));
                config.setH2Config(h2Config);
            }

            // SQLite config
            if (data.containsKey("sqlite")) {
                Map<String, Object> sqliteData = (Map<String, Object>) data.get("sqlite");
                PluginConfig.SQLiteConfig sqliteConfig = new PluginConfig.SQLiteConfig();
                sqliteConfig.setFile((String) sqliteData.getOrDefault("file", "preppylevels.db"));
                config.setSqliteConfig(sqliteConfig);
            }

            // XP requirements
            if (data.containsKey("xp-requirements")) {
                Map<String, Object> xpReqData = (Map<String, Object>) data.get("xp-requirements");
                Map<Integer, Integer> xpRequirements = new HashMap<>();
                for (Map.Entry<String, Object> entry : xpReqData.entrySet()) {
                    try {
                        int level = Integer.parseInt(entry.getKey());
                        int xp = ((Number) entry.getValue()).intValue();
                        xpRequirements.put(level, xp);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid XP requirement entry: " + entry.getKey());
                    }
                }
                config.setXpRequirements(xpRequirements);
            } else {
                config.setXpRequirements(new HashMap<>());
            }

            config.setDefaultXpIncrement((Integer) data.getOrDefault("default-xp-increment", 100));

            // Auto XP config
            if (data.containsKey("auto-xp")) {
                Map<String, Object> autoXpData = (Map<String, Object>) data.get("auto-xp");
                PluginConfig.AutoXpConfig autoXpConfig = new PluginConfig.AutoXpConfig();
                autoXpConfig.setEnabled((Boolean) autoXpData.getOrDefault("enabled", true));
                
                if (autoXpData.containsKey("tasks")) {
                    Map<String, Object> tasksData = (Map<String, Object>) autoXpData.get("tasks");
                    Map<String, Integer> tasks = new HashMap<>();
                    for (Map.Entry<String, Object> entry : tasksData.entrySet()) {
                        tasks.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                    }
                    autoXpConfig.setTasks(tasks);
                } else {
                    autoXpConfig.setTasks(new HashMap<>());
                }
                config.setAutoXpConfig(autoXpConfig);
            }

            // Messages
            if (data.containsKey("messages")) {
                Map<String, Object> messagesData = (Map<String, Object>) data.get("messages");
                PluginConfig.MessageConfig messageConfig = new PluginConfig.MessageConfig();
                messageConfig.setXpGained((String) messagesData.getOrDefault("xp-gained", "&a+{xp} XP"));
                messageConfig.setLevelUp((String) messagesData.getOrDefault("level-up", "&6&lLEVEL UP!"));
                messageConfig.setXpNeeded((String) messagesData.getOrDefault("xp-needed", "&7You need {xp} more XP"));
                config.setMessageConfig(messageConfig);
            }

            // XP Bar config
            if (data.containsKey("xp-bar")) {
                Map<String, Object> xpBarData = (Map<String, Object>) data.get("xp-bar");
                PluginConfig.XpBarConfig xpBarConfig = new PluginConfig.XpBarConfig();
                xpBarConfig.setUpdateInterval((Integer) xpBarData.getOrDefault("update-interval", 20));
                xpBarConfig.setShowLevel((Boolean) xpBarData.getOrDefault("show-level", true));
                config.setXpBarConfig(xpBarConfig);
            }

            logger.info("Configuration loaded successfully!");
            return config;
        } catch (Exception e) {
            logger.error("Failed to load config", e);
            return createDefaultConfig();
        }
    }

    private PluginConfig createDefaultConfig() {
        // Return a default config if file creation fails
        PluginConfig config = new PluginConfig();
        config.setStorageType(PluginConfig.StorageType.H2);
        config.setDefaultXpIncrement(100);
        Map<Integer, Integer> xpReq = new HashMap<>();
        xpReq.put(1, 100);
        config.setXpRequirements(xpReq);
        return config;
    }
}


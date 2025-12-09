package jsemolik.dev.preppyLevels.config;

import org.yaml.snakeyaml.Yaml;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {
    private final org.bukkit.plugin.Plugin plugin;
    private final Path configPath;

    public ConfigLoader(org.bukkit.plugin.Plugin plugin, Path configPath) {
        this.plugin = plugin;
        this.configPath = configPath;
    }

    @SuppressWarnings("unchecked")
    public PluginConfig loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                plugin.getLogger().warning("Config file not found, creating default config...");
                createDefaultConfigFile();
            }

            Yaml yaml = new Yaml();
            Map<String, Object> data;
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                Object loaded = yaml.load(inputStream);
                if (loaded == null) {
                    plugin.getLogger().warning("Config file is empty, using defaults");
                    return createDefaultConfig();
                }
                data = (Map<String, Object>) loaded;
            }

            PluginConfig config = new PluginConfig();
            
            // Storage type
            String storageTypeStr = (String) data.getOrDefault("storage-type", "H2");
            config.setStorageType(PluginConfig.StorageType.valueOf(storageTypeStr.toUpperCase()));

            // MySQL config
            if (data.containsKey("mysql")) {
                @SuppressWarnings("unchecked")
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
            PluginConfig.H2Config h2Config = new PluginConfig.H2Config();
            if (data.containsKey("h2")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> h2Data = (Map<String, Object>) data.get("h2");
                h2Config.setFile((String) h2Data.getOrDefault("file", "preppylevels.db"));
            } else {
                h2Config.setFile("preppylevels.db");
            }
            config.setH2Config(h2Config);

            // SQLite config
            PluginConfig.SQLiteConfig sqliteConfig = new PluginConfig.SQLiteConfig();
            if (data.containsKey("sqlite")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sqliteData = (Map<String, Object>) data.get("sqlite");
                sqliteConfig.setFile((String) sqliteData.getOrDefault("file", "preppylevels.db"));
            } else {
                sqliteConfig.setFile("preppylevels.db");
            }
            config.setSqliteConfig(sqliteConfig);

            // XP requirements
            if (data.containsKey("xp-requirements")) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> xpReqData = (Map<Object, Object>) data.get("xp-requirements");
                Map<Integer, Integer> xpRequirements = new HashMap<>();
                for (Map.Entry<Object, Object> entry : xpReqData.entrySet()) {
                    try {
                        int level;
                        if (entry.getKey() instanceof Integer) {
                            level = (Integer) entry.getKey();
                        } else {
                            level = Integer.parseInt(entry.getKey().toString());
                        }
                        int xp = ((Number) entry.getValue()).intValue();
                        xpRequirements.put(level, xp);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid XP requirement entry: " + entry.getKey());
                    }
                }
                config.setXpRequirements(xpRequirements);
            } else {
                config.setXpRequirements(new HashMap<>());
            }

            config.setDefaultXpIncrement((Integer) data.getOrDefault("default-xp-increment", 100));
            PluginConfig.MessageConfig messageConfig = new PluginConfig.MessageConfig();
            if (data.containsKey("messages")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> messagesData = (Map<String, Object>) data.get("messages");
                messageConfig.setXpGained((String) messagesData.getOrDefault("xp-gained", "&a+{xp} XP"));
                messageConfig.setLevelUp((String) messagesData.getOrDefault("level-up", "&6&lLEVEL UP!"));
                messageConfig.setXpNeeded((String) messagesData.getOrDefault("xp-needed", "&7You need {xp} more XP"));
            } else {
                messageConfig.setXpGained("&a+{xp} XP");
                messageConfig.setLevelUp("&6&lLEVEL UP!");
                messageConfig.setXpNeeded("&7You need {xp} more XP");
            }
            config.setMessageConfig(messageConfig);

            // XP Bar config
            PluginConfig.XpBarConfig xpBarConfig = new PluginConfig.XpBarConfig();
            if (data.containsKey("xp-bar")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> xpBarData = (Map<String, Object>) data.get("xp-bar");
                xpBarConfig.setUpdateInterval((Integer) xpBarData.getOrDefault("update-interval", 20));
                xpBarConfig.setShowLevel((Boolean) xpBarData.getOrDefault("show-level", true));
            } else {
                xpBarConfig.setUpdateInterval(20);
                xpBarConfig.setShowLevel(true);
            }
            config.setXpBarConfig(xpBarConfig);
            
            // Auto XP config (with defaults)
            PluginConfig.AutoXpConfig autoXpConfig = new PluginConfig.AutoXpConfig();
            if (data.containsKey("auto-xp")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> autoXpData = (Map<String, Object>) data.get("auto-xp");
                autoXpConfig.setEnabled((Boolean) autoXpData.getOrDefault("enabled", true));
                
                if (autoXpData.containsKey("tasks")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tasksData = (Map<String, Object>) autoXpData.get("tasks");
                    Map<String, Integer> tasks = new HashMap<>();
                    for (Map.Entry<String, Object> entry : tasksData.entrySet()) {
                        tasks.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                    }
                    autoXpConfig.setTasks(tasks);
                } else {
                    autoXpConfig.setTasks(new HashMap<>());
                }
            } else {
                autoXpConfig.setEnabled(true);
                autoXpConfig.setTasks(new HashMap<>());
            }
            config.setAutoXpConfig(autoXpConfig);

            plugin.getLogger().info("Configuration loaded successfully!");
            return config;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load config: " + e.getMessage());
            e.printStackTrace();
            return createDefaultConfig();
        }
    }

    private void createDefaultConfigFile() {
        try {
            // Create parent directories if they don't exist
            Files.createDirectories(configPath.getParent());
            
            // Copy default config from resources
            try (InputStream defaultConfig = plugin.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (defaultConfig != null) {
                    Files.copy(defaultConfig, configPath, StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("Default config file created at: " + configPath);
                } else {
                    plugin.getLogger().severe("Default config.yml not found in resources!");
                    // Create a minimal config file
                    createMinimalConfigFile();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create default config file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createMinimalConfigFile() {
        try {
            String minimalConfig = 
                "storage-type: H2\n" +
                "h2:\n" +
                "  file: preppylevels.db\n" +
                "xp-requirements:\n" +
                "  1: 100\n" +
                "default-xp-increment: 100\n" +
                "auto-xp:\n" +
                "  enabled: true\n" +
                "  tasks: {}\n" +
                "messages:\n" +
                "  xp-gained: \"&a+{xp} XP\"\n" +
                "  level-up: \"&6&lLEVEL UP!\"\n" +
                "  xp-needed: \"&7You need {xp} more XP\"\n" +
                "xp-bar:\n" +
                "  update-interval: 20\n" +
                "  show-level: true\n";
            Files.writeString(configPath, minimalConfig);
            plugin.getLogger().info("Minimal config file created");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create minimal config file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private PluginConfig createDefaultConfig() {
        // Return a default config if file creation fails
        PluginConfig config = new PluginConfig();
        config.setStorageType(PluginConfig.StorageType.H2);
        config.setDefaultXpIncrement(100);
        
        // Set H2 config
        PluginConfig.H2Config h2Config = new PluginConfig.H2Config();
        h2Config.setFile("preppylevels.db");
        config.setH2Config(h2Config);
        
        // Set SQLite config
        PluginConfig.SQLiteConfig sqliteConfig = new PluginConfig.SQLiteConfig();
        sqliteConfig.setFile("preppylevels.db");
        config.setSqliteConfig(sqliteConfig);
        
        // Set XP requirements
        Map<Integer, Integer> xpReq = new HashMap<>();
        xpReq.put(1, 100);
        config.setXpRequirements(xpReq);
        
        // Set messages
        PluginConfig.MessageConfig messageConfig = new PluginConfig.MessageConfig();
        messageConfig.setXpGained("&a+{xp} XP");
        messageConfig.setLevelUp("&6&lLEVEL UP!");
        messageConfig.setXpNeeded("&7You need {xp} more XP");
        config.setMessageConfig(messageConfig);
        
        // Set XP bar config
        PluginConfig.XpBarConfig xpBarConfig = new PluginConfig.XpBarConfig();
        xpBarConfig.setUpdateInterval(20);
        xpBarConfig.setShowLevel(true);
        config.setXpBarConfig(xpBarConfig);
        
        // Set auto XP config
        PluginConfig.AutoXpConfig autoXpConfig = new PluginConfig.AutoXpConfig();
        autoXpConfig.setEnabled(true);
        autoXpConfig.setTasks(new HashMap<>());
        config.setAutoXpConfig(autoXpConfig);
        
        return config;
    }
}


package jsemolik.dev.preppyLevels.config;

import java.util.HashMap;
import java.util.Map;

public class PluginConfig {
    private StorageType storageType;
    private MySQLConfig mysqlConfig;
    private H2Config h2Config;
    private SQLiteConfig sqliteConfig;
    private Map<Integer, Integer> xpRequirements;
    private int defaultXpIncrement;
    private AutoXpConfig autoXpConfig;
    private MessageConfig messageConfig;
    private XpBarConfig xpBarConfig;

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public MySQLConfig getMysqlConfig() {
        return mysqlConfig;
    }

    public void setMysqlConfig(MySQLConfig mysqlConfig) {
        this.mysqlConfig = mysqlConfig;
    }

    public H2Config getH2Config() {
        return h2Config;
    }

    public void setH2Config(H2Config h2Config) {
        this.h2Config = h2Config;
    }

    public SQLiteConfig getSqliteConfig() {
        return sqliteConfig;
    }

    public void setSqliteConfig(SQLiteConfig sqliteConfig) {
        this.sqliteConfig = sqliteConfig;
    }

    public Map<Integer, Integer> getXpRequirements() {
        return xpRequirements;
    }

    public void setXpRequirements(Map<Integer, Integer> xpRequirements) {
        this.xpRequirements = xpRequirements;
    }

    public int getXpRequiredForLevel(int level) {
        if (xpRequirements.containsKey(level)) {
            return xpRequirements.get(level);
        }
        
        // Find the highest defined level
        int highestLevel = 0;
        int highestXp = 0;
        for (Map.Entry<Integer, Integer> entry : xpRequirements.entrySet()) {
            if (entry.getKey() > highestLevel) {
                highestLevel = entry.getKey();
                highestXp = entry.getValue();
            }
        }
        
        // Calculate based on increment
        int levelsAbove = level - highestLevel;
        return highestXp + (levelsAbove * defaultXpIncrement);
    }

    public int getDefaultXpIncrement() {
        return defaultXpIncrement;
    }

    public void setDefaultXpIncrement(int defaultXpIncrement) {
        this.defaultXpIncrement = defaultXpIncrement;
    }

    public AutoXpConfig getAutoXpConfig() {
        return autoXpConfig;
    }

    public void setAutoXpConfig(AutoXpConfig autoXpConfig) {
        this.autoXpConfig = autoXpConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public void setMessageConfig(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    public XpBarConfig getXpBarConfig() {
        return xpBarConfig;
    }

    public void setXpBarConfig(XpBarConfig xpBarConfig) {
        this.xpBarConfig = xpBarConfig;
    }

    public enum StorageType {
        MYSQL, H2, SQLITE, YAML, JSON
    }

    public static class MySQLConfig {
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;
        private int poolSize;

        // Getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
    }

    public static class H2Config {
        private String file;

        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
    }

    public static class SQLiteConfig {
        private String file;

        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
    }

    public static class AutoXpConfig {
        private boolean enabled;
        private Map<String, Integer> tasks;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Map<String, Integer> getTasks() { return tasks; }
        public void setTasks(Map<String, Integer> tasks) { this.tasks = tasks; }
        
        public int getXpForTask(String task) {
            return tasks != null ? tasks.getOrDefault(task, 0) : 0;
        }
    }

    public static class MessageConfig {
        private String xpGained;
        private String levelUp;
        private String xpNeeded;

        public String getXpGained() { return xpGained; }
        public void setXpGained(String xpGained) { this.xpGained = xpGained; }
        public String getLevelUp() { return levelUp; }
        public void setLevelUp(String levelUp) { this.levelUp = levelUp; }
        public String getXpNeeded() { return xpNeeded; }
        public void setXpNeeded(String xpNeeded) { this.xpNeeded = xpNeeded; }
    }

    public static class XpBarConfig {
        private int updateInterval;
        private boolean showLevel;

        public int getUpdateInterval() { return updateInterval; }
        public void setUpdateInterval(int updateInterval) { this.updateInterval = updateInterval; }
        public boolean isShowLevel() { return showLevel; }
        public void setShowLevel(boolean showLevel) { this.showLevel = showLevel; }
    }
}


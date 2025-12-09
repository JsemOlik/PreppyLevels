package jsemolik.dev.preppyLevels.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySQLStorageProvider implements StorageProvider {
    private final Plugin plugin;
    private final jsemolik.dev.preppyLevels.config.PluginConfig.MySQLConfig config;
    private HikariDataSource dataSource;
    private final ExecutorService executor;

    public MySQLStorageProvider(jsemolik.dev.preppyLevels.config.PluginConfig.MySQLConfig config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase());
                hikariConfig.setUsername(config.getUsername());
                hikariConfig.setPassword(config.getPassword());
                hikariConfig.setMaximumPoolSize(config.getPoolSize());
                hikariConfig.setMinimumIdle(2);
                
                dataSource = new HikariDataSource(hikariConfig);
                
                try (Connection conn = dataSource.getConnection()) {
                    conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS player_data (" +
                        "player_id VARCHAR(36) PRIMARY KEY, " +
                        "player_name VARCHAR(16) NOT NULL, " +
                        "level INT NOT NULL DEFAULT 1, " +
                        "xp BIGINT NOT NULL DEFAULT 0" +
                        ")"
                    );
                }
                plugin.getLogger().info("MySQL database initialized successfully");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to initialize MySQL database: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            if (dataSource != null) {
                dataSource.close();
            }
            executor.shutdown();
        });
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT player_name, level, xp FROM player_data WHERE player_id = ?"
                 )) {
                stmt.setString(1, playerId.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return new PlayerData(
                        playerId,
                        rs.getString("player_name"),
                        rs.getInt("level"),
                        rs.getLong("xp")
                    );
                }
                return null;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load player data for " + playerId + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> savePlayerData(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_data (player_id, player_name, level, xp) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE player_name = ?, level = ?, xp = ?"
                 )) {
                stmt.setString(1, playerData.getPlayerId().toString());
                stmt.setString(2, playerData.getPlayerName());
                stmt.setInt(3, playerData.getLevel());
                stmt.setLong(4, playerData.getXp());
                stmt.setString(5, playerData.getPlayerName());
                stmt.setInt(6, playerData.getLevel());
                stmt.setLong(7, playerData.getXp());
                stmt.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save player data for " + playerData.getPlayerId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> playerExists(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM player_data WHERE player_id = ?"
                 )) {
                stmt.setString(1, playerId.toString());
                return stmt.executeQuery().next();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to check if player exists: " + playerId + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, executor);
    }
}


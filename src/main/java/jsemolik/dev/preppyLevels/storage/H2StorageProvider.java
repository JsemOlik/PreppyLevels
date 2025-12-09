package jsemolik.dev.preppyLevels.storage;

import org.h2.jdbcx.JdbcConnectionPool;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class H2StorageProvider implements StorageProvider {
    private final Plugin plugin;
    private final Path dataDirectory;
    private final String dbFile;
    private JdbcConnectionPool connectionPool;
    private final ExecutorService executor;

    public H2StorageProvider(jsemolik.dev.preppyLevels.config.PluginConfig.H2Config config, Plugin plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        this.dbFile = config.getFile();
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                String jdbcUrl = "jdbc:h2:" + dataDirectory.resolve(dbFile).toAbsolutePath();
                connectionPool = JdbcConnectionPool.create(jdbcUrl, "", "");
                
                try (Connection conn = connectionPool.getConnection()) {
                    conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS player_data (" +
                        "player_id VARCHAR(36) PRIMARY KEY, " +
                        "player_name VARCHAR(16) NOT NULL, " +
                        "level INT NOT NULL DEFAULT 1, " +
                        "xp BIGINT NOT NULL DEFAULT 0" +
                        ")"
                    );
                }
                plugin.getLogger().info("H2 database initialized successfully");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to initialize H2 database: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            if (connectionPool != null) {
                connectionPool.dispose();
            }
            executor.shutdown();
        });
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connectionPool.getConnection();
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
            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "MERGE INTO player_data (player_id, player_name, level, xp) VALUES (?, ?, ?, ?)"
                 )) {
                stmt.setString(1, playerData.getPlayerId().toString());
                stmt.setString(2, playerData.getPlayerName());
                stmt.setInt(3, playerData.getLevel());
                stmt.setLong(4, playerData.getXp());
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
            try (Connection conn = connectionPool.getConnection();
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


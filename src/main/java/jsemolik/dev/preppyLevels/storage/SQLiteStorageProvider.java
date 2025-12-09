package jsemolik.dev.preppyLevels.storage;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLiteStorageProvider implements StorageProvider {
    private final Logger logger;
    private final Path dataDirectory;
    private final String dbFile;
    private final ExecutorService executor;

    public SQLiteStorageProvider(jsemolik.dev.preppyLevels.config.PluginConfig.SQLiteConfig config, Logger logger, Path dataDirectory) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.dbFile = config.getFile();
        this.executor = Executors.newCachedThreadPool();
    }

    private Connection getConnection() throws Exception {
        String jdbcUrl = "jdbc:sqlite:" + dataDirectory.resolve(dbFile).toAbsolutePath();
        return DriverManager.getConnection(jdbcUrl);
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS player_data (" +
                    "player_id TEXT PRIMARY KEY, " +
                    "player_name TEXT NOT NULL, " +
                    "level INTEGER NOT NULL DEFAULT 1, " +
                    "xp INTEGER NOT NULL DEFAULT 0" +
                    ")"
                );
                logger.info("SQLite database initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize SQLite database", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            executor.shutdown();
        });
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
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
                logger.error("Failed to load player data for " + playerId, e);
                return null;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> savePlayerData(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO player_data (player_id, player_name, level, xp) VALUES (?, ?, ?, ?)"
                 )) {
                stmt.setString(1, playerData.getPlayerId().toString());
                stmt.setString(2, playerData.getPlayerName());
                stmt.setInt(3, playerData.getLevel());
                stmt.setLong(4, playerData.getXp());
                stmt.executeUpdate();
            } catch (Exception e) {
                logger.error("Failed to save player data for " + playerData.getPlayerId(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> playerExists(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM player_data WHERE player_id = ?"
                 )) {
                stmt.setString(1, playerId.toString());
                return stmt.executeQuery().next();
            } catch (Exception e) {
                logger.error("Failed to check if player exists: " + playerId, e);
                return false;
            }
        }, executor);
    }
}


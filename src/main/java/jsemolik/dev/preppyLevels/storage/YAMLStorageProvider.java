package jsemolik.dev.preppyLevels.storage;

import org.yaml.snakeyaml.Yaml;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YAMLStorageProvider implements StorageProvider {
    private final Plugin plugin;
    private final Path dataDirectory;
    private final Yaml yaml;
    private final ExecutorService executor;

    public YAMLStorageProvider(Plugin plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        this.yaml = new Yaml();
        this.executor = Executors.newCachedThreadPool();
    }

    private Path getPlayerFile(UUID playerId) {
        return dataDirectory.resolve("players").resolve(playerId.toString() + ".yml");
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(dataDirectory.resolve("players"));
                plugin.getLogger().info("YAML storage initialized successfully");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to initialize YAML storage: " + e.getMessage());
                e.printStackTrace();
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
            Path file = getPlayerFile(playerId);
            if (!Files.exists(file)) {
                return null;
            }
            
            try {
                Map<String, Object> data = yaml.load(Files.newInputStream(file));
                return new PlayerData(
                    playerId,
                    (String) data.get("player_name"),
                    ((Number) data.getOrDefault("level", 1)).intValue(),
                    ((Number) data.getOrDefault("xp", 0)).longValue()
                );
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
            Path file = getPlayerFile(playerData.getPlayerId());
            try {
                Files.createDirectories(file.getParent());
                
                Map<String, Object> data = new HashMap<>();
                data.put("player_id", playerData.getPlayerId().toString());
                data.put("player_name", playerData.getPlayerName());
                data.put("level", playerData.getLevel());
                data.put("xp", playerData.getXp());
                
                yaml.dump(data, Files.newBufferedWriter(file));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save player data for " + playerData.getPlayerId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> playerExists(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            return Files.exists(getPlayerFile(playerId));
        }, executor);
    }
}


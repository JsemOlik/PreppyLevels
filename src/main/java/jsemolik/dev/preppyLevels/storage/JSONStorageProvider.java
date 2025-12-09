package jsemolik.dev.preppyLevels.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JSONStorageProvider implements StorageProvider {
    private final Plugin plugin;
    private final Path dataDirectory;
    private final Gson gson;
    private final ExecutorService executor;

    public JSONStorageProvider(Plugin plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.executor = Executors.newCachedThreadPool();
    }

    private Path getPlayerFile(UUID playerId) {
        return dataDirectory.resolve("players").resolve(playerId.toString() + ".json");
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(dataDirectory.resolve("players"));
                plugin.getLogger().info("JSON storage initialized successfully");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to initialize JSON storage: " + e.getMessage());
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
                String json = Files.readString(file);
                PlayerDataJson data = gson.fromJson(json, PlayerDataJson.class);
                return new PlayerData(
                    playerId,
                    data.player_name,
                    data.level,
                    data.xp
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
                
                PlayerDataJson data = new PlayerDataJson();
                data.player_id = playerData.getPlayerId().toString();
                data.player_name = playerData.getPlayerName();
                data.level = playerData.getLevel();
                data.xp = playerData.getXp();
                
                Files.writeString(file, gson.toJson(data));
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

    private static class PlayerDataJson {
        String player_id;
        String player_name;
        int level;
        long xp;
    }
}


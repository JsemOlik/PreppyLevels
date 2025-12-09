package jsemolik.dev.preppyLevels.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageProvider {
    /**
     * Initialize the storage provider
     */
    CompletableFuture<Void> initialize();

    /**
     * Shutdown the storage provider
     */
    CompletableFuture<Void> shutdown();

    /**
     * Load player data
     */
    CompletableFuture<PlayerData> loadPlayerData(UUID playerId);

    /**
     * Save player data
     */
    CompletableFuture<Void> savePlayerData(PlayerData playerData);

    /**
     * Check if player data exists
     */
    CompletableFuture<Boolean> playerExists(UUID playerId);
}


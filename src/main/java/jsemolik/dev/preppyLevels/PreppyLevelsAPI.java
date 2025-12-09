package jsemolik.dev.preppyLevels;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Public API for other plugins to interact with PreppyLevels
 */
public class PreppyLevelsAPI {
    private final PreppyLevels plugin;

    public PreppyLevelsAPI(PreppyLevels plugin) {
        this.plugin = plugin;
    }

    /**
     * Give XP to a player
     * @param playerId The UUID of the player
     * @param playerName The name of the player
     * @param xpAmount The amount of XP to give
     * @return CompletableFuture that completes when XP is added
     */
    public CompletableFuture<Void> giveXp(UUID playerId, String playerName, long xpAmount) {
        return plugin.getLevelManager().addXp(playerId, playerName, xpAmount);
    }

    /**
     * Get a player's current level
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with the player's level
     */
    public CompletableFuture<Integer> getLevel(UUID playerId) {
        return plugin.getLevelManager().getPlayerData(playerId)
            .thenApply(data -> data != null ? data.getLevel() : 1);
    }

    /**
     * Get a player's current XP
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with the player's total XP
     */
    public CompletableFuture<Long> getXp(UUID playerId) {
        return plugin.getLevelManager().getPlayerData(playerId)
            .thenApply(data -> data != null ? data.getXp() : 0L);
    }

    /**
     * Get the XP needed for a player to reach their next level
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with the XP needed
     */
    public CompletableFuture<Long> getXpNeededForNextLevel(UUID playerId) {
        return plugin.getLevelManager().getPlayerData(playerId)
            .thenApply(data -> {
                if (data == null) {
                    return (long) plugin.getPluginConfig().getXpRequiredForLevel(2);
                }
                return plugin.getLevelManager().getXpForNextLevel(data);
            });
    }
}


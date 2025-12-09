package jsemolik.dev.preppyLevels;

import jsemolik.dev.preppyLevels.config.PluginConfig;
import jsemolik.dev.preppyLevels.storage.PlayerData;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LevelManager {
    private final PreppyLevels plugin;
    private final PluginConfig config;
    private final Logger logger;
    private final ConcurrentHashMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public LevelManager(PreppyLevels plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.logger = plugin.getLogger();
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID playerId) {
        // Check cache first
        PlayerData cached = cache.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Load from storage
        return plugin.getStorageProvider().loadPlayerData(playerId).thenApply(data -> {
            if (data != null) {
                cache.put(playerId, data);
            }
            return data;
        });
    }

    public CompletableFuture<Void> addXp(UUID playerId, String playerName, long xpAmount) {
        return getPlayerData(playerId).thenCompose(data -> {
            if (data == null) {
                // Create new player data
                data = new jsemolik.dev.preppyLevels.storage.PlayerData(playerId, playerName, 1, 0);
            }

            long oldXp = data.getXp();
            int oldLevel = data.getLevel();
            
            data.addXp(xpAmount);
            
            // Check for level up
            int newLevel = calculateLevel(data.getXp());
            boolean leveledUp = newLevel > oldLevel;
            
            if (leveledUp) {
                data.setLevel(newLevel);
            }

            // Update cache
            cache.put(playerId, data);

            // Save to storage
            return plugin.getStorageProvider().savePlayerData(data).thenRun(() -> {
                // Send messages
                if (leveledUp) {
                    sendLevelUpMessage(playerId, newLevel);
                } else {
                    sendXpGainedMessage(playerId, xpAmount, data);
                }
            });
        });
    }

    private int calculateLevel(long totalXp) {
        if (totalXp < 0) {
            return 1;
        }
        
        int level = 1;
        long accumulatedXp = 0;
        
        while (true) {
            int xpNeeded = config.getXpRequiredForLevel(level);
            if (accumulatedXp + xpNeeded > totalXp) {
                break;
            }
            accumulatedXp += xpNeeded;
            level++;
        }
        
        return level;
    }

    public long getXpForNextLevel(PlayerData data) {
        int nextLevel = data.getLevel() + 1;
        int xpNeededForNext = config.getXpRequiredForLevel(nextLevel);
        
        // Calculate how much XP the player has in their current level
        long xpInCurrentLevel = 0;
        for (int i = 1; i < data.getLevel(); i++) {
            xpInCurrentLevel += config.getXpRequiredForLevel(i);
        }
        
        long xpProgress = data.getXp() - xpInCurrentLevel;
        return xpNeededForNext - xpProgress;
    }

    public float getLevelProgress(PlayerData data) {
        int currentLevel = data.getLevel();
        int xpNeededForCurrent = config.getXpRequiredForLevel(currentLevel);
        
        // Calculate how much XP the player has in their current level
        long xpInPreviousLevels = 0;
        for (int i = 1; i < currentLevel; i++) {
            xpInPreviousLevels += config.getXpRequiredForLevel(i);
        }
        
        long xpProgress = data.getXp() - xpInPreviousLevels;
        return Math.min(1.0f, (float) xpProgress / xpNeededForCurrent);
    }

    private void sendXpGainedMessage(UUID playerId, long xpAmount, PlayerData data) {
        plugin.getServer().getPlayer(playerId).ifPresent(player -> {
            long xpNeeded = getXpForNextLevel(data);
            int nextLevel = data.getLevel() + 1;
            
            String message = config.getMessageConfig().getXpGained()
                .replace("{xp}", String.valueOf(xpAmount))
                .replace("{current}", String.valueOf(data.getXp()))
                .replace("{needed}", String.valueOf(xpNeeded))
                .replace("{next}", String.valueOf(nextLevel));
            
            player.sendMessage(net.kyori.adventure.text.Component.text(message.replace("&", "§")));
        });
    }

    private void sendLevelUpMessage(UUID playerId, int level) {
        plugin.getServer().getPlayer(playerId).ifPresent(player -> {
            String message = config.getMessageConfig().getLevelUp()
                .replace("{level}", String.valueOf(level));
            
            player.sendMessage(net.kyori.adventure.text.Component.text(message.replace("&", "§")));
        });
    }

    public void invalidateCache(UUID playerId) {
        cache.remove(playerId);
    }

    public void clearCache() {
        cache.clear();
    }
}


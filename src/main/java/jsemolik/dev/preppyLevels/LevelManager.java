package jsemolik.dev.preppyLevels;

import jsemolik.dev.preppyLevels.config.PluginConfig;
import jsemolik.dev.preppyLevels.storage.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LevelManager {
    private final PreppyLevels plugin;
    private final PluginConfig config;
    private final ConcurrentHashMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public LevelManager(PreppyLevels plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
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
            // Create or get player data
            PlayerData playerData = data;
            if (playerData == null) {
                playerData = new PlayerData(playerId, playerName, 1, 0);
            }

            long oldXp = playerData.getXp();
            int oldLevel = playerData.getLevel();
            
            playerData.addXp(xpAmount);
            
            // Check for level up
            int newLevel = calculateLevel(playerData.getXp());
            boolean leveledUp = newLevel > oldLevel;
            
            if (leveledUp) {
                playerData.setLevel(newLevel);
            }

            // Update cache
            cache.put(playerId, playerData);

            // Make final copies for lambda
            final PlayerData finalData = playerData;
            final int finalNewLevel = newLevel;
            final boolean finalLeveledUp = leveledUp;

            // Save to storage
            return plugin.getStorageProvider().savePlayerData(finalData).thenRun(() -> {
                // Run on main thread for Bukkit API calls
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Send messages only on level up
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        if (finalLeveledUp) {
                            sendLevelUpMessage(player, finalData);
                        }
                        
                        // Update XP bar
                        updateXpBar(player, finalData);
                    }
                });
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
        int xpNeededForNext = config.getXpRequiredForLevel(currentLevel);
        
        // Calculate how much XP the player has accumulated for previous levels
        long xpInPreviousLevels = 0;
        for (int i = 1; i < currentLevel; i++) {
            xpInPreviousLevels += config.getXpRequiredForLevel(i);
        }
        
        // Calculate XP progress in current level (towards next level)
        long xpProgress = data.getXp() - xpInPreviousLevels;
        
        // Return progress as a float between 0.0 and 1.0
        if (xpNeededForNext <= 0) {
            return 1.0f; // Already max level or invalid
        }
        return Math.max(0.0f, Math.min(1.0f, (float) xpProgress / xpNeededForNext));
    }

    public void updateXpBar(Player player, PlayerData data) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        int level = data.getLevel();
        float progress = getLevelProgress(data);
        
        // Set the level (this shows the number in the XP bar)
        player.setLevel(level);
        
        // Set the progress (0.0 to 1.0, this shows the progress bar)
        player.setExp(Math.max(0.0f, Math.min(1.0f, progress)));
    }

    private void sendLevelUpMessage(Player player, PlayerData data) {
        int level = data.getLevel();
        long xpNeeded = getXpForNextLevel(data);
        int nextLevel = level + 1;
        
        // Play level up sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Build the level up message with proper colors
        Component message = Component.text("LEVEL UP! ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("You are now level ", NamedTextColor.YELLOW))
            .append(Component.text(level, NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text(". ", NamedTextColor.YELLOW))
            .append(Component.text("You need ", NamedTextColor.GRAY))
            .append(Component.text(xpNeeded, NamedTextColor.GREEN))
            .append(Component.text(" more XP to reach level ", NamedTextColor.GRAY))
            .append(Component.text(nextLevel, NamedTextColor.GOLD));
        
        player.sendMessage(message);
    }

    public void invalidateCache(UUID playerId) {
        cache.remove(playerId);
    }

    public void clearCache() {
        cache.clear();
    }
}


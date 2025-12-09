package jsemolik.dev.preppyLevels.placeholders;

import jsemolik.dev.preppyLevels.PreppyLevels;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PreppyLevelsPlaceholders extends PlaceholderExpansion {
    private final PreppyLevels plugin;

    public PreppyLevelsPlaceholders(PreppyLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "preppylevels";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        UUID playerId = player.getUniqueId();

        // Handle different placeholder types
        switch (params.toLowerCase()) {
            case "level":
                return String.valueOf(getLevelSync(playerId));
            
            case "xp":
            case "totalxp":
                return String.valueOf(getXpSync(playerId));
            
            case "xpneeded":
            case "xp_needed":
            case "xp-needed":
                return String.valueOf(getXpNeededSync(playerId));
            
            case "nextlevel":
            case "next_level":
            case "next-level":
                int level = getLevelSync(playerId);
                return String.valueOf(level + 1);
            
            case "levelprogress":
            case "level_progress":
            case "level-progress":
                return String.format("%.1f%%", getLevelProgressSync(playerId) * 100);
            
            default:
                return null;
        }
    }

    private int getLevelSync(UUID playerId) {
        try {
            return plugin.getAPI().getLevel(playerId).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return 1; // Default level
        }
    }

    private long getXpSync(UUID playerId) {
        try {
            return plugin.getAPI().getXp(playerId).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return 0L;
        }
    }

    private long getXpNeededSync(UUID playerId) {
        try {
            return plugin.getAPI().getXpNeededForNextLevel(playerId).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return 0L;
        }
    }

    private float getLevelProgressSync(UUID playerId) {
        try {
            return plugin.getLevelManager().getPlayerData(playerId)
                .thenApply(data -> {
                    if (data == null) {
                        return 0.0f;
                    }
                    return plugin.getLevelManager().getLevelProgress(data);
                })
                .get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return 0.0f;
        }
    }
}

package jsemolik.dev.preppyLevels;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AutoXpHandler implements Listener {
    private final PreppyLevels plugin;
    private final ConcurrentHashMap<UUID, Long> playTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> lastChatTime = new ConcurrentHashMap<>();
    private BukkitRunnable playTimeTask;

    public AutoXpHandler(PreppyLevels plugin) {
        this.plugin = plugin;
        startPlayTimeTracking();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        
        // Give join XP
        int joinXp = plugin.getPluginConfig().getAutoXpConfig().getXpForTask("join");
        if (joinXp > 0) {
            plugin.getLevelManager().addXp(playerId, playerName, joinXp);
        }
        
        // Start tracking playtime
        playTime.put(playerId, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        playTime.remove(playerId);
        lastChatTime.remove(playerId);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        
        int chatXp = plugin.getPluginConfig().getAutoXpConfig().getXpForTask("chat");
        if (chatXp > 0) {
            // Prevent spam - only give XP once per 10 seconds
            long lastChat = lastChatTime.getOrDefault(playerId, 0L);
            if (System.currentTimeMillis() - lastChat > 10000) {
                plugin.getLevelManager().addXp(playerId, playerName, chatXp);
                lastChatTime.put(playerId, System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        
        int commandXp = plugin.getPluginConfig().getAutoXpConfig().getXpForTask("command");
        if (commandXp > 0) {
            plugin.getLevelManager().addXp(playerId, playerName, commandXp);
        }
    }

    private void startPlayTimeTracking() {
        int timeXp = plugin.getPluginConfig().getAutoXpConfig().getXpForTask("time-played");
        
        if (timeXp > 0) {
            playTimeTask = new BukkitRunnable() {
                @Override
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                        UUID playerId = player.getUniqueId();
                        Long lastUpdate = playTime.get(playerId);
                        
                        if (lastUpdate != null) {
                            long timeSinceLastUpdate = (currentTime - lastUpdate) / 1000 / 60; // minutes
                            if (timeSinceLastUpdate >= 1) {
                                plugin.getLevelManager().addXp(playerId, player.getName(), timeXp);
                                playTime.put(playerId, currentTime);
                            }
                        }
                    }
                }
            };
            playTimeTask.runTaskTimer(plugin, 1200L, 1200L); // Every 60 seconds (1200 ticks)
        }
    }

    public void shutdown() {
        if (playTimeTask != null) {
            playTimeTask.cancel();
        }
    }
}


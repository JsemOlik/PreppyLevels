package jsemolik.dev.preppyLevels;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoXpHandler {
    private final PreppyLevels plugin;
    private final Logger logger;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<UUID, Long> playTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> lastChatTime = new ConcurrentHashMap<>();

    public AutoXpHandler(PreppyLevels plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Start playtime tracking
        scheduler.scheduleAtFixedRate(this::updatePlayTime, 60, 60, TimeUnit.SECONDS);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Give join XP
        int joinXp = plugin.getConfig().getAutoXpConfig().getXpForTask("join");
        if (joinXp > 0) {
            plugin.getLevelManager().addXp(playerId, player.getUsername(), joinXp);
        }
        
        // Start tracking playtime
        playTime.put(playerId, System.currentTimeMillis());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        playTime.remove(playerId);
        lastChatTime.remove(playerId);
    }

    private void updatePlayTime() {
        long currentTime = System.currentTimeMillis();
        int timeXp = plugin.getConfig().getAutoXpConfig().getXpForTask("time-played");
        
        if (timeXp > 0) {
            for (Player player : plugin.getServer().getAllPlayers()) {
                UUID playerId = player.getUniqueId();
                Long lastUpdate = playTime.get(playerId);
                
                if (lastUpdate != null) {
                    long timeSinceLastUpdate = (currentTime - lastUpdate) / 1000 / 60; // minutes
                    if (timeSinceLastUpdate >= 1) {
                        plugin.getLevelManager().addXp(playerId, player.getUsername(), timeXp);
                        playTime.put(playerId, currentTime);
                    }
                }
            }
        }
    }

    public void onChatMessage(UUID playerId, String playerName) {
        int chatXp = plugin.getConfig().getAutoXpConfig().getXpForTask("chat");
        if (chatXp > 0) {
            // Prevent spam - only give XP once per 10 seconds
            long lastChat = lastChatTime.getOrDefault(playerId, 0L);
            if (System.currentTimeMillis() - lastChat > 10000) {
                plugin.getLevelManager().addXp(playerId, playerName, chatXp);
                lastChatTime.put(playerId, System.currentTimeMillis());
            }
        }
    }

    public void onCommand(UUID playerId, String playerName) {
        int commandXp = plugin.getConfig().getAutoXpConfig().getXpForTask("command");
        if (commandXp > 0) {
            plugin.getLevelManager().addXp(playerId, playerName, commandXp);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}


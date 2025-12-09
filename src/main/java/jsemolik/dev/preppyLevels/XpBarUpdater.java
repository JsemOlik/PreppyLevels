package jsemolik.dev.preppyLevels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class XpBarUpdater {
    private final PreppyLevels plugin;
    private BukkitTask task;

    public XpBarUpdater(PreppyLevels plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int interval = plugin.getPluginConfig().getXpBarConfig().getUpdateInterval();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllPlayersXpBar, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    private void updateAllPlayersXpBar() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getLevelManager().getPlayerData(player.getUniqueId()).thenAccept(data -> {
                if (data != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getLevelManager().updateXpBar(player, data);
                    });
                }
            });
        }
    }
}


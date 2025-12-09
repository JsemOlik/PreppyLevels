package jsemolik.dev.preppyLevels;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LevelCommand implements CommandExecutor, TabCompleter {
    private final PreppyLevels plugin;

    public LevelCommand(PreppyLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        plugin.getLevelManager().getPlayerData(playerId).thenAccept(data -> {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                if (data == null) {
                    player.sendMessage(Component.text("You are level 1 with 0 XP.", NamedTextColor.GRAY));
                    return;
                }

                int level = data.getLevel();
                long xp = data.getXp();
                long xpNeeded = plugin.getLevelManager().getXpForNextLevel(data);
                int nextLevel = level + 1;
                float progress = plugin.getLevelManager().getLevelProgress(data);

                player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GRAY));
                player.sendMessage(Component.text("Level: ", NamedTextColor.GRAY)
                    .append(Component.text(level, NamedTextColor.GOLD)));
                player.sendMessage(Component.text("Total XP: ", NamedTextColor.GRAY)
                    .append(Component.text(xp, NamedTextColor.AQUA)));
                player.sendMessage(Component.text("XP to Level " + nextLevel + ": ", NamedTextColor.GRAY)
                    .append(Component.text(xpNeeded, NamedTextColor.GREEN)));
                player.sendMessage(Component.text("Progress: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.1f%%", progress * 100), NamedTextColor.YELLOW)));
                player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GRAY));
            });
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}


package ink.ziip.hammer.hammercore.command;

import ink.ziip.hammer.hammercore.api.object.user.HammerUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MenuCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        HammerUser hammerUser = HammerUser.getUser(sender);
        Player player = hammerUser.getPlayer();

        if (player != null) {
            String commandToRun = player.hasPermission(ink.ziip.hammer.hammercore.manager.ConfigManager.QUICK_MENU_PERMISSION)
                    ? ink.ziip.hammer.hammercore.manager.ConfigManager.QUICK_MENU_COMMAND
                    : ink.ziip.hammer.hammercore.manager.ConfigManager.QUICK_FALLBACK_MENU_COMMAND;
            if (commandToRun != null && !commandToRun.isBlank()) {
                player.performCommand(commandToRun);
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}

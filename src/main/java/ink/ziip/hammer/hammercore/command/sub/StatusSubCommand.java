package ink.ziip.hammer.hammercore.command.sub;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.command.BaseSubCommand;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class StatusSubCommand extends BaseSubCommand {

    public StatusSubCommand() {
        super("status");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String enabled = text(ConfigManager.SETTINGS_ENABLED);
        String infoBar = text(ConfigManager.INFO_BAR_ENABLED);
        String actionBar = text(ConfigManager.ACTION_BAR_ENABLED);
        HammerCore.getInstance().getMessageManager().send(sender, "command.status", Map.of(
                "version", HammerCore.getInstance().getDescription().getVersion(),
                "enabled", enabled,
                "info_bar", infoBar,
                "action_bar", actionBar));
        return true;
    }

    private String text(boolean value) {
        String key = value ? "command.status-enabled" : "command.status-disabled";
        return HammerCore.getInstance().getMessageManager().text(key);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

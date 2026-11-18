package ink.ziip.hammer.hammercore.command.sub;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.command.BaseSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ReloadSubCommand extends BaseSubCommand {

    public ReloadSubCommand() {
        super("reload");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            HammerCore.getInstance().reloadRuntimeConfiguration();
            HammerCore.getInstance().getMessageManager().send(sender, "command.reload-success", Map.of());
        } catch (RuntimeException exception) {
            HammerCore.getInstance().getMessageManager().send(sender, "command.reload-failed",
                    Map.of("error", String.valueOf(exception.getMessage())));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

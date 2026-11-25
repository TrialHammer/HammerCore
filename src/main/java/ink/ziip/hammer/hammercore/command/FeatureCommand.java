package ink.ziip.hammer.hammercore.command;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.command.sub.ReloadSubCommand;
import ink.ziip.hammer.hammercore.manager.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FeatureCommand implements TabExecutor {
    private final HammerCore core;
    private final String feature;

    public FeatureCommand(HammerCore core, String feature) {
        this.core = core;
        this.feature = feature;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        String sub = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);
        if (args.length > 1) return false;
        if (sub.equals("reload")) {
            if (!sender.hasPermission("hammercore.reload")) {
                sender.sendMessage(MessageManager.NO_PERMISSION);
                return true;
            }
            return new ReloadSubCommand().onCommand(sender, command, label, args);
        }
        if (!sub.equals("status")) return false;

        var manager = core.getFeatureManager();
        var messages = core.getMessageManager();
        messages.send(sender, feature + ".status", Map.of("state", messages.text("feature-state." + manager.state(feature))));
        var config = manager.config(feature);
        if (config == null) return true;
        if (feature.equals("cauldron-interact")) {
            messages.send(sender, feature + ".status-switches", Map.of(
                    "water", toggle(config.getBoolean("features.water-fill.enabled")),
                    "bottle", toggle(config.getBoolean("features.bottle-fill.enabled")),
                    "ingredient", toggle(config.getBoolean("features.ingredient.enabled")),
                    "upgrade", toggle(config.getBoolean("features.dispenser-upgrade.enabled")),
                    "distillation", toggle(manager.isDistillationActive())));
        } else {
            var temperature = manager.temperature();
            if (temperature == null) return true;
            messages.send(sender, feature + ".status-details", Map.of(
                    "minimum", temperature.minimum() == null ? "—" : temperature.minimum().toString(),
                    "maximum", temperature.maximum() == null ? "—" : temperature.maximum().toString(),
                    "fire", String.valueOf(temperature.target("fire")), "water", String.valueOf(temperature.target("water")),
                    "interval", String.valueOf(temperature.interval()), "count", String.valueOf(temperature.controlledCount())));
            temperature.readings().forEach(row -> messages.send(sender, feature + ".status-player", Map.of(
                    "player", row.player(), "element", row.element(), "season", row.season(),
                    "temperature", String.valueOf(row.temperature()), "modifier", String.valueOf(row.modifier()))));
        }
        return true;
    }

    private String toggle(boolean enabled) {
        return core.getMessageManager().text(enabled ? "command.status-enabled" : "command.status-disabled");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
                                      @NotNull String[] args) {
        List<String> options = sender.hasPermission("hammercore.reload") ? List.of("status", "reload") : List.of("status");
        return args.length == 1 ? options.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList() : List.of();
    }
}

package ink.ziip.hammer.hammercore.command.sub;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.command.BaseSubCommand;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import ink.ziip.hammer.hammercore.manager.MessageManager;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Port of AntiVillagerLag's {@code /avlunoptimize} as
 * {@code /hammercore restorevillagers}: restores villagers that were
 * optimised (AI disabled and renamed by that plugin) back to their original
 * state within a configurable radius.
 *
 * <p>Only villagers that still carry the AntiVillagerLag marker are touched, so
 * unrelated villagers keep their vanilla state and any player-assigned name.</p>
 */
public class RestoreVillagersCommand extends BaseSubCommand {

    /** Namespaces the marker/data may have been written under. */
    private static final String[] NAMESPACES = {"antivillagerlag", "hammercore"};
    private static final String MARKER_KEY = "marker";
    private static final String[] DATA_KEYS = {"marker", "cooldown", "levelcooldown", "time"};

    public RestoreVillagersCommand() {
        super("restorevillagers");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        MessageManager messages = HammerCore.getInstance().getMessageManager();

        if (!(sender instanceof Player player)) {
            messages.send(sender, "villager.only-player", Map.of());
            return true;
        }

        int maxRadius = Math.max(1, ConfigManager.VILLAGER_RESTORE_MAX_RADIUS);
        int radius = Math.max(1, Math.min(ConfigManager.VILLAGER_RESTORE_DEFAULT_RADIUS, maxRadius));
        if (args.length >= 1) {
            try {
                radius = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                messages.send(player, "villager.radius-invalid", Map.of());
                return true;
            }
        }

        if (radius < 1 || radius > maxRadius) {
            messages.send(player, "villager.radius-limit", Map.of("radius", String.valueOf(maxRadius)));
            return true;
        }

        messages.send(player, "villager.searching", Map.of("radius", String.valueOf(radius)));

        int restored = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Villager villager && restore(villager)) {
                restored++;
            }
        }

        messages.send(player, "villager.restored", Map.of("count", String.valueOf(restored)));
        return true;
    }

    /** Restores one villager to its original state; returns false when it was not optimised. */
    private boolean restore(Villager villager) {
        PersistentDataContainer container = villager.getPersistentDataContainer();
        if (!hasMarker(container)) {
            return false;
        }

        villager.setAware(true);
        villager.setSilent(false);
        villager.setCustomName(null);
        villager.setCustomNameVisible(false);

        for (String namespace : NAMESPACES) {
            for (String key : DATA_KEYS) {
                container.remove(new NamespacedKey(namespace, key));
            }
        }
        return true;
    }

    private boolean hasMarker(PersistentDataContainer container) {
        for (String namespace : NAMESPACES) {
            if (container.has(new NamespacedKey(namespace, MARKER_KEY), PersistentDataType.BOOLEAN)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }

        int maxRadius = Math.max(1, ConfigManager.VILLAGER_RESTORE_MAX_RADIUS);
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(String.valueOf(Math.max(1, Math.min(ConfigManager.VILLAGER_RESTORE_DEFAULT_RADIUS, maxRadius))));
        for (int preset : new int[]{10, 25, 50, 100}) {
            if (preset <= maxRadius) {
                candidates.add(String.valueOf(preset));
            }
        }

        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.startsWith(args[0])) {
                result.add(candidate);
            }
        }
        return result;
    }
}

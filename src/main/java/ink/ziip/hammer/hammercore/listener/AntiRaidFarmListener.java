package ink.ziip.hammer.hammercore.listener;

import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.raid.RaidTriggerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Port of the anti raid farm feature: each player may only start a raid once per
 * configured cooldown, which stops raid farms from being triggered repeatedly.
 */
public class AntiRaidFarmListener extends BaseListener {

    private final Map<UUID, Long> lastRaidMillis = new ConcurrentHashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onRaidTrigger(RaidTriggerEvent event) {
        long cooldownSeconds = ConfigManager.ANTI_RAID_FARM_COOLDOWN_SECONDS;
        if (cooldownSeconds <= 0L) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission(ConfigManager.ANTI_RAID_FARM_BYPASS_PERMISSION)) {
            return;
        }

        long now = System.currentTimeMillis();
        long cooldownMillis = cooldownSeconds * 1000L;
        Long last = lastRaidMillis.get(player.getUniqueId());
        if (last != null && now - last < cooldownMillis) {
            event.setCancelled(true);
            return;
        }

        lastRaidMillis.put(player.getUniqueId(), now);
    }
}

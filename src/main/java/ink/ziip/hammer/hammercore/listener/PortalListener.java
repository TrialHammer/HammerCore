package ink.ziip.hammer.hammercore.listener;

import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.util.BoundingBox;

public class PortalListener extends BaseListener {

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.getReason() == PortalCreateEvent.CreateReason.FIRE ||
                event.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR) {
            BlockState blockState = event.getBlocks().get(0);
            BoundingBox boundingBox = new BoundingBox(
                    ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_PORTAL_MIN_X,
                    ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_PORTAL_MIN_Y,
                    ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_PORTAL_MIN_Z,
                    ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_PORTAL_MAX_X,
                    ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_PORTAL_MAX_Y,
                    ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_PORTAL_MAX_Z);
            if (blockState.getWorld().getName().equals(ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_PORTAL_NETHER_WORLD) && !boundingBox.contains(blockState.getX(), blockState.getY(), blockState.getZ())) {
                event.setCancelled(true);
            }
        }
    }
}

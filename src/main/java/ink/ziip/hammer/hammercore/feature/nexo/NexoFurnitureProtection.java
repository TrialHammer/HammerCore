package ink.ziip.hammer.hammercore.feature.nexo;

import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureDamageEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

final class NexoFurnitureProtection implements Listener {
    private static final NamespacedKey OWNER = NamespacedKey.fromString("innova:furniture_owner");

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(NexoFurnitureBreakEvent event) {
        if (!ownerAllows(event.getBaseEntity(), event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamage(NexoFurnitureDamageEvent event) {
        if (!ownerAllows(event.getBaseEntity(), event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(NexoFurnitureInteractEvent event) {
        if (!ownerAllows(event.getBaseEntity(), event.getPlayer())) event.setCanRotate(Event.Result.DENY);
    }

    private static boolean ownerAllows(ItemDisplay display, Player player) {
        String owner = display.getPersistentDataContainer().get(OWNER, PersistentDataType.STRING);
        return mayChangeFurniture(owner, player == null ? null : player.getUniqueId(),
                player != null && player.hasPermission("codframe.admin"));
    }

    static boolean mayChangeFurniture(String owner, UUID player, boolean bypass) {
        return owner == null || bypass || (player != null && owner.equals(player.toString()));
    }
}

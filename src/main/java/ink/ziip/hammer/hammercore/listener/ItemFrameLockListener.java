package ink.ziip.hammer.hammercore.listener;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import ink.ziip.hammer.hammercore.api.object.lock.ItemFrameLock;
import ink.ziip.hammer.hammercore.api.object.user.HammerUser;
import ink.ziip.hammer.hammercore.api.util.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ItemFrameLockListener extends BaseListener {

    @EventHandler
    public void onPlayerInteractItemFrameLock(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame)) return;
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            ItemMeta itemMeta = itemFrame.getItem().getItemMeta();
            if (itemMeta != null && itemMeta.hasLore()) {
                for (String lore : itemMeta.getLore()) {
                    if (Utils.isTeleportationItem(lore)) return;
                }
            }
        }

        HammerUser hammerUser = HammerUser.getUser(player);
        ItemFrameLock itemFrameLock = ItemFrameLock.getItemFrameLock(itemFrame);
        if (player.isSneaking() && itemFrame.getItem().getType() != Material.AIR) {
            if (!itemFrameLock.hasOwner()) {
                itemFrameLock.setOwner(player);
                hammerUser.sendActionBar(message("item-frame.now-owned", Map.of()), false);
                event.setCancelled(true);
                return;
            }
            if (itemFrameLock.isOwner(player)) {
                itemFrameLock.removeOwner();
                hammerUser.sendActionBar(message("item-frame.unlocked", Map.of()), false);
            } else {
                hammerUser.sendActionBar(message("item-frame.locked-by-owner",
                        Map.of("owner", String.valueOf(itemFrameLock.getOwnerName()))), false);
            }
            event.setCancelled(true);
            return;
        }

        if (!itemFrameLock.isOwner(player)) {
            hammerUser.sendActionBar(message("item-frame.locked-by-owner",
                    Map.of("owner", String.valueOf(itemFrameLock.getOwnerName()))), false);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBreakItemFrame(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ItemFrame itemFrame)) return;
        ItemFrameLock lock = ItemFrameLock.getItemFrameLock(itemFrame);
        if (!lock.hasOwner()) return;
        if (event.getDamager() instanceof Player player) {
            if (lock.isOwner(player)) return;
            HammerUser.getUser(player).sendActionBar(message("item-frame.locked-by-owner",
                    Map.of("owner", String.valueOf(lock.getOwnerName()))), false);
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBreakItemFrame(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof ItemFrame itemFrame && ItemFrameLock.getItemFrameLock(itemFrame).hasOwner()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Intentionally reserved for future item-frame block ownership rules.
    }

    private String message(String key, Map<String, String> placeholders) {
        return HammerCore.getInstance().getMessageManager().format(key, placeholders);
    }
}

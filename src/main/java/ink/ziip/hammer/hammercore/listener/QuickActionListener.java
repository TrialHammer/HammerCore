package ink.ziip.hammer.hammercore.listener;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import ink.ziip.hammer.hammercore.api.object.user.HammerUser;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import ink.ziip.hammer.hammercore.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.Map;

import static ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_DISABLE_ELYTRA;

public class QuickActionListener extends BaseListener {

    @EventHandler
    public void onPlayerSwapItemToOpenMenu(PlayerSwapHandItemsEvent event) {
        if (!ConfigManager.QUICK_SWAP_HAND_MENU) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        event.setCancelled(true);
        openMenu(player);
    }

    @EventHandler
    public void onPlayerDropItemToToggleBend(PlayerDropItemEvent event) {
        if (!ConfigManager.QUICK_DROP_TOGGLE_BENDING) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        event.setCancelled(true);
        if (ConfigManager.QUICK_BEND_TOGGLE_COMMAND != null && !ConfigManager.QUICK_BEND_TOGGLE_COMMAND.isBlank()) {
            player.performCommand(ConfigManager.QUICK_BEND_TOGGLE_COMMAND);
        }
    }

    @EventHandler
    public void onPlayerInteractWithMenu(PlayerInteractEvent event) {
        if (!ConfigManager.QUICK_CLOCK_MENU || event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getItem() == null || event.getItem().getType() != ConfigManager.QUICK_CLOCK_MATERIAL) return;
        openMenu(event.getPlayer());
    }

    private void openMenu(Player player) {
        String commandToRun = player.hasPermission(ConfigManager.QUICK_MENU_PERMISSION)
                ? ConfigManager.QUICK_MENU_COMMAND
                : ConfigManager.QUICK_FALLBACK_MENU_COMMAND;
        if (commandToRun != null && !commandToRun.isBlank()) {
            player.performCommand(commandToRun);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (ConfigManager.UTIL_DISABLE_JOIN_MESSAGE) event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (ConfigManager.UTIL_DISABLE_QUIT_MESSAGE) event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!ConfigManager.UTIL_AUTO_RESPAWN_ENABLED) return;
        Player player = event.getEntity();
        new BukkitRunnable() {
            public void run() {
                player.spigot().respawn();
                if (ConfigManager.UTIL_AUTO_RESPAWN_LOCATION.getWorld() != null) {
                    player.teleport(ConfigManager.UTIL_AUTO_RESPAWN_LOCATION);
                }
            }
        }.runTaskLater(HammerCore.getInstance(), ConfigManager.UTIL_AUTO_RESPAWN_DELAY_TICKS);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractItemFrameToTeleport(PlayerInteractEntityEvent event) {
        if (!ConfigManager.SOUVENIR_ENABLED) return;
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame) || !event.getPlayer().isSneaking()) return;
        ItemMeta itemMeta = itemFrame.getItem().getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore()) return;

        for (String lore : itemMeta.getLore()) {
            if (!java.util.regex.Pattern.matches(ConfigManager.SOUVENIR_TELEPORT_PATTERN, ChatColor.stripColor(lore))) continue;
            String warp = ChatColor.stripColor(lore).replaceAll("\\[[0-9]{3}-", "").replaceAll("]", "");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), ConfigManager.SOUVENIR_WARP_COMMAND
                    .replace("%warp%", warp)
                    .replace("%player%", event.getPlayer().getName()));
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerBreakBlockToVoid(BlockBreakEvent event) {
        if (!UTIL_DISABLE_ELYTRA) return;
        Player player = event.getPlayer();
        Location location = player.getLocation().add(0, -3, 0);
        if (location.getWorld() == null) return;
        if (player.getLocation().getY() >= event.getBlock().getLocation().getY()
                && location.getWorld().getBlockAt(location).getType() == Material.AIR) {
            HammerCore.getInstance().getMessageManager().send(player, "quick-action.void-warning", Map.of());
        }
    }

    @EventHandler
    public void onPlayerDropItemsInSpawn(PlayerDropItemEvent event) {
        if (!ConfigManager.SPAWN_LITTER_ENABLED) return;
        Player player = event.getPlayer();
        Location location = player.getLocation();
        if (location.getWorld() == null || !location.getWorld().getName().equals(ConfigManager.SPAWN_LITTER_WORLD)) return;

        BoundingBox boundingBox = new BoundingBox(
                ConfigManager.SPAWN_LITTER_MIN_X, ConfigManager.SPAWN_LITTER_MIN_Y, ConfigManager.SPAWN_LITTER_MIN_Z,
                ConfigManager.SPAWN_LITTER_MAX_X, ConfigManager.SPAWN_LITTER_MAX_Y, ConfigManager.SPAWN_LITTER_MAX_Z);
        if (!boundingBox.contains(location.getX(), location.getY(), location.getZ())) return;

        HammerUser hammerUser = HammerUser.getUser(player);
        if (System.currentTimeMillis() - hammerUser.getDropItemsTime() <= ConfigManager.SPAWN_LITTER_COOLDOWN_MILLIS) return;
        hammerUser.setDropItemsTime(System.currentTimeMillis());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            HammerCore.getInstance().getMessageManager().send(onlinePlayer,
                    "quick-action.litter-warning",
                    Map.of("player", player.getName(), "seconds", String.valueOf(ConfigManager.SPAWN_LITTER_CLEANUP_SECONDS)));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getItemDrop().isValid()) event.getItemDrop().remove();
            }
        }.runTaskLater(HammerCore.getInstance(), ConfigManager.SPAWN_LITTER_CLEANUP_DELAY_TICKS);
    }
}

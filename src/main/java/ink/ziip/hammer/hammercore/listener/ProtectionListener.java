package ink.ziip.hammer.hammercore.listener;

import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import ink.ziip.hammer.hammercore.api.util.Utils;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import ink.ziip.hammer.hammercore.manager.MessageManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.regex.Pattern;

import static ink.ziip.hammer.hammercore.manager.ConfigManager.UTIL_DISABLE_ELYTRA;

public class ProtectionListener extends BaseListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVillagerCareerChange(VillagerCareerChangeEvent event) {
        if (!ConfigManager.WORLD_PROTECTION_NO_CARTOGRAPHER)
            return;
        if (event.getProfession().equals(Villager.Profession.CARTOGRAPHER)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (!ConfigManager.WORLD_PROTECTION_ANTI_EXPLOSION_ENABLED) return;
        World world = event.getLocation().getWorld();
        if (world == null || !ConfigManager.WORLD_PROTECTION_ANTI_EXPLOSION_WORLD_LIST.contains(world.getName())) return;
        if (!ConfigManager.WORLD_PROTECTION_ANTI_EXPLOSION_ONLY_CREEPERS || event.getEntity() instanceof Creeper) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!ConfigManager.WORLD_PROTECTION_ANTI_GRIEFING_ENABLED) return;
        World world = event.getBlock().getWorld();
        if (!(event.getEntity() instanceof Enderman))
            return;
        if (ConfigManager.WORLD_PROTECTION_ANTI_GRIEFING_WORLD_LIST.contains(world.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityBreakFarmLand(EntityInteractEvent event) {
        if (!ConfigManager.WORLD_PROTECTION_PREVENT_FARM_LAND_DESTROY)
            return;
        if ((event.getEntity() instanceof Player))
            return;

        Block block = event.getBlock();
        if (block.getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractWithFarm(PlayerInteractEvent event) {
        if (!ConfigManager.WORLD_PROTECTION_PREVENT_FARM_LAND_DESTROY)
            return;
        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block == null)
                return;
            if (block.getType() == Material.FARMLAND) {
                if (event.getPlayer().hasPermission(ConfigManager.WORLD_PROTECTION_PROTECT_FARM_PERMISSION)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerSendInvalidChars(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String pattern = ConfigManager.WORLD_PROTECTION_INVALID_CHAT_PATTERN;

        if (Pattern.matches(pattern, event.getMessage())) {
            event.setCancelled(true);
            player.sendMessage(MessageManager.INVALID_CHARACTERS);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.hasPermission(ConfigManager.WORLD_PROTECTION_KEEP_INVENTORY_PERMISSION) || player.getLocation().getY() < ConfigManager.WORLD_PROTECTION_KEEP_INVENTORY_MIN_Y
                || (player.getLocation().getWorld() != null
                && ConfigManager.WORLD_PROTECTION_KEEP_INVENTORY_NETHER_WORLDS.contains(player.getLocation().getWorld().getName())
                && player.getLocation().getY() >= ConfigManager.WORLD_PROTECTION_KEEP_INVENTORY_NETHER_MAX_Y)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            event.setKeepInventory(true);
            event.setKeepLevel(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUsingSouvenir(PlayerInteractEvent event) {
        if (!ConfigManager.SOUVENIR_ENABLED) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack itemStack = event.getItem();
        if (itemStack == null)
            return;

        if (itemStack.getType() != ConfigManager.SOUVENIR_MAP_MATERIAL
                && (!ConfigManager.SOUVENIR_ENDER_EYE_ENABLED || itemStack.getType() != Material.ENDER_EYE))
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;

        if (!itemMeta.hasLore())
            return;

        itemMeta.getLore().forEach(content -> {
            boolean isSouvenir = Pattern.matches(ConfigManager.SOUVENIR_GENERAL_PATTERN, ChatColor.stripColor(content));
            if (isSouvenir) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUsingCartographyToCopyMap(PrepareItemCraftEvent event) {
        if (!ConfigManager.SOUVENIR_ENABLED) return;
        if (event.getInventory().getResult() != null && event.getInventory().getResult().getType() == ConfigManager.SOUVENIR_MAP_MATERIAL) {
            ItemMeta itemMeta = event.getInventory().getResult().getItemMeta();
            if (itemMeta != null) {
                if (itemMeta.hasLore()) {
                    for (String lore : itemMeta.getLore()) {
                        String regex = ConfigManager.SOUVENIR_WORKBENCH_PATTERN;
                        if (Pattern.matches(regex, ChatColor.stripColor(lore))) {
                            event.getInventory().setResult(new ItemStack(Material.AIR));
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUsingWorkbenchToCopyMap(InventoryClickEvent event) {
        if (!ConfigManager.SOUVENIR_ENABLED) return;
        ItemStack itemStack = event.getCurrentItem();
        if (event.getClickedInventory() != null) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT && itemStack != null && event.getClickedInventory().getType() == InventoryType.WORKBENCH) {
                if (itemStack.getType() == ConfigManager.SOUVENIR_MAP_MATERIAL) {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null) {
                        if (itemMeta.hasLore()) {
                            for (String lore : itemMeta.getLore()) {
                                String regex = ConfigManager.SOUVENIR_WORKBENCH_PATTERN;
                                if (Pattern.matches(regex, ChatColor.stripColor(lore))) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUsingWorkbenchForCustomItems(InventoryClickEvent event) {
        if (!ConfigManager.SOUVENIR_ENABLED) return;
        if (event.getClickedInventory() != null) {
            if (event.getClickedInventory().getType() == InventoryType.WORKBENCH) {
                for (ItemStack itemStack : event.getClickedInventory().getContents()) {
                    if (itemStack == null) continue;
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null) {
                        if (itemMeta.hasLore()) {
                            for (String lore : itemMeta.getLore()) {
                                String regex = ConfigManager.SOUVENIR_WORKBENCH_PATTERN;
                                if (Pattern.matches(regex, ChatColor.stripColor(lore))) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUsingWorkbenchForArchitectureItem(InventoryClickEvent event) {
        if (!ConfigManager.SOUVENIR_ENABLED) return;
        ItemStack itemStack = event.getCurrentItem();
        if (event.getClickedInventory() != null) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT && itemStack != null && event.getInventory().getType() != InventoryType.MERCHANT) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.hasLore()) {
                        for (String lore : itemMeta.getLore()) {
                            String regex = ConfigManager.SOUVENIR_ARCHITECTURE_PATTERN;
                            if (Pattern.matches(regex, ChatColor.stripColor(lore))) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMovingArchitectureItemIntoAnother(InventoryClickEvent event) {
        if (!ConfigManager.SOUVENIR_ENABLED) return;
        Inventory top = event.getView().getTopInventory();
        Inventory bottom = event.getView().getBottomInventory();

        if (top.getType() != InventoryType.MERCHANT && bottom.getType() == InventoryType.PLAYER && !event.getWhoClicked().isOp()) {
            if (top.getType() == InventoryType.CRAFTING) {
                if (top.getSize() == 5) {
                    return;
                }
            }
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && !Objects.equals(event.getClickedInventory(), top)) {
                ItemStack itemStack = event.getCurrentItem();
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.hasLore()) {
                        for (String lore : itemMeta.getLore()) {
                            String regex = ConfigManager.SOUVENIR_ARCHITECTURE_PATTERN;
                            if (Pattern.matches(regex, ChatColor.stripColor(lore))) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnElytraFly(EntityToggleGlideEvent event) {
        if (UTIL_DISABLE_ELYTRA && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }
}

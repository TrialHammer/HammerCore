package ink.ziip.hammer.hammercore.feature.cauldron;

import com.dre.brewery.BCauldron;
import com.dre.brewery.api.BreweryApi;
import com.dre.brewery.recipe.BCauldronRecipe;
import com.dre.brewery.recipe.RecipeItem;
import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class CauldronAutomation extends BaseListener {
    private static final NamespacedKey LEGACY_UPGRADE = NamespacedKey.fromString("cauldroninteract:dispenser-upgraded");

    private enum Use { WATER, BOTTLE, INGREDIENT }

    private final HammerCore core;
    private final Plugin brewery;
    private final ConfigurationSection config;
    private final NamespacedKey upgradeKey;
    private final Map<Integer, BukkitTask> deferred = new HashMap<>();
    private final Set<Block> busy = new HashSet<>();
    private final Set<Block> stands = new HashSet<>();
    private final long delay;
    private final boolean dispenserEnabled;
    private BreweryDistillation distillation;
    private BukkitTask standWatcher;

    public CauldronAutomation(HammerCore core, Plugin brewery, ConfigurationSection config) {
        this.core = core;
        this.brewery = brewery;
        this.config = config;
        this.upgradeKey = new NamespacedKey(core, "dispenser-upgraded");
        this.delay = Math.clamp(config.getLong("behaviour.deferred-inventory-delay-ticks", 1), 1L, 200L);
        this.dispenserEnabled = config.getBoolean("features.water-fill.enabled")
                || config.getBoolean("features.bottle-fill.enabled") || config.getBoolean("features.ingredient.enabled");
    }

    public void start() {
        if (config.getBoolean("features.distillation.enabled")) {
            String materialName = config.getString("features.distillation.ingredient-material", "GLOWSTONE_DUST");
            Material material = Material.matchMaterial(materialName);
            try {
                distillation = new BreweryDistillation(material == null ? Material.GLOWSTONE_DUST : material);
                for (var world : core.getServer().getWorlds()) {
                    for (var chunk : world.getLoadedChunks()) scan(chunk);
                }
                standWatcher = core.getServer().getScheduler().runTaskTimer(core, this::checkStands,
                        Math.max(0, config.getLong("features.distillation.initial-delay-ticks", 10)),
                        Math.max(1, config.getLong("features.distillation.check-interval-ticks", 10)));
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                stopDistillation(e);
            }
        }
        register();
    }

    public void stop() {
        unRegister();
        if (standWatcher != null) standWatcher.cancel();
        standWatcher = null;
        deferred.values().forEach(BukkitTask::cancel);
        deferred.clear();
        busy.clear();
        stands.clear();
    }

    public boolean isDistillationActive() {
        return distillation != null && brewery.isEnabled();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void dispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        if (!dispenserEnabled || !brewery.isEnabled() || block.getType() != Material.DISPENSER
                || !(block.getState() instanceof Dispenser dispenser)) return;
        if (config.getBoolean("features.dispenser-upgrade.enabled") && !isUpgraded(dispenser)) return;
        if (busy.contains(block)) {
            event.setCancelled(true);
            return;
        }

        Block destination = inFront(block);
        if (destination.getType() != Material.CAULDRON && destination.getType() != Material.WATER_CAULDRON) return;
        ItemStack item = event.getItem();
        Use use = choose(destination, item);
        if (use == null) return;

        // The vanilla dispenser restores its selected item on cancellation. Read
        // the live inventory after that, and change the cauldron only if we can pay.
        ItemStack input = item.clone();
        input.setAmount(1);
        event.setCancelled(true);
        busy.add(block);
        later(() -> {
            try {
                if (event.isCancelled()) perform(block, destination, input, use);
            } finally {
                busy.remove(block);
            }
        });
    }

    private Use choose(Block cauldron, ItemStack item) {
        if (item.getType() == Material.WATER_BUCKET && config.getBoolean("features.water-fill.enabled")) return Use.WATER;
        if (cauldron.getType() != Material.WATER_CAULDRON) return null;
        if (item.getType() == Material.GLASS_BOTTLE && config.getBoolean("features.bottle-fill.enabled")
                && BreweryApi.getCauldron(cauldron) != null) return Use.BOTTLE;
        if (config.getBoolean("features.ingredient.enabled")
                && (BCauldronRecipe.getAcceptedMaterials().contains(item.getType()) || item.hasItemMeta())
                && RecipeItem.getMatchingRecipeItem(item, false) != null) return Use.INGREDIENT;
        return null;
    }

    private void perform(Block source, Block target, ItemStack input, Use use) {
        if (!brewery.isEnabled() || !loaded(source) || !loaded(target)
                || !(source.getState() instanceof Dispenser dispenser) || !target.equals(inFront(source))) return;
        Inventory inventory = dispenser.getInventory();
        int slot = matchingSlot(inventory, input);
        if (slot < 0) return;
        ItemStack stack = inventory.getItem(slot);
        if (stack.getAmount() == 1) inventory.clear(slot);
        else {
            stack.setAmount(stack.getAmount() - 1);
            inventory.setItem(slot, stack);
        }

        ItemStack output = null;
        boolean returnContainer = config.getBoolean("behaviour.return-empty-containers");
        boolean committed = false;
        try {
            switch (use) {
                case WATER -> {
                    if (!refill(target)) return;
                    if (returnContainer) output = new ItemStack(Material.BUCKET);
                }
                case BOTTLE -> {
                    if (target.getType() != Material.WATER_CAULDRON || !(target.getBlockData() instanceof Levelled water)) return;
                    BCauldron recipe = BreweryApi.getCauldron(target);
                    if (recipe == null) return;
                    output = recipe.getIngredients().cook(recipe.getState(), null);
                    if (output == null || (config.getBoolean("behaviour.consume-one-water-level-per-bottle")
                            && !drain(target, water))) return;
                }
                case INGREDIENT -> {
                    if (target.getType() != Material.WATER_CAULDRON || !BCauldron.ingredientAdd(target, input, null)) return;
                    if (returnContainer) output = emptyContainer(input.getType());
                }
            }
            committed = true;
        } finally {
            if (!committed) {
                if (inventory.getItem(slot) == null) inventory.setItem(slot, input);
                else inventory.addItem(input).values().forEach(leftover -> drop(source, leftover));
            }
        }
        if (output != null) {
            if (use == Use.WATER) drop(source, output);
            else inventory.addItem(output).values().forEach(leftover -> drop(source, leftover));
        }
        if (use != Use.INGREDIENT && config.getBoolean("effects.sounds.enabled")) {
            var sound = use == Use.WATER ? "bucket-empty" : "bottle-fill";
            target.getWorld().playSound(target.getLocation(), soundKey(sound),
                    soundValue(sound, "volume"), soundValue(sound, "pitch"));
        }
    }

    private boolean refill(Block target) {
        if (target.getType() != Material.CAULDRON && target.getType() != Material.WATER_CAULDRON) return false;
        int old = target.getBlockData() instanceof Levelled data ? data.getLevel() : 0;
        var state = target.getState();
        state.setType(Material.WATER_CAULDRON);
        Levelled water = (Levelled) state.getBlockData();
        int max = water.getMaximumLevel();
        if (old >= max) return false;
        water.setLevel(config.getBoolean("behaviour.fill-cauldron-to-maximum") ? max : old + 1);
        state.setBlockData(water);
        var change = new CauldronLevelChangeEvent(target, null, CauldronLevelChangeEvent.ChangeReason.BUCKET_EMPTY, state);
        core.getServer().getPluginManager().callEvent(change);
        return !change.isCancelled() && state.update(true);
    }

    private boolean drain(Block target, Levelled water) {
        var state = target.getState();
        if (water.getLevel() <= 1) state.setType(Material.CAULDRON);
        else {
            water.setLevel(water.getLevel() - 1);
            state.setBlockData(water);
        }
        var change = new CauldronLevelChangeEvent(target, null, CauldronLevelChangeEvent.ChangeReason.BOTTLE_FILL, state);
        core.getServer().getPluginManager().callEvent(change);
        if (change.isCancelled() || !state.update(true)) return false;
        if (target.getType() == Material.CAULDRON) BreweryApi.removeCauldron(target);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectPendingDispense(InventoryMoveItemEvent event) {
        if (!busy.isEmpty() && event.getSource().getHolder() instanceof Dispenser dispenser
                && busy.contains(dispenser.getBlock())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void hopperInput(InventoryMoveItemEvent event) {
        if (!isDistillationActive()) return;
        Inventory inventory = event.getDestination();
        if (inventory.getType() != InventoryType.BREWING) return;
        Block block = inventory.getHolder() instanceof BrewingStand stand ? stand.getBlock()
                : inventory.getLocation() != null ? inventory.getLocation().getBlock() : null;
        if (block != null) later(() -> {
            if (!event.isCancelled()) checkStand(block);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void chunkLoaded(ChunkLoadEvent event) {
        if (isDistillationActive() && config.getBoolean("features.distillation.track-chunk-loads")) scan(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void blockPlaced(BlockPlaceEvent event) {
        if (isDistillationActive() && config.getBoolean("features.distillation.track-block-place")
                && event.getBlockPlaced().getType() == Material.BREWING_STAND) stands.add(event.getBlockPlaced());
    }

    private void scan(Chunk chunk) {
        for (var tile : chunk.getTileEntities()) {
            if (tile instanceof BrewingStand) stands.add(tile.getBlock());
        }
    }

    private void checkStands() {
        if (!isDistillationActive()) return;
        stands.removeIf(block -> !loaded(block) || block.getType() != Material.BREWING_STAND);
        stands.forEach(this::checkStand);
        if (distillation == null) stands.clear();
    }

    private void checkStand(Block block) {
        if (!isDistillationActive() || !loaded(block) || block.getType() != Material.BREWING_STAND) return;
        try {
            distillation.check((BrewingStand) block.getState());
        } catch (RuntimeException | LinkageError e) {
            stopDistillation(e);
        }
    }

    private void stopDistillation(Throwable e) {
        distillation = null;
        if (standWatcher != null) standWatcher.cancel();
        standWatcher = null;
        core.getLogger().log(Level.WARNING, "Could not start BreweryX distillation automatically", e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void upgrade(PlayerInteractEvent event) {
        if (!brewery.isEnabled() || !config.getBoolean("features.dispenser-upgrade.enabled")
                || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND
                || !event.getPlayer().isSneaking() || event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.DISPENSER) return;
        ItemStack hoe = event.getItem();
        if (hoe == null || !Tag.ITEMS_HOES.isTagged(hoe.getType())) return;
        Dispenser dispenser = (Dispenser) event.getClickedBlock().getState();
        if (isUpgraded(dispenser)) return;
        dispenser.getPersistentDataContainer().set(upgradeKey, PersistentDataType.BYTE, (byte) 1);
        if (!dispenser.update()) return;
        event.setCancelled(true);
        var player = event.getPlayer();
        if (config.getBoolean("features.dispenser-upgrade.send-message")) {
            core.getMessageManager().send(player, "cauldron-interact.upgraded", Map.of());
        }
        if (config.getBoolean("effects.sounds.enabled")) {
            player.playSound(dispenser.getLocation(), soundKey("upgrade"),
                    soundValue("upgrade", "volume"), soundValue("upgrade", "pitch"));
        }
        if (config.getBoolean("effects.upgrade-particles.enabled")) {
            String path = "effects.upgrade-particles";
            try {
                var particle = Particle.valueOf(config.getString(path + ".particle", "WAX_ON"));
                int count = config.getInt(path + ".count");
                if (count > 0) player.spawnParticle(particle, dispenser.getLocation().add(
                        config.getDouble(path + ".offset.x"), config.getDouble(path + ".offset.y"),
                        config.getDouble(path + ".offset.z")), count);
            } catch (IllegalArgumentException e) {
                core.getLogger().warning("Invalid cauldron upgrade particle: " + e.getMessage());
            }
        }
        if (config.getBoolean("features.dispenser-upgrade.consume-durability") && player.getGameMode() != GameMode.CREATIVE
                && hoe.getItemMeta() instanceof Damageable damage) {
            int next = damage.getDamage() + 1;
            if (next >= (damage.hasMaxDamage() ? damage.getMaxDamage() : hoe.getType().getMaxDurability())) {
                player.getInventory().setItemInMainHand(null);
            } else {
                damage.setDamage(next);
                hoe.setItemMeta(damage);
                player.getInventory().setItemInMainHand(hoe);
            }
        }
    }

    private boolean isUpgraded(Dispenser dispenser) {
        var data = dispenser.getPersistentDataContainer();
        return data.has(upgradeKey) || data.has(LEGACY_UPGRADE);
    }

    private ItemStack emptyContainer(Material input) {
        if (config.getStringList("behaviour.bucket-suffixes").stream().anyMatch(input.name()::endsWith)) {
            return new ItemStack(Material.BUCKET);
        }
        return config.getStringList("behaviour.bottle-materials").stream().anyMatch(input.name()::equalsIgnoreCase)
                ? new ItemStack(Material.GLASS_BOTTLE) : null;
    }

    private void drop(Block source, ItemStack stack) {
        String offset = "behaviour.drop-offset.";
        source.getWorld().dropItem(source.getLocation().add(config.getDouble(offset + "x"),
                config.getDouble(offset + "y"), config.getDouble(offset + "z")), stack);
    }

    private String soundKey(String name) {
        String key = config.getString("effects.sounds." + name + ".key");
        if (key != null && key.matches("[A-Z_]+")) {
            try {
                return Sound.valueOf(key).getKeyOrThrow().toString();
            } catch (IllegalArgumentException ignored) {
                // A resource-pack sound need not be in the registry.
            }
        }
        return key;
    }

    private float soundValue(String name, String field) {
        return (float) config.getDouble("effects.sounds." + name + "." + field, 1.0);
    }

    private void later(Runnable action) {
        BukkitTask scheduled = new BukkitRunnable() {
            @Override
            public void run() {
                deferred.remove(getTaskId());
                action.run();
            }
        }.runTaskLater(core, delay);
        deferred.put(scheduled.getTaskId(), scheduled);
    }

    private static Block inFront(Block source) {
        return source.getRelative(((Directional) source.getBlockData()).getFacing());
    }

    private static boolean loaded(Block block) {
        return block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4);
    }

    private static int matchingSlot(Inventory inventory, ItemStack wanted) {
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack actual = inventory.getItem(i);
            if (actual != null && actual.getAmount() > 0 && actual.isSimilar(wanted)) return i;
        }
        return -1;
    }
}

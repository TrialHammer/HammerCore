package ink.ziip.hammer.hammercore.feature.cauldron;

import ink.ziip.hammer.hammercore.HammerCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CauldronAutomationTest {
    private MockedStatic<Bukkit> bukkit;
    private CauldronAutomation automation;
    private HammerCore core;
    private BukkitScheduler scheduler;
    private BukkitTask task;
    private PluginManager events;
    private Block source;
    private Block target;
    private BlockState nextState;
    private Levelled nextLevel;
    private Inventory stock;
    private World world;
    private AtomicReference<ItemStack> slot;
    private Runnable pending;

    @BeforeEach
    void setup() {
        core = mock(HammerCore.class);
        when(core.getName()).thenReturn("HammerCore");
        var server = mock(Server.class);
        when(core.getServer()).thenReturn(server);
        scheduler = mock(BukkitScheduler.class);
        task = mock(BukkitTask.class);
        events = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(events);
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(call -> mock(Registry.class));
        doAnswer(call -> {
            pending = call.getArgument(1);
            return task;
        }).when(scheduler).runTaskLater(eq(core), any(Runnable.class), eq(1L));

        var config = YamlConfiguration.loadConfiguration(new InputStreamReader(
                getClass().getResourceAsStream("/config.yml"), StandardCharsets.UTF_8))
                .getConfigurationSection("cauldron-interact");
        config.set("effects.sounds.enabled", false);

        var brewery = mock(Plugin.class);
        when(brewery.isEnabled()).thenReturn(true);
        world = mock(World.class);
        when(world.isChunkLoaded(0, 0)).thenReturn(true);
        source = mock(Block.class);
        target = mock(Block.class);
        when(source.getWorld()).thenReturn(world);
        when(target.getWorld()).thenReturn(world);
        when(source.getLocation()).thenReturn(new Location(world, 0, 64, 0));
        when(source.getType()).thenReturn(Material.DISPENSER);
        when(target.getType()).thenReturn(Material.CAULDRON);
        var direction = mock(Directional.class);
        when(direction.getFacing()).thenReturn(BlockFace.EAST);
        when(source.getBlockData()).thenReturn(direction);
        when(source.getRelative(BlockFace.EAST)).thenReturn(target);
        var dispenser = mock(Dispenser.class);
        when(source.getState()).thenReturn(dispenser);
        stock = mock(Inventory.class);
        when(dispenser.getInventory()).thenReturn(stock);

        var water = mock(ItemStack.class);
        when(water.getType()).thenReturn(Material.WATER_BUCKET);
        when(water.getAmount()).thenReturn(1);
        when(water.isSimilar(any(ItemStack.class))).thenReturn(true);
        slot = new AtomicReference<>(water);
        when(stock.getSize()).thenReturn(1);
        when(stock.getItem(0)).thenAnswer(call -> slot.get());
        doAnswer(call -> { slot.set(null); return null; }).when(stock).clear(0);
        doAnswer(call -> { slot.set(call.getArgument(1)); return null; }).when(stock).setItem(eq(0), any(ItemStack.class));

        nextState = mock(BlockState.class);
        when(target.getState()).thenReturn(nextState);
        nextLevel = mock(Levelled.class);
        when(nextLevel.getMaximumLevel()).thenReturn(3);
        when(nextState.getBlockData()).thenReturn(nextLevel);
        when(nextState.update(true)).thenReturn(true);
        automation = new CauldronAutomation(core, brewery, config);
    }

    @AfterEach
    void tearDown() {
        if (bukkit != null) bukkit.close();
    }

    @Test
    void fillsTheCauldronAfterVanillaRestoresTheCancelledDispense() {
        var event = waterBucket();
        automation.dispense(event);
        assertTrue(event.isCancelled());
        verifyNoInteractions(nextState);

        pending.run();
        assertNull(slot.get());
        verify(nextState).setType(Material.WATER_CAULDRON);
        verify(nextLevel).setLevel(3);
        var order = inOrder(events, nextState);
        var levelEvent = ArgumentCaptor.forClass(CauldronLevelChangeEvent.class);
        order.verify(events).callEvent(levelEvent.capture());
        assertEquals(CauldronLevelChangeEvent.ChangeReason.BUCKET_EMPTY, levelEvent.getValue().getReason());
        order.verify(nextState).update(true);
        var output = ArgumentCaptor.forClass(ItemStack.class);
        verify(world).dropItem(eq(new Location(world, 0.5, 63.25, 0.5)), output.capture());
        assertEquals(Material.BUCKET, output.getValue().getType());
    }

    @Test
    void aCancelledWaterChangeRestoresTheBucketWithoutProducingOutput() {
        doAnswer(call -> {
            call.<CauldronLevelChangeEvent>getArgument(0).setCancelled(true);
            return null;
        }).when(events).callEvent(any(CauldronLevelChangeEvent.class));
        automation.dispense(waterBucket());
        pending.run();

        assertEquals(Material.WATER_BUCKET, slot.get().getType());
        verify(nextState, never()).update(true);
        verify(world, never()).dropItem(any(), any());
    }

    @Test
    void aMissingInputCannotFillACauldron() {
        automation.dispense(waterBucket());
        slot.set(null);
        pending.run();
        verifyNoInteractions(nextState);
    }

    @Test
    void unloadingCancelsAQueuedDispenserBeforeItChangesTheWorld() {
        automation.dispense(waterBucket());
        automation.stop();

        verify(task).cancel();
        verifyNoInteractions(nextState);
        assertNotNull(slot.get());
    }

    private BlockDispenseEvent waterBucket() {
        return new BlockDispenseEvent(source, new ItemStack(Material.WATER_BUCKET), new Vector());
    }
}

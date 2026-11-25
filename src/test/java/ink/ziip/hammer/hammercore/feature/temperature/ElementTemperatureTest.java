package ink.ziip.hammer.hammercore.feature.temperature;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import ink.ziip.hammer.hammercore.HammerCore;
import me.casperge.realisticseasons.api.SeasonsAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ElementTemperatureTest {
    public abstract static class RealisticSeasons implements Plugin {
        public abstract Manager getTemperatureManager();
    }

    public static class Manager {
        final TemperatureData data = new TemperatureData();

        public TemperatureData getTempData() {
            return data;
        }
    }

    public static class TemperatureData {
        boolean enabled = true;

        public boolean isEnabledWorld(World world) {
            return enabled;
        }

        public TemperatureSettings getTempSettings() {
            return new TemperatureSettings();
        }
    }

    public static class TemperatureSettings {
        public int getColdHungerTemp() { return -14; }
        public int getWarmSlownessTemp() { return 66; }
        public int getWarmNoHealingTemp() { return 60; }
    }

    @Test
    void waitsForRealisticSeasonsAndCancelsEffectsWhenAPlayerOrTheFeatureIsExempt() {
        var seasons = mock(RealisticSeasons.class);
        var temperatureManager = new Manager();
        when(seasons.isEnabled()).thenReturn(true);
        when(seasons.getTemperatureManager()).thenReturn(null, temperatureManager);
        var korra = mock(Plugin.class);
        when(korra.isEnabled()).thenReturn(true);
        var core = mock(HammerCore.class);
        var server = mock(Server.class);
        when(core.getServer()).thenReturn(server);
        when(core.getLogger()).thenReturn(Logger.getAnonymousLogger());
        var scheduler = mock(BukkitScheduler.class);
        when(server.getScheduler()).thenReturn(scheduler);
        var arguments = ArgumentCaptor.forClass(Runnable.class);
        var task = mock(BukkitTask.class);
        when(scheduler.runTaskTimer(eq(core), arguments.capture(), eq(20L), eq(20L))).thenReturn(task);
        var api = SeasonsAPI.getInstance();
        var plugins = mock(PluginManager.class);

        try (var bukkit = mockStatic(Bukkit.class); var bending = mockStatic(BendingPlayer.class);
             var hammer = mockStatic(HammerCore.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(plugins);
            hammer.when(HammerCore::getInstance).thenReturn(core);
            var player = mock(Player.class);
            when(player.getUniqueId()).thenReturn(UUID.randomUUID());
            when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
            when(server.getOnlinePlayers()).thenAnswer(call -> List.of(player));
            var bender = mock(BendingPlayer.class);
            bending.when(() -> BendingPlayer.getBendingPlayer(player)).thenReturn(bender);
            when(bender.hasElement(Element.FIRE)).thenReturn(true);
            when(bender.hasElement(Element.WATER)).thenReturn(true);

            var config = new YamlConfiguration();
            config.set("priority", List.of("water", "fire"));
            var feature = new ElementTemperature(core, seasons, korra, config);
            feature.start();
            var tick = arguments.getValue();
            tick.run();
            assertTrue(feature.isWaiting());
            assertEquals(0, feature.controlledCount());

            tick.run();
            assertTrue(feature.isReady());
            assertEquals(54, feature.maximum());
            assertEquals(4, api.applied);
            assertEquals(1, feature.controlledCount());
            var first = api.lastEffect;
            tick.run();
            assertSame(first, api.lastEffect);

            when(player.hasPermission("elementtemperature.exempt")).thenReturn(true);
            tick.run();
            assertTrue(first.cancelled);
            assertEquals(0, feature.controlledCount());

            when(player.hasPermission("elementtemperature.exempt")).thenReturn(false);
            tick.run();
            var second = api.lastEffect;
            feature.stop();
            assertNotSame(first, second);
            assertTrue(second.cancelled);
            assertEquals(0, feature.controlledCount());
            verify(task).cancel();
        }
    }
}

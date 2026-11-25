package ink.ziip.hammer.hammercore.listener.packetlistener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.utility.MinecraftReflection;
import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.packetlistener.BasePacketListener;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.mockito.Mockito.*;

class PacketPlayerTest {
    private static final String BYPASS_PERMISSION = "hammercore.utils.hidenbt";

    private final Boolean previousHideLore = ConfigManager.UTIL_ARCHITECTURE_LORE_HIDDEN;
    private final Boolean previousHideParticles = ConfigManager.FLOODGATE_HIDE_PARTICLES;
    private final String previousPermission = ConfigManager.UTIL_ARCHITECTURE_HIDE_PERMISSION;

    private PacketListener inventory;
    private PacketListener actionBar;
    private PacketListener particles;

    @BeforeEach
    void registerListeners() {
        ConfigManager.UTIL_ARCHITECTURE_LORE_HIDDEN = true;
        ConfigManager.FLOODGATE_HIDE_PARTICLES = true;
        ConfigManager.UTIL_ARCHITECTURE_HIDE_PERMISSION = BYPASS_PERMISSION;

        var core = mock(HammerCore.class);
        var manager = mock(ProtocolManager.class);
        try (var bukkit = mockStatic(Bukkit.class); var plugin = mockStatic(HammerCore.class);
             var protocol = mockStatic(ProtocolLibrary.class); var nms = mockStatic(MinecraftReflection.class)) {
            bukkit.when(Bukkit::getVersion).thenReturn("Test (MC: 1.21.11)");
            nms.when(MinecraftReflection::signUpdateExists).thenReturn(false);
            plugin.when(HammerCore::getInstance).thenReturn(core);
            protocol.when(ProtocolLibrary::getProtocolManager).thenReturn(manager);
            List.<BasePacketListener>of(new ArchitectureItemListener(), new ActionBarListener(), new ParticleListener())
                    .forEach(BasePacketListener::register);
        }
        var adapters = ArgumentCaptor.forClass(PacketListener.class);
        verify(manager, times(3)).addPacketListener(adapters.capture());
        inventory = adapters.getAllValues().get(0);
        actionBar = adapters.getAllValues().get(1);
        particles = adapters.getAllValues().get(2);
    }

    @AfterEach
    void restoreConfig() {
        ConfigManager.UTIL_ARCHITECTURE_LORE_HIDDEN = previousHideLore;
        ConfigManager.FLOODGATE_HIDE_PARTICLES = previousHideParticles;
        ConfigManager.UTIL_ARCHITECTURE_HIDE_PERMISSION = previousPermission;
    }

    @Test
    void initialInventoryPacketsDoNotQueryTemporaryPlayerPermissions() {
        var player = mock(Player.class);
        when(player.hasPermission(BYPASS_PERMISSION)).thenThrow(UnsupportedOperationException.class);
        for (var type : List.of(PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT)) {
            var event = mock(PacketEvent.class);
            when(event.getPacketType()).thenReturn(type);
            when(event.isPlayerTemporary()).thenReturn(true);
            when(event.getPlayer()).thenReturn(player);

            inventory.onPacketSending(event);

            verify(event, never()).getPacket();
            verify(event, never()).setCancelled(anyBoolean());
        }
        verifyNoInteractions(player);
    }

    @Test
    void otherPlayerDependentListenersAlsoSkipTemporaryAndMissingPlayers() {
        var player = mock(Player.class);
        when(player.getUniqueId()).thenThrow(UnsupportedOperationException.class);
        var temporary = mock(PacketEvent.class);
        when(temporary.isPlayerTemporary()).thenReturn(true);
        when(temporary.getPlayer()).thenReturn(player);
        var missing = mock(PacketEvent.class);

        actionBar.onPacketSending(temporary);
        particles.onPacketSending(temporary);
        actionBar.onPacketSending(missing);
        particles.onPacketSending(missing);
        inventory.onPacketSending(missing);

        verifyNoInteractions(player);
        verify(temporary, never()).getPacket();
        verify(temporary, never()).setCancelled(anyBoolean());
        verify(missing, never()).getPacket();
        verify(missing, never()).setCancelled(anyBoolean());
    }

    @Test
    void regularSurvivalPlayersStillReachTheInventoryFilterUnlessTheyHaveBypassPermission() {
        var player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        var filteredEvent = mock(PacketEvent.class);
        when(filteredEvent.getPlayer()).thenReturn(player);

        inventory.onPacketSending(filteredEvent);

        verify(player).hasPermission(BYPASS_PERMISSION);
        verify(player).getGameMode();
        verify(filteredEvent).getPacket();

        when(player.hasPermission(BYPASS_PERMISSION)).thenReturn(true);
        var bypassedEvent = mock(PacketEvent.class);
        when(bypassedEvent.getPlayer()).thenReturn(player);
        inventory.onPacketSending(bypassedEvent);

        verify(bypassedEvent, never()).getPacket();
    }

    @Test
    void creativePlayersStillSkipTheInventoryFilter() {
        var player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        var event = mock(PacketEvent.class);
        when(event.getPlayer()).thenReturn(player);

        inventory.onPacketSending(event);

        verify(event, never()).getPacket();
    }
}

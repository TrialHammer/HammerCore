package ink.ziip.hammer.hammercore.api.listener.packetlistener;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public abstract class BasePacketListener {

    protected final ProtocolManager protocolManager;

    protected BasePacketListener() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    protected static @Nullable Player getPacketPlayer(PacketEvent event) {
        // Initial play packets can arrive before ProtocolLib has a real Bukkit player.
        return event.isPlayerTemporary() ? null : event.getPlayer();
    }

    public abstract void register();

    public abstract void unRegister();
}

package ink.ziip.hammer.hammercore.listener.packetlistener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.packetlistener.BasePacketListener;
import ink.ziip.hammer.hammercore.api.object.user.HammerUser;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import org.bukkit.entity.Player;

public class ParticleListener extends BasePacketListener {

    private final PacketAdapter packetAdapter;

    public ParticleListener() {
        super();
        packetAdapter = new PacketAdapter(HammerCore.getInstance(), ListenerPriority.HIGH, PacketType.Play.Server.WORLD_PARTICLES) {
            @Override
            public void onPacketSending(PacketEvent packetEvent) {
                if (!ConfigManager.FLOODGATE_HIDE_PARTICLES) return;
                Player player = getPacketPlayer(packetEvent);
                if (player != null && HammerUser.getUser(player).isBedrockPlayer()) {
                    packetEvent.setCancelled(true);
                }
            }
        };
    }

    @Override
    public void register() {
        protocolManager.addPacketListener(packetAdapter);
    }

    @Override
    public void unRegister() {
        protocolManager.removePacketListener(packetAdapter);
    }
}

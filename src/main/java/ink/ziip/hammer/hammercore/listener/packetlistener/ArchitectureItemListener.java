package ink.ziip.hammer.hammercore.listener.packetlistener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.packetlistener.BasePacketListener;
import ink.ziip.hammer.hammercore.manager.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ArchitectureItemListener extends BasePacketListener {

    private final PacketAdapter packetAdapter;

    public ArchitectureItemListener() {
        super();
        packetAdapter = new PacketAdapter(HammerCore.getInstance(), ListenerPriority.HIGH, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent packetEvent) {
                if (!ConfigManager.UTIL_ARCHITECTURE_LORE_HIDDEN) return;
                Player player = getPacketPlayer(packetEvent);
                if (player == null || player.hasPermission(ConfigManager.UTIL_ARCHITECTURE_HIDE_PERMISSION)) return;
                GameMode gameMode = player.getGameMode();
                if (gameMode != GameMode.SURVIVAL && gameMode != GameMode.ADVENTURE) return;

                PacketContainer packetContainer = packetEvent.getPacket();
                PacketType type = packetEvent.getPacketType();

                if (type == PacketType.Play.Server.SET_SLOT) {
                    StructureModifier<ItemStack> items = packetContainer.getItemModifier();

                    ItemStack itemStack = items.read(0);

                    if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getItemMeta() != null && itemStack.getItemMeta().getLore() != null) {
                        itemStack.getItemMeta().getLore().forEach(
                                lore -> ConfigManager.UTIL_ARCHITECTURE_LORE_HIDDEN_CONTENT.forEach(
                                        filter -> {
                                            if (lore.contains(filter)) {
                                                ItemMeta itemMeta = itemStack.getItemMeta();
                                                itemMeta.setLore(null);
                                                itemStack.setItemMeta(itemMeta);
                                            }
                                        }));
                    }
                }

                if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                    List<ItemStack> itemStacks = packetContainer.getItemListModifier().read(0);
                    for (ItemStack itemStack : itemStacks) {
                        if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getItemMeta() != null && itemStack.getItemMeta().getLore() != null) {
                            itemStack.getItemMeta().getLore().forEach(
                                    lore -> ConfigManager.UTIL_ARCHITECTURE_LORE_HIDDEN_CONTENT.forEach(
                                            filter -> {
                                                if (lore.contains(filter)) {
                                                    ItemMeta itemMeta = itemStack.getItemMeta();
                                                    itemMeta.setLore(null);
                                                    itemStack.setItemMeta(itemMeta);
                                                }
                                            }));
                        }
                    }
                }

                packetEvent.setPacket(packetContainer);
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

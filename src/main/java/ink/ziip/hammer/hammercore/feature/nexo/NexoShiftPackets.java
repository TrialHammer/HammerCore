package ink.ziip.hammer.hammercore.feature.nexo;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.kyori.adventure.text.Component;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Give legacy plugin output a named font at the wire, without modifying either plugin or pack. */
final class NexoShiftPackets extends PacketListenerAbstract {
    private final Logger log;
    private final AtomicBoolean loggedFailure = new AtomicBoolean();
    private final Map<UUID, TagWindow> tagWindows = new ConcurrentHashMap<>();

    private NexoShiftPackets(Logger log) {
        super(PacketListenerPriority.HIGHEST);
        this.log = log;
    }

    static Runnable register(Logger log) {
        var manager = PacketEvents.getAPI().getEventManager();
        var listener = new NexoShiftPackets(log);
        manager.registerListener(listener);
        return () -> {
            manager.unregisterListener(listener);
            listener.tagWindows.clear();
        };
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled() || !(event.getPacketType() instanceof PacketType.Play.Server type)) return;
        try {
            UUID uuid = event.getUser().getUUID();
            if (uuid != null && FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
            boolean changed = switch (type) {
                case OPEN_WINDOW -> openWindow(event);
                case OPEN_HORSE_WINDOW -> clearWindow(event);
                case CLOSE_WINDOW -> closeWindow(event);
                case WINDOW_ITEMS -> windowItems(event);
                case SET_SLOT -> setSlot(event);
                case ACTION_BAR -> {
                    var packet = new WrapperPlayServerActionBar(event);
                    yield replace(packet.getActionBarText(), packet::setActionBarText);
                }
                case BOSS_BAR -> {
                    var packet = new WrapperPlayServerBossBar(event);
                    var action = packet.getAction();
                    yield (action == WrapperPlayServerBossBar.Action.ADD
                            || action == WrapperPlayServerBossBar.Action.UPDATE_TITLE)
                            && replace(packet.getTitle(), packet::setTitle);
                }
                case SYSTEM_CHAT_MESSAGE -> {
                    var packet = new WrapperPlayServerSystemChatMessage(event);
                    yield replace(packet.getMessage(), packet::setMessage);
                }
                case DISGUISED_CHAT -> {
                    var packet = new WrapperPlayServerDisguisedChat(event);
                    yield replace(packet.getMessage(), packet::setMessage) | styleChatName(packet.getChatFormatting());
                }
                case CHAT_MESSAGE -> {
                    var packet = new WrapperPlayServerChatMessage(event);
                    if (!(packet.getMessage() instanceof ChatMessage_v1_19_3 message)) yield false;
                    // Only change the unsigned rendering and the decoration, never the signed body.
                    yield replace(message.getUnsignedChatContent().orElse(null), message::setUnsignedChatContent)
                            | styleChatName(message.getChatFormatting());
                }
                case SET_TITLE_TEXT -> {
                    var packet = new WrapperPlayServerSetTitleText(event);
                    yield replace(packet.getTitle(), packet::setTitle);
                }
                case SET_TITLE_SUBTITLE -> {
                    var packet = new WrapperPlayServerSetTitleSubtitle(event);
                    yield replace(packet.getSubtitle(), packet::setSubtitle);
                }
                case PLAYER_LIST_HEADER_AND_FOOTER -> {
                    var packet = new WrapperPlayServerPlayerListHeaderAndFooter(event);
                    yield replace(packet.getHeader(), packet::setHeader) | replace(packet.getFooter(), packet::setFooter);
                }
                case TEAMS -> {
                    var info = new WrapperPlayServerTeams(event).getTeamInfo().orElse(null);
                    yield info != null && (replace(info.getDisplayName(), info::setDisplayName)
                            | replace(info.getPrefix(), info::setPrefix) | replace(info.getSuffix(), info::setSuffix));
                }
                default -> false;
            };
            if (changed) event.markForReEncode(true);
        } catch (RuntimeException | LinkageError e) {
            if (loggedFailure.compareAndSet(false, true)) {
                log.log(Level.WARNING, "Could not apply an explicit Innova glyph font to " + type, e);
            }
        }
    }

    private boolean openWindow(PacketSendEvent event) {
        var packet = new WrapperPlayServerOpenWindow(event);
        UUID uuid = event.getUser().getUUID();
        if (uuid != null) {
            int type = packet.getType();
            if (type >= 0 && type < 6 && NexoShiftText.isTagDisplayMenu(packet.getTitle())) {
                // Generic 9x1 through 9x6 containers. Never restyle the player's lower inventory.
                tagWindows.put(uuid, new TagWindow(packet.getContainerId(), (type + 1) * 9));
            } else {
                tagWindows.remove(uuid);
            }
        }
        return replace(packet.getTitle(), packet::setTitle);
    }

    private boolean clearWindow(PacketSendEvent event) {
        UUID uuid = event.getUser().getUUID();
        if (uuid != null) tagWindows.remove(uuid);
        return false;
    }

    private boolean closeWindow(PacketSendEvent event) {
        UUID uuid = event.getUser().getUUID();
        if (uuid != null) {
            TagWindow window = tagWindows.get(uuid);
            if (window != null && window.id() == new WrapperPlayServerCloseWindow(event).getWindowId()) {
                tagWindows.remove(uuid, window);
            }
        }
        return false;
    }

    private boolean windowItems(PacketSendEvent event) {
        TagWindow window = currentWindow(event);
        if (window == null) return false;
        var packet = new WrapperPlayServerWindowItems(event);
        if (packet.getWindowId() != window.id()) return false;
        List<ItemStack> original = packet.getItems();
        List<ItemStack> updated = null;
        for (int i = 0; i < Math.min(original.size(), window.slots()); i++) {
            ItemStack item = original.get(i);
            ItemStack styled = styleItem(item);
            if (styled != item) {
                if (updated == null) updated = new ArrayList<>(original);
                updated.set(i, styled);
            }
        }
        if (updated == null) return false;
        packet.setItems(updated);
        return true;
    }

    private boolean setSlot(PacketSendEvent event) {
        TagWindow window = currentWindow(event);
        if (window == null) return false;
        var packet = new WrapperPlayServerSetSlot(event);
        if (packet.getWindowId() != window.id() || packet.getSlot() < 0 || packet.getSlot() >= window.slots()) return false;
        ItemStack original = packet.getItem();
        ItemStack styled = styleItem(original);
        if (styled == original) return false;
        packet.setItem(styled);
        return true;
    }

    private TagWindow currentWindow(PacketSendEvent event) {
        UUID uuid = event.getUser().getUUID();
        return uuid == null ? null : tagWindows.get(uuid);
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        UUID uuid = event.getUser().getUUID();
        if (uuid != null) tagWindows.remove(uuid);
    }

    private static ItemStack styleItem(ItemStack item) {
        if (item == null || item.isEmpty()) return item;
        var customName = item.getComponent(ComponentTypes.CUSTOM_NAME).orElse(null);
        var itemName = item.getComponent(ComponentTypes.ITEM_NAME).orElse(null);
        var styledCustomName = NexoShiftText.style(customName);
        var styledItemName = NexoShiftText.style(itemName);
        var lore = item.getComponent(ComponentTypes.LORE).orElse(null);
        List<Component> styledLore = null;
        if (lore != null) {
            var lines = lore.getLines();
            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                var styled = NexoShiftText.style(line);
                if (line != styled) {
                    if (styledLore == null) styledLore = new ArrayList<>(lines);
                    styledLore.set(i, styled);
                }
            }
        }
        if (customName == styledCustomName && itemName == styledItemName && styledLore == null) return item;
        ItemStack result = item.copy();
        if (customName != styledCustomName) result.setComponent(ComponentTypes.CUSTOM_NAME, styledCustomName);
        if (itemName != styledItemName) result.setComponent(ComponentTypes.ITEM_NAME, styledItemName);
        if (styledLore != null) result.setComponent(ComponentTypes.LORE, new ItemLore(styledLore));
        return result;
    }

    private static boolean styleChatName(ChatType.Bound formatting) {
        return formatting != null && (replace(formatting.getName(), formatting::setName)
                | replace(formatting.getTargetName(), formatting::setTargetName));
    }

    private static boolean replace(Component original, Consumer<Component> set) {
        Component styled = NexoShiftText.style(original);
        if (styled == original) return false;
        set.accept(styled);
        return true;
    }

    private record TagWindow(int id, int slots) {}
}

package ink.ziip.hammer.hammercore.feature.nexo;

import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/** Keeps all optional Nexo class references behind the plugin-availability check. */
public final class NexoIntegration {
    private final JavaPlugin core;
    private Listener furniture;
    private Runnable stopShiftPackets;

    public NexoIntegration(JavaPlugin core) {
        this.core = core;
    }

    public void start() {
        Plugin nexo = plugin("Nexo");
        if (nexo != null && nexo.isEnabled() && plugin("InnovaGlyphCompat") == null) {
            try {
                NexoBuiltinGlyphs.install(core);
            } catch (RuntimeException | LinkageError | ReflectiveOperationException e) {
                core.getLogger().log(Level.WARNING, "Could not install the Innova Nexo built-in glyph layout", e);
            }
        } else if (plugin("InnovaGlyphCompat") != null) {
            core.getLogger().warning("Remove the old InnovaGlyphCompat.jar before using HammerCore's Nexo glyph integration.");
        }
        if (nexo != null && nexo.isEnabled()) {
            try {
                if (NexoBuiltinGlyphs.isInnovaLayout()) {
                    Plugin packetEvents = plugin("packetevents");
                    if (packetEvents != null && packetEvents.isEnabled()) {
                        stopShiftPackets = NexoShiftPackets.register(core.getLogger());
                        core.getLogger().info("Explicit Innova Nexo shift fonts enabled for menus, chat and HUD.");
                    } else {
                        core.getLogger().warning("PacketEvents is unavailable; legacy Innova text will use the default font.");
                    }
                }
            } catch (RuntimeException | LinkageError e) {
                core.getLogger().log(Level.WARNING, "Could not enable explicit Innova shift fonts", e);
            }
        }
        refreshGameplay();
    }

    public void refreshGameplay() {
        stopGameplay();
        var command = core.getCommand("innovacatalog");
        command.setTabCompleter((sender, cmd, alias, args) -> java.util.List.of());
        command.setExecutor((sender, cmd, alias, args) -> {
            sender.sendMessage(ChatColor.GRAY + "当前服务器未启用 Innova 配方目录。");
            return true;
        });
        Plugin nexo = plugin("Nexo");
        if (nexo == null || !nexo.isEnabled() || !core.getConfig().getBoolean("settings.enabled", true)) return;
        if (plugin("InnovaNativeCompat") != null) {
            core.getLogger().warning("Remove the old InnovaNativeCompat.jar to activate HammerCore's furniture protection and recipe catalogue.");
            return;
        }
        try {
            var catalog = new NexoRecipeCatalog(core, nexo.getDataFolder());
            furniture = new NexoFurnitureProtection();
            core.getServer().getPluginManager().registerEvents(furniture, core);
            command.setExecutor(catalog);
            core.getLogger().info("Nexo furniture ownership and Innova recipe catalogue enabled.");
        } catch (RuntimeException | LinkageError e) {
            stopGameplay();
            core.getLogger().log(Level.WARNING, "Could not enable Nexo gameplay integration", e);
        }
    }

    public void stop() {
        if (stopShiftPackets != null) {
            stopShiftPackets.run();
            stopShiftPackets = null;
        }
        stopGameplay();
    }

    private void stopGameplay() {
        if (furniture != null) HandlerList.unregisterAll(furniture);
        furniture = null;
    }

    private Plugin plugin(String name) {
        return core.getServer().getPluginManager().getPlugin(name);
    }
}

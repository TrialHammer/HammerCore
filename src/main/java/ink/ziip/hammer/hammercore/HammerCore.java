package ink.ziip.hammer.hammercore;

import ink.ziip.hammer.hammercore.api.object.location.WoodLocation;
import ink.ziip.hammer.hammercore.api.object.user.HammerUser;
import ink.ziip.hammer.hammercore.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HammerCore extends JavaPlugin {

    private static HammerCore instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private CommandManager commandManager;
    private PacketListenerManager packetListenerManager;
    private PlaceholderExpansionManager placeholderExpansionManager;
    private TaskManager taskManager;
    private ListenerManager listenerManager;

    public HammerCore() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("Could not find PlaceholderAPI; disabling HammerCore.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Configuration and messages are always loaded so /hammercore reload can recover a disabled plugin.
        configManager = new ConfigManager();
        configManager.load();
        messageManager = new MessageManager();
        messageManager.load();

        commandManager = new CommandManager();
        commandManager.load();

        if (!Boolean.TRUE.equals(ConfigManager.SETTINGS_ENABLED)) {
            getLogger().info("HammerCore features are disabled via settings.enabled; use /hammercore reload after enabling.");
            return;
        }

        placeholderExpansionManager = new PlaceholderExpansionManager();
        placeholderExpansionManager.load();

        packetListenerManager = new PacketListenerManager();
        packetListenerManager.load();

        taskManager = new TaskManager();
        taskManager.load();

        listenerManager = new ListenerManager();
        listenerManager.load();
    }

    @Override
    public void onDisable() {
        if (listenerManager != null) listenerManager.unload();
        if (taskManager != null) taskManager.unload();
        if (packetListenerManager != null) packetListenerManager.unload();
        if (placeholderExpansionManager != null) placeholderExpansionManager.unload();
        if (commandManager != null) commandManager.unload();
        if (messageManager != null) messageManager.unload();
        if (configManager != null) configManager.unload();

        HammerUser.cleanHammerUsers();
        WoodLocation.cleanWoods();
    }

    public static HammerCore getInstance() {
        return instance;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    /** Reloads YAML, messages, permissions and re-registers listeners and tasks. */
    public void reloadRuntimeConfiguration() {
        configManager.reload();
        messageManager.reload();

        if (!Boolean.TRUE.equals(ConfigManager.SETTINGS_ENABLED)) {
            if (listenerManager != null) {
                listenerManager.unload();
                listenerManager = null;
            }
            if (taskManager != null) {
                taskManager.unload();
                taskManager = null;
            }
            getLogger().info("HammerCore features are disabled via settings.enabled.");
            return;
        }

        if (placeholderExpansionManager == null) {
            placeholderExpansionManager = new PlaceholderExpansionManager();
            placeholderExpansionManager.load();
        }
        if (packetListenerManager == null) {
            packetListenerManager = new PacketListenerManager();
            packetListenerManager.load();
        }
        if (taskManager == null) {
            taskManager = new TaskManager();
        }
        taskManager.reload();
        if (listenerManager == null) {
            listenerManager = new ListenerManager();
        }
        listenerManager.reload();
        if (ConfigManager.SETTINGS_DEBUG) {
            getLogger().info("Reloaded HammerCore configuration and messages.");
        }
    }
}

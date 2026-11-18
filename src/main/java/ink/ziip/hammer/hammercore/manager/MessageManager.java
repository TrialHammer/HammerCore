package ink.ziip.hammer.hammercore.manager;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.manager.BaseManager;
import ink.ziip.hammer.hammercore.api.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * All player-facing text is loaded from {@code messages.yml}. The resource
 * bundled in the jar acts as the default set, so no user-visible strings are
 * hardcoded in Java.
 */
public class MessageManager extends BaseManager {

    private FileConfiguration config;

    public static String NO_PERMISSION;
    public static String DRINKING_ALCOHOL_IS_BAD_FOR_YOUR_HEALTH;
    public static String MENG_PO_SOUP_TIPS;
    public static String WHAT_ARE_YOU_DOING;
    public static String NO_ENTITY_WAS_TARGETED;
    public static String INVALID_CHARACTERS;

    public MessageManager() {
        config = loadFile();
    }

    @Override
    public void load() {
        config = loadFile();
        loadMessages();
    }

    @Override
    public void unload() {
    }

    @Override
    public void reload() {
        load();
    }

    private FileConfiguration loadFile() {
        File file = new File(HammerCore.getInstance().getDataFolder(), "messages.yml");
        if (!file.exists()) HammerCore.getInstance().saveResource("messages.yml", false);
        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defaults = bundledDefaults();
        if (defaults != null) {
            loaded.setDefaults(defaults);
            loaded.options().copyDefaults(true);
        }
        return loaded;
    }

    /**
     * Moves legacy {@code config.yml#message.*} entries into messages.yml, keeping
     * any text a server owner had customised. Existing messages.yml values win.
     *
     * @return true when legacy keys were found and removed from config.yml
     */
    public static boolean migrateLegacyMessages(FileConfiguration legacyConfig) {
        var section = legacyConfig.getConfigurationSection("message");
        if (section == null) return false;

        File file = new File(HammerCore.getInstance().getDataFolder(), "messages.yml");
        if (!file.exists()) HammerCore.getInstance().saveResource("messages.yml", false);
        YamlConfiguration messages = YamlConfiguration.loadConfiguration(file);

        Map<String, String> mapping = Map.of(
                "no-permission", "command.no-permission",
                "what-are-you-doing", "command.what-are-you-doing",
                "no-entity-was-targeted", "command.no-entity-targeted",
                "drinking-alcohol-is-bad-for-your-health", "brewery.drinking-warning",
                "meng-po-soup-tips", "brewery.meng-po-soup");

        // A legacy config.yml value is a real server-owner setting; a messages.yml
        // entry that still equals the bundled default is only a default. Prefer the
        // legacy value, but never overwrite text the owner already customised.
        YamlConfiguration bundled = bundledResource();
        boolean dirty = false;
        for (var entry : mapping.entrySet()) {
            String legacyValue = section.getString(entry.getKey());
            if (legacyValue == null) continue;
            String current = messages.getString(entry.getValue());
            String defaultText = bundled == null ? null : bundled.getString(entry.getValue());
            if (current == null || current.equals(defaultText)) {
                messages.set(entry.getValue(), legacyValue);
                dirty = true;
            }
        }
        if (dirty) {
            try {
                messages.save(file);
            } catch (Exception ignored) {
            }
        }

        legacyConfig.set("message", null);
        return true;
    }

    private static YamlConfiguration bundledResource() {
        try (InputStream stream = HammerCore.getInstance().getResource("messages.yml")) {
            if (stream == null) return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception exception) {
            return null;
        }
    }

    private YamlConfiguration bundledDefaults() {
        try (InputStream stream = HammerCore.getInstance().getResource("messages.yml")) {
            if (stream == null) return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception exception) {
            return null;
        }
    }

    private void loadMessages() {
        NO_PERMISSION = text("command.no-permission");
        WHAT_ARE_YOU_DOING = text("command.what-are-you-doing");
        NO_ENTITY_WAS_TARGETED = text("command.no-entity-targeted");
        DRINKING_ALCOHOL_IS_BAD_FOR_YOUR_HEALTH = text("brewery.drinking-warning");
        MENG_PO_SOUP_TIPS = text("brewery.meng-po-soup");
        INVALID_CHARACTERS = text("util.invalid-characters");
    }

    /** Returns the configured, colour-translated text, or an empty string when absent. */
    public String text(String key) {
        String value = config.getString(key);
        return value == null ? "" : Utils.translateColorCodes(value);
    }

    public String format(String key, Map<String, String> placeholders) {
        String value = text(key);
        for (var entry : placeholders.entrySet()) {
            value = value.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return value;
    }

    /** Returns the configured raw value or {@code null} when absent. */
    public String raw(String key) {
        return config.getString(key);
    }

    public void send(org.bukkit.command.CommandSender sender, String key, Map<String, String> placeholders) {
        String message = format(key, placeholders);
        if (!message.isBlank()) sender.sendMessage(message);
    }
}

package ink.ziip.hammer.hammercore.manager;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.manager.BaseManager;
import ink.ziip.hammer.hammercore.api.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigManager extends BaseManager {

    private FileConfiguration config;

    public static Boolean SETTINGS_ENABLED;
    public static Boolean SETTINGS_DEBUG;
    public static Boolean INFO_BAR_ENABLED;
    public static int INFO_BAR_REFRESH_TICKS;
    public static Boolean INFO_BAR_REMOVE_ON_QUIT;
    public static String INFO_BAR_BAR_COLOR;
    public static String INFO_BAR_BAR_STYLE;
    public static String INFO_BAR_CONTENT_JAVA_DEFAULT;
    public static Map<String, String> INFO_BAR_CONTENT_JAVA_WORLD_LIST;
    public static String INFO_BAR_CONTENT_BEDROCK_DEFAULT;
    public static Map<String, String> INFO_BAR_CONTENT_BEDROCK_WORLD_LIST;
    public static String INFO_BAR_WORLD_NAME_JAVA_DEFAULT;
    public static Map<String, String> INFO_BAR_WORLD_NAME_JAVA_WORLD_LIST;
    public static String INFO_BAR_WORLD_NAME_BEDROCK_DEFAULT;
    public static Map<String, String> INFO_BAR_WORLD_NAME_BEDROCK_WORLD_LIST;
    public static String INFO_BAR_INFO_SEASON_DEFAULT;
    public static Map<String, String> INFO_BAR_INFO_SEASON_LIST;
    public static String INFO_BAR_INFO_WEATHER_DEFAULT;
    public static Map<String, String> INFO_BAR_INFO_WEATHER_LIST;

    public static boolean ACTION_BAR_ENABLED;
    public static int ACTION_BAR_REFRESH_TICKS;
    public static long ACTION_BAR_IDLE_RESET_MILLIS;
    public static String ACTION_BAR_CONTENT;

    public static boolean WORLD_PROTECTION_ANTI_EXPLOSION_ENABLED;
    public static boolean WORLD_PROTECTION_ANTI_EXPLOSION_ONLY_CREEPERS;
    public static List<String> WORLD_PROTECTION_ANTI_EXPLOSION_WORLD_LIST;
    public static boolean WORLD_PROTECTION_ANTI_GRIEFING_ENABLED;
    public static List<String> WORLD_PROTECTION_ANTI_GRIEFING_WORLD_LIST;
    public static Boolean WORLD_PROTECTION_NO_CARTOGRAPHER;
    public static Boolean WORLD_PROTECTION_PREVENT_FARM_LAND_DESTROY;
    public static String WORLD_PROTECTION_INVALID_CHAT_PATTERN;
    public static String WORLD_PROTECTION_KEEP_INVENTORY_PERMISSION;
    public static double WORLD_PROTECTION_KEEP_INVENTORY_MIN_Y;
    public static double WORLD_PROTECTION_KEEP_INVENTORY_NETHER_MAX_Y;
    public static List<String> WORLD_PROTECTION_KEEP_INVENTORY_NETHER_WORLDS;
    public static String WORLD_PROTECTION_PROTECT_FARM_PERMISSION;

    public static Boolean FLOODGATE_HIDE_PARTICLES;
    public static Boolean UTIL_ARCHITECTURE_LORE_HIDDEN;
    public static String UTIL_ARCHITECTURE_HIDE_PERMISSION;
    public static List<String> UTIL_ARCHITECTURE_LORE_HIDDEN_CONTENT;
    public static Boolean UTIL_PEELING_RECOVERY;
    public static long UTIL_PEELING_RETENTION_MILLIS;
    public static int UTIL_PEELING_CLEANUP_INTERVAL_TICKS;
    public static Boolean UTIL_DISABLE_JOIN_MESSAGE;
    public static Boolean UTIL_DISABLE_QUIT_MESSAGE;
    public static Boolean UTIL_AUTO_RESPAWN_ENABLED;
    public static long UTIL_AUTO_RESPAWN_DELAY_TICKS;
    public static Location UTIL_AUTO_RESPAWN_LOCATION;
    public static Boolean UTIL_DISABLE_ELYTRA;
    public static Boolean UTIL_RESTRICT_PORTAL_RANGE;
    public static String UTIL_PORTAL_NETHER_WORLD;
    public static double UTIL_PORTAL_MIN_X, UTIL_PORTAL_MIN_Y, UTIL_PORTAL_MIN_Z;
    public static double UTIL_PORTAL_MAX_X, UTIL_PORTAL_MAX_Y, UTIL_PORTAL_MAX_Z;

    public static boolean QUICK_SWAP_HAND_MENU;
    public static boolean QUICK_DROP_TOGGLE_BENDING;
    public static boolean QUICK_CLOCK_MENU;
    public static Material QUICK_CLOCK_MATERIAL;
    public static String QUICK_MENU_PERMISSION;
    public static String QUICK_MENU_COMMAND;
    public static String QUICK_FALLBACK_MENU_COMMAND;
    public static String QUICK_BEND_TOGGLE_COMMAND;
    public static boolean SPAWN_LITTER_ENABLED;
    public static String SPAWN_LITTER_WORLD;
    public static double SPAWN_LITTER_MIN_X, SPAWN_LITTER_MIN_Y, SPAWN_LITTER_MIN_Z;
    public static double SPAWN_LITTER_MAX_X, SPAWN_LITTER_MAX_Y, SPAWN_LITTER_MAX_Z;
    public static long SPAWN_LITTER_COOLDOWN_MILLIS;
    public static long SPAWN_LITTER_CLEANUP_DELAY_TICKS;
    public static int SPAWN_LITTER_CLEANUP_SECONDS;
    public static boolean SOUVENIR_ENABLED;
    public static Material SOUVENIR_MAP_MATERIAL;
    public static boolean SOUVENIR_ENDER_EYE_ENABLED;
    public static String SOUVENIR_GENERAL_PATTERN;
    public static String SOUVENIR_WORKBENCH_PATTERN;
    public static String SOUVENIR_ARCHITECTURE_PATTERN;
    public static String SOUVENIR_TELEPORT_PATTERN;
    public static String SOUVENIR_WARP_COMMAND;

    public static long JOIN_REMOVE_EFFECTS_LONGER_THAN_TICKS;

    public static Boolean PROJECTKORRA_ALL_EARTH_BINDER;
    public static long PROJECTKORRA_ADD_EARTH_DELAY_TICKS;
    public static String PROJECTKORRA_ADD_EARTH_COMMAND;

    public static boolean MINECART_ENABLED;
    public static boolean MINECART_REMOVE_ON_LEAVE;
    public static boolean MINECART_ACCELERATOR_ENABLED;
    public static int MINECART_ACCELERATOR_RAILS_REQUIRED;
    public static double MINECART_BOOSTED_MAX_SPEED;
    public static double MINECART_NORMAL_MAX_SPEED;
    public static double MINECART_SPEED_CAP;

    public static String BREWERY_PREMIUM_MENG_PO_RECIPE;
    public static String BREWERY_WINE_RECIPE;
    public static String BREWERY_MENG_PO_BEND_REMOVE_COMMAND;
    public static String BREWERY_MENG_PO_WARP_COMMAND;
    public static String BREWERY_WINE_REWARD_COMMAND;
    public static String BREWERY_WINE_LORE_PATTERN;
    public static boolean BREWERY_ENABLED;
    public static boolean PROJECTKORRA_ENABLED;
    public static boolean SEASONS_ENABLED;
    public static String SEASONS_WORLD;
    public static long MAGIC_STICK_COOLDOWN_MILLIS;
    public static int MAGIC_STICK_USES;
    public static double TARGET_THRESHOLD;
    public static double TARGET_RANGE;
    public static int RANDOM_EFFECT_DURATION_TICKS;
    public static int RANDOM_EFFECT_AMPLIFIER;
    public static int LEVITATION_DURATION_TICKS;
    public static int LEVITATION_AMPLIFIER;
    public static double SONIC_BOOM_VELOCITY;
    public static int SONIC_BOOM_PARTICLE_COUNT;
    public static float SONIC_BOOM_SOUND_VOLUME;
    public static float SONIC_BOOM_SOUND_PITCH;
    public static long SONIC_BOOM_INTERVAL_TICKS;
    public static int SONIC_BOOM_STEPS;
    public static boolean SNOWBALL_ENABLED;
    public static double SNOWBALL_VELOCITY_MULTIPLIER;
    public static double SNOWBALL_DAMAGE;

    public static boolean ANTI_RAID_FARM_ENABLED;
    public static long ANTI_RAID_FARM_COOLDOWN_SECONDS;
    public static String ANTI_RAID_FARM_BYPASS_PERMISSION;

    public static int VILLAGER_RESTORE_DEFAULT_RADIUS;
    public static int VILLAGER_RESTORE_MAX_RADIUS;

    public ConfigManager() {
        config = HammerCore.getInstance().getConfig();
    }

    @Override public void load() { loadConfig(); }
    @Override public void unload() { }
    @Override public void reload() {
        HammerCore.getInstance().reloadConfig();
        // reloadConfig() swaps JavaPlugin#getConfig(), so refresh our reference.
        config = HammerCore.getInstance().getConfig();
        loadConfig();
    }

    private void loadConfig() {
        migrateLegacyConfig();
        SETTINGS_ENABLED = config.getBoolean("settings.enabled", true);
        SETTINGS_DEBUG = config.getBoolean("settings.debug", false);
        INFO_BAR_ENABLED = config.getBoolean("info-bar.enabled", true);
        INFO_BAR_REFRESH_TICKS = config.getInt("info-bar.refresh-ticks", 40);
        INFO_BAR_REMOVE_ON_QUIT = config.getBoolean("info-bar.remove-on-quit", true);
        INFO_BAR_BAR_COLOR = config.getString("info-bar.bar-color", "WHITE");
        INFO_BAR_BAR_STYLE = config.getString("info-bar.bar-style", "SOLID");
        INFO_BAR_CONTENT_JAVA_DEFAULT = config.getString("info-bar.content.java.default");
        INFO_BAR_CONTENT_JAVA_WORLD_LIST = Utils.splitList(config.getStringList("info-bar.content.java.world"));
        INFO_BAR_CONTENT_BEDROCK_DEFAULT = config.getString("info-bar.content.bedrock.default");
        INFO_BAR_CONTENT_BEDROCK_WORLD_LIST = Utils.splitList(config.getStringList("info-bar.content.bedrock.world"));
        INFO_BAR_WORLD_NAME_JAVA_DEFAULT = config.getString("info-bar.world-name.java.default");
        INFO_BAR_WORLD_NAME_JAVA_WORLD_LIST = Utils.splitList(config.getStringList("info-bar.world-name.java.world"));
        INFO_BAR_WORLD_NAME_BEDROCK_DEFAULT = config.getString("info-bar.world-name.bedrock.default");
        INFO_BAR_WORLD_NAME_BEDROCK_WORLD_LIST = Utils.splitList(config.getStringList("info-bar.world-name.bedrock.world"));
        INFO_BAR_INFO_SEASON_DEFAULT = config.getString("info-bar.info.season.default");
        INFO_BAR_INFO_SEASON_LIST = Utils.splitList(config.getStringList("info-bar.info.season.season"));
        INFO_BAR_INFO_WEATHER_DEFAULT = config.getString("info-bar.info.weather.default");
        INFO_BAR_INFO_WEATHER_LIST = Utils.splitList(config.getStringList("info-bar.info.weather.weather"));

        ACTION_BAR_ENABLED = config.getBoolean("action-bar.enabled", true);
        ACTION_BAR_REFRESH_TICKS = config.getInt("action-bar.refresh-ticks", 40);
        ACTION_BAR_IDLE_RESET_MILLIS = config.getLong("action-bar.idle-reset-millis", 1000L);
        ACTION_BAR_CONTENT = config.getString("action-bar.content");

        WORLD_PROTECTION_ANTI_EXPLOSION_ENABLED = config.getBoolean("world-protection.anti-explosion.enabled", true);
        WORLD_PROTECTION_ANTI_EXPLOSION_ONLY_CREEPERS = config.getBoolean("world-protection.anti-explosion.only-creepers", true);
        WORLD_PROTECTION_ANTI_EXPLOSION_WORLD_LIST = config.getStringList("world-protection.anti-explosion.worlds");
        WORLD_PROTECTION_ANTI_GRIEFING_ENABLED = config.getBoolean("world-protection.anti-griefing.enabled", true);
        WORLD_PROTECTION_ANTI_GRIEFING_WORLD_LIST = config.getStringList("world-protection.anti-griefing.worlds");
        WORLD_PROTECTION_NO_CARTOGRAPHER = config.getBoolean("world-protection.no-cartographer", true);
        WORLD_PROTECTION_PREVENT_FARM_LAND_DESTROY = config.getBoolean("world-protection.prevent-farm-land-destroy", true);
        WORLD_PROTECTION_INVALID_CHAT_PATTERN = config.getString("world-protection.invalid-chat-pattern", "[ꀀ-\uA48F]");
        WORLD_PROTECTION_KEEP_INVENTORY_PERMISSION = config.getString("world-protection.keep-inventory.permission", "hammercore.utils.keepinventory");
        WORLD_PROTECTION_KEEP_INVENTORY_MIN_Y = config.getDouble("world-protection.keep-inventory.minimum-y", -64);
        WORLD_PROTECTION_KEEP_INVENTORY_NETHER_MAX_Y = config.getDouble("world-protection.keep-inventory.nether-maximum-y", 127);
        WORLD_PROTECTION_KEEP_INVENTORY_NETHER_WORLDS = config.getStringList("world-protection.keep-inventory.nether-worlds");
        WORLD_PROTECTION_PROTECT_FARM_PERMISSION = config.getString("world-protection.protect-farm-permission", "hammercore.utils.protectfarm");

        FLOODGATE_HIDE_PARTICLES = config.getBoolean("floodgate.hide-particles", true);
        UTIL_ARCHITECTURE_LORE_HIDDEN = config.getBoolean("util.architecture-lore.hidden", true);
        UTIL_ARCHITECTURE_HIDE_PERMISSION = config.getString("util.architecture-lore.hide-permission", "hammercore.utils.hidenbt");
        UTIL_ARCHITECTURE_LORE_HIDDEN_CONTENT = config.getStringList("util.architecture-lore.content");
        UTIL_PEELING_RECOVERY = config.getBoolean("util.peeling-recovery.enabled", true);
        UTIL_PEELING_RETENTION_MILLIS = config.getLong("util.peeling-recovery.retention-millis", 300000L);
        UTIL_PEELING_CLEANUP_INTERVAL_TICKS = config.getInt("util.peeling-recovery.cleanup-interval-ticks", 12000);
        UTIL_DISABLE_JOIN_MESSAGE = config.getBoolean("util.disable-join-message", true);
        UTIL_DISABLE_QUIT_MESSAGE = config.getBoolean("util.disable-quit-message", true);
        UTIL_AUTO_RESPAWN_ENABLED = config.getBoolean("util.auto-respawn.enabled", true);
        UTIL_AUTO_RESPAWN_DELAY_TICKS = config.getLong("util.auto-respawn.delay-ticks", 1L);
        UTIL_DISABLE_ELYTRA = config.getBoolean("util.disable-elytra", false);
        UTIL_RESTRICT_PORTAL_RANGE = config.getBoolean("util.restrict-portal-range.enabled", false);
        UTIL_PORTAL_NETHER_WORLD = config.getString("util.restrict-portal-range.nether-world", "world_nether");
        UTIL_PORTAL_MIN_X = config.getDouble("util.restrict-portal-range.min-x", -1000);
        UTIL_PORTAL_MIN_Y = config.getDouble("util.restrict-portal-range.min-y", -64);
        UTIL_PORTAL_MIN_Z = config.getDouble("util.restrict-portal-range.min-z", -1000);
        UTIL_PORTAL_MAX_X = config.getDouble("util.restrict-portal-range.max-x", 1000);
        UTIL_PORTAL_MAX_Y = config.getDouble("util.restrict-portal-range.max-y", 320);
        UTIL_PORTAL_MAX_Z = config.getDouble("util.restrict-portal-range.max-z", 1000);

        QUICK_SWAP_HAND_MENU = config.getBoolean("util.quick-actions.swap-hand-opens-menu", true);
        QUICK_DROP_TOGGLE_BENDING = config.getBoolean("util.quick-actions.drop-item-toggles-bending", true);
        QUICK_CLOCK_MENU = config.getBoolean("util.quick-actions.clock-opens-menu", true);
        QUICK_CLOCK_MATERIAL = material("util.quick-actions.clock-material", Material.CLOCK);
        QUICK_MENU_PERMISSION = config.getString("util.quick-actions.menu-permission", "group.player");
        QUICK_MENU_COMMAND = config.getString("util.quick-actions.menu-command");
        QUICK_FALLBACK_MENU_COMMAND = config.getString("util.quick-actions.fallback-menu-command", "tutorial 5");
        QUICK_BEND_TOGGLE_COMMAND = config.getString("util.quick-actions.bend-toggle-command");
        SPAWN_LITTER_ENABLED = config.getBoolean("util.spawn-litter.enabled", true);
        SPAWN_LITTER_WORLD = config.getString("util.spawn-litter.world", "world");
        SPAWN_LITTER_MIN_X = config.getDouble("util.spawn-litter.min-x", 56);
        SPAWN_LITTER_MIN_Y = config.getDouble("util.spawn-litter.min-y", 22);
        SPAWN_LITTER_MIN_Z = config.getDouble("util.spawn-litter.min-z", 120);
        SPAWN_LITTER_MAX_X = config.getDouble("util.spawn-litter.max-x", 103);
        SPAWN_LITTER_MAX_Y = config.getDouble("util.spawn-litter.max-y", 82);
        SPAWN_LITTER_MAX_Z = config.getDouble("util.spawn-litter.max-z", 217);
        SPAWN_LITTER_COOLDOWN_MILLIS = config.getLong("util.spawn-litter.cooldown-millis", 120000L);
        SPAWN_LITTER_CLEANUP_DELAY_TICKS = config.getLong("util.spawn-litter.cleanup-delay-ticks", 1200L);
        SPAWN_LITTER_CLEANUP_SECONDS = config.getInt("util.spawn-litter.cleanup-seconds", 120);
        SOUVENIR_ENABLED = config.getBoolean("util.souvenir.enabled", true);
        SOUVENIR_MAP_MATERIAL = material("util.souvenir.map-material", Material.FILLED_MAP);
        SOUVENIR_ENDER_EYE_ENABLED = config.getBoolean("util.souvenir.ender-eye-enabled", true);
        SOUVENIR_GENERAL_PATTERN = config.getString("util.souvenir.general-lore-pattern", "<[0-9]{4}-[0-9]{2}>");
        SOUVENIR_WORKBENCH_PATTERN = config.getString("util.souvenir.workbench-lore-pattern", "\\[[0-9]{4}-[0-9]{2}]");
        SOUVENIR_ARCHITECTURE_PATTERN = config.getString("util.souvenir.architecture-lore-pattern");
        SOUVENIR_TELEPORT_PATTERN = config.getString("util.souvenir.teleport-lore-pattern", "\\[[0-9]{3}-.*]");
        SOUVENIR_WARP_COMMAND = config.getString("util.souvenir.warp-command", "huskhomes:warp %warp% %player%");

        JOIN_REMOVE_EFFECTS_LONGER_THAN_TICKS = config.getLong("join.remove-effects-longer-than-ticks", 6000L);

        PROJECTKORRA_ALL_EARTH_BINDER = config.getBoolean("projectkorra.all-earth-binder", false);
        PROJECTKORRA_ADD_EARTH_DELAY_TICKS = config.getLong("projectkorra.add-earth-delay-ticks", 20L);
        PROJECTKORRA_ADD_EARTH_COMMAND = config.getString("projectkorra.add-earth-command", "bend add earth %player%");

        MINECART_ENABLED = config.getBoolean("minecart.enabled", true);
        MINECART_REMOVE_ON_LEAVE = config.getBoolean("minecart.remove-on-leave", true);
        MINECART_ACCELERATOR_ENABLED = config.getBoolean("minecart.accelerator.enabled", true);
        MINECART_ACCELERATOR_RAILS_REQUIRED = config.getInt("minecart.accelerator.rails-required", 5);
        MINECART_BOOSTED_MAX_SPEED = config.getDouble("minecart.accelerator.boosted-max-speed", 2.4);
        MINECART_NORMAL_MAX_SPEED = config.getDouble("minecart.accelerator.normal-max-speed", 0.4);
        MINECART_SPEED_CAP = config.getDouble("minecart.accelerator.speed-cap", 2.0);

        BREWERY_PREMIUM_MENG_PO_RECIPE = config.getString("brewery.quality-names.premium-meng-po");
        BREWERY_WINE_RECIPE = config.getString("brewery.quality-names.wine");
        BREWERY_MENG_PO_BEND_REMOVE_COMMAND = config.getString("brewery.commands.meng-po-bend-remove", "bend remove %player%");
        BREWERY_MENG_PO_WARP_COMMAND = config.getString("brewery.commands.meng-po-warp", "warp 元素祭坛 %player%");
        BREWERY_WINE_REWARD_COMMAND = config.getString("brewery.commands.wine-reward", "si give 23中秋巡礼-风滚草镇 1 %player%");
        BREWERY_WINE_LORE_PATTERN = config.getString("brewery.wine-lore-pattern");
        BREWERY_ENABLED = config.getBoolean("integrations.brewery.enabled", true);
        PROJECTKORRA_ENABLED = config.getBoolean("integrations.projectkorra.enabled", true);
        SEASONS_ENABLED = config.getBoolean("integrations.seasons.enabled", true);
        SEASONS_WORLD = config.getString("integrations.seasons.world", "world");
        MAGIC_STICK_COOLDOWN_MILLIS = config.getLong("admin.magic-stick.cooldown-millis", 120000L);
        MAGIC_STICK_USES = config.getInt("admin.magic-stick.uses", 10);
        TARGET_THRESHOLD = config.getDouble("admin.target-threshold", 1.0D);
        TARGET_RANGE = config.getDouble("admin.target-range", 50.0D);
        RANDOM_EFFECT_DURATION_TICKS = config.getInt("admin.random-effect-duration-ticks", 100);
        RANDOM_EFFECT_AMPLIFIER = config.getInt("admin.random-effect-amplifier", 1);
        LEVITATION_DURATION_TICKS = config.getInt("admin.levitation-duration-ticks", 60);
        LEVITATION_AMPLIFIER = config.getInt("admin.levitation-amplifier", 1);
        SONIC_BOOM_VELOCITY = config.getDouble("admin.sonic-boom.velocity", 2.0D);
        SONIC_BOOM_PARTICLE_COUNT = config.getInt("admin.sonic-boom.particle-count", 3);
        SONIC_BOOM_SOUND_VOLUME = (float) config.getDouble("admin.sonic-boom.sound-volume", 1.0D);
        SONIC_BOOM_SOUND_PITCH = (float) config.getDouble("admin.sonic-boom.sound-pitch", 1.0D);
        SONIC_BOOM_INTERVAL_TICKS = config.getLong("admin.sonic-boom.interval-ticks", 5L);
        SONIC_BOOM_STEPS = config.getInt("admin.sonic-boom.steps", 4);
        SNOWBALL_ENABLED = config.getBoolean("snowball.enabled", true);
        SNOWBALL_VELOCITY_MULTIPLIER = config.getDouble("snowball.velocity-multiplier", 2.0D);
        SNOWBALL_DAMAGE = config.getDouble("snowball.damage", 3.0D);
        ANTI_RAID_FARM_ENABLED = config.getBoolean("anti-raid-farm.enabled", true);
        ANTI_RAID_FARM_COOLDOWN_SECONDS = config.getLong("anti-raid-farm.cooldown-seconds", 180L);
        ANTI_RAID_FARM_BYPASS_PERMISSION = config.getString("anti-raid-farm.bypass-permission", "hammercore.antiraidfarm.bypass");
        VILLAGER_RESTORE_DEFAULT_RADIUS = config.getInt("villager-restore.default-radius", 50);
        VILLAGER_RESTORE_MAX_RADIUS = config.getInt("villager-restore.max-radius", 50);

        World world = Bukkit.getWorld(Objects.requireNonNull(config.getString("util.auto-respawn.location.world")));
        UTIL_AUTO_RESPAWN_LOCATION = new Location(world,
                config.getDouble("util.auto-respawn.location.x"),
                config.getDouble("util.auto-respawn.location.y"),
                config.getDouble("util.auto-respawn.location.z"),
                (float) config.getDouble("util.auto-respawn.location.yaw"),
                (float) config.getDouble("util.auto-respawn.location.pitch"));
    }

    private void migrateLegacyConfig() {
        // Snapshot the keys actually present in the file before defaults are merged.
        var existingKeys = new java.util.HashSet<>(config.getKeys(true));
        boolean changed = false;

        // v3/v4 -> v5: normalize legacy list/boolean shapes.
        if (config.isList("world-protection.anti-explosion")) {
            var worlds = config.getStringList("world-protection.anti-explosion");
            config.set("world-protection.anti-explosion", null);
            config.set("world-protection.anti-explosion.enabled", true);
            config.set("world-protection.anti-explosion.only-creepers", true);
            config.set("world-protection.anti-explosion.worlds", worlds);
            changed = true;
        }
        if (config.isList("world-protection.anti-griefing")) {
            var worlds = config.getStringList("world-protection.anti-griefing");
            config.set("world-protection.anti-griefing", null);
            config.set("world-protection.anti-griefing.enabled", true);
            config.set("world-protection.anti-griefing.worlds", worlds);
            changed = true;
        }
        if (config.isBoolean("util.peeling-recovery")) {
            boolean enabled = config.getBoolean("util.peeling-recovery");
            config.set("util.peeling-recovery", null);
            config.set("util.peeling-recovery.enabled", enabled);
            config.set("util.peeling-recovery.retention-millis", 300000L);
            config.set("util.peeling-recovery.cleanup-interval-ticks", 12000);
            changed = true;
        }
        if (config.isBoolean("util.restrict-portal-range")) {
            boolean enabled = config.getBoolean("util.restrict-portal-range");
            config.set("util.restrict-portal-range", null);
            config.set("util.restrict-portal-range.enabled", enabled);
            config.set("util.restrict-portal-range.nether-world", "world_nether");
            config.set("util.restrict-portal-range.min-x", -1000);
            config.set("util.restrict-portal-range.max-x", 1000);
            config.set("util.restrict-portal-range.min-y", -64);
            config.set("util.restrict-portal-range.max-y", 320);
            config.set("util.restrict-portal-range.min-z", -1000);
            config.set("util.restrict-portal-range.max-z", 1000);
            changed = true;
        }

        // v5: Spartan support removed.
        if (config.contains("floodgate.bypass-spartan-for-bedrock")) {
            config.set("floodgate.bypass-spartan-for-bedrock", null);
            changed = true;
        }

        // v6: item-frame locking moved to CodFrame, which also imports the legacy
        // hammercore:owneruuid / hammercore:ownerusername frame data on load.
        if (config.contains("item-frame-lock")) {
            config.set("item-frame-lock", null);
            changed = true;
        }

        // v5: player-visible messages moved from config.yml#message into messages.yml.
        if (MessageManager.migrateLegacyMessages(config)) {
            changed = true;
        }

        // v7: port anti raid farm (anti-raid-farm.*) and AntiVillagerLag's
        // villager restore command (villager-restore.*).
        int version = config.getInt("settings.version", 0);
        if (version < 7) {
            config.set("settings.version", 7);
            changed = true;
        }

        // Merge any keys that are missing from an older config.yml so the file
        // is fully upgraded (values already in the file always win).
        config.options().copyDefaults(true);
        boolean defaultsAdded = !new java.util.HashSet<>(config.getKeys(true)).equals(existingKeys);
        if (changed || defaultsAdded) {
            HammerCore.getInstance().saveConfig();
        }
    }

    public static long magicStickCooldownMillis() { return MAGIC_STICK_COOLDOWN_MILLIS; }
    public static int magicStickUses() { return MAGIC_STICK_USES; }

    private Material material(String path, Material fallback) {
        Material value = Material.matchMaterial(config.getString(path, fallback.name()));
        return value == null ? fallback : value;
    }
}

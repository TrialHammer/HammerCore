package ink.ziip.hammer.hammercore.manager;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import ink.ziip.hammer.hammercore.api.manager.BaseManager;
import ink.ziip.hammer.hammercore.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

/**
 * Registers listeners based on switches and installed optional plugins.
 * Optional-plugin listener classes are only referenced inside guards so their
 * classes (and transitive plugin APIs) are never loaded when those plugins are absent.
 */
public class ListenerManager extends BaseManager {

    @Override
    public void load() {
        register(new JoinQuitListener());

        if (ConfigManager.INFO_BAR_ENABLED) {
            register(new BossBarTaskListener());
        }
        if (ConfigManager.MINECART_ENABLED) {
            register(new MineCartListener());
        }

        register(new ProtectionListener());

        if (ConfigManager.UTIL_RESTRICT_PORTAL_RANGE) {
            register(new PortalListener());
        }

        if (ConfigManager.BREWERY_ENABLED
                && Bukkit.getPluginManager().getPlugin("BreweryX") != null
                && Bukkit.getPluginManager().getPlugin("ProjectKorra") != null) {
            register(new BreweryListener());
        }

        if (ConfigManager.PROJECTKORRA_ENABLED
                && Bukkit.getPluginManager().getPlugin("ProjectKorra") != null
                && ConfigManager.PROJECTKORRA_ALL_EARTH_BINDER) {
            register(new ProjectKorraListener());
        }

        if (ConfigManager.UTIL_PEELING_RECOVERY) {
            register(new PeelingRecoveryListener());
        }

        register(new QuickActionListener());

        if (ConfigManager.SNOWBALL_ENABLED) {
            register(new SnowballListener());
        }
        if (ConfigManager.SEASONS_ENABLED
                && Bukkit.getPluginManager().getPlugin("RealisticSeasons") != null
                && Bukkit.getPluginManager().getPlugin("Seasons") != null) {
            register(new SeasonListener());
        }
    }

    private void register(BaseListener listener) {
        listener.register();
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(HammerCore.getInstance());
    }

    @Override
    public void reload() {
        unload();
        load();
    }
}

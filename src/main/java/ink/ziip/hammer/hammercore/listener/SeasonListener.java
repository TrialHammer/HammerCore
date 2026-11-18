package ink.ziip.hammer.hammercore.listener;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Method;

/**
 * Bridges RealisticSeasons season changes into the Seasons API when both
 * plugins are installed. Reflection keeps HammerCore free of hard compile-time
 * dependencies on either optional plugin.
 */
public class SeasonListener extends BaseListener {

    @Override
    @SuppressWarnings("unchecked")
    public void register() {
        try {
            Class<?> eventClass = Class.forName("me.casperge.realisticseasons.api.SeasonChangeEvent");
            EventExecutor executor = (listener, event) -> {
                try {
                    Method getNewSeason = event.getClass().getMethod("getNewSeason");
                    Object season = getNewSeason.invoke(event);
                    if (season != null) applySeason(season.toString());
                } catch (ReflectiveOperationException exception) {
                    HammerCore.getInstance().getLogger().warning("Could not synchronise RealisticSeasons change: " + exception.getMessage());
                }
            };
            Bukkit.getPluginManager().registerEvent((Class<? extends Event>) eventClass, this,
                    EventPriority.MONITOR, executor, HammerCore.getInstance());
        } catch (ClassNotFoundException exception) {
            HammerCore.getInstance().getLogger().warning("RealisticSeasons API is not available; season sync is disabled.");
        }
    }

    private void applySeason(String seasonName) {
        try {
            Object seasons = Class.forName("uk.co.harieo.seasons.plugin.Seasons")
                    .getMethod("getInstance").invoke(null);
            World world = Bukkit.getWorld(ink.ziip.hammer.hammercore.manager.ConfigManager.SEASONS_WORLD);
            if (world == null) return;
            Object cycle = seasons.getClass().getMethod("getWorldCycle", World.class).invoke(seasons, world);
            Class<?> seasonClass = Class.forName("uk.co.harieo.seasons.plugin.models.Season");
            String mapped = switch (seasonName.toUpperCase()) {
                case "SPRING" -> "SPRING";
                case "SUMMER" -> "SUMMER";
                case "FALL", "AUTUMN" -> "AUTUMN";
                case "WINTER" -> "WINTER";
                default -> seasonName.toUpperCase();
            };
            Object target = Enum.valueOf((Class<? extends Enum>) seasonClass.asSubclass(Enum.class), mapped);
            cycle.getClass().getMethod("setSeason", seasonClass).invoke(cycle, target);
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            HammerCore.getInstance().getLogger().warning("Could not apply Seasons season: " + exception.getMessage());
        }
    }
}

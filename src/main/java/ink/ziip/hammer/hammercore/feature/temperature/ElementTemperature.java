package ink.ziip.hammer.hammercore.feature.temperature;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.listener.BaseListener;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public final class ElementTemperature extends BaseListener {
    private record Correction(Object effect, int modifier, int reading, long appliedAt, String element, String season) {}
    public record Reading(String player, String element, String season, int temperature, int modifier) {}

    private final HammerCore core;
    private final Plugin seasons;
    private final Plugin korra;
    private final ConfigurationSection config;
    private final int interval;
    private final List<String> priority;
    private final Map<UUID, Correction> corrections = new HashMap<>();
    private BukkitTask ticker;
    private SeasonsTemperature api;
    private TemperatureControl bounds;
    private boolean warned;
    private long elapsed;

    public ElementTemperature(HammerCore core, Plugin seasons, Plugin korra, ConfigurationSection config) {
        this.core = core;
        this.seasons = seasons;
        this.korra = korra;
        this.config = config;
        interval = Math.max(5, config.getInt("check-interval-ticks", 20));
        List<String> configured = config.getStringList("priority");
        priority = configured.isEmpty() ? List.of("fire", "water")
                : configured.stream().map(key -> key.toLowerCase(Locale.ROOT)).toList();
        bounds = limits(null, null);
    }

    public void start() {
        register();
        ticker = core.getServer().getScheduler().runTaskTimer(core, this::tick, interval, interval);
    }

    public void stop() {
        unRegister();
        if (ticker != null) ticker.cancel();
        ticker = null;
        clear();
        api = null;
    }

    private void tick() {
        elapsed += interval;
        if (!seasons.isEnabled() || !korra.isEnabled()) {
            clear();
            api = null;
            bounds = limits(null, null);
            return;
        }
        if (api == null) {
            try {
                // RealisticSeasons initializes its temperature system after plugin enable.
                api = SeasonsTemperature.connect(seasons);
                if (api == null) return;
                bounds = limits(api.cold(), api.hot());
                if (bounds == null) {
                    core.getLogger().warning("Element temperature has no safe target range. Check element-temperature.safety.");
                } else {
                    core.getLogger().info("Element temperature active (fire " + target("fire") + ", water " + target("water") + ").");
                }
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                warn("Could not initialize RealisticSeasons temperature API", e);
                if (ticker != null) ticker.cancel();
                ticker = null;
                return;
            }
        }
        if (bounds == null) return;

        for (Player player : core.getServer().getOnlinePlayers()) {
            try {
                update(player);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                remove(player.getUniqueId());
                warn("Could not update element temperature", e);
            }
        }
    }

    private void update(Player player) throws ReflectiveOperationException {
        UUID id = player.getUniqueId();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR
                || (config.getBoolean("respect-exempt-permission", true)
                && player.hasPermission("elementtemperature.exempt"))) {
            remove(id);
            return;
        }
        String element = element(player);
        String season = element == null ? null : api.activeSeason(player.getWorld());
        if (season == null) {
            remove(id);
            return;
        }

        int current = api.read(player);
        Correction existing = corrections.get(id);
        int previous = existing == null ? 0 : existing.modifier();
        int target = target(element);
        int next = TemperatureControl.adjust(target, current, previous,
                existing == null ? current : existing.reading(),
                existing == null ? Long.MAX_VALUE : elapsed - existing.appliedAt());
        if (previous == next) {
            if (existing != null && (!existing.element().equals(element) || !existing.season().equals(season))) {
                corrections.put(id, new Correction(existing.effect(), previous,
                        existing.reading(), existing.appliedAt(), element, season));
            }
            return;
        }

        if (existing != null) {
            if (existing.effect() != null) api.cancel(existing.effect());
            corrections.remove(id);
        }
        Object effect = next == 0 ? null : api.apply(player, next);
        if (next == 0 || effect != null) {
            corrections.put(id, new Correction(effect, next, current, elapsed, element, season));
        }
        if (config.getBoolean("debug")) {
            core.getLogger().info("Element temperature " + player.getName() + " (" + element + ", " + season
                    + "): " + current + " -> " + target + " (modifier " + next + ")");
        }
    }

    private String element(Player player) {
        BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
        if (bender == null) return null;
        for (String key : priority) {
            if ((key.equals("fire") && bender.hasElement(Element.FIRE))
                    || (key.equals("water") && bender.hasElement(Element.WATER))) return key;
        }
        return null;
    }

    private TemperatureControl limits(Integer cold, Integer hot) {
        return TemperatureControl.limits(cold, hot, config.getInt("safety.margin", 6),
                config.getInt("safety.fallback-min", -8), config.getInt("safety.fallback-max", 60));
    }

    public boolean isReady() {
        return api != null && bounds != null && seasons.isEnabled() && korra.isEnabled();
    }

    public boolean isWaiting() {
        return ticker != null && api == null;
    }

    public int interval() {
        return interval;
    }

    public Integer minimum() {
        return bounds == null ? null : bounds.minimum();
    }

    public Integer maximum() {
        return bounds == null ? null : bounds.maximum();
    }

    public int target(String element) {
        int configured = config.getInt("targets." + element, element.equals("fire") ? 26 : 14);
        return bounds == null ? configured : bounds.target(configured);
    }

    public int controlledCount() {
        return corrections.size();
    }

    public List<Reading> readings() {
        if (!isReady()) return List.of();
        return corrections.entrySet().stream().limit(15).map(entry -> {
            Player player = core.getServer().getPlayer(entry.getKey());
            Correction correction = entry.getValue();
            if (player == null) return null;
            int reading = correction.reading();
            try {
                reading = api.read(player);
            } catch (ReflectiveOperationException e) {
                warn("Could not read element temperature", e);
            }
            return new Reading(player.getName(), correction.element(), correction.season(), reading, correction.modifier());
        }).filter(Objects::nonNull).toList();
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        remove(event.getPlayer().getUniqueId());
    }

    private void remove(UUID id) {
        Correction correction = corrections.remove(id);
        if (correction == null || correction.effect() == null) return;
        try {
            api.cancel(correction.effect());
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            warn("Could not remove element temperature effect", e);
        }
    }

    private void clear() {
        for (UUID id : List.copyOf(corrections.keySet())) remove(id);
    }

    private void warn(String message, Throwable error) {
        if (!warned) core.getLogger().log(Level.WARNING, message, error);
        warned = true;
    }
}

package ink.ziip.hammer.hammercore.feature.temperature;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/** RealisticSeasons is optional and does not have to be present on the build classpath. */
record SeasonsTemperature(Object api, Object tempData, Method enabledWorld, Method season,
                          Method temperature, Method apply, Method cancel, Integer cold, Integer hot) {

    static SeasonsTemperature connect(Plugin plugin) throws ReflectiveOperationException {
        Object manager = plugin.getClass().getMethod("getTemperatureManager").invoke(plugin);
        if (manager == null) return null;
        Object data = manager.getClass().getMethod("getTempData").invoke(manager);
        if (data == null) return null;

        Class<?> apiClass = Class.forName("me.casperge.realisticseasons.api.SeasonsAPI", true,
                plugin.getClass().getClassLoader());
        Object api = apiClass.getMethod("getInstance").invoke(null);
        if (api == null) return null;
        Method apply = apiClass.getMethod("applyPermanentTemperatureEffect", Player.class, int.class);

        Object settings;
        try {
            settings = data.getClass().getMethod("getTempSettings").invoke(data);
        } catch (ReflectiveOperationException ignored) {
            settings = null;
        }
        Integer cold = thresholds(settings, "getColdHungerTemp", "getColdSlownessTemp", "getColdFreezingTemp")
                .max(Integer::compare).orElse(null);
        Integer hot = thresholds(settings, "getWarmNoHealingTemp", "getWarmNoHealTemp", "getWarmSlownessTemp", "getWarmFireTemp")
                .min(Integer::compare).orElse(null);
        return new SeasonsTemperature(api, data, data.getClass().getMethod("isEnabledWorld", World.class),
                apiClass.getMethod("getSeason", World.class), apiClass.getMethod("getTemperature", Player.class),
                apply, apply.getReturnType().getMethod("cancel"), cold, hot);
    }

    String activeSeason(World world) throws ReflectiveOperationException {
        if (!Boolean.TRUE.equals(enabledWorld.invoke(tempData, world))) return null;
        Object result = season.invoke(api, world);
        if (!(result instanceof Enum<?> activeSeason) || activeSeason.name().equals("DISABLED")) return null;
        return activeSeason.name();
    }

    int read(Player player) throws ReflectiveOperationException {
        return (int) temperature.invoke(api, player);
    }

    Object apply(Player player, int modifier) throws ReflectiveOperationException {
        return apply.invoke(api, player, modifier);
    }

    void cancel(Object effect) throws ReflectiveOperationException {
        cancel.invoke(effect);
    }

    private static Stream<Integer> thresholds(Object settings, String... getters) {
        if (settings == null) return Stream.empty();
        return Arrays.stream(getters).map(getter -> {
            try {
                Object value = settings.getClass().getMethod(getter).invoke(settings);
                return value instanceof Integer number ? number : null;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }).filter(Objects::nonNull);
    }
}

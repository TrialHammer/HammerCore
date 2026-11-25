package ink.ziip.hammer.hammercore.manager;

import ink.ziip.hammer.hammercore.HammerCore;
import ink.ziip.hammer.hammercore.api.manager.BaseManager;
import ink.ziip.hammer.hammercore.feature.cauldron.CauldronAutomation;
import ink.ziip.hammer.hammercore.feature.temperature.ElementTemperature;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public final class FeatureManager extends BaseManager {
    private final HammerCore core;
    private CauldronAutomation cauldron;
    private ElementTemperature temperature;

    public FeatureManager(HammerCore core) {
        this.core = core;
    }

    @Override
    public void load() {
        if (availability("cauldron-interact") == null) {
            try {
                cauldron = new CauldronAutomation(core, plugin("BreweryX"), config("cauldron-interact"));
                cauldron.start();
            } catch (RuntimeException | LinkageError e) {
                if (cauldron != null) cauldron.stop();
                cauldron = null;
                core.getLogger().log(Level.WARNING, "Could not start cauldron automation", e);
            }
        }
        if (availability("element-temperature") == null) {
            try {
                temperature = new ElementTemperature(core, plugin("RealisticSeasons"), plugin("ProjectKorra"),
                        config("element-temperature"));
                temperature.start();
            } catch (RuntimeException | LinkageError e) {
                if (temperature != null) temperature.stop();
                temperature = null;
                core.getLogger().log(Level.WARNING, "Could not start element temperature", e);
            }
        }
    }

    @Override
    public void unload() {
        if (temperature != null) temperature.stop();
        if (cauldron != null) cauldron.stop();
        temperature = null;
        cauldron = null;
    }

    @Override
    public void reload() {
        unload();
        load();
    }

    public ConfigurationSection config(String feature) {
        return core.getConfig().getConfigurationSection(feature);
    }

    public String state(String feature) {
        String unavailable = availability(feature);
        if (unavailable != null) return unavailable;
        if (feature.equals("cauldron-interact")) return cauldron != null ? "active" : "unavailable";
        if (temperature == null) return "unavailable";
        return temperature.isReady() ? "active" : temperature.isWaiting() ? "waiting" : "unavailable";
    }

    public boolean isDistillationActive() {
        return cauldron != null && cauldron.isDistillationActive();
    }

    public ElementTemperature temperature() {
        return temperature;
    }

    private String availability(String feature) {
        if (!core.getConfig().getBoolean("settings.enabled", true)
                || !core.getConfig().getBoolean(feature + ".enabled", true)) return "disabled";
        if (feature.equals("cauldron-interact")) {
            if (plugin("CauldronInteract") != null) return "standalone";
            if (plugin("BreweryX") == null) return "dependency";
        } else {
            if (plugin("ElementTemperature") != null) return "standalone";
            if (plugin("RealisticSeasons") == null || plugin("ProjectKorra") == null) return "dependency";
        }
        return null;
    }

    private Plugin plugin(String name) {
        Plugin plugin = core.getServer().getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled() ? plugin : null;
    }
}

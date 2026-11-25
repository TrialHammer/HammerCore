package me.casperge.realisticseasons.api;

import org.bukkit.World;
import org.bukkit.entity.Player;

/** Test fixture for the optional API's reflective boundary. */
public final class SeasonsAPI {
    public enum Season { SUMMER, DISABLED }

    public static final class Effect {
        public boolean cancelled;

        public void cancel() {
            cancelled = true;
        }
    }

    private static final SeasonsAPI INSTANCE = new SeasonsAPI();
    public Season season = Season.SUMMER;
    public int observed = 10;
    public int applied;
    public Effect lastEffect;

    public static SeasonsAPI getInstance() {
        return INSTANCE;
    }

    public Season getSeason(World world) {
        return season;
    }

    public int getTemperature(Player player) {
        return observed;
    }

    public Effect applyPermanentTemperatureEffect(Player player, int modifier) {
        applied = modifier;
        lastEffect = new Effect();
        return lastEffect;
    }
}

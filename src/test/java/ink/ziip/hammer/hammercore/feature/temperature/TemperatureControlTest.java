package ink.ziip.hammer.hammercore.feature.temperature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureControlTest {
    @Test
    void clampsTargetsUsingTheConfiguredPenaltyThresholds() {
        var bounds = TemperatureControl.limits(-14, 60, 6, -8, 60);
        assertEquals(-8, bounds.target(-100));
        assertEquals(54, bounds.target(100));
        assertEquals(26, bounds.target(26));
        assertEquals(14, bounds.target(14));
    }

    @Test
    void usesFallbacksWhenNoThresholdsAreAvailableButDoesNotInvertUnsafeRanges() {
        assertEquals(new TemperatureControl(-10, 50), TemperatureControl.limits(null, null, 6, -10, 50));
        assertNull(TemperatureControl.limits(10, 20, 6, -8, 60));
    }

    @Test
    void avoidsReapplyingTheSameReadingBeforeRealisticSeasonsUpdates() {
        assertEquals(36, TemperatureControl.adjust(26, -10, 0, -10, 40));
        assertEquals(36, TemperatureControl.adjust(26, -10, 36, -10, 20));
        assertEquals(37, TemperatureControl.adjust(26, 24, 35, -10, 20));
        assertEquals(36, TemperatureControl.adjust(26, 25, 36, 24, 40));
        assertEquals(300, TemperatureControl.adjust(26, Integer.MIN_VALUE, 36, 0, 40));
    }
}

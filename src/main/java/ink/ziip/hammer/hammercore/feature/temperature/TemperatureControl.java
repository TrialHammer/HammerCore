package ink.ziip.hammer.hammercore.feature.temperature;

record TemperatureControl(int minimum, int maximum) {
    private static final int MAX_MODIFIER = 300;
    private static final int DEFAULT_READING_INTERVAL = 40;

    static TemperatureControl limits(Integer cold, Integer hot, int margin, int fallbackMin, int fallbackMax) {
        long padding = Math.max(0, margin);
        long min = cold == null ? fallbackMin : cold + padding;
        long max = hot == null ? fallbackMax : hot - padding;
        if (min > max || min > Integer.MAX_VALUE || max < Integer.MIN_VALUE) return null;
        return new TemperatureControl((int) min, (int) max);
    }

    int target(int requested) {
        return Math.clamp(requested, minimum, maximum);
    }

    static int adjust(int target, int current, int previous, int previousReading, long ticksSinceChange) {
        long error = (long) target - current;
        if (Math.abs(error) <= 1 || (current == previousReading && ticksSinceChange < DEFAULT_READING_INTERVAL)) {
            return previous;
        }
        return Math.clamp(previous + error, -MAX_MODIFIER, MAX_MODIFIER);
    }
}

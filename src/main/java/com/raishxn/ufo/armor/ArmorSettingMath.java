package com.raishxn.ufo.armor;

/**
 * Range and step arithmetic for an armor module setting, kept free of Minecraft types so the
 * server's clamp can be unit tested without a running game.
 *
 * <p>A setting value arrives from a client packet as a {@code VAR_INT}, so the clamp has to hold for
 * the whole range of {@code int}, not only for values that look plausible.
 */
public final class ArmorSettingMath {
    private ArmorSettingMath() {}

    /**
     * Snaps {@code value} into {@code [min, max]} on the {@code step} grid.
     *
     * @throws IllegalArgumentException if the range is empty, the step is not positive, or the range
     *     is not a whole number of steps
     */
    public static int clamp(int min, int max, int step, int value) {
        if (step <= 0) throw new IllegalArgumentException("step must be positive");
        if (min > max) throw new IllegalArgumentException("min must not exceed max");
        if ((max - min) % step != 0) throw new IllegalArgumentException("range must be a whole number of steps");
        // Bound before snapping. Subtracting min directly from a value at the edge of the wire type
        // overflows, and an overflowed intermediate used to flip the result to the opposite end of
        // the range - which handed a crafted packet the maximum instead of the minimum.
        int bounded = Math.max(min, Math.min(max, value));
        int snapped = min + Math.round((bounded - min) / (float) step) * step;
        return Math.max(min, Math.min(max, snapped));
    }
}

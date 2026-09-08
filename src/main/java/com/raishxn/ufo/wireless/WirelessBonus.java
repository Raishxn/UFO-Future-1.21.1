package com.raishxn.ufo.wireless;

/** Immutable per-job factors. Independent of Minecraft for arithmetic regression tests. */
public record WirelessBonus(double speed, double energy, double heat) {
    public static final WirelessBonus NONE = new WirelessBonus(1, 1, 1);
    public WirelessBonus {
        if (!Double.isFinite(speed) || !Double.isFinite(energy) || !Double.isFinite(heat)
                || speed < 1 || speed > 11 || energy <= 0 || energy > 1 || heat <= 0 || heat > 1) {
            throw new IllegalArgumentException("Invalid wireless factors");
        }
    }
    public int duration(int ticks) { return Math.max(1, (int) Math.ceil(ticks / speed)); }
    /** Do not erase available bonuses between jobs, or misreport a running job's locked factors. */
    public static WirelessBonus display(boolean hasJob, WirelessBonus applied,
            java.util.function.Supplier<WirelessBonus> available) {
        return hasJob ? applied : available.get();
    }
    public static WirelessBonus automatic(int machines, int saturation, double maxSpeed, double maxEnergy, double maxHeat) {
        double scale = Math.clamp((machines - 1.0) / Math.max(1, saturation - 1), 0, 1);
        return new WirelessBonus(1 + maxSpeed * scale, 1 - maxEnergy * scale, 1 - maxHeat * scale);
    }
    public long energyCost(long cost) { return cost == 0 ? 0 : Math.max(1, (long) Math.ceil(cost * energy)); }
    public double heatPerTick(int originalTicks, int adjustedTicks) {
        return (double) originalTicks / Math.max(1, adjustedTicks) * heat;
    }
}

package com.raishxn.ufo.block.entity.processing;

/**
 * Integer-only energy charging and cost calculations for the Stellar Nexus.
 */
public final class StellarEnergyMath {
    private StellarEnergyMath() {
    }

    public static long chargeRequest(long stored, long capacity, long ratePerTick) {
        if (stored < 0L || capacity <= stored || ratePerTick <= 0L) {
            return 0L;
        }
        return Math.min(capacity - stored, ratePerTick);
    }

    public static long effectiveCost(long baseCost, boolean safeMode, boolean overclocked) {
        if (baseCost <= 0L) {
            return 0L;
        }

        long multiplier = safeMode ? 2L : 1L;
        if (overclocked) {
            multiplier = saturatedMultiply(multiplier, 8L);
        }
        return saturatedMultiply(baseCost, multiplier);
    }

    /** Returns the number of ticks needed to reach {@code target}, or -1 if impossible. */
    public static long ticksToReach(long stored, long target, long acceptedPerTick) {
        if (target <= stored) {
            return 0L;
        }
        if (acceptedPerTick <= 0L) {
            return -1L;
        }
        long missing = target - Math.max(0L, stored);
        return ceilDiv(missing, acceptedPerTick);
    }

    private static long ceilDiv(long numerator, long denominator) {
        long quotient = numerator / denominator;
        return quotient + (numerator % denominator == 0L ? 0L : 1L);
    }

    private static long saturatedMultiply(long left, long right) {
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }
}

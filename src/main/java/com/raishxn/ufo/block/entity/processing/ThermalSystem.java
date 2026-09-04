package com.raishxn.ufo.block.entity.processing;

/**
 * Shared integer-only thermal calculations.
 *
 * <p>A coolant removes {@code heatNumerator / millibucketDenominator}
 * heat units per mB. The rational representation supports both strong coolants
 * (for example 100 heat/mB) and weak coolants (1 heat/120 mB) without floating
 * point rounding.</p>
 */
public final class ThermalSystem {
    private static final CoolingPlan EMPTY_PLAN = new CoolingPlan(0L, 0L);

    private ThermalSystem() {
    }

    public record CoolantProfile(
            long heatNumerator,
            long millibucketDenominator,
            long maxFlowPerTick) {
        public boolean isUsable() {
            return this.heatNumerator > 0L
                    && this.millibucketDenominator > 0L
                    && this.maxFlowPerTick > 0L;
        }
    }

    public record CoolingPlan(long requestedMillibuckets, long heatRemoved) {
    }

    /**
     * Plans the smallest useful extraction for the current heat and available
     * coolant, capped by the profile's per-tick flow.
     */
    public static CoolingPlan planCooling(
            long currentHeat,
            long availableMillibuckets,
            CoolantProfile profile) {
        if (currentHeat <= 0L || availableMillibuckets <= 0L || profile == null || !profile.isUsable()) {
            return EMPTY_PLAN;
        }

        long availableThisTick = Math.min(availableMillibuckets, profile.maxFlowPerTick());
        long possibleCooling = coolingFromExtracted(availableThisTick, profile);
        long desiredCooling = Math.min(currentHeat, possibleCooling);
        if (desiredCooling <= 0L) {
            return EMPTY_PLAN;
        }

        long scaledHeat = saturatedMultiply(desiredCooling, profile.millibucketDenominator());
        long requested = Math.min(availableThisTick, ceilDiv(scaledHeat, profile.heatNumerator()));
        long removed = Math.min(currentHeat, coolingFromExtracted(requested, profile));
        return removed > 0L ? new CoolingPlan(requested, removed) : EMPTY_PLAN;
    }

    /** Calculates cooling from the amount actually committed by a port. */
    public static long coolingFromExtracted(long extractedMillibuckets, CoolantProfile profile) {
        if (extractedMillibuckets <= 0L || profile == null || !profile.isUsable()) {
            return 0L;
        }
        return saturatedMultiply(extractedMillibuckets, profile.heatNumerator())
                / profile.millibucketDenominator();
    }

    /** Applies non-negative passive dissipation without underflow. */
    public static long dissipate(long currentHeat, long heatToRemove) {
        if (currentHeat <= 0L) {
            return 0L;
        }
        if (heatToRemove <= 0L) {
            return currentHeat;
        }
        return heatToRemove >= currentHeat ? 0L : currentHeat - heatToRemove;
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

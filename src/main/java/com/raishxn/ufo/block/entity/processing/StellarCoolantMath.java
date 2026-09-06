package com.raishxn.ufo.block.entity.processing;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Integer-only coolant calculations for the Stellar Nexus.
 */
public final class StellarCoolantMath {
    private StellarCoolantMath() {
    }

    /**
     * Calculates heat removed by an amount that was actually extracted.
     */
    public static long coolingFromExtracted(
            long extracted,
            long efficiency,
            long tierMultiplier,
            long targetFlow) {
        return ThermalSystem.coolingFromExtracted(
                extracted, profile(efficiency, tierMultiplier, targetFlow));
    }

    /**
     * Returns the smallest useful extraction for the current heat, capped at one
     * tick's target flow.
     */
    public static long amountRequiredForHeat(
            long heat,
            long efficiency,
            long tierMultiplier,
            long targetFlow) {
        return ThermalSystem.planCooling(
                heat, targetFlow, profile(efficiency, tierMultiplier, targetFlow))
                .requestedMillibuckets();
    }

    /**
     * Resolves the coolant flow target. Safe Mode and overclock penalties are
     * operation costs and therefore do not apply while the machine is idle.
     */
    public static long targetFlow(
            long baseFlow,
            boolean activeOperation,
            boolean safeMode,
            boolean overclocked) {
        if (baseFlow <= 0L) {
            return 0L;
        }
        if (!activeOperation) {
            return baseFlow;
        }

        long target = safeMode ? saturatedMultiply(baseFlow, 5L) / 2L : baseFlow;
        return overclocked ? saturatedMultiply(target, 5L) : target;
    }

    /**
     * Returns coolant indexes ordered from greatest to lowest configured
     * efficiency. Equal efficiencies preserve their declaration order.
     */
    public static int[] priorityByEfficiency(long... efficiencies) {
        if (efficiencies == null || efficiencies.length == 0) {
            return new int[0];
        }
        Integer[] indexes = IntStream.range(0, efficiencies.length).boxed().toArray(Integer[]::new);
        Arrays.sort(indexes, Comparator
                .comparingLong((Integer index) -> efficiencies[index]).reversed()
                .thenComparingInt(Integer::intValue));
        return Arrays.stream(indexes).mapToInt(Integer::intValue).toArray();
    }

    public static ThermalSystem.CoolantProfile profile(
            long efficiency, long tierMultiplier, long targetFlow) {
        if (efficiency <= 0L || tierMultiplier <= 0L || targetFlow <= 0L) {
            return new ThermalSystem.CoolantProfile(0L, 0L, 0L);
        }
        long numerator = efficiency > Long.MAX_VALUE / tierMultiplier
                ? Long.MAX_VALUE : efficiency * tierMultiplier;
        return new ThermalSystem.CoolantProfile(numerator, targetFlow, targetFlow);
    }

    private static long saturatedMultiply(long left, long right) {
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }
}

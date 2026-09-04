package com.raishxn.ufo.block.entity.processing;

import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Keeps the amount promised to an AE2 crafting job deterministic while
 * preserving catalyst bonuses as separately accounted byproducts.
 */
public final class AutocraftingOutputPolicy {
    public static final int LEGACY_UNVERSIONED = 0;
    public static final int DETERMINISTIC_BASE = 1;

    private AutocraftingOutputPolicy() {
    }

    public static boolean matchesDeterministicPromise(long patternAmount, long baseAmount) {
        return baseAmount > 0L && patternAmount == baseAmount;
    }

    public static OutputPlan plan(long baseAmount, double bonusChance, DoubleSupplier random) {
        if (baseAmount <= 0L) {
            return OutputPlan.EMPTY;
        }

        double sanitizedChance = sanitizeChance(bonusChance);
        long bonusRolls = (long) sanitizedChance;
        double fractionalRollChance = sanitizedChance - bonusRolls;
        if (fractionalRollChance > 0.0D) {
            double roll = Objects.requireNonNull(random, "random").getAsDouble();
            if (Double.isFinite(roll) && roll >= 0.0D && roll < fractionalRollChance) {
                bonusRolls = saturatedAdd(bonusRolls, 1L);
            }
        }

        return new OutputPlan(baseAmount, saturatedMultiply(baseAmount, bonusRolls));
    }

    /**
     * Completes an unversioned in-flight job with enough output to satisfy any
     * base-or-maximum pattern accepted by the legacy controller.
     */
    public static long legacySafeTotal(long baseAmount, long maximumBonusRolls) {
        if (baseAmount <= 0L) {
            return 0L;
        }
        return saturatedMultiply(baseAmount, saturatedAdd(Math.max(0L, maximumBonusRolls), 1L));
    }

    public static long maximumTotal(long baseAmount, double bonusChance) {
        if (baseAmount <= 0L) {
            return 0L;
        }
        double sanitizedChance = sanitizeChance(bonusChance);
        long maximumBonusRolls = (long) Math.ceil(sanitizedChance);
        return legacySafeTotal(baseAmount, maximumBonusRolls);
    }

    private static double sanitizeChance(double bonusChance) {
        if (!Double.isFinite(bonusChance) || bonusChance <= 0.0D) {
            return 0.0D;
        }
        return Math.min(bonusChance, Long.MAX_VALUE - 1.0D);
    }

    private static long saturatedAdd(long left, long right) {
        if (left <= 0L) {
            return Math.max(0L, right);
        }
        if (right <= 0L) {
            return left;
        }
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private static long saturatedMultiply(long value, long multiplier) {
        if (value <= 0L || multiplier <= 0L) {
            return 0L;
        }
        return value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
    }

    public record OutputPlan(long promisedAmount, long byproductAmount) {
        private static final OutputPlan EMPTY = new OutputPlan(0L, 0L);

        public long totalAmount() {
            return saturatedAdd(this.promisedAmount, this.byproductAmount);
        }
    }
}

package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StellarEnergyMathTest {
    @Test
    void chargeRequestRespectsRateAndRemainingCapacity() {
        assertEquals(2_000_000L, StellarEnergyMath.chargeRequest(1_000L, 10_000_000L, 2_000_000L));
        assertEquals(500L, StellarEnergyMath.chargeRequest(9_999_500L, 10_000_000L, 2_000_000L));
        assertEquals(0L, StellarEnergyMath.chargeRequest(10_000_000L, 10_000_000L, 2_000_000L));
    }

    @Test
    void effectiveCostUsesExactIntegerRatios() {
        assertEquals(6_500_000_000L, StellarEnergyMath.effectiveCost(6_500_000_000L, false, false));
        assertEquals(16_250_000_000L, StellarEnergyMath.effectiveCost(6_500_000_000L, true, false));
        assertEquals(65_000_000_000L, StellarEnergyMath.effectiveCost(6_500_000_000L, false, true));
        assertEquals(162_500_000_000L, StellarEnergyMath.effectiveCost(6_500_000_000L, true, true));
        assertEquals(Long.MAX_VALUE, StellarEnergyMath.effectiveCost(Long.MAX_VALUE, true, true));
    }

    @Test
    void estimatesTicksWithCeilingAndReportsImpossibleRate() {
        assertEquals(0L, StellarEnergyMath.ticksToReach(100L, 100L, 10L));
        assertEquals(3L, StellarEnergyMath.ticksToReach(0L, 21L, 10L));
        assertEquals(-1L, StellarEnergyMath.ticksToReach(0L, 21L, 0L));
    }
}

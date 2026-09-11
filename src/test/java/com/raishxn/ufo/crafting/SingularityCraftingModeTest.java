package com.raishxn.ufo.crafting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingularityCraftingModeTest {
    @Test
    void modesTradeAggregateThroughputForEnergy() {
        assertEquals(64, SingularityCraftingMode.BALANCED.routesPerTick());
        assertEquals(2, SingularityCraftingMode.BALANCED.operationCost());
        assertEquals(1.0D, SingularityCraftingMode.BALANCED.energyMultiplier());
        assertEquals(128, SingularityCraftingMode.SPEED.routesPerTick());
        assertEquals(1, SingularityCraftingMode.SPEED.operationCost());
        assertEquals(2.0D, SingularityCraftingMode.SPEED.energyMultiplier());
        assertEquals(32, SingularityCraftingMode.EFFICIENCY.routesPerTick());
        assertEquals(4, SingularityCraftingMode.EFFICIENCY.operationCost());
        assertEquals(0.5D, SingularityCraftingMode.EFFICIENCY.energyMultiplier());
        assertEquals(128, SingularityCraftingMode.ROUTE_LIMIT);
        assertEquals(32, SingularityCraftingMode.EFFICIENCY.parallelJobLimit());
        assertEquals(64, SingularityCraftingMode.BALANCED.parallelJobLimit());
        assertEquals(128, SingularityCraftingMode.SPEED.parallelJobLimit());
        assertEquals(20, SingularityCraftingMode.EFFICIENCY.autoCraftIntervalTicks());
        assertEquals(5, SingularityCraftingMode.BALANCED.autoCraftIntervalTicks());
        assertEquals(1, SingularityCraftingMode.SPEED.autoCraftIntervalTicks());
    }

    @Test
    void modeCycleAndInvalidOrdinalsAreStable() {
        assertEquals(SingularityCraftingMode.SPEED, SingularityCraftingMode.BALANCED.next());
        assertEquals(SingularityCraftingMode.EFFICIENCY, SingularityCraftingMode.SPEED.next());
        assertEquals(SingularityCraftingMode.BALANCED, SingularityCraftingMode.EFFICIENCY.next());
        assertEquals(SingularityCraftingMode.BALANCED, SingularityCraftingMode.byOrdinal(-1));
        assertEquals(SingularityCraftingMode.BALANCED, SingularityCraftingMode.byOrdinal(999));
    }

    @Test
    void uniformFieldTierLocksItsExclusiveModeAndMixesAreInvalid() {
        assertEquals(SingularityCraftingMode.EFFICIENCY,
                SingularityCraftingMode.forFieldTiers(25, 0, 0));
        assertEquals(SingularityCraftingMode.BALANCED,
                SingularityCraftingMode.forFieldTiers(0, 25, 0));
        assertEquals(SingularityCraftingMode.SPEED,
                SingularityCraftingMode.forFieldTiers(0, 0, 25));
        assertTrue(SingularityCraftingMode.hasUniformFieldTier(25, 0, 0));
        assertTrue(SingularityCraftingMode.hasUniformFieldTier(0, 25, 0));
        assertTrue(SingularityCraftingMode.hasUniformFieldTier(0, 0, 25));
        assertFalse(SingularityCraftingMode.hasUniformFieldTier(1, 12, 12));
        assertFalse(SingularityCraftingMode.hasUniformFieldTier(0, 1, 24));
        assertFalse(SingularityCraftingMode.hasUniformFieldTier(0, 0, 0));
    }

    @Test
    void fieldTiersScaleNineSlotPatternPages() {
        assertEquals(9, SingularityPatternCapacity.calculate(1, 0, 0));
        assertEquals(18, SingularityPatternCapacity.calculate(0, 1, 0));
        assertEquals(36, SingularityPatternCapacity.calculate(0, 0, 1));
        assertEquals(225, SingularityPatternCapacity.calculate(25, 0, 0));
        assertEquals(900, SingularityPatternCapacity.calculate(0, 0, 25));
        assertEquals(900, SingularityPatternCapacity.calculate(0, 0, 999));
    }
}

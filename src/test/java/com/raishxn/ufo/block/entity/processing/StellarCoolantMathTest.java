package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class StellarCoolantMathTest {
    @Test
    void appliesTierMultiplierBeforeIntegerDivision() {
        assertEquals(6L, StellarCoolantMath.coolingFromExtracted(50L, 4L, 3L, 100L));
    }

    @Test
    void requestsOnlyEnoughCoolantForCurrentHeat() {
        long required = StellarCoolantMath.amountRequiredForHeat(3L, 8L, 4L, 250L);

        assertEquals(24L, required);
        assertEquals(3L, StellarCoolantMath.coolingFromExtracted(required, 8L, 4L, 250L));
    }

    @Test
    void capsRequestedAmountAtThePerTickTarget() {
        assertEquals(1_250L, StellarCoolantMath.amountRequiredForHeat(1_000L, 1L, 2L, 1_250L));
    }

    @Test
    void rejectsInvalidInputsAndDoesNotOverflow() {
        assertEquals(0L, StellarCoolantMath.coolingFromExtracted(1L, 1L, 1L, 0L));
        assertEquals(0L, StellarCoolantMath.amountRequiredForHeat(0L, 8L, 4L, 250L));
        assertEquals(Long.MAX_VALUE, StellarCoolantMath.coolingFromExtracted(
                Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 1L));
    }

    @Test
    void appliesOperationPenaltiesOnlyWhileActive() {
        assertEquals(100L, StellarCoolantMath.targetFlow(100L, true, false, false));
        assertEquals(250L, StellarCoolantMath.targetFlow(100L, true, true, false));
        assertEquals(500L, StellarCoolantMath.targetFlow(100L, true, false, true));
        assertEquals(1_250L, StellarCoolantMath.targetFlow(100L, true, true, true));
        assertEquals(100L, StellarCoolantMath.targetFlow(100L, false, true, true));
        assertEquals(0L, StellarCoolantMath.targetFlow(0L, true, true, true));
    }

    @Test
    void prioritizesCoolantsByConfiguredEfficiency() {
        assertArrayEquals(new int[]{2, 1, 0},
                StellarCoolantMath.priorityByEfficiency(1L, 4L, 8L));
        assertArrayEquals(new int[]{0, 1, 2},
                StellarCoolantMath.priorityByEfficiency(8L, 8L, 1L));
        assertArrayEquals(new int[0], StellarCoolantMath.priorityByEfficiency());
    }
}

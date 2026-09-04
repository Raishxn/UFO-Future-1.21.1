package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftingOutputPolicyTest {
    @Test
    void acceptsOnlyTheDeterministicBasePromise() {
        assertTrue(AutocraftingOutputPolicy.matchesDeterministicPromise(8L, 8L));
        assertFalse(AutocraftingOutputPolicy.matchesDeterministicPromise(16L, 8L));
        assertFalse(AutocraftingOutputPolicy.matchesDeterministicPromise(0L, 0L));
    }

    @Test
    void keepsGuaranteedOutputSeparateFromFractionalByproduct() {
        var missed = AutocraftingOutputPolicy.plan(10L, 0.5D, () -> 0.75D);
        var hit = AutocraftingOutputPolicy.plan(10L, 0.5D, () -> 0.25D);

        assertEquals(new AutocraftingOutputPolicy.OutputPlan(10L, 0L), missed);
        assertEquals(new AutocraftingOutputPolicy.OutputPlan(10L, 10L), hit);
    }

    @Test
    void preservesGuaranteedBonusRollsAndAddsAtMostOneFractionalRoll() {
        var missed = AutocraftingOutputPolicy.plan(3L, 2.5D, () -> 0.5D);
        var hit = AutocraftingOutputPolicy.plan(3L, 2.5D, () -> Math.nextDown(0.5D));

        assertEquals(new AutocraftingOutputPolicy.OutputPlan(3L, 6L), missed);
        assertEquals(new AutocraftingOutputPolicy.OutputPlan(3L, 9L), hit);
        assertEquals(12L, hit.totalAmount());
    }

    @Test
    void computesMaximumWithoutChangingThePromisedAmount() {
        assertEquals(40L, AutocraftingOutputPolicy.maximumTotal(10L, 2.5D));
        assertEquals(10L, AutocraftingOutputPolicy.maximumTotal(10L, 0.0D));
    }

    @Test
    void legacyMigrationCoversEveryPreviouslyAcceptedPromise() {
        assertEquals(40L, AutocraftingOutputPolicy.legacySafeTotal(10L, 3L));
        assertEquals(10L, AutocraftingOutputPolicy.legacySafeTotal(10L, 0L));
    }

    @Test
    void saturatesAmountsAndRejectsInvalidInputs() {
        var saturated = AutocraftingOutputPolicy.plan(Long.MAX_VALUE, 2.5D, () -> 0.0D);

        assertEquals(Long.MAX_VALUE, saturated.promisedAmount());
        assertEquals(Long.MAX_VALUE, saturated.byproductAmount());
        assertEquals(Long.MAX_VALUE, saturated.totalAmount());
        assertEquals(new AutocraftingOutputPolicy.OutputPlan(0L, 0L),
                AutocraftingOutputPolicy.plan(-1L, Double.NaN, () -> 0.0D));
        assertEquals(5L, AutocraftingOutputPolicy.maximumTotal(5L, Double.POSITIVE_INFINITY));
    }
}

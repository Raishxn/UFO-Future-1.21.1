package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeBatchScaleTest {
    @Test
    void acceptsTheExactSubBatchEncodedInTheIntegrationWorld() {
        var scale = RecipeBatchScale.between(2250L, 73728L).orElseThrow();

        assertEquals(new RecipeBatchScale(125L, 4096L), scale);
        assertEquals(250L, scale.exact(8192L).orElseThrow());
        assertTrue(scale.matches(2250L, 73728L));
        assertFalse(scale.matches(2249L, 73728L));
    }

    @Test
    void rejectsFractionalIngredientAmountsAndRoundsEnergyUp() {
        var scale = new RecipeBatchScale(1L, 3L);

        assertTrue(scale.exact(9L).isPresent());
        assertFalse(scale.exact(10L).isPresent());
        assertEquals(4L, scale.ceil(10L));
    }

    @Test
    void avoidsOverflowWhenScalingLargeValues() {
        var scale = new RecipeBatchScale(Long.MAX_VALUE, 1L);

        assertTrue(scale.exact(2L).isEmpty());
        assertEquals(Long.MAX_VALUE, scale.ceil(2L));
    }
}

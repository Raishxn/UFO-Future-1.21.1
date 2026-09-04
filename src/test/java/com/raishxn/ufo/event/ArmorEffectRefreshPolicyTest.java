package com.raishxn.ufo.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorEffectRefreshPolicyTest {
    @Test
    void appliesImmediatelyWhenEffectIsMissingOrWeaker() {
        assertTrue(ArmorEffectRefreshPolicy.shouldRefresh(false, false, 0, 0, 1, 200));
        assertTrue(ArmorEffectRefreshPolicy.shouldRefresh(true, false, 0, 500, 1, 200));
    }

    @Test
    void refreshesOnlyWhenEqualEffectReachesThreshold() {
        assertFalse(ArmorEffectRefreshPolicy.shouldRefresh(true, false, 1, 201, 1, 200));
        assertTrue(ArmorEffectRefreshPolicy.shouldRefresh(true, false, 1, 200, 1, 200));
    }

    @Test
    void preservesStrongerAndInfiniteEffectsFromOtherSources() {
        assertFalse(ArmorEffectRefreshPolicy.shouldRefresh(true, false, 2, 20, 1, 200));
        assertFalse(ArmorEffectRefreshPolicy.shouldRefresh(true, true, 1, 0, 1, 200));
    }

    @Test
    void rejectsInvalidThresholds() {
        assertThrows(IllegalArgumentException.class,
                () -> ArmorEffectRefreshPolicy.shouldRefresh(true, false, 0, 0, -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ArmorEffectRefreshPolicy.shouldRefresh(true, false, 0, 0, 0, -1));
    }
}

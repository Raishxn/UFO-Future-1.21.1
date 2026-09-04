package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StellarExplosionPolicyTest {
    @Test
    void defaultsToLocalOnlyWhenGriefIsDisabled() {
        var policy = StellarExplosionPolicy.resolve(
                false, true, 100, 64, 4096, 1_000_000, 50_000_000L, true, true);

        assertEquals(StellarExplosionPolicy.Mode.LOCAL_ONLY, policy.mode());
        assertFalse(policy.allowsBlockGrief());
        assertEquals(0, policy.radius());
        assertFalse(policy.createLava());
        assertFalse(policy.secondaryExplosions());
    }

    @Test
    void disallowedDimensionCannotBeOverriddenByOtherSettings() {
        var policy = StellarExplosionPolicy.resolve(
                true, false, 100, 64, 4096, 1_000_000, 50_000_000L, true, true);

        assertEquals(StellarExplosionPolicy.Mode.LOCAL_ONLY, policy.mode());
    }

    @Test
    void clampsEveryDestructiveBudget() {
        var policy = StellarExplosionPolicy.resolve(
                true, true, 100, 100, 20_000, 2_000_000, Long.MAX_VALUE, true, true);

        assertTrue(policy.allowsBlockGrief());
        assertEquals(64, policy.radius());
        assertEquals(4096, policy.maxBlockChangesPerTick());
        assertEquals(1_000_000, policy.maxTotalBlockChanges());
        assertEquals(50_000_000L, policy.maxNanosPerTick());
        assertTrue(policy.createLava());
        assertTrue(policy.secondaryExplosions());
    }
}

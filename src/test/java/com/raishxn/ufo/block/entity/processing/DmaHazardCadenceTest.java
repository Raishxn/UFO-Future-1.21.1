package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DmaHazardCadenceTest {
    @Test
    void emitsEighteenUniqueRingPointsInsteadOfThirtySixDuplicatedPoints() {
        assertEquals(18, DmaHazardCadence.particlesPerEmission());

        Set<Long> offsets = new HashSet<>();
        for (int point = 0; point < DmaHazardCadence.POINTS_PER_RING; point++) {
            offsets.add(Math.round(DmaHazardCadence.angleOffset(point) * 1_000_000.0D));
        }
        assertEquals(DmaHazardCadence.POINTS_PER_RING, offsets.size());
    }

    @Test
    void particleEmissionsAreThrottledAndStaggeredByPosition() {
        assertTrue(DmaHazardCadence.shouldEmitParticles(10, 0));
        assertFalse(DmaHazardCadence.shouldEmitParticles(11, 0));
        assertTrue(DmaHazardCadence.shouldEmitParticles(7, 3));
        assertFalse(DmaHazardCadence.shouldEmitParticles(10, 3));
    }

    @Test
    void damageQueryKeepsItsOriginalOncePerSecondCadence() {
        assertTrue(DmaHazardCadence.shouldCheckDamage(0));
        assertFalse(DmaHazardCadence.shouldCheckDamage(19));
        assertTrue(DmaHazardCadence.shouldCheckDamage(20));
        assertTrue(DmaHazardCadence.shouldCheckDamage(-20));
    }

    @Test
    void rejectsRingPointsOutsideTheGeometry() {
        assertThrows(IllegalArgumentException.class, () -> DmaHazardCadence.angleOffset(-1));
        assertThrows(IllegalArgumentException.class,
                () -> DmaHazardCadence.angleOffset(DmaHazardCadence.POINTS_PER_RING));
    }
}

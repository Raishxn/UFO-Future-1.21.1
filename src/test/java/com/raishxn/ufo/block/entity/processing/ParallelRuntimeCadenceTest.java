package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelRuntimeCadenceTest {
    @Test
    void throttledClientStateIsEvaluatedOnlyEveryFiveTicks() {
        assertTrue(ParallelRuntimeCadence.shouldEvaluateClientState(100, Long.MIN_VALUE, true));
        assertFalse(ParallelRuntimeCadence.shouldEvaluateClientState(104, 100, true));
        assertTrue(ParallelRuntimeCadence.shouldEvaluateClientState(105, 100, true));
    }

    @Test
    void forcedAndClockResetEvaluationsRunImmediately() {
        assertTrue(ParallelRuntimeCadence.shouldEvaluateClientState(101, 100, false));
        assertTrue(ParallelRuntimeCadence.shouldEvaluateClientState(10, 100, true));
    }

    @Test
    void idleRuntimeDoesNotDirtyItsChunk() {
        assertFalse(ParallelRuntimeCadence.hasPersistentActivity(0, 0, -1));
        assertTrue(ParallelRuntimeCadence.hasPersistentActivity(1, 0, -1));
        assertTrue(ParallelRuntimeCadence.hasPersistentActivity(0, 1, -1));
        assertTrue(ParallelRuntimeCadence.hasPersistentActivity(0, 0, 0));
    }

    @Test
    void invalidRuntimeCountersAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ParallelRuntimeCadence.hasPersistentActivity(-1, 0, -1));
        assertThrows(IllegalArgumentException.class,
                () -> ParallelRuntimeCadence.hasPersistentActivity(0, -1, -1));
    }
}

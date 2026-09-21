package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickAccelerationLimiterTest {
    @Test
    void permitsAConfiguredBurstButCapsTheSameGameTick() {
        var limiter = new TickAccelerationLimiter();

        assertTrue(limiter.tryAcquire(1200, 4));
        assertTrue(limiter.tryAcquire(1200, 4));
        assertTrue(limiter.tryAcquire(1200, 4));
        assertTrue(limiter.tryAcquire(1200, 4));
        assertFalse(limiter.tryAcquire(1200, 4));
    }

    @Test
    void resetsTheBudgetOnTheNextGameTick() {
        var limiter = new TickAccelerationLimiter();

        assertTrue(limiter.tryAcquire(1200, 1));
        assertFalse(limiter.tryAcquire(1200, 1));
        assertTrue(limiter.tryAcquire(1201, 1));
    }
}

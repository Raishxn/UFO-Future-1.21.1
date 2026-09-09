package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DmaTickWakePolicyTest {
    @Test
    void persistedWirelessJobStartsAwakeAfterWorldReload() {
        assertTrue(DmaTickWakePolicy.hasRestoredWork(true, 0, false));
    }

    @Test
    void persistedProgressStartsAwakeEvenBeforeRecipeResolution() {
        assertTrue(DmaTickWakePolicy.hasRestoredWork(false, 148, false));
    }

    @Test
    void persistedRecipeMarkerStartsAwakeBeforeInputsAreVisible() {
        assertTrue(DmaTickWakePolicy.hasRestoredWork(false, 0, true));
    }

    @Test
    void idleMachineMayStartSleeping() {
        assertFalse(DmaTickWakePolicy.hasRestoredWork(false, 0, false));
    }

    @Test
    void restoredJobFallsBackWhenGridTickerNeverReported() {
        assertTrue(DmaTickWakePolicy.shouldUseBlockTickerFallback(true, 100, Long.MIN_VALUE));
    }

    @Test
    void activeGridTickerSuppressesBlockFallback() {
        assertFalse(DmaTickWakePolicy.shouldUseBlockTickerFallback(true, 100, 100));
        assertFalse(DmaTickWakePolicy.shouldUseBlockTickerFallback(true, 100, 80));
    }

    @Test
    void stalledGridTickerEnablesBlockFallbackAfterGracePeriod() {
        assertTrue(DmaTickWakePolicy.shouldUseBlockTickerFallback(true, 101, 80));
    }

    @Test
    void idleMachineNeverUsesBlockFallback() {
        assertFalse(DmaTickWakePolicy.shouldUseBlockTickerFallback(false, 100, Long.MIN_VALUE));
    }
}

package com.raishxn.ufo.api.multiblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiblockRuntimeStateResolverTest {

    @Test
    void resolvesNominalLifecycle() {
        assertState(MultiblockRuntimeState.UNFORMED, false, false, false, 0, 0, 0, 0);
        assertState(MultiblockRuntimeState.IDLE, true, true, false, 0, 0, 0, 0);
        assertState(MultiblockRuntimeState.RESERVING, true, true, false, 1, 0, 0, 0);
        assertState(MultiblockRuntimeState.RUNNING, true, true, false, 1, 1, 0, 0);
        assertState(MultiblockRuntimeState.OUTPUT_BLOCKED, true, true, false, 1, 0, 1, 0);
    }

    @Test
    void resolvesPauseAndFailurePriorities() {
        assertState(MultiblockRuntimeState.PAUSED_NO_GRID, true, false, true, 1, 1, 1, 1);
        assertState(MultiblockRuntimeState.OUTPUT_BLOCKED, true, true, true, 1, 0, 1, 1);
        assertState(MultiblockRuntimeState.OVERHEATED, true, true, true, 1, 0, 0, 1);
        assertState(MultiblockRuntimeState.INVALID_RECIPE, true, true, false, 1, 0, 0, 1);
    }

    @Test
    void rejectsNegativeCounters() {
        assertThrows(IllegalArgumentException.class, () -> new MultiblockRuntimeStateResolver.Signals(
                true, true, false, -1, 0, 0, 0));
    }

    private static void assertState(MultiblockRuntimeState expected, boolean formed, boolean gridConnected,
                                    boolean overheated, int active, int running, int blocked, int invalid) {
        assertEquals(expected, MultiblockRuntimeStateResolver.resolve(new MultiblockRuntimeStateResolver.Signals(
                formed, gridConnected, overheated, active, running, blocked, invalid)));
    }
}

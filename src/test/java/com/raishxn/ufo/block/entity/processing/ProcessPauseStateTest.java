package com.raishxn.ufo.block.entity.processing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProcessPauseStateTest {
    @Test
    void inactiveProcessCannotBePaused() {
        ProcessPauseState state = new ProcessPauseState();

        state.toggle(false);

        assertFalse(state.isPaused());
    }

    @Test
    void activeProcessCanPauseResumeAndClear() {
        ProcessPauseState state = new ProcessPauseState();

        state.toggle(true);
        assertTrue(state.isPaused());

        state.toggle(true);
        assertFalse(state.isPaused());

        state.toggle(true);
        state.clear();
        assertFalse(state.isPaused());
    }
}

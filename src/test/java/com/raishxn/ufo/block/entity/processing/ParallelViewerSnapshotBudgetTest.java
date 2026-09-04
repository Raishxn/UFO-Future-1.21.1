package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParallelViewerSnapshotBudgetTest {
    @Test
    void viewerPayloadNeverExposesMoreRowsThanTheParallelRuntimeSupports() {
        assertEquals(0, ParallelViewerSnapshotBudget.rowCount(0));
        assertEquals(9, ParallelViewerSnapshotBudget.rowCount(9));
        assertEquals(27, ParallelViewerSnapshotBudget.rowCount(27));
        assertEquals(27, ParallelViewerSnapshotBudget.rowCount(28));
        assertEquals(27, ParallelViewerSnapshotBudget.rowCount(Integer.MAX_VALUE));
    }

    @Test
    void negativeViewerRowCountsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ParallelViewerSnapshotBudget.rowCount(-1));
    }
}

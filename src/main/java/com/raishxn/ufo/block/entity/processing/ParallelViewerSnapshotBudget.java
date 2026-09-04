package com.raishxn.ufo.block.entity.processing;

/** Bounds transient viewer payloads independently of persistent process state. */
public final class ParallelViewerSnapshotBudget {
    public static final int MAX_PROCESS_ROWS = 27;

    private ParallelViewerSnapshotBudget() {
    }

    public static int rowCount(int requestedRows) {
        if (requestedRows < 0) {
            throw new IllegalArgumentException("requestedRows must be non-negative");
        }
        return Math.min(MAX_PROCESS_ROWS, requestedRows);
    }
}

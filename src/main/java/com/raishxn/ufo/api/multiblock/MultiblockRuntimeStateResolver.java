package com.raishxn.ufo.api.multiblock;

public final class MultiblockRuntimeStateResolver {
    private MultiblockRuntimeStateResolver() {
    }

    public record Signals(
            boolean formed,
            boolean gridConnected,
            boolean overheated,
            int activeProcesses,
            int runningProcesses,
            int outputBlockedProcesses,
            int invalidRecipeProcesses) {
        public Signals {
            if (activeProcesses < 0 || runningProcesses < 0 || outputBlockedProcesses < 0
                    || invalidRecipeProcesses < 0) {
                throw new IllegalArgumentException("Process counts cannot be negative");
            }
        }
    }

    public static MultiblockRuntimeState resolve(Signals signals) {
        if (!signals.formed()) return MultiblockRuntimeState.UNFORMED;
        if (!signals.gridConnected()) return MultiblockRuntimeState.PAUSED_NO_GRID;
        if (signals.outputBlockedProcesses() > 0) return MultiblockRuntimeState.OUTPUT_BLOCKED;
        if (signals.overheated()) return MultiblockRuntimeState.OVERHEATED;
        if (signals.invalidRecipeProcesses() > 0) return MultiblockRuntimeState.INVALID_RECIPE;
        if (signals.runningProcesses() > 0) return MultiblockRuntimeState.RUNNING;
        if (signals.activeProcesses() > 0) return MultiblockRuntimeState.RESERVING;
        return MultiblockRuntimeState.IDLE;
    }
}

package com.raishxn.ufo.api.multiblock;

public enum MultiblockRuntimeState {
    UNFORMED,
    FORMING,
    IDLE,
    RESERVING,
    RUNNING,
    OUTPUT_BLOCKED,
    PAUSED_NO_GRID,
    PAUSED_NO_ENERGY,
    PAUSED_NO_COOLANT,
    INVALID_RECIPE,
    OVERHEATED,
    ERROR_RECOVERABLE
}

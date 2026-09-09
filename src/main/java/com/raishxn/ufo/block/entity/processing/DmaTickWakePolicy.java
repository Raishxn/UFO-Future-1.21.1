package com.raishxn.ufo.block.entity.processing;

/** Keeps a persisted DMA job awake while its recipe and inventories are being restored. */
public final class DmaTickWakePolicy {
    public static final int GRID_TICK_GRACE = 20;

    private DmaTickWakePolicy() {
    }

    public static boolean hasRestoredWork(boolean jobStarted, int progress, boolean hasResumeRecipe) {
        return jobStarted || progress > 0 || hasResumeRecipe;
    }

    public static boolean shouldUseBlockTickerFallback(
            boolean hasRestoredWork, long gameTime, long lastGridServiceTick) {
        return hasRestoredWork
                && (lastGridServiceTick == Long.MIN_VALUE
                        || gameTime - lastGridServiceTick > GRID_TICK_GRACE);
    }
}

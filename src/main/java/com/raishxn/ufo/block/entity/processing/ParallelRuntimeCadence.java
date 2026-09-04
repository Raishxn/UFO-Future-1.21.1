package com.raishxn.ufo.block.entity.processing;

/** Pure scheduling/dirty policy for parallel multiblock runtime work. */
public final class ParallelRuntimeCadence {
    public static final int CLIENT_SYNC_INTERVAL_TICKS = 5;

    private ParallelRuntimeCadence() {
    }

    public static boolean shouldEvaluateClientState(long gameTime, long lastEvaluationTick,
                                                    boolean throttled) {
        if (!throttled || lastEvaluationTick == Long.MIN_VALUE || gameTime < lastEvaluationTick) {
            return true;
        }
        return gameTime - lastEvaluationTick >= CLIENT_SYNC_INTERVAL_TICKS;
    }

    public static boolean hasPersistentActivity(int activeProcesses, int temperature,
                                                int overloadTimer) {
        if (activeProcesses < 0 || temperature < 0) {
            throw new IllegalArgumentException("runtime counters cannot be negative");
        }
        return activeProcesses > 0 || temperature > 0 || overloadTimer >= 0;
    }
}

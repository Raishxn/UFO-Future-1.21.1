package com.raishxn.ufo.block.entity.processing;

public record StellarExplosionPolicy(
        Mode mode,
        int radius,
        int maxBlockChangesPerTick,
        int maxTotalBlockChanges,
        long maxNanosPerTick,
        boolean createLava,
        boolean secondaryExplosions) {

    public enum Mode {
        LOCAL_ONLY,
        BOUNDED_GRIEF
    }

    public static StellarExplosionPolicy resolve(
            boolean blockGriefEnabled,
            boolean dimensionAllowed,
            int requestedRadius,
            int configuredMaxRadius,
            int configuredMaxBlockChangesPerTick,
            int configuredMaxTotalBlockChanges,
            long configuredMaxNanosPerTick,
            boolean createLava,
            boolean secondaryExplosions) {
        if (!blockGriefEnabled || !dimensionAllowed) {
            return new StellarExplosionPolicy(Mode.LOCAL_ONLY, 0, 0, 0, 0L, false, false);
        }

        return new StellarExplosionPolicy(
                Mode.BOUNDED_GRIEF,
                clamp(Math.min(requestedRadius, configuredMaxRadius), 1, 64),
                clamp(configuredMaxBlockChangesPerTick, 1, 4096),
                clamp(configuredMaxTotalBlockChanges, 1, 1_000_000),
                clamp(configuredMaxNanosPerTick, 100_000L, 50_000_000L),
                createLava,
                secondaryExplosions);
    }

    public boolean allowsBlockGrief() {
        return this.mode == Mode.BOUNDED_GRIEF;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}

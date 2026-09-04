package com.raishxn.ufo.block.entity.processing;

/**
 * Scheduling and geometry constants for the DMA heat hazard. Keeping the
 * decision pure makes the server-side packet/query budget explicit and tested.
 */
public final class DmaHazardCadence {
    public static final int PARTICLE_INTERVAL_TICKS = 10;
    public static final int DAMAGE_INTERVAL_TICKS = 20;
    public static final int RING_COUNT = 3;
    public static final int POINTS_PER_RING = 6;

    private DmaHazardCadence() {
    }

    public static boolean shouldEmitParticles(long gameTime, long blockPositionKey) {
        long stagger = Math.floorMod(blockPositionKey, PARTICLE_INTERVAL_TICKS);
        return Math.floorMod(gameTime + stagger, PARTICLE_INTERVAL_TICKS) == 0;
    }

    public static boolean shouldCheckDamage(long gameTime) {
        return Math.floorMod(gameTime, DAMAGE_INTERVAL_TICKS) == 0;
    }

    public static int particlesPerEmission() {
        return RING_COUNT * POINTS_PER_RING;
    }

    public static double angleOffset(int point) {
        if (point < 0 || point >= POINTS_PER_RING) {
            throw new IllegalArgumentException("point outside DMA hazard ring");
        }
        return point * (Math.PI * 2.0D / POINTS_PER_RING);
    }
}

package com.raishxn.ufo.block.entity.processing;

/**
 * Pure coolant tuning tables. Fluid identity matching lives in
 * {@link CoolantRegistry}; this class only maps a resolved coolant kind to the
 * {@link ThermalSystem.CoolantProfile} each consumer applies.
 *
 * <p>Profiles are deliberately per consumer where balances diverge: the parallel
 * controllers preserve the Gelid {@code 1/120} contract validated in L-0013,
 * while the DMA keeps its own historical table (Gelid {@code 1/24}, Starlight
 * {@code 30 heat/mB}). Unifying these numbers is a rebalance and requires the
 * energy/thermal metrics milestone first.</p>
 */
public final class CoolantTuning {
    private static final ThermalSystem.CoolantProfile TEMPORAL_PROFILE =
            new ThermalSystem.CoolantProfile(100L, 1L, 10L);
    private static final ThermalSystem.CoolantProfile STABLE_PROFILE =
            new ThermalSystem.CoolantProfile(50L, 1L, 10L);
    private static final ThermalSystem.CoolantProfile GELID_PARALLEL_PROFILE =
            new ThermalSystem.CoolantProfile(1L, 120L, 1_000L);
    private static final ThermalSystem.CoolantProfile GELID_DMA_PROFILE =
            new ThermalSystem.CoolantProfile(1L, 24L, 1_000L);
    private static final ThermalSystem.CoolantProfile STARLIGHT_DMA_PROFILE =
            new ThermalSystem.CoolantProfile(30L, 1L, 10L);
    private static final ThermalSystem.CoolantProfile FALLBACK_PROFILE =
            new ThermalSystem.CoolantProfile(15L, 1L, 10L);

    private CoolantTuning() {
    }

    /** Coolant families resolved by exact fluid identity. */
    public enum CoolantKind {
        TEMPORAL, STABLE, STARLIGHT, GELID, GENERIC
    }

    /** Profiles used by the parallel controllers (and the Stellar facade). */
    public static ThermalSystem.CoolantProfile parallelProfile(CoolantKind kind) {
        return switch (kind) {
            case TEMPORAL -> TEMPORAL_PROFILE;
            case STABLE -> STABLE_PROFILE;
            case GELID -> GELID_PARALLEL_PROFILE;
            case STARLIGHT, GENERIC -> FALLBACK_PROFILE;
        };
    }

    /** Profiles preserved from the DMA's own balance table. */
    public static ThermalSystem.CoolantProfile dmaProfile(CoolantKind kind) {
        return switch (kind) {
            case TEMPORAL -> TEMPORAL_PROFILE;
            case STABLE -> STABLE_PROFILE;
            case STARLIGHT -> STARLIGHT_DMA_PROFILE;
            case GELID -> GELID_DMA_PROFILE;
            case GENERIC -> FALLBACK_PROFILE;
        };
    }
}

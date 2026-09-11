package com.raishxn.ufo.crafting;

/** Server-work and energy policy for the Infinity Fabrication Singularity. */
public enum SingularityCraftingMode {
    BALANCED(64, 5, 2, 1.0D),
    SPEED(128, 1, 1, 2.0D),
    EFFICIENCY(32, 20, 4, 0.5D);

    /** Absolute safety ceiling; each field tier exposes a smaller or equal live limit. */
    public static final int ROUTE_LIMIT = 128;

    private final int routesPerTick;
    private final int autoCraftIntervalTicks;
    private final int operationCost;
    private final double energyMultiplier;

    SingularityCraftingMode(int routesPerTick, int autoCraftIntervalTicks,
                            int operationCost, double energyMultiplier) {
        this.routesPerTick = routesPerTick;
        this.autoCraftIntervalTicks = autoCraftIntervalTicks;
        this.operationCost = operationCost;
        this.energyMultiplier = energyMultiplier;
    }

    public int routesPerTick() {
        return routesPerTick;
    }

    /** Number of independent requested/automatic output routes this tier may keep in flight. */
    public int parallelJobLimit() {
        return routesPerTick;
    }

    /**
     * Repeat cadence for stockless automatic patterns. Efficiency deliberately accumulates a
     * larger batch before crafting; Speed checks every tick.
     */
    public int autoCraftIntervalTicks() {
        return autoCraftIntervalTicks;
    }

    public int operationCost() {
        return operationCost;
    }

    public double energyMultiplier() {
        return energyMultiplier;
    }

    /** Resolves the locked mode after the multiblock has validated one uniform field tier. */
    public static SingularityCraftingMode forFieldTiers(int mk1Fields, int mk2Fields, int mk3Fields) {
        if (mk1Fields > 0) return EFFICIENCY;
        if (mk2Fields > 0) return BALANCED;
        if (mk3Fields > 0) return SPEED;
        return BALANCED;
    }

    public static boolean hasUniformFieldTier(int mk1Fields, int mk2Fields, int mk3Fields) {
        int activeTiers = (mk1Fields > 0 ? 1 : 0)
                + (mk2Fields > 0 ? 1 : 0)
                + (mk3Fields > 0 ? 1 : 0);
        return activeTiers == 1;
    }

    public SingularityCraftingMode next() {
        SingularityCraftingMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static SingularityCraftingMode byOrdinal(int ordinal) {
        SingularityCraftingMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : BALANCED;
    }
}

package com.raishxn.ufo.armor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Numeric settings exposed by configurable UFO armor modules. */
public enum UfoArmorSetting {
    FLIGHT_SPEED(UfoArmorModule.VOID_FLIGHT, 25, 400, 25, 100, "%"),
    FLIGHT_INERTIA(UfoArmorModule.VOID_FLIGHT, 0, 100, 10, 50, "%"),
    STEP_HEIGHT(UfoArmorModule.PHASE_STEP, 1, 8, 1, 2, "m"),
    REACH_DISTANCE(UfoArmorModule.PHASE_STEP, 0, 32, 1, 8, "m"),
    MAGNET_RANGE(UfoArmorModule.ENTROPY_MAGNET, 4, 32, 2, 16, "m"),
    TRANSLOCATOR_THRESHOLD(UfoArmorModule.MATTER_TRANSLOCATOR, 5, 50, 5, 25, "%"),
    TRANSLOCATOR_HEAL(UfoArmorModule.MATTER_TRANSLOCATOR, 25, 100, 5, 50, "%"),
    REGENERATION_LEVEL(UfoArmorModule.CHRONO_REGENERATOR, 1, 10, 1, 5, ""),
    RELAY_TRANSFER_RATE(UfoArmorModule.QUANTUM_RELAY, 50_000, 1_000_000, 50_000, 250_000, " RF"),
    CLOAKING_RANGE(UfoArmorModule.CLOAKING_FIELD, 16, 128, 16, 64, "m"),
    KINETIC_SPEED(UfoArmorModule.KINETIC_OVERDRIVE, 25, 1000, 25, 200, "%"),
    KINETIC_JUMP(UfoArmorModule.KINETIC_OVERDRIVE, 1, 10, 1, 5, ""),
    STRIKE_MULTIPLIER(UfoArmorModule.SINGULARITY_STRIKE, 2, 100, 2, 10, "x"),
    REPRISAL_MULTIPLIER(UfoArmorModule.REPRISAL_MATRIX, 10, 1000, 10, 100, "x"),
    LUCK_LEVEL(UfoArmorModule.LOOT_SINGULARITY, 1, 20, 1, 10, "");

    private final UfoArmorModule module;
    private final int min;
    private final int max;
    private final int step;
    private final int defaultValue;
    private final String suffix;

    UfoArmorSetting(UfoArmorModule module, int min, int max, int step, int defaultValue, String suffix) {
        this.module = module;
        this.min = min;
        this.max = max;
        this.step = step;
        this.defaultValue = defaultValue;
        this.suffix = suffix;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public UfoArmorModule module() {
        return module;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;

    }

    public int step() {
        return step;
    }

    public int defaultValue() {
        return defaultValue;
    }

    public String formatted(int value) {
        if (this == RELAY_TRANSFER_RATE) return (value / 1_000) + " kRF/t";
        return suffix.equals("x") ? value + "x" : value + suffix;
    }

    public String translationKey() {
        return "setting.ufo." + id();
    }

    public int clamp(int value) {
        int snapped = min + Math.round((value - min) / (float) step) * step;
        return Math.max(min, Math.min(max, snapped));
    }

    public static List<UfoArmorSetting> forModule(UfoArmorModule module) {
        return Arrays.stream(values()).filter(setting -> setting.module == module).toList();
    }
}

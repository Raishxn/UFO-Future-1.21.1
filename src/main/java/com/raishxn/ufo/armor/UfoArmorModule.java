package com.raishxn.ufo.armor;

import net.minecraft.world.item.ArmorItem;

import java.util.Arrays;
import java.util.Locale;

/** The server-authoritative catalogue of UFO armor upgrades. */
public enum UfoArmorModule {
    AEGIS_SINGULARITY(ArmorItem.Type.CHESTPLATE, 2_000),
    REALITY_ANCHOR(ArmorItem.Type.CHESTPLATE, 100_000),
    CHRONO_REGENERATOR(ArmorItem.Type.CHESTPLATE, 400),
    VOID_FLIGHT(ArmorItem.Type.CHESTPLATE, 200),
    PHASE_STEP(ArmorItem.Type.BOOTS, 40),
    ENTROPY_MAGNET(ArmorItem.Type.LEGGINGS, 80),
    QUANTUM_RELAY(ArmorItem.Type.HELMET, 120),
    MATTER_TRANSLOCATOR(ArmorItem.Type.LEGGINGS, 25_000),
    ABYSSAL_SIGHT(ArmorItem.Type.HELMET, 20),
    ADAPTIVE_BIOSPHERE(ArmorItem.Type.HELMET, 40),
    KINETIC_OVERDRIVE(ArmorItem.Type.BOOTS, 100),
    SINGULARITY_STRIKE(ArmorItem.Type.LEGGINGS, 500),
    REPRISAL_MATRIX(ArmorItem.Type.CHESTPLATE, 1_000),
    LOOT_SINGULARITY(ArmorItem.Type.LEGGINGS, 100),
    CLOAKING_FIELD(ArmorItem.Type.CHESTPLATE, 100),
    ASTRAL_WINGS(ArmorItem.Type.CHESTPLATE, 0);

    private final ArmorItem.Type armorType;
    private final int energyCost;

    UfoArmorModule(ArmorItem.Type armorType, int energyCost) {
        this.armorType = armorType;
        this.energyCost = energyCost;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String itemId() {
        return "ufo_" + id() + "_card";
    }

    public ArmorItem.Type armorType() {
        return armorType;
    }

    public int energyCost() {
        return energyCost;
    }

    public String translationKey() {
        return "module.ufo." + id();
    }

    public static UfoArmorModule byId(String id) {
        return Arrays.stream(values()).filter(module -> module.id().equals(id)).findFirst().orElse(null);
    }

    public static int capacity(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 5;
            case CHESTPLATE -> 8;
            case LEGGINGS -> 5;
            case BOOTS -> 4;
            default -> 0;
        };
    }
}

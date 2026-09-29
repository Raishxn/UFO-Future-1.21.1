package com.raishxn.ufo.recipe;

import net.minecraft.resources.ResourceLocation;

/** Keeps selections from the short-lived consolidated catalog usable in existing worlds. */
public final class StellarSimulationRecipeIds {
    private StellarSimulationRecipeIds() { }

    public static ResourceLocation canonical(ResourceLocation id) {
        if (id == null) return null;
        String replacement = canonicalPath(id.getNamespace(), id.getPath());
        return replacement.equals(id.getPath()) ? id
                : ResourceLocation.fromNamespaceAndPath(id.getNamespace(), replacement);
    }

    public static String canonicalPath(String namespace, String path) {
        if (!namespace.equals("ufo")) return path;
        return switch (path) {
            case "stellar_nexus/massive_iron_synthesis" -> "stellar_simulation/massive_iron_synthesis";
            case "stellar_nexus/massive_copper_synthesis" -> "stellar_simulation/massive_copper_synthesis";
            case "stellar_nexus/massive_gold_synthesis" -> "stellar_simulation/massive_gold_synthesis";
            case "stellar_nexus/massive_netherite_synthesis" -> "stellar_simulation/massive_netherite_synthesis";
            case "stellar_simulation/cosmic_material_convergence" -> "stellar_simulation/massive_iron_synthesis";
            case "stellar_simulation/quantum_singularity_convergence" -> "stellar_simulation/ae2_singularity";
            case "stellar_simulation/advancedae_convergence" -> "stellar_simulation/advancedae_quantum";
            case "stellar_simulation/extendedae_convergence" -> "stellar_simulation/extendedae_entro";
            case "stellar_simulation/megacells_convergence" -> "stellar_simulation/megacells_skysteel";
            case "stellar_simulation/mekanism_ethylene_convergence" -> "stellar_simulation/mekanism_ethylene";
            case "stellar_simulation/mekanism_convergence" -> "stellar_simulation/mekanism_metallurgic_surge";
            default -> path;
        };
    }
}

package com.raishxn.ufo.recipe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StellarSimulationRecipeIdsTest {
    @Test
    void originalSelectionsStayIntactAndPreviewSelectionsRemainUsable() {
        assertEquals("stellar_simulation/massive_iron_synthesis",
                StellarSimulationRecipeIds.canonicalPath("ufo", "stellar_simulation/massive_iron_synthesis"));
        assertEquals("stellar_simulation/mekanism_ethylene",
                StellarSimulationRecipeIds.canonicalPath("ufo", "stellar_simulation/mekanism_ethylene"));
        assertEquals("stellar_simulation/massive_iron_synthesis",
                StellarSimulationRecipeIds.canonicalPath("ufo", "stellar_simulation/cosmic_material_convergence"));
        assertEquals("stellar_simulation/megacells_skysteel",
                StellarSimulationRecipeIds.canonicalPath("ufo", "stellar_simulation/megacells_convergence"));
        assertEquals("stellar_simulation/custom",
                StellarSimulationRecipeIds.canonicalPath("my_pack", "stellar_simulation/custom"));
    }
}

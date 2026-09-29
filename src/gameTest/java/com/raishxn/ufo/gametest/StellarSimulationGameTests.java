package com.raishxn.ufo.gametest;

import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.fml.ModList;

import java.util.Set;

/** Regression coverage for the Stellar Nexus' short-cycle endgame contract. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class StellarSimulationGameTests {
    private static final int MK2_MAX_DURATION_TICKS = 3 * 60 * 20;
    private static final int MK3_MAX_DURATION_TICKS = 4 * 60 * 20;

    private StellarSimulationGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void simulationsStayWithinTheirTierTimeBudget(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(ModRecipes.STELLAR_SIMULATION_TYPE.get());
        helper.assertTrue(!recipes.isEmpty(), "No Stellar Nexus simulations were loaded");

        for (var holder : recipes) {
            StellarSimulationRecipe recipe = holder.value();
            int limit = recipe.getFieldTier() >= 3 ? MK3_MAX_DURATION_TICKS : MK2_MAX_DURATION_TICKS;
            helper.assertTrue(recipe.getTime() <= limit,
                    holder.id() + " takes " + recipe.getFormattedTime()
                            + "; tier " + recipe.getFieldTier() + " is limited to " + (limit / 20) + " seconds");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void focusedProgramsAndOmnibusLoad(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(ModRecipes.STELLAR_SIMULATION_TYPE.get());
        Set<String> catalog = Set.of(
                "massive_iron_synthesis", "massive_copper_synthesis", "massive_gold_synthesis",
                "massive_netherite_synthesis", "stellar_synthesis", "ae2_singularity",
                "advancedae_quantum", "extendedae_entro", "megacells_skysteel",
                "mekanism_ethylene", "mekanism_fission", "mekanism_metallurgic_surge",
                "vanilla_reagent_fabrication", "stellar_omnibus");
        helper.assertTrue(recipes.stream().anyMatch(holder -> holder.id().equals(
                        ResourceLocation.parse("ufo:stellar_simulation/massive_iron_synthesis"))),
                "Original iron program did not load");
        boolean omnibusDependenciesLoaded = Set.of("advanced_ae", "extendedae", "megacells",
                "mekanism", "mekanismgenerators", "appflux")
                .stream().allMatch(modId -> ModList.get().isLoaded(modId));
        boolean omnibusLoaded = recipes.stream().anyMatch(holder -> holder.id().equals(
                ResourceLocation.parse("ufo:stellar_simulation/stellar_omnibus")));
        helper.assertTrue(omnibusLoaded == omnibusDependenciesLoaded,
                "MK3 omnibus availability does not match its mod dependencies");
        for (var holder : recipes) {
            String path = holder.id().getPath();
            if (!holder.id().getNamespace().equals("ufo")
                    || !path.startsWith("stellar_simulation/")
                    || !catalog.contains(path.substring("stellar_simulation/".length()))) continue;
            var recipe = holder.value();
            helper.assertTrue(recipe.getItemOutputs().size() >= 4,
                    holder.id() + " has too few item outputs");
            helper.assertTrue(recipe.getItemOutputs().stream().mapToLong(out -> out.amount()).sum() >= 200_000_000L,
                    holder.id() + " does not produce hundreds of millions of items");
            helper.assertTrue(recipe.getFluidOutputs().stream().mapToLong(out -> out.amount()).sum() >= 100_000_000L,
                    holder.id() + " does not produce bulk fluids");
        }
        helper.succeed();
    }
}

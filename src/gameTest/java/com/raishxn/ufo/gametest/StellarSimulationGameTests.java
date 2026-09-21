package com.raishxn.ufo.gametest;

import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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
}

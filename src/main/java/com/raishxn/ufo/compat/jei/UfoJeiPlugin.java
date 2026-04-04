package com.raishxn.ufo.compat.jei;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;

import net.pedroksl.ae2addonlib.recipes.IngredientStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

@JeiPlugin
public class UfoJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = UfoMod.id("jei_plugin");

    public UfoJeiPlugin() {}

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        var jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new DimensionalMatterAssemblerRecipeCategory(jeiHelpers));
        registry.addRecipeCategories(new StellarSimulationRecipeCategory(jeiHelpers));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(
                DimensionalMatterAssemblerRecipeCategory.RECIPE_TYPE,
                List.copyOf(recipeManager.getAllRecipesFor(com.raishxn.ufo.init.ModRecipes.DMA_RECIPE_TYPE.get()).stream()
                        .map(RecipeHolder::value)
                        .toList()));
        registration.addRecipes(
                StellarSimulationRecipeCategory.RECIPE_TYPE,
                List.copyOf(recipeManager.getAllRecipesFor(com.raishxn.ufo.init.ModRecipes.STELLAR_SIMULATION_TYPE.get()).stream()
                        .map(RecipeHolder::value)
                        .toList()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        var dmaBlock = ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get().asItem().getDefaultInstance();
        registration.addRecipeCatalyst(dmaBlock, DimensionalMatterAssemblerRecipeCategory.RECIPE_TYPE);

        var nexusController = MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().asItem().getDefaultInstance();
        registration.addRecipeCatalyst(nexusController, StellarSimulationRecipeCategory.RECIPE_TYPE);
    }

    public static Ingredient stackOf(IngredientStack.Item stack) {
        if (!stack.isEmpty()) {
            return Ingredient.of(Arrays.stream(stack.getIngredient().getItems())
                    .map(oldStack -> oldStack.copyWithCount(stack.getAmount())));
        }
        return Ingredient.of(ItemStack.EMPTY);
    }

    public static List<FluidStack> stackOf(IngredientStack.Fluid stack) {
        FluidIngredient ingredient = stack.getIngredient();
        return Arrays.stream(ingredient.getStacks())
                .map(oldStack -> oldStack.copyWithAmount(stack.getAmount()))
                .toList();
    }
}

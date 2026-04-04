package com.raishxn.ufo.datagen;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.pedroksl.ae2addonlib.recipes.IngredientStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DMARecipeBuilder {
    private final String name;
    private final List<IngredientStack.Item> itemInputs = new ArrayList<>();
    private final List<IngredientStack.Fluid> fluidInputs = new ArrayList<>();
    private final List<GenericStack> itemOutputs = new ArrayList<>();
    private final List<GenericStack> fluidOutputs = new ArrayList<>();
    private int energy = 0;
    private int time = 0; // The recipe class doesn't seem to use time, but we keep it for API compatibility.

    private DMARecipeBuilder(String name) {
        this.name = name;
    }

    public static DMARecipeBuilder create(String name) {
        return new DMARecipeBuilder(name);
    }

    public DMARecipeBuilder inputItem(ItemLike item) {
        return inputItem(item, 1);
    }

    public DMARecipeBuilder inputItem(ItemLike item, int count) {
        this.itemInputs.add(new IngredientStack.Item(Ingredient.of(item), count));
        return this;
    }
    
    // For specific AEItemKey inputs if ever needed, though IngredientStack is better
    public DMARecipeBuilder inputFluid(Fluid fluid, int amount) {
        this.fluidInputs.add(new IngredientStack.Fluid(FluidIngredient.of(fluid), amount));
        return this;
    }

    /**
     * @deprecated Coolant is now player-managed and not part of recipes.
     * Kept for KubeJS backward compatibility — this is a NO-OP.
     */
    @Deprecated
    public DMARecipeBuilder inputCoolant(Fluid fluid, int amount) {
        // NO-OP: coolant is no longer part of recipes
        return this;
    }

    public DMARecipeBuilder output(ItemLike item) {
        return output(item, 1);
    }

    public DMARecipeBuilder output(ItemLike item, int amount) {
        this.itemOutputs.add(new GenericStack(AEItemKey.of(item), amount));
        return this;
    }

    public DMARecipeBuilder output(ItemLike item, int amount, float chance) {
        // Current DimensionalMatterAssemblerRecipe doesn't support chance yet, but API compatibility:
        return output(item, amount);
    }

    public DMARecipeBuilder outputFluid(Fluid fluid, int amount, float chance) {
        this.fluidOutputs.add(new GenericStack(AEFluidKey.of(fluid), amount));
        return this;
    }

    public DMARecipeBuilder energy(int energy) {
        this.energy = energy;
        return this;
    }

    public DMARecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public void save(RecipeOutput output) {
        save(output, ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, this.name));
    }
    
    public void save(RecipeOutput output, ResourceLocation id) {
        var recipe = new DimensionalMatterAssemblerRecipe(
                this.itemInputs,
                this.fluidInputs,
                this.itemOutputs,
                this.fluidOutputs,
                this.energy,
                this.time
        );
        output.accept(id, recipe, null);
    }
}

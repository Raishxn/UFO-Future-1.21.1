package com.raishxn.ufo.datagen;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.pedroksl.ae2addonlib.recipes.IngredientStack;

import java.util.ArrayList;
import java.util.List;

public class StellarSimulationRecipeBuilder {
    private final String name;
    private String simulationName = "";
    private final List<IngredientStack.Item> itemInputs = new ArrayList<>();
    private final List<IngredientStack.Fluid> fluidInputs = new ArrayList<>();
    private final List<GenericStack> itemOutputs = new ArrayList<>();
    private final List<GenericStack> fluidOutputs = new ArrayList<>();
    private long energy = 0;
    private int time = 0;
    private int coolingLevel = 3;
    private int fieldTier = 3;
    private String fuelFluid = "";
    private long fuelAmount = 0;
    private long coolantAmount = 0;

    private StellarSimulationRecipeBuilder(String name) {
        this.name = name;
    }

    public static StellarSimulationRecipeBuilder create(String name) {
        return new StellarSimulationRecipeBuilder(name);
    }

    public StellarSimulationRecipeBuilder simulationName(String simName) {
        this.simulationName = simName;
        return this;
    }

    public StellarSimulationRecipeBuilder inputItem(ItemLike item, int count) {
        this.itemInputs.add(new IngredientStack.Item(Ingredient.of(item), count));
        return this;
    }

    public StellarSimulationRecipeBuilder inputFluid(Fluid fluid, int amount) {
        this.fluidInputs.add(new IngredientStack.Fluid(FluidIngredient.of(fluid), amount));
        return this;
    }

    public StellarSimulationRecipeBuilder output(ItemLike item, long amount) {
        this.itemOutputs.add(new GenericStack(AEItemKey.of(item), amount));
        return this;
    }

    public StellarSimulationRecipeBuilder outputFluid(Fluid fluid, long amount) {
        this.fluidOutputs.add(new GenericStack(AEFluidKey.of(fluid), amount));
        return this;
    }

    public StellarSimulationRecipeBuilder fuel(String fluidRegistryName, long amount) {
        this.fuelFluid = fluidRegistryName;
        this.fuelAmount = amount;
        return this;
    }

    public StellarSimulationRecipeBuilder fuel(Fluid fluid, long amount) {
        // Safe to use toString or resource path, StellarSimulation uses direct string
        this.fuelFluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid).toString();
        this.fuelAmount = amount;
        return this;
    }

    public StellarSimulationRecipeBuilder coolant(long amount) {
        this.coolantAmount = amount;
        return this;
    }

    public StellarSimulationRecipeBuilder coolant(Fluid fluidPlaceholder, long amount) {
        // Coolant is determined by tier or just universally required, the original has coolant level and amount
        this.coolantAmount = amount;
        return this;
    }

    public StellarSimulationRecipeBuilder fieldLevel(int level) {
        this.fieldTier = level;
        return this;
    }

    public StellarSimulationRecipeBuilder coolingLevel(int level) {
        this.coolingLevel = level;
        return this;
    }

    public StellarSimulationRecipeBuilder energy(long energy) {
        this.energy = energy;
        return this;
    }

    public StellarSimulationRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public void save(RecipeOutput output) {
        var id = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, this.name);
        var recipe = new StellarSimulationRecipe(
                this.itemInputs,
                this.fluidInputs,
                this.itemOutputs,
                this.fluidOutputs,
                this.simulationName.isEmpty() ? id.getPath() : this.simulationName,
                this.energy,
                this.time,
                this.coolingLevel,
                this.fieldTier,
                this.fuelFluid,
                this.fuelAmount,
                this.coolantAmount
        );
        output.accept(id, recipe, null);
    }
}

package com.raishxn.ufo.datagen;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.recipe.UniversalMultiblockMachineKind;
import com.raishxn.ufo.recipe.UniversalMultiblockRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class UniversalMultiblockRecipeBuilder {
    private final String name;
    private final UniversalMultiblockMachineKind machine;
    private final List<UniversalMultiblockRecipe.ItemRequirement> itemInputs = new ArrayList<>();
    private final List<UniversalMultiblockRecipe.FluidRequirement> fluidInputs = new ArrayList<>();
    private ItemStack itemOutput = ItemStack.EMPTY;
    private FluidStack fluidOutput = FluidStack.EMPTY;
    private long fluidOutputAmount = 0L;
    private long energy = 0L;
    private int time = 200;
    private int requiredTier = 1;

    private UniversalMultiblockRecipeBuilder(String name, UniversalMultiblockMachineKind machine) {
        this.name = name;
        this.machine = machine;
    }

    public static UniversalMultiblockRecipeBuilder create(String name, UniversalMultiblockMachineKind machine) {
        return new UniversalMultiblockRecipeBuilder(name, machine);
    }

    public UniversalMultiblockRecipeBuilder inputItem(ItemLike item, long amount) {
        this.itemInputs.add(new UniversalMultiblockRecipe.ItemRequirement(Ingredient.of(item), amount));
        return this;
    }

    public UniversalMultiblockRecipeBuilder inputFluid(Fluid fluid, long amount) {
        this.fluidInputs.add(new UniversalMultiblockRecipe.FluidRequirement(new FluidStack(fluid, 1), amount));
        return this;
    }

    public UniversalMultiblockRecipeBuilder outputItem(ItemLike item, int amount) {
        this.itemOutput = new ItemStack(item, amount);
        return this;
    }

    public UniversalMultiblockRecipeBuilder outputFluid(Fluid fluid, long amount) {
        this.fluidOutput = new FluidStack(fluid, 1);
        this.fluidOutputAmount = amount;
        return this;
    }

    public UniversalMultiblockRecipeBuilder energy(long energy) {
        this.energy = energy;
        return this;
    }

    public UniversalMultiblockRecipeBuilder time(int time) {
        this.time = time;
        return this;
    }

    public UniversalMultiblockRecipeBuilder requiredTier(int requiredTier) {
        this.requiredTier = requiredTier;
        return this;
    }

    public void save(RecipeOutput output) {
        save(output, ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, this.name));
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new UniversalMultiblockRecipe(
                this.machine,
                this.name,
                this.itemInputs,
                this.fluidInputs,
                this.itemOutput,
                this.fluidOutput,
                this.fluidOutputAmount,
                this.energy,
                this.time,
                this.requiredTier), null);
    }
}

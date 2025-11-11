package com.raishxn.ufo.datagen;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class DMARecipeBuilder {
    private final String id;
    private final List<GenericStack> outputs = new ArrayList<>();
    private final List<Float> outputChances = new ArrayList<>();
    private final List<IngredientStack.Item> itemInputs = new ArrayList<>();
    private IngredientStack.Fluid fluidInput = null;
    private IngredientStack.Fluid coolantInput = null;
    private int energy = 0;
    private int processTime = 0;

    public DMARecipeBuilder(String id) {
        this.id = id;
    }

    public static DMARecipeBuilder create(String id) {
        return new DMARecipeBuilder(id);
    }

    // --- Outputs ---
    public DMARecipeBuilder output(ItemLike item) {
        return output(item, 1, 1.0f);
    }

    public DMARecipeBuilder output(ItemLike item, int amount) {
        return output(item, amount, 1.0f);
    }

    public DMARecipeBuilder output(ItemLike item, int amount, float chance) {
        this.outputs.add(GenericStack.fromItemStack(new ItemStack(item, amount)));
        this.outputChances.add(chance);
        return this;
    }

    // NOVO: Suporte a output de FLUIDO também, para aproveitar seus slots extras
    public DMARecipeBuilder outputFluid(Fluid fluid, int amount, float chance) {
        this.outputs.add(new GenericStack(AEFluidKey.of(fluid), amount));
        this.outputChances.add(chance);
        return this;
    }

    // --- Item Inputs ---
    public DMARecipeBuilder inputItem(ItemLike item) {
        return inputItem(item, 1);
    }

    public DMARecipeBuilder inputItem(ItemLike item, int amount) {
        this.itemInputs.add(IngredientStack.of(Ingredient.of(item), amount));
        return this;
    }

    public DMARecipeBuilder inputItem(TagKey<Item> tag, int amount) {
        this.itemInputs.add(IngredientStack.of(Ingredient.of(tag), amount));
        return this;
    }

    // --- Fluid Inputs ---
    public DMARecipeBuilder inputFluid(Fluid fluid, int amount) {
        this.fluidInput = IngredientStack.of(new FluidStack(fluid, amount));
        return this;
    }

    public DMARecipeBuilder inputCoolant(Fluid fluid, int amount) {
        this.coolantInput = IngredientStack.of(new FluidStack(fluid, amount));
        return this;
    }

    // --- Process Stats ---
    public DMARecipeBuilder energy(int energy) {
        this.energy = energy;
        return this;
    }

    public DMARecipeBuilder time(int ticks) {
        this.processTime = ticks;
        return this;
    }

    // --- Finalizar ---
    public void save(RecipeOutput consumer) {
        validate();
        DimensionalMatterAssemblerRecipe recipe = new DimensionalMatterAssemblerRecipe(
                outputs, outputChances, itemInputs, fluidInput, coolantInput, energy, processTime
        );
        consumer.accept(UfoMod.id(id), recipe, null);
    }

    private void validate() {
        if (outputs.isEmpty()) throw new IllegalStateException("Recipe " + id + " has no outputs!");
        if (fluidInput == null) throw new IllegalStateException("Recipe " + id + " has no fluid input!");
        if (coolantInput == null) throw new IllegalStateException("Recipe " + id + " has no coolant input!");
        if (itemInputs.size() > 9) throw new IllegalStateException("Recipe " + id + " has too many item inputs (max 9)!");

        // Validação de contagem de outputs no Builder também
        int itemCount = 0;
        int fluidCount = 0;
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEItemKey) itemCount++;
            if (stack.what() instanceof AEFluidKey) fluidCount++;
        }
        if (itemCount > 2) throw new IllegalStateException("Recipe " + id + " has too many item outputs (max 2)!");
        if (fluidCount > 2) throw new IllegalStateException("Recipe " + id + " has too many fluid outputs (max 2)!");
    }
}
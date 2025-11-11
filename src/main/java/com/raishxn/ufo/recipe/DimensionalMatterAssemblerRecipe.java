package com.raishxn.ufo.recipe;

import java.util.ArrayList;
import java.util.List;

import com.glodblock.github.glodium.recipe.stack.IngredientStack;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

import com.raishxn.ufo.init.ModRecipes;
import org.jetbrains.annotations.NotNull;

public class DimensionalMatterAssemblerRecipe implements Recipe<RecipeInput> {

    protected final List<IngredientStack.Item> itemInputs;
    protected final IngredientStack.Fluid fluidInput;
    protected final IngredientStack.Fluid coolantInput;
    protected final List<GenericStack> outputs;
    protected final List<Float> outputChances;
    protected final int energy;
    protected final int processTime;

    public DimensionalMatterAssemblerRecipe(List<GenericStack> outputs,
                                            List<Float> outputChances,
                                            List<IngredientStack.Item> itemInputs,
                                            IngredientStack.Fluid fluidInput,
                                            IngredientStack.Fluid coolantInput,
                                            int energy, int processTime) {
        this.outputs = outputs;
        this.outputChances = ensureChances(outputs, outputChances);
        this.itemInputs = itemInputs;
        this.fluidInput = fluidInput;
        this.coolantInput = coolantInput;
        this.energy = energy;
        this.processTime = processTime;
        validateRecipe();
    }

    private static List<Float> ensureChances(List<GenericStack> outputs, List<Float> chances) {
        if (chances == null) chances = new ArrayList<>();
        List<Float> correctedChances = new ArrayList<>(chances);
        while (correctedChances.size() < outputs.size()) {
            correctedChances.add(1.0f);
        }
        return correctedChances;
    }

    // --- VALIDAÇÃO AJUSTADA ---
    private void validateRecipe() {
        if (itemInputs.size() > 9) {
            throw new IllegalArgumentException("DMA recipe cannot have more than 9 item inputs.");
        }

        int itemCount = 0;
        int fluidCount = 0;

        // Conta quantos itens e fluidos temos no TOTAL (principais + chances)
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEItemKey) {
                itemCount++;
            } else if (stack.what() instanceof AEFluidKey) {
                fluidCount++;
            }
        }

        // Aplica as regras de limite máximo
        if (itemCount > 2) {
            throw new IllegalArgumentException("DMA recipe cannot have more than 2 item outputs (found " + itemCount + ").");
        }
        if (fluidCount > 2) {
            throw new IllegalArgumentException("DMA recipe cannot have more than 2 fluid outputs (found " + fluidCount + ").");
        }
    }
    // ---------------------------

    @Override
    public boolean matches(@NotNull RecipeInput recipeInput, @NotNull Level level) { return false; }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput inv, HolderLookup.@NotNull Provider registries) {
        return getResultItem(registries).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        List<ItemStack> items = getItemOutputs();
        return items.isEmpty() ? ItemStack.EMPTY : items.getFirst();
    }

    public List<IngredientStack.Item> getItemInputs() { return itemInputs; }
    public IngredientStack.Fluid getFluidInput() { return fluidInput; }
    public IngredientStack.Fluid getCoolantInput() { return coolantInput; }
    public List<GenericStack> getOutputs() { return outputs; }
    public List<Float> getOutputChances() { return outputChances; }
    public int getEnergy() { return energy; }
    public int getProcessTime() { return processTime; }

    public float getChance(int index) {
        if (index >= 0 && index < outputChances.size()) {
            return outputChances.get(index);
        }
        return 0.0f;
    }

    public List<ItemStack> getItemOutputs() {
        List<ItemStack> stacks = new ArrayList<>();
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEItemKey itemKey) {
                stacks.add(itemKey.toStack((int) stack.amount()));
            }
        }
        return stacks;
    }

    public List<FluidStack> getFluidOutputs() {
        List<FluidStack> stacks = new ArrayList<>();
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEFluidKey fluidKey) {
                stacks.add(fluidKey.toStack((int) stack.amount()));
            }
        }
        return stacks;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return DimensionalMatterAssemblerRecipeSerializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.DMA_TYPE.get();
    }
}
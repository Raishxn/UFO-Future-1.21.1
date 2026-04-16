package com.raishxn.ufo.block.entity.processing;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import com.raishxn.ufo.recipe.QMFRecipe;
import com.raishxn.ufo.recipe.UniversalMultiblockRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.pedroksl.ae2addonlib.recipes.IngredientStack;

import java.util.ArrayList;
import java.util.List;

public record MultiblockProcessingRecipe(
        ResourceLocation id,
        String name,
        List<ItemRequirement> itemInputs,
        List<FluidRequirement> fluidInputs,
        List<ChemicalRequirement> chemicalInputs,
        List<OutputStack> outputs,
        long energy,
        int time,
        int requiredTier) {

    public record ItemRequirement(Ingredient ingredient, long amount) {
    }

    public record FluidRequirement(FluidStack fluid, long amount) {
    }

    public record ChemicalRequirement(ResourceLocation chemicalId, long amount) {
    }

    public record OutputStack(ItemStack item, FluidStack fluid, long amount) {
        public OutputStack {
            item = item == null ? ItemStack.EMPTY : item;
            fluid = fluid == null ? FluidStack.EMPTY : fluid;
            amount = Math.max(0L, amount);
        }
    }

    public OutputStack primaryOutput() {
        if (outputs.isEmpty()) {
            return new OutputStack(ItemStack.EMPTY, FluidStack.EMPTY, 0L);
        }
        return outputs.getFirst();
    }

    public static MultiblockProcessingRecipe fromQmf(ResourceLocation id, QMFRecipe recipe) {
        List<ItemRequirement> itemInputs = recipe.getItemInputs().stream()
                .map(input -> new ItemRequirement(input.ingredient(), input.amount()))
                .toList();
        List<FluidRequirement> fluidInputs = recipe.getFluidInputs().stream()
                .map(input -> new FluidRequirement(input.fluid(), input.amount()))
                .toList();
        List<ChemicalRequirement> chemicalInputs = recipe.getChemicalInputs().stream()
                .map(input -> new ChemicalRequirement(input.chemicalId(), input.amount()))
                .toList();
        List<OutputStack> outputs = List.of(new OutputStack(normalizeItem(recipe.getResultItem()), FluidStack.EMPTY, recipe.getResultItem().getCount()));
        return new MultiblockProcessingRecipe(id, recipe.getRecipeName(), itemInputs, fluidInputs, chemicalInputs, outputs, recipe.getEnergy(), recipe.getTime(), recipe.getRequiredTier());
    }

    public static MultiblockProcessingRecipe fromUniversal(ResourceLocation id, UniversalMultiblockRecipe recipe) {
        List<ItemRequirement> itemInputs = recipe.getItemInputs().stream()
                .map(input -> new ItemRequirement(input.ingredient(), input.amount()))
                .toList();
        List<FluidRequirement> fluidInputs = recipe.getFluidInputs().stream()
                .map(input -> new FluidRequirement(input.fluid(), input.amount()))
                .toList();
        List<ChemicalRequirement> chemicalInputs = recipe.getChemicalInputs().stream()
                .map(input -> new ChemicalRequirement(input.chemicalId(), input.amount()))
                .toList();

        List<OutputStack> outputs = new ArrayList<>();
        if (!recipe.getItemOutput().isEmpty()) {
            outputs.add(new OutputStack(normalizeItem(recipe.getItemOutput()), FluidStack.EMPTY, recipe.getItemOutputAmount()));
        }
        if (!recipe.getFluidOutput().isEmpty() && recipe.getFluidOutputAmount() > 0) {
            outputs.add(new OutputStack(ItemStack.EMPTY, recipe.getFluidOutput(), recipe.getFluidOutputAmount()));
        }

        return new MultiblockProcessingRecipe(id, recipe.getRecipeName(), itemInputs, fluidInputs, chemicalInputs, outputs, recipe.getEnergy(), recipe.getTime(), recipe.getRequiredTier());
    }

    public static MultiblockProcessingRecipe fromDma(ResourceLocation id, DimensionalMatterAssemblerRecipe recipe) {
        List<ItemRequirement> itemInputs = recipe.getItemInputs().stream()
                .filter(input -> input != null && !input.isEmpty())
                .map(input -> new ItemRequirement(input.getIngredient(), input.getAmount()))
                .toList();
        List<FluidRequirement> fluidInputs = recipe.getFluidInputs().stream()
                .filter(input -> input != null && !input.isEmpty())
                .map(input -> new FluidRequirement(input.getIngredient().getStacks()[0], input.getAmount()))
                .toList();
        List<OutputStack> outputs = new ArrayList<>();
        for (GenericStack output : recipe.getItemOutputs()) {
            if (output.what() instanceof AEItemKey itemKey) {
                outputs.add(new OutputStack(itemKey.toStack(1), FluidStack.EMPTY, output.amount()));
            }
        }
        for (GenericStack output : recipe.getFluidOutputs()) {
            if (output.what() instanceof AEFluidKey fluidKey) {
                outputs.add(new OutputStack(ItemStack.EMPTY, new FluidStack(fluidKey.getFluid(), 1), output.amount()));
            }
        }
        return new MultiblockProcessingRecipe(id, id.getPath(), itemInputs, fluidInputs, List.of(), outputs, recipe.getEnergy(), recipe.getTime(), 1);
    }

    private static ItemStack normalizeItem(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty()) {
            copy.setCount(1);
        }
        return copy;
    }
}

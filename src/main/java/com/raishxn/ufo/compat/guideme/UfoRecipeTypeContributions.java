package com.raishxn.ufo.compat.guideme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import appeng.api.stacks.AEItemKey;

import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;

import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.compiler.tags.RecipeTypeMappingSupplier.RecipeTypeMappings;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytSlotGrid;
import guideme.document.block.recipes.LytStandardRecipeBox;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

public final class UfoRecipeTypeContributions implements RecipeTypeMappingSupplier {

    @Override
    public void collect(RecipeTypeMappings mappings) {
        mappings.add(ModRecipes.DMA_RECIPE_TYPE.get(), UfoRecipeTypeContributions::dmaRecipe);
    }

    private static LytStandardRecipeBox<DimensionalMatterAssemblerRecipe> dmaRecipe(
            RecipeHolder<DimensionalMatterAssemblerRecipe> holder) {
        var recipe = holder.value();

        List<Ingredient> itemInputs = new ArrayList<>();
        for (var input : recipe.getItemInputs()) {
            if (!input.isEmpty()) {
                // Preserve the recipe quantity for every item/tag alternative without
                // mutating the ingredient's shared display stacks.
                itemInputs.add(Ingredient.of(Arrays.stream(input.getIngredient().getItems())
                        .map(stack -> stack.copyWithCount(input.getAmount()))));
            }
        }

        List<ItemStack> itemOutputs = new ArrayList<>();
        for (var output : recipe.getItemOutputs()) {
            if (output.what() instanceof AEItemKey itemKey) {
                itemOutputs.add(itemKey.toStack((int) output.amount()));
            }
        }

        List<String> fluidDescriptions = new ArrayList<>();
        for (var input : recipe.getFluidInputs()) {
            if (!input.isEmpty()) {
                FluidStack[] samples = input.getIngredient().getStacks();
                if (samples.length > 0) {
                    fluidDescriptions.add(samples[0].getHoverName().getString() + " " + input.getAmount() + " mB");
                }
            }
        }
        for (var output : recipe.getFluidOutputs()) {
            fluidDescriptions.add(output.what().getDisplayName().getString() + " " + output.amount() + " mB");
        }

        var builder = LytStandardRecipeBox.builder()
                .icon(ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get())
                .title(ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get().getName().getString());
        if (!itemInputs.isEmpty()) {
            builder.input(LytSlotGrid.column(itemInputs, true));
        }
        if (!itemOutputs.isEmpty()) {
            builder.output(LytSlotGrid.columnFromStack(itemOutputs, true));
        }
        if (!fluidDescriptions.isEmpty()) {
            builder.addBottom(LytParagraph.of(Component
                    .translatable("guide.ufo.dma.fluids", String.join(", ", fluidDescriptions))
                    .getString()));
        }
        builder.addBottom(LytParagraph.of(Component.translatable(
                "jei.ufo.stellar_simulation.energy_detail",
                String.format(Locale.ROOT, "%,d", recipe.getEnergy())).getString()
                + "   "
                + Component.translatable(
                        "jei.ufo.stellar_simulation.time",
                        String.format(Locale.ROOT, "%.1fs", recipe.getTime() / 20.0)).getString()));

        return builder.build(holder);
    }
}

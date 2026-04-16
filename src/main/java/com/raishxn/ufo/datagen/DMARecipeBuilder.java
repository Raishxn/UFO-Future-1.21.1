package com.raishxn.ufo.datagen;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.util.ModTags;
import com.raishxn.ufo.recipe.UniversalMultiblockMachineKind;
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
    private static final int BULK_QMF_FACTOR = 64;
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
        saveBulkQmfMirror(output);
    }

    private void saveBulkQmfMirror(RecipeOutput output) {
        if (!this.name.startsWith("dma/")) {
            return;
        }

        if (this.itemOutputs.size() > 1 || this.fluidOutputs.size() > 1) {
            return;
        }

        var builder = UniversalMultiblockRecipeBuilder.create(
                "universal/qmf/bulk/" + this.name.substring("dma/".length()),
                UniversalMultiblockMachineKind.QMF);

        for (var input : this.itemInputs) {
            builder.inputItem(resolveSingleItem(input.getIngredient()), (long) input.getAmount() * BULK_QMF_FACTOR);
        }

        for (var input : this.fluidInputs) {
            var stacks = input.getIngredient().getStacks();
            if (stacks.length == 0) {
                return;
            }
            builder.inputFluid(stacks[0].getFluid(), (long) input.getAmount() * BULK_QMF_FACTOR);
        }

        if (!this.itemOutputs.isEmpty()) {
            var itemOutput = this.itemOutputs.getFirst();
            if (!(itemOutput.what() instanceof AEItemKey itemKey)) {
                return;
            }
            if (itemKey.toStack(1).is(ModTags.Items.CATALYST)) {
                return;
            }
            builder.outputItem(itemKey.toStack(1).getItem(), (int) (itemOutput.amount() * BULK_QMF_FACTOR));
        }

        if (!this.fluidOutputs.isEmpty()) {
            var fluidOutput = this.fluidOutputs.getFirst();
            if (!(fluidOutput.what() instanceof AEFluidKey fluidKey)) {
                return;
            }
            builder.outputFluid(fluidKey.getFluid(), fluidOutput.amount() * BULK_QMF_FACTOR);
        }

        builder.energy((long) this.energy * BULK_QMF_FACTOR)
                .time(this.time)
                .requiredTier(1)
                .save(output);
    }

    private static Item resolveSingleItem(Ingredient ingredient) {
        var stacks = ingredient.getItems();
        if (stacks.length == 0) {
            throw new IllegalStateException("Cannot mirror DMA recipe with empty ingredient");
        }
        return stacks[0].getItem();
    }
}

package com.raishxn.ufo.recipe;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.raishxn.ufo.UfoMod;

public class DimensionalMatterAssemblerRecipe implements Recipe<RecipeInput> {

    public static final ResourceLocation TYPE_ID = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "dimensional_assembly");
    // Você precisará registrar este RecipeType na sua classe de registro de receitas

    protected final List<IngredientStack.Item> itemInputs; // 9 slots
    protected final IngredientStack.Fluid fluidInput;
    protected final IngredientStack.Fluid coolantInput;
    protected final List<GenericStack> outputs; // Suporta até 2 itens e 2 fluidos misturados
    protected final int energy;
    protected final int processTime;

    public DimensionalMatterAssemblerRecipe(List<GenericStack> outputs, List<IngredientStack.Item> itemInputs,
                                            IngredientStack.Fluid fluidInput, IngredientStack.Fluid coolantInput,
                                            int energy, int processTime) {
        this.outputs = outputs;
        this.itemInputs = itemInputs;
        this.fluidInput = fluidInput;
        this.coolantInput = coolantInput;
        this.energy = energy;
        this.processTime = processTime;
    }

    @Override
    public boolean matches(@NotNull RecipeInput recipeInput, @NotNull Level level) {
        // A lógica de correspondência geralmente é feita na Entidade para máquinas complexas
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput inv, HolderLookup.@NotNull Provider registries) {
        return getResultItem(registries).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        // Retorna o primeiro item encontrado nas saídas como resultado principal
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEItemKey key) {
                return key.toStack((int) stack.amount());
            }
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> getItemOutputs() {
        List<ItemStack> items = new ArrayList<>();
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEItemKey key) {
                items.add(key.toStack((int) stack.amount()));
            }
        }
        return items;
    }

    public List<FluidStack> getFluidOutputs() {
        List<FluidStack> fluids = new ArrayList<>();
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEFluidKey key) {
                fluids.add(key.toStack((int) stack.amount()));
            }
        }
        return fluids;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return DimensionalMatterAssemblerRecipeSerializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        // Retorne o RecipeType registrado aqui. Exemplo: ModRecipeTypes.DIMENSIONAL_ASSEMBLY.get()
        throw new UnsupportedOperationException("RecipeType needs to be registered in UfoMod");
    }

    public List<IngredientStack.Item> getItemInputs() { return itemInputs; }
    public IngredientStack.Fluid getFluidInput() { return fluidInput; }
    public IngredientStack.Fluid getCoolantInput() { return coolantInput; }
    public int getEnergy() { return energy; }
    public int getProcessTime() { return processTime; }
}
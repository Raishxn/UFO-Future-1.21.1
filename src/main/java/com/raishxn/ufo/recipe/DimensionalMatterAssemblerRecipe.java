package com.raishxn.ufo.recipe;

import java.util.ArrayList;
import java.util.List;

import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.init.ModRecipes;
import org.jetbrains.annotations.NotNull;

public class DimensionalMatterAssemblerRecipe implements Recipe<RecipeInput> {

    // Os inputs
    protected final List<IngredientStack.Item> itemInputs; // 1 a 9 slots
    protected final IngredientStack.Fluid fluidInput;      // 1 slot de fluido principal
    protected final IngredientStack.Fluid coolantInput;    // 1 slot de coolant

    // Os outputs (unificados em GenericStack para facilitar serialização, mas validados)
    protected final List<GenericStack> outputs;

    protected final int energy;
    protected final int processTime;

    public DimensionalMatterAssemblerRecipe(List<GenericStack> outputs,
                                            List<IngredientStack.Item> itemInputs,
                                            IngredientStack.Fluid fluidInput,
                                            IngredientStack.Fluid coolantInput,
                                            int energy, int processTime) {
        this.outputs = outputs;
        this.itemInputs = itemInputs;
        this.fluidInput = fluidInput;
        this.coolantInput = coolantInput;
        this.energy = energy;
        this.processTime = processTime;
        validateRecipe();
    }

    private void validateRecipe() {
        if (itemInputs.size() > 9) {
            throw new IllegalArgumentException("Dimensional Matter Assembler recipe cannot have more than 9 item inputs.");
        }
        int itemCount = 0;
        int fluidCount = 0;
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEItemKey) itemCount++;
            if (stack.what() instanceof AEFluidKey) fluidCount++;
        }
        if (itemCount > 2) {
            throw new IllegalArgumentException("Dimensional Matter Assembler recipe cannot have more than 2 item outputs.");
        }
        if (fluidCount > 2) {
            throw new IllegalArgumentException("Dimensional Matter Assembler recipe cannot have more than 2 fluid outputs.");
        }
    }

    @Override
    public boolean matches(@NotNull RecipeInput recipeInput, @NotNull Level level) {
        // Lógica de matching é complexa para máquinas com muitos inputs e geralmente
        // é feita diretamente na classe da BlockEntity (Machine), não aqui.
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput inv, HolderLookup.@NotNull Provider registries) {
        // Retorna o primeiro item de saída como principal para fins de craft vanilla, se houver
        return getResultItem(registries).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        // Irrelevante para essa máquina, mas 3x3 é um bom padrão já que temos 9 inputs
        return width >= 3 && height >= 3;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        List<ItemStack> items = getItemOutputs();
        return items.isEmpty() ? ItemStack.EMPTY : items.getFirst();
    }

    // === Getters Específicos para a Máquina ===

    public List<IngredientStack.Item> getItemInputs() {
        return itemInputs;
    }

    public IngredientStack.Fluid getFluidInput() {
        return fluidInput;
    }

    public IngredientStack.Fluid getCoolantInput() {
        return coolantInput;
    }

    public List<GenericStack> getOutputs() {
        return outputs;
    }

    // Helper para obter SÓ os itens de saída (para colocar nos slots de saída de item)
    public List<ItemStack> getItemOutputs() {
        List<ItemStack> stacks = new ArrayList<>();
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEItemKey itemKey) {
                stacks.add(itemKey.toStack((int) stack.amount()));
            }
        }
        return stacks;
    }

    // Helper para obter SÓ os fluidos de saída (para colocar nos tanques de saída)
    public List<FluidStack> getFluidOutputs() {
        List<FluidStack> stacks = new ArrayList<>();
        for (GenericStack stack : outputs) {
            if (stack.what() instanceof AEFluidKey fluidKey) {
                stacks.add(fluidKey.toStack((int) stack.amount()));
            }
        }
        return stacks;
    }

    public int getEnergy() {
        return energy;
    }

    public int getProcessTime() {
        return processTime;
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
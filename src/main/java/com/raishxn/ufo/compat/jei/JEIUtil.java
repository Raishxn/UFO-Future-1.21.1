package com.raishxn.ufo.compat.jei;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.Arrays;
import java.util.List;

public class JEIUtil {

    /**
     * Converte um IngredientStack de Item do Glodium para um Ingredient do JEI.
     * (Baseado no teu exemplo advanced_ae)
     */
    public static Ingredient stackOf(IngredientStack.Item stack) {
        if (!stack.isEmpty()) {
            return Ingredient.of(Arrays.stream(stack.getIngredient().getItems())
                    .map(oldStack -> oldStack.copyWithCount(stack.getAmount())));
        }
        return Ingredient.of(ItemStack.EMPTY);
    }

    /**
     * Converte um IngredientStack de Fluido do Glodium para uma Lista de FluidStacks do JEI.
     * (Baseado no teu exemplo advanced_ae)
     */
    public static List<FluidStack> stackOf(IngredientStack.Fluid stack) {
        FluidIngredient ingredient = stack.getIngredient();
        return Arrays.stream(ingredient.getStacks())
                .map(oldStack -> oldStack.copyWithAmount(stack.getAmount()))
                .toList();
    }

    // Métodos isItem() e isFluid() removidos pois não são mais necessários.
}
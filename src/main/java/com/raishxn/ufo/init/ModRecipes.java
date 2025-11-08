package com.raishxn.ufo.init;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, UfoMod.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, UfoMod.MOD_ID);

    public static final String DMA_ID = "dimensional_assembly";

    // Serializador
    public static final Supplier<RecipeSerializer<DimensionalMatterAssemblerRecipe>> DMA_SERIALIZER =
            SERIALIZERS.register(DMA_ID, () -> DimensionalMatterAssemblerRecipeSerializer.INSTANCE);

    // Tipo de Receita
    public static final Supplier<RecipeType<DimensionalMatterAssemblerRecipe>> DMA_TYPE =
            RECIPE_TYPES.register(DMA_ID, () -> new RecipeType<DimensionalMatterAssemblerRecipe>() {
                @Override
                public String toString() {
                    return UfoMod.MOD_ID + ":" + DMA_ID;
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
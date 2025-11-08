package com.raishxn.ufo.init;

import com.raishxn.ufo.UfoMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, UfoMod.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, UfoMod.MOD_ID);

    // Registra o serializador (como ExtendedAE faz)
    public static final Supplier<RecipeSerializer<DmaRecipe>> DMA_RECIPE_SERIALIZER =
            SERIALIZERS.register("dma", DmaRecipeSerializer::new);

    // Registra o tipo (como ExtendedAE faz)
    public static final Supplier<RecipeType<DmaRecipe>> DMA_RECIPE_TYPE =
            RECIPE_TYPES.register("dma", () -> DmaRecipe.Type.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
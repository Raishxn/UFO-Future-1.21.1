package com.raishxn.ufo.recipe;

import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.NotNull;

public class DimensionalMatterAssemblerRecipeSerializer implements RecipeSerializer<DimensionalMatterAssemblerRecipe> {

    public static final DimensionalMatterAssemblerRecipeSerializer INSTANCE = new DimensionalMatterAssemblerRecipeSerializer();

    private DimensionalMatterAssemblerRecipeSerializer() {}

    // Codec para JSON
    public static final MapCodec<DimensionalMatterAssemblerRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
            GenericStack.CODEC.listOf().fieldOf("outputs").forGetter(DimensionalMatterAssemblerRecipe::getOutputs),
            IngredientStack.ITEM_CODEC.listOf().fieldOf("item_inputs").forGetter(DimensionalMatterAssemblerRecipe::getItemInputs),
            IngredientStack.FLUID_CODEC.fieldOf("fluid_input").forGetter(DimensionalMatterAssemblerRecipe::getFluidInput),
            IngredientStack.FLUID_CODEC.fieldOf("coolant_input").forGetter(DimensionalMatterAssemblerRecipe::getCoolantInput),
            Codec.INT.fieldOf("energy").forGetter(DimensionalMatterAssemblerRecipe::getEnergy),
            Codec.INT.fieldOf("process_time").forGetter(DimensionalMatterAssemblerRecipe::getProcessTime)
    ).apply(builder, DimensionalMatterAssemblerRecipe::new));

    // StreamCodec para Rede
    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionalMatterAssemblerRecipe> STREAM_CODEC = StreamCodec.composite(
            GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()), DimensionalMatterAssemblerRecipe::getOutputs,
            IngredientStack.ITEM_STREAM_CODEC.apply(ByteBufCodecs.list()), DimensionalMatterAssemblerRecipe::getItemInputs,
            IngredientStack.FLUID_STREAM_CODEC, DimensionalMatterAssemblerRecipe::getFluidInput,
            IngredientStack.FLUID_STREAM_CODEC, DimensionalMatterAssemblerRecipe::getCoolantInput,
            ByteBufCodecs.INT, DimensionalMatterAssemblerRecipe::getEnergy,
            ByteBufCodecs.INT, DimensionalMatterAssemblerRecipe::getProcessTime,
            DimensionalMatterAssemblerRecipe::new
    );

    @Override
    public @NotNull MapCodec<DimensionalMatterAssemblerRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, DimensionalMatterAssemblerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
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

import java.util.List;

public class DimensionalMatterAssemblerRecipeSerializer implements RecipeSerializer<DimensionalMatterAssemblerRecipe> {

    public static final DimensionalMatterAssemblerRecipeSerializer INSTANCE = new DimensionalMatterAssemblerRecipeSerializer();

    private DimensionalMatterAssemblerRecipeSerializer() {}

    // Codec para JSON (MapCodec não tem esse limite baixo, então continua igual)
    public static final MapCodec<DimensionalMatterAssemblerRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
            GenericStack.CODEC.listOf().fieldOf("outputs").forGetter(DimensionalMatterAssemblerRecipe::getOutputs),
            Codec.FLOAT.listOf().optionalFieldOf("chances", List.of()).forGetter(DimensionalMatterAssemblerRecipe::getOutputChances),
            IngredientStack.ITEM_CODEC.listOf().fieldOf("item_inputs").forGetter(DimensionalMatterAssemblerRecipe::getItemInputs),
            IngredientStack.FLUID_CODEC.fieldOf("fluid_input").forGetter(DimensionalMatterAssemblerRecipe::getFluidInput),
            IngredientStack.FLUID_CODEC.fieldOf("coolant_input").forGetter(DimensionalMatterAssemblerRecipe::getCoolantInput),
            Codec.INT.fieldOf("energy").forGetter(DimensionalMatterAssemblerRecipe::getEnergy),
            Codec.INT.fieldOf("process_time").forGetter(DimensionalMatterAssemblerRecipe::getProcessTime)
    ).apply(builder, DimensionalMatterAssemblerRecipe::new));

    // StreamCodec para Rede (Implementação manual para suportar mais de 6 campos)
    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionalMatterAssemblerRecipe> STREAM_CODEC = StreamCodec.of(
            DimensionalMatterAssemblerRecipeSerializer::toNetwork,
            DimensionalMatterAssemblerRecipeSerializer::fromNetwork
    );

    // Método auxiliar para ESCREVER na rede
    private static void toNetwork(RegistryFriendlyByteBuf buf, DimensionalMatterAssemblerRecipe recipe) {
        GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.getOutputs());
        ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()).encode(buf, recipe.getOutputChances());
        IngredientStack.ITEM_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.getItemInputs());
        IngredientStack.FLUID_STREAM_CODEC.encode(buf, recipe.getFluidInput());
        IngredientStack.FLUID_STREAM_CODEC.encode(buf, recipe.getCoolantInput());
        buf.writeInt(recipe.getEnergy());
        buf.writeInt(recipe.getProcessTime());
    }

    // Método auxiliar para LER da rede
    private static DimensionalMatterAssemblerRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        List<GenericStack> outputs = GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
        List<Float> chances = ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()).decode(buf);
        List<IngredientStack.Item> itemInputs = IngredientStack.ITEM_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
        IngredientStack.Fluid fluidInput = IngredientStack.FLUID_STREAM_CODEC.decode(buf);
        IngredientStack.Fluid coolantInput = IngredientStack.FLUID_STREAM_CODEC.decode(buf);
        int energy = buf.readInt();
        int processTime = buf.readInt();

        return new DimensionalMatterAssemblerRecipe(outputs, chances, itemInputs, fluidInput, coolantInput, energy, processTime);
    }

    @Override
    public @NotNull MapCodec<DimensionalMatterAssemblerRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, DimensionalMatterAssemblerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
package com.raishxn.ufo.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.pedroksl.ae2addonlib.recipes.IngredientStack;

import appeng.api.stacks.GenericStack;

/**
 * Codec-based serializer for {@link StellarSimulationRecipe}.
 * <p>
 * Follows the exact same pattern as {@link DimensionalMatterAssemblerRecipeSerializer}
 * with two additional fields: {@code cooling_level} and {@code field_tier}.
 * <p>
 * Because StreamCodec.composite() only supports up to 6 parameters, we use
 * a manual StreamCodec.of() for the 8-parameter network codec.
 * <p>
 * JSON format:
 * <pre>{@code
 * {
 *   "type": "ufo:stellar_simulation",
 *   "item_inputs": [...],
 *   "fluid_inputs": [...],
 *   "item_outputs": [...],
 *   "fluid_outputs": [...],
 *   "energy": 5000000,
 *   "time": 24000,
 *   "cooling_level": 2,
 *   "field_tier": 1
 * }
 * }</pre>
 */
public class StellarSimulationRecipeSerializer implements RecipeSerializer<StellarSimulationRecipe> {

    public static final StellarSimulationRecipeSerializer INSTANCE = new StellarSimulationRecipeSerializer();

    private StellarSimulationRecipeSerializer() {}

    // ═══════════════════════════════════════════════════════════
    //  MapCodec — JSON datapacks
    // ═══════════════════════════════════════════════════════════

    public static final MapCodec<StellarSimulationRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            IngredientStack.Item.CODEC.listOf().fieldOf("item_inputs")
                    .forGetter(StellarSimulationRecipe::getItemInputs),
            IngredientStack.Fluid.CODEC.listOf().fieldOf("fluid_inputs")
                    .forGetter(StellarSimulationRecipe::getFluidInputs),
            GenericStack.CODEC.listOf().fieldOf("item_outputs")
                    .forGetter(StellarSimulationRecipe::getItemOutputs),
            GenericStack.CODEC.listOf().fieldOf("fluid_outputs")
                    .forGetter(StellarSimulationRecipe::getFluidOutputs),
            Codec.INT.fieldOf("energy")
                    .forGetter(StellarSimulationRecipe::getEnergy),
            Codec.INT.fieldOf("time")
                    .forGetter(StellarSimulationRecipe::getTime),
            Codec.INT.optionalFieldOf("cooling_level", 0)
                    .forGetter(StellarSimulationRecipe::getCoolingLevel),
            Codec.INT.optionalFieldOf("field_tier", 1)
                    .forGetter(StellarSimulationRecipe::getFieldTier)
    ).apply(builder, StellarSimulationRecipe::new));

    // ═══════════════════════════════════════════════════════════
    //  StreamCodec — Network sync (manual, 8 params > composite max of 6)
    // ═══════════════════════════════════════════════════════════

    public static final StreamCodec<RegistryFriendlyByteBuf, StellarSimulationRecipe> STREAM_CODEC =
            StreamCodec.of(
                    StellarSimulationRecipeSerializer::encode,
                    StellarSimulationRecipeSerializer::decode
            );

    private static void encode(RegistryFriendlyByteBuf buf, StellarSimulationRecipe recipe) {
        // Item inputs
        IngredientStack.Item.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list())
                .encode(buf, recipe.getItemInputs());
        // Fluid inputs
        IngredientStack.Fluid.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list())
                .encode(buf, recipe.getFluidInputs());
        // Item outputs
        GenericStack.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list())
                .encode(buf, recipe.getItemOutputs());
        // Fluid outputs
        GenericStack.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list())
                .encode(buf, recipe.getFluidOutputs());
        // Scalars
        buf.writeInt(recipe.getEnergy());
        buf.writeInt(recipe.getTime());
        buf.writeInt(recipe.getCoolingLevel());
        buf.writeInt(recipe.getFieldTier());
    }

    private static StellarSimulationRecipe decode(RegistryFriendlyByteBuf buf) {
        var itemInputs = IngredientStack.Item.STREAM_CODEC
                .apply(net.minecraft.network.codec.ByteBufCodecs.list()).decode(buf);
        var fluidInputs = IngredientStack.Fluid.STREAM_CODEC
                .apply(net.minecraft.network.codec.ByteBufCodecs.list()).decode(buf);
        var itemOutputs = GenericStack.STREAM_CODEC
                .apply(net.minecraft.network.codec.ByteBufCodecs.list()).decode(buf);
        var fluidOutputs = GenericStack.STREAM_CODEC
                .apply(net.minecraft.network.codec.ByteBufCodecs.list()).decode(buf);
        int energy = buf.readInt();
        int time = buf.readInt();
        int coolingLevel = buf.readInt();
        int fieldTier = buf.readInt();

        return new StellarSimulationRecipe(
                itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                energy, time, coolingLevel, fieldTier);
    }

    // ═══════════════════════════════════════════════════════════
    //  RecipeSerializer interface
    // ═══════════════════════════════════════════════════════════

    @Override
    public @NotNull MapCodec<StellarSimulationRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, StellarSimulationRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

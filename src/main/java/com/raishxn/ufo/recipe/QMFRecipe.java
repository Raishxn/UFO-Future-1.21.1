package com.raishxn.ufo.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.init.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.List;

public class QMFRecipe implements Recipe<RecipeInput> {
    private final String recipeName;
    private final List<QMFRecipeIngredient> itemInputs;
    private final List<QMFFluidIngredient> fluidInputs;
    private final ItemStack itemOutput;
    private final long energy;
    private final int time;

    public QMFRecipe(String recipeName, List<QMFRecipeIngredient> itemInputs, List<QMFFluidIngredient> fluidInputs, ItemStack itemOutput, long energy, int time) {
        this.recipeName = recipeName;
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.itemOutput = itemOutput;
        this.energy = energy;
        this.time = time;
    }

    public String getRecipeName() { return recipeName; }
    public List<QMFRecipeIngredient> getItemInputs() { return itemInputs; }
    public List<QMFFluidIngredient> getFluidInputs() { return fluidInputs; }
    public long getEnergy() { return energy; }
    public int getTime() { return time; }

    @Override
    public boolean matches(RecipeInput pInput, Level pLevel) { return false; }

    @Override
    public ItemStack assemble(RecipeInput pInput, HolderLookup.Provider pRegistries) { return itemOutput.copy(); }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) { return true; }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) { return itemOutput; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.QMF_SERIALIZER.get(); }

    @Override
    public RecipeType<?> getType() { return ModRecipes.QMF_TYPE.get(); }

    public record QMFRecipeIngredient(Ingredient ingredient, long amount) {
        public static final Codec<QMFRecipeIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(QMFRecipeIngredient::ingredient),
                Codec.LONG.fieldOf("amount").forGetter(QMFRecipeIngredient::amount)
        ).apply(instance, QMFRecipeIngredient::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, QMFRecipeIngredient> STREAM_CODEC = StreamCodec.of(
            (buf, ing) -> { Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing.ingredient); buf.writeLong(ing.amount); },
            buf -> new QMFRecipeIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), buf.readLong())
        );
    }

    public record QMFFluidIngredient(FluidStack fluid, long amount) {
         public static final Codec<QMFFluidIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FluidStack.CODEC.fieldOf("fluid").forGetter(QMFFluidIngredient::fluid),
                Codec.LONG.fieldOf("amount").forGetter(QMFFluidIngredient::amount)
        ).apply(instance, QMFFluidIngredient::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, QMFFluidIngredient> STREAM_CODEC = StreamCodec.of(
            (buf, ing) -> { FluidStack.STREAM_CODEC.encode(buf, ing.fluid); buf.writeLong(ing.amount); },
            buf -> new QMFFluidIngredient(FluidStack.STREAM_CODEC.decode(buf), buf.readLong())
        );
    }

    public static class Serializer implements RecipeSerializer<QMFRecipe> {
        public static final MapCodec<QMFRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("recipe_name", "QMF Recipe").forGetter(QMFRecipe::getRecipeName),
                QMFRecipeIngredient.CODEC.listOf().fieldOf("item_inputs").forGetter(QMFRecipe::getItemInputs),
                QMFFluidIngredient.CODEC.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(QMFRecipe::getFluidInputs),
                ItemStack.CODEC.fieldOf("output").forGetter(recipe -> recipe.itemOutput),
                Codec.LONG.fieldOf("energy").forGetter(QMFRecipe::getEnergy),
                Codec.INT.fieldOf("time").forGetter(QMFRecipe::getTime)
        ).apply(instance, QMFRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, QMFRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                buf.writeUtf(recipe.recipeName);
                QMFRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.itemInputs);
                QMFFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.fluidInputs);
                ItemStack.STREAM_CODEC.encode(buf, recipe.itemOutput);
                buf.writeLong(recipe.energy);
                buf.writeInt(recipe.time);
            },
            buf -> new QMFRecipe(
                buf.readUtf(),
                QMFRecipeIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                QMFFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                ItemStack.STREAM_CODEC.decode(buf),
                buf.readLong(),
                buf.readInt()
            )
        );

        @Override
        public MapCodec<QMFRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, QMFRecipe> streamCodec() { return STREAM_CODEC; }
    }
}

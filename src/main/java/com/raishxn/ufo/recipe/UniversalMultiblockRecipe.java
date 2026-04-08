package com.raishxn.ufo.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.raishxn.ufo.api.multiblock.MultiblockMachineTier;
import com.raishxn.ufo.init.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class UniversalMultiblockRecipe implements Recipe<RecipeInput> {
    private final UniversalMultiblockMachineKind machine;
    private final String recipeName;
    private final List<ItemRequirement> itemInputs;
    private final List<FluidRequirement> fluidInputs;
    private final ItemStack itemOutput;
    private final FluidStack fluidOutput;
    private final long fluidOutputAmount;
    private final long energy;
    private final int time;
    private final int requiredTier;

    public UniversalMultiblockRecipe(
            UniversalMultiblockMachineKind machine,
            String recipeName,
            List<ItemRequirement> itemInputs,
            List<FluidRequirement> fluidInputs,
            ItemStack itemOutput,
            FluidStack fluidOutput,
            long fluidOutputAmount,
            long energy,
            int time,
            int requiredTier) {
        this.machine = machine;
        this.recipeName = recipeName;
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.itemOutput = itemOutput;
        this.fluidOutput = fluidOutput;
        this.fluidOutputAmount = Math.max(0L, fluidOutputAmount);
        this.energy = energy;
        this.time = time;
        this.requiredTier = Math.max(MultiblockMachineTier.MK1.level(), requiredTier);
    }

    public UniversalMultiblockMachineKind getMachine() {
        return machine;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public List<ItemRequirement> getItemInputs() {
        return itemInputs;
    }

    public List<FluidRequirement> getFluidInputs() {
        return fluidInputs;
    }

    public ItemStack getItemOutput() {
        return itemOutput.copy();
    }

    public FluidStack getFluidOutput() {
        return fluidOutput.copy();
    }

    public long getFluidOutputAmount() {
        return fluidOutputAmount;
    }

    public long getEnergy() {
        return energy;
    }

    public int getTime() {
        return time;
    }

    public int getRequiredTier() {
        return requiredTier;
    }

    @Override
    public boolean matches(RecipeInput pInput, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput pInput, HolderLookup.Provider pRegistries) {
        return this.itemOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return this.itemOutput;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.UNIVERSAL_MULTIBLOCK_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.UNIVERSAL_MULTIBLOCK_TYPE.get();
    }

    public record ItemRequirement(Ingredient ingredient, long amount) {
        public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemRequirement::ingredient),
                Codec.LONG.fieldOf("amount").forGetter(ItemRequirement::amount)
        ).apply(instance, ItemRequirement::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemRequirement> STREAM_CODEC = StreamCodec.of(
                (buf, ingredient) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient.ingredient);
                    buf.writeLong(ingredient.amount);
                },
                buf -> new ItemRequirement(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), buf.readLong())
        );
    }

    public record FluidRequirement(FluidStack fluid, long amount) {
        public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FluidStack.CODEC.fieldOf("fluid").forGetter(FluidRequirement::fluid),
                Codec.LONG.fieldOf("amount").forGetter(FluidRequirement::amount)
        ).apply(instance, FluidRequirement::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidRequirement> STREAM_CODEC = StreamCodec.of(
                (buf, ingredient) -> {
                    FluidStack.STREAM_CODEC.encode(buf, ingredient.fluid);
                    buf.writeLong(ingredient.amount);
                },
                buf -> new FluidRequirement(FluidStack.STREAM_CODEC.decode(buf), buf.readLong())
        );
    }

    public static class Serializer implements RecipeSerializer<UniversalMultiblockRecipe> {
        public static final MapCodec<UniversalMultiblockRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                UniversalMultiblockMachineKind.CODEC.fieldOf("machine").forGetter(UniversalMultiblockRecipe::getMachine),
                Codec.STRING.optionalFieldOf("recipe_name", "Universal Multiblock Recipe").forGetter(UniversalMultiblockRecipe::getRecipeName),
                ItemRequirement.CODEC.listOf().fieldOf("item_inputs").forGetter(UniversalMultiblockRecipe::getItemInputs),
                FluidRequirement.CODEC.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(UniversalMultiblockRecipe::getFluidInputs),
                ItemStack.CODEC.optionalFieldOf("item_output", ItemStack.EMPTY).forGetter(recipe -> recipe.itemOutput),
                FluidStack.CODEC.optionalFieldOf("fluid_output", FluidStack.EMPTY).forGetter(recipe -> recipe.fluidOutput),
                Codec.LONG.optionalFieldOf("fluid_output_amount", 0L).forGetter(UniversalMultiblockRecipe::getFluidOutputAmount),
                Codec.LONG.fieldOf("energy").forGetter(UniversalMultiblockRecipe::getEnergy),
                Codec.INT.fieldOf("time").forGetter(UniversalMultiblockRecipe::getTime),
                Codec.INT.optionalFieldOf("required_tier", MultiblockMachineTier.MK1.level()).forGetter(UniversalMultiblockRecipe::getRequiredTier)
        ).apply(instance, UniversalMultiblockRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, UniversalMultiblockRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeUtf(recipe.machine.serializedName());
                    buf.writeUtf(recipe.recipeName);
                    ItemRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.itemInputs);
                    FluidRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.fluidInputs);
                    ItemStack.STREAM_CODEC.encode(buf, recipe.itemOutput);
                    FluidStack.STREAM_CODEC.encode(buf, recipe.fluidOutput);
                    buf.writeLong(recipe.fluidOutputAmount);
                    buf.writeLong(recipe.energy);
                    buf.writeInt(recipe.time);
                    buf.writeInt(recipe.requiredTier);
                },
                buf -> new UniversalMultiblockRecipe(
                        UniversalMultiblockMachineKind.fromSerializedName(buf.readUtf()),
                        buf.readUtf(),
                        ItemRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                        FluidRequirement.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                        ItemStack.STREAM_CODEC.decode(buf),
                        FluidStack.STREAM_CODEC.decode(buf),
                        buf.readLong(),
                        buf.readLong(),
                        buf.readInt(),
                        buf.readInt()
                )
        );

        @Override
        public MapCodec<UniversalMultiblockRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UniversalMultiblockRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

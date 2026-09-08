package com.raishxn.ufo.block.entity.pattern;

import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QuantumPatternPredicates {

    private static final ResourceLocation AE2_QUARTZ_VIBRANT_GLASS = ResourceLocation.fromNamespaceAndPath("ae2", "quartz_vibrant_glass");
    private static final ResourceLocation AE2_QUARTZ_BLOCK = ResourceLocation.fromNamespaceAndPath("ae2", "quartz_block");
    private static final ResourceLocation AE2_FLUIX_BLOCK = ResourceLocation.fromNamespaceAndPath("ae2", "fluix_block");

    private QuantumPatternPredicates() {
    }

    public static Map<Character, BlockState> getDefaultCreativeStates() {
        Map<Character, BlockState> map = new HashMap<>();
        map.put('C', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get().defaultBlockState());
        map.put('F', MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().defaultBlockState());

        Block vibrantGlass = BuiltInRegistries.BLOCK.get(AE2_QUARTZ_VIBRANT_GLASS);
        if (vibrantGlass != null && vibrantGlass != Blocks.AIR) {
            map.put('G', vibrantGlass.defaultBlockState());
        }
        defaultState(AE2_QUARTZ_BLOCK).ifPresent(state -> map.put('Q', state));
        defaultState(AE2_FLUIX_BLOCK).ifPresent(state -> map.put('X', state));

        map.put('J', MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get().defaultBlockState());
        map.put('K', MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get().defaultBlockState());
        return map;
    }

    public static boolean isQuantumCasing(BlockState state) {
        return state.is(MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get());
    }

    public static boolean isUniversalHatch(BlockState state) {
        return state.is(MultiblockBlocks.QUANTUM_PATTERN_HATCH.get())
                || state.is(MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get())
                || state.is(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get())
                || state.is(MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get())
                || state.is(MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get());
    }

    public static boolean isQuantumCasingOrUniversalHatch(BlockState state) {
        return isQuantumCasing(state) || isUniversalHatch(state);
    }

    public static boolean isQuantumCasingOrMovableHatch(BlockState state) {
        return isQuantumCasing(state)
                || state.is(MultiblockBlocks.QUANTUM_PATTERN_HATCH.get())
                || state.is(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get())
                || state.is(MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get());
    }

    public static boolean isAnyFieldGenerator(BlockState state) {
        return state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get())
                || state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get())
                || state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get());
    }

    public static boolean isQuartzVibrantGlass(BlockState state) {
        return isBlock(state, AE2_QUARTZ_VIBRANT_GLASS);
    }

    public static boolean isQuartzBlock(BlockState state) {
        return isBlock(state, AE2_QUARTZ_BLOCK);
    }

    public static boolean isFluixBlock(BlockState state) {
        return isBlock(state, AE2_FLUIX_BLOCK);
    }

    private static boolean isBlock(BlockState state, ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block != null && block != Blocks.AIR && state.is(block);
    }

    private static java.util.Optional<BlockState> defaultState(ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block == null || block == Blocks.AIR
                ? java.util.Optional.empty()
                : java.util.Optional.of(block.defaultBlockState());
    }

    public static Component casingName() {
        return Component.literal("Quantum Hyper Mechanical Casing");
    }

    public static Component casingOrHatchName() {
        return Component.literal("Quantum Hyper Mechanical Casing or Universal Hatch");
    }

    public static Component casingOrMovableHatchName() {
        return Component.literal("Quantum Hyper Mechanical Casing, Quantum Pattern Hatch, ME Massive Fluid Hatch, or AE Energy Input Hatch");
    }

    public static Component patternHatchName() {
        return Component.literal("Quantum Pattern Hatch");
    }

    public static Component fieldName() {
        return Component.literal("Stellar Field Generator Mk.I or better");
    }

    public static List<BlockState> fieldCandidates() {
        return List.of(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().defaultBlockState());
    }

    public static List<BlockState> allFieldCandidates() {
        return List.of(
                MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().defaultBlockState(),
                MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get().defaultBlockState(),
                MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get().defaultBlockState()
        );
    }

    public static Component glassName() {
        return Component.literal("AE2 Quartz Vibrant Glass");
    }

    public static Component quartzBlockName() {
        return Component.literal("AE2 Quartz Block");
    }

    public static Component fluixBlockName() {
        return Component.literal("AE2 Fluix Block");
    }

    public static List<BlockState> glassCandidates() {
        Block block = BuiltInRegistries.BLOCK.get(AE2_QUARTZ_VIBRANT_GLASS);
        if (block == null || block == Blocks.AIR) {
            return List.of();
        }
        return List.of(block.defaultBlockState());
    }

    public static List<BlockState> quartzBlockCandidates() {
        return defaultState(AE2_QUARTZ_BLOCK).map(List::of).orElseGet(List::of);
    }

    public static List<BlockState> fluixBlockCandidates() {
        return defaultState(AE2_FLUIX_BLOCK).map(List::of).orElseGet(List::of);
    }

    public static List<BlockState> casingAndHatchCandidates() {
        return List.of(
                MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get().defaultBlockState(),
                MultiblockBlocks.QUANTUM_PATTERN_HATCH.get().defaultBlockState(),
                MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get().defaultBlockState(),
                MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get().defaultBlockState(),
                MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get().defaultBlockState(),
                MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get().defaultBlockState()
        );
    }

    public static List<BlockState> casingAndMovableHatchCandidates() {
        return List.of(
                MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get().defaultBlockState(),
                MultiblockBlocks.QUANTUM_PATTERN_HATCH.get().defaultBlockState(),
                MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get().defaultBlockState(),
                MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get().defaultBlockState());
    }
}

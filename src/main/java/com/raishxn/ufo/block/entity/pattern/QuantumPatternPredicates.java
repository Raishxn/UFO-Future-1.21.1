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

        return map;
    }

    public static boolean isQuantumCasing(BlockState state) {
        return state.is(MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get());
    }

    public static boolean isAnyFieldGenerator(BlockState state) {
        return state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get())
                || state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get())
                || state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get());
    }

    public static boolean isQuartzVibrantGlass(BlockState state) {
        Block block = BuiltInRegistries.BLOCK.get(AE2_QUARTZ_VIBRANT_GLASS);
        return block != null && block != Blocks.AIR && state.is(block);
    }

    public static Component casingName() {
        return Component.literal("Quantum Hyper Mechanical Casing");
    }

    public static Component patternHatchName() {
        return Component.literal("Quantum Pattern Hatch");
    }

    public static Component fieldName() {
        return Component.literal("Any Stellar Field Generator (T1/T2/T3)");
    }

    public static List<BlockState> fieldCandidates() {
        return List.of(
                MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().defaultBlockState(),
                MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get().defaultBlockState(),
                MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get().defaultBlockState()
        );
    }

    public static Component glassName() {
        return Component.literal("AE2 Quartz Vibrant Glass");
    }
}

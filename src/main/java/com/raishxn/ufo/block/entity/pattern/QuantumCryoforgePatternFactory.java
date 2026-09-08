package com.raishxn.ufo.block.entity.pattern;

import com.raishxn.ufo.api.multiblock.MultiblockCellRole;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.topology.QuantumCryoforgeTopologySchema;
import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class QuantumCryoforgePatternFactory {

    private static MultiblockDefinition definition;

    private QuantumCryoforgePatternFactory() {
    }

    public static synchronized MultiblockDefinition getDefinition() {
        if (definition == null) {
            definition = new MultiblockDefinition(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_cryoforge"),
                    QuantumCryoforgeTopologySchema.schemaVersion(),
                    Component.translatable("block.ufo.quantum_cryoforge_controller"),
                    createPattern(),
                    getDefaultCreativeStates(),
                    MultiblockDefinition.horizontalFacings(),
                    Map.of(
                            'J', MultiblockCellRole.PORT,
                            'K', MultiblockCellRole.PORT,
                            'C', MultiblockCellRole.CONTROLLER,
                            'B', MultiblockCellRole.STRUCTURE,
                            'D', MultiblockCellRole.STRUCTURE,
                            'F', MultiblockCellRole.STRUCTURE,
                            'E', MultiblockCellRole.STRUCTURE,
                            'Q', MultiblockCellRole.STRUCTURE,
                            'L', MultiblockCellRole.STRUCTURE,
                            'A', MultiblockCellRole.IGNORED));
        }
        return definition;
    }

    public static Map<Character, BlockState> getDefaultCreativeStates() {
        Map<Character, BlockState> map = new HashMap<>();
        map.put('B', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get().defaultBlockState());
        map.put('D', Blocks.BLUE_ICE.defaultBlockState());
        map.put('F', MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().defaultBlockState());

        Block vibrantGlass = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("ae2", "quartz_vibrant_glass"));
        if (vibrantGlass != null && vibrantGlass != Blocks.AIR) {
            map.put('E', vibrantGlass.defaultBlockState());
        }
        Block quartz = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("ae2", "quartz_block"));
        if (quartz != null && quartz != Blocks.AIR) map.put('Q', quartz.defaultBlockState());
        Block fluix = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("ae2", "fluix_block"));
        if (fluix != null && fluix != Blocks.AIR) map.put('L', fluix.defaultBlockState());

        map.put('J', MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get().defaultBlockState());
        map.put('K', MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get().defaultBlockState());
        return map;
    }

    public static MultiblockPattern getPattern() {
        return getDefinition().pattern();
    }

    private static MultiblockPattern createPattern() {
        MultiblockPattern.Builder builder = new MultiblockPattern.Builder()
                .strict()
                .controllerChar('C')
                .where('C', (state, level, pos) -> state.is(MultiblockBlocks.QUANTUM_CRYOFORGE_CONTROLLER.get()))
                .where('B', (state, level, pos) -> QuantumPatternPredicates.isQuantumCasingOrUniversalHatch(state),
                        QuantumPatternPredicates.casingOrHatchName())
                .candidates('B', QuantumPatternPredicates.casingAndHatchCandidates())
                .where('D', (state, level, pos) -> state.is(net.minecraft.world.level.block.Blocks.BLUE_ICE),
                        net.minecraft.network.chat.Component.literal("Blue Ice"))
                .candidates('D', net.minecraft.world.level.block.Blocks.BLUE_ICE.defaultBlockState())
                .where('F', (state, level, pos) -> QuantumPatternPredicates.isAnyFieldGenerator(state),
                        QuantumPatternPredicates.fieldName())
                .candidates('F', QuantumPatternPredicates.allFieldCandidates())
                .where('E', (state, level, pos) -> QuantumPatternPredicates.isQuartzVibrantGlass(state),
                        QuantumPatternPredicates.glassName())
                .candidates('E', QuantumPatternPredicates.glassCandidates())
                .where('Q', (state, level, pos) -> QuantumPatternPredicates.isQuartzBlock(state),
                        QuantumPatternPredicates.quartzBlockName())
                .candidates('Q', QuantumPatternPredicates.quartzBlockCandidates())
                .where('L', (state, level, pos) -> QuantumPatternPredicates.isFluixBlock(state),
                        QuantumPatternPredicates.fluixBlockName())
                .candidates('L', QuantumPatternPredicates.fluixBlockCandidates())
                .where('A', MultiblockPattern.ANY, Component.literal("Any"));
        QuantumCryoforgeTopologySchema.layers().forEach(builder::layer);
        return builder.serviceHatches('B', MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get(),
                MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get()).build();
    }
}

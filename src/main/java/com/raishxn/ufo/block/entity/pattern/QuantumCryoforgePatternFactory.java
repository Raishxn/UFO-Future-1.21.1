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
                            'C', MultiblockCellRole.CONTROLLER,
                            'B', MultiblockCellRole.STRUCTURE,
                            'D', MultiblockCellRole.STRUCTURE,
                            'F', MultiblockCellRole.STRUCTURE,
                            'E', MultiblockCellRole.STRUCTURE,
                            'A', MultiblockCellRole.AIR));
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
                .where('A', (state, level, pos) -> state.isAir());
        QuantumCryoforgeTopologySchema.layers().forEach(builder::layer);
        return builder.build();
    }
}

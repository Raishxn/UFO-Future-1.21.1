package com.raishxn.ufo.block.entity.pattern;

import com.raishxn.ufo.api.multiblock.MultiblockCellRole;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.topology.QuantumPatternFabricationMatrixTopologySchema;
import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** Canonical runtime definition for the approved Quantum Pattern Fabrication Matrix. */
public final class QuantumPatternFabricationMatrixPatternFactory {
    private static MultiblockDefinition definition;

    private QuantumPatternFabricationMatrixPatternFactory() {
    }

    public static synchronized MultiblockDefinition getDefinition() {
        if (definition == null) {
            definition = new MultiblockDefinition(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_pattern_fabrication_matrix"),
                    QuantumPatternFabricationMatrixTopologySchema.schemaVersion(),
                    Component.translatable("block.ufo.quantum_pattern_fabrication_matrix_controller"),
                    createPattern(),
                    QuantumPatternPredicates.getDefaultCreativeStates(),
                    MultiblockDefinition.horizontalFacings(),
                    Map.of(
                            'H', MultiblockCellRole.CONTROLLER,
                            'L', MultiblockCellRole.STRUCTURE,
                            'C', MultiblockCellRole.STRUCTURE,
                            'F', MultiblockCellRole.STRUCTURE,
                            'G', MultiblockCellRole.STRUCTURE,
                            'Q', MultiblockCellRole.STRUCTURE,
                            'X', MultiblockCellRole.STRUCTURE,
                            'A', MultiblockCellRole.IGNORED));
        }
        return definition;
    }

    public static MultiblockPattern getPattern() {
        return getDefinition().pattern();
    }

    public static Map<Character, BlockState> getDefaultCreativeStates() {
        return QuantumPatternPredicates.getDefaultCreativeStates();
    }

    private static MultiblockPattern createPattern() {
        MultiblockPattern.Builder builder = new MultiblockPattern.Builder()
                .strict()
                .controllerChar('H')
                .where('H', (state, level, pos) -> state.is(
                        MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get()))
                .where('L', (state, level, pos) -> QuantumPatternPredicates.isQuantumGridLink(state),
                        QuantumPatternPredicates.gridLinkName())
                .candidates('L', MultiblockBlocks.QUANTUM_GRID_LINK.get().defaultBlockState())
                .where('C', (state, level, pos) -> QuantumPatternPredicates.isQuantumCasing(state),
                        QuantumPatternPredicates.casingName())
                .candidates('C', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get().defaultBlockState())
                .where('F', (state, level, pos) -> QuantumPatternPredicates.isAnyFieldGenerator(state),
                        QuantumPatternPredicates.fieldName())
                .candidates('F', QuantumPatternPredicates.allFieldCandidates())
                .where('G', (state, level, pos) -> QuantumPatternPredicates.isQuartzVibrantGlass(state),
                        QuantumPatternPredicates.glassName())
                .candidates('G', QuantumPatternPredicates.glassCandidates())
                .where('Q', (state, level, pos) -> QuantumPatternPredicates.isQuartzBlock(state),
                        QuantumPatternPredicates.quartzBlockName())
                .candidates('Q', QuantumPatternPredicates.quartzBlockCandidates())
                .where('X', (state, level, pos) -> QuantumPatternPredicates.isFluixBlock(state),
                        QuantumPatternPredicates.fluixBlockName())
                .candidates('X', QuantumPatternPredicates.fluixBlockCandidates())
                .where('A', MultiblockPattern.ANY, Component.literal("Open interior space"));
        QuantumPatternFabricationMatrixTopologySchema.layers().forEach(builder::layer);
        return builder.build();
    }
}

package com.raishxn.ufo.block.entity.pattern;

import com.raishxn.ufo.util.UfoText;

import com.raishxn.ufo.api.multiblock.MultiblockCellRole;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.topology.InfinityFabricationSingularityTopologySchema;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** Canonical runtime definition for the approved Infinity Fabrication Singularity. */
public final class InfinityFabricationSingularityPatternFactory {
    private static MultiblockDefinition definition;

    private InfinityFabricationSingularityPatternFactory() {
    }

    public static synchronized MultiblockDefinition getDefinition() {
        if (definition == null) {
            definition = new MultiblockDefinition(
                    ResourceLocation.fromNamespaceAndPath("ufo", "infinity_fabrication_singularity"),
                    InfinityFabricationSingularityTopologySchema.schemaVersion(),
                    Component.translatable("block.ufo.infinity_fabrication_singularity_controller"),
                    createPattern(),
                    QuantumPatternPredicates.getDefaultCreativeStates(),
                    MultiblockDefinition.horizontalFacings(),
                    Map.of(
                            'H', MultiblockCellRole.CONTROLLER,
                            'L', MultiblockCellRole.STRUCTURE,
                            'K', MultiblockCellRole.PORT,
                            'C', MultiblockCellRole.STRUCTURE,
                            'F', MultiblockCellRole.STRUCTURE,
                            'G', MultiblockCellRole.STRUCTURE,
                            'Q', MultiblockCellRole.STRUCTURE,
                            'V', MultiblockCellRole.STRUCTURE,
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
                        MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get()))
                .where('L', (state, level, pos) -> QuantumPatternPredicates.isQuantumGridLink(state),
                        QuantumPatternPredicates.gridLinkName())
                .candidates('L', MultiblockBlocks.QUANTUM_GRID_LINK.get().defaultBlockState())
                .where('C', (state, level, pos) -> QuantumPatternPredicates.isQuantumCasing(state)
                                || state.is(MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get()),
                        UfoText.literal("gui.ufo.text.quantum_casing_or_fe_energy_input_hatch"))
                .candidates('C', MultiblockBlocks.QUANTUM_HYPER_MECHANICAL_CASING.get().defaultBlockState(),
                        MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get().defaultBlockState())
                .where('K', MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get())
                .candidates('K', MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get().defaultBlockState())
                .where('V', (state, level, pos) -> QuantumPatternPredicates.isGravitonCasing(state),
                        QuantumPatternPredicates.gravitonCasingName())
                .candidates('V', ModBlocks.GRAVITON_PLATED_CASING.get().defaultBlockState())
                .where('F', (state, level, pos) -> QuantumPatternPredicates.isAnyFieldGenerator(state),
                        QuantumPatternPredicates.fieldName())
                .candidates('F', QuantumPatternPredicates.allFieldCandidates())
                .uniform('F', QuantumPatternPredicates.uniformFieldName())
                .where('G', (state, level, pos) -> QuantumPatternPredicates.isQuartzVibrantGlass(state),
                        QuantumPatternPredicates.glassName())
                .candidates('G', QuantumPatternPredicates.glassCandidates())
                .where('Q', (state, level, pos) -> QuantumPatternPredicates.isQuartzBlock(state),
                        QuantumPatternPredicates.quartzBlockName())
                .candidates('Q', QuantumPatternPredicates.quartzBlockCandidates())
                .where('X', (state, level, pos) -> QuantumPatternPredicates.isFluixBlock(state),
                        QuantumPatternPredicates.fluixBlockName())
                .candidates('X', QuantumPatternPredicates.fluixBlockCandidates())
                .where('A', MultiblockPattern.ANY, UfoText.literal("gui.ufo.text.open_interior_space"));
        InfinityFabricationSingularityTopologySchema.layers().forEach(builder::layer);
        return builder.build();
    }
}

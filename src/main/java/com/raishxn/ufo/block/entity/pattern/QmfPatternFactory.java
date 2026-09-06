package com.raishxn.ufo.block.entity.pattern;

import com.raishxn.ufo.api.multiblock.MultiblockCellRole;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.topology.QmfTopologySchema;
import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class QmfPatternFactory {

    private static MultiblockDefinition definition;

    public static synchronized MultiblockDefinition getDefinition() {
        if (definition == null) {
            definition = new MultiblockDefinition(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_matter_fabricator"),
                    QmfTopologySchema.schemaVersion(),
                    Component.translatable("block.ufo.quantum_matter_fabricator_controller"),
                    createPattern(),
                    getDefaultCreativeStates(),
                    MultiblockDefinition.horizontalFacings(),
                    Map.of(
                            'J', MultiblockCellRole.PORT,
                            'K', MultiblockCellRole.PORT,
                            'H', MultiblockCellRole.CONTROLLER,
                            'C', MultiblockCellRole.STRUCTURE,
                            'F', MultiblockCellRole.STRUCTURE,
                            'G', MultiblockCellRole.STRUCTURE,
                            'Q', MultiblockCellRole.STRUCTURE,
                            'X', MultiblockCellRole.STRUCTURE,
                            'A', MultiblockCellRole.IGNORED));
        }
        return definition;
    }

    public static Map<Character, BlockState> getDefaultCreativeStates() {
        return QuantumPatternPredicates.getDefaultCreativeStates();
    }

    public static MultiblockPattern getPattern() {
        return getDefinition().pattern();
    }

    private static MultiblockPattern createPattern() {
        MultiblockPattern.Builder builder = new MultiblockPattern.Builder()
                .strict()
                .controllerChar('H')
                .where('H', (state, level, pos) -> state.is(MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER.get()))
                .where('C', (state, level, pos) -> QuantumPatternPredicates.isQuantumCasingOrMovableHatch(state), QuantumPatternPredicates.casingOrMovableHatchName())
                .candidates('C', QuantumPatternPredicates.casingAndMovableHatchCandidates())
                .where('F', (state, level, pos) -> QuantumPatternPredicates.isAnyFieldGenerator(state), QuantumPatternPredicates.fieldName())
                .candidates('F', QuantumPatternPredicates.allFieldCandidates())
                .where('G', (state, level, pos) -> QuantumPatternPredicates.isQuartzVibrantGlass(state), QuantumPatternPredicates.glassName())
                .where('Q', (state, level, pos) -> QuantumPatternPredicates.isQuartzBlock(state), QuantumPatternPredicates.quartzBlockName())
                .where('X', (state, level, pos) -> QuantumPatternPredicates.isFluixBlock(state), QuantumPatternPredicates.fluixBlockName())
                .where('A', MultiblockPattern.ANY, Component.literal("Any"));
        QmfTopologySchema.layers().forEach(builder::layer);
        return builder.serviceHatches('C', MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get(),
                MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get()).build();
    }
}

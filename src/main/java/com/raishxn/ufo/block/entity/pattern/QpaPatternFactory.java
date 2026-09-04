package com.raishxn.ufo.block.entity.pattern;

import com.raishxn.ufo.api.multiblock.MultiblockCellRole;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.topology.QpaTopologySchema;
import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class QpaPatternFactory {

    private static MultiblockDefinition definition;

    public static synchronized MultiblockDefinition getDefinition() {
        if (definition == null) {
            definition = new MultiblockDefinition(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_processor_assembler"),
                    QpaTopologySchema.schemaVersion(),
                    Component.translatable("block.ufo.quantum_processor_assembler_controller"),
                    createPattern(),
                    getDefaultCreativeStates(),
                    MultiblockDefinition.horizontalFacings(),
                    Map.of(
                            'H', MultiblockCellRole.CONTROLLER,
                            'C', MultiblockCellRole.STRUCTURE,
                            'F', MultiblockCellRole.STRUCTURE,
                            'G', MultiblockCellRole.STRUCTURE,
                            'Q', MultiblockCellRole.STRUCTURE,
                            'X', MultiblockCellRole.STRUCTURE,
                            'A', MultiblockCellRole.AIR));
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
                .where('H', (state, level, pos) -> state.is(MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get()))
                .where('C', (state, level, pos) -> QuantumPatternPredicates.isQuantumCasingOrMovableHatch(state), QuantumPatternPredicates.casingOrMovableHatchName())
                .candidates('C', QuantumPatternPredicates.casingAndMovableHatchCandidates())
                .where('F', (state, level, pos) -> QuantumPatternPredicates.isAnyFieldGenerator(state), QuantumPatternPredicates.fieldName())
                .candidates('F', QuantumPatternPredicates.allFieldCandidates())
                .where('G', (state, level, pos) -> QuantumPatternPredicates.isQuartzVibrantGlass(state), QuantumPatternPredicates.glassName())
                .where('Q', (state, level, pos) -> QuantumPatternPredicates.isQuartzBlock(state), QuantumPatternPredicates.quartzBlockName())
                .where('X', (state, level, pos) -> QuantumPatternPredicates.isFluixBlock(state), QuantumPatternPredicates.fluixBlockName())
                .where('A', (state, level, pos) -> state.isAir());
        QpaTopologySchema.layers().forEach(builder::layer);
        return builder.build();
    }
}

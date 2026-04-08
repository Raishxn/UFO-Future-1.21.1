package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.QuantumProcessorAssemblerControllerBE;
import com.raishxn.ufo.block.entity.QuantumSlicerControllerBE;
import com.raishxn.ufo.block.entity.QmfControllerBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.block.entity.pattern.QmfPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QpaPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumSlicerPatternFactory;
import com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class MultiblockControllerDefinitions {

    private MultiblockControllerDefinitions() {
    }

    public static Optional<MultiblockControllerDefinition> getDefinition(BlockEntity be) {
        if (be instanceof StellarNexusControllerBE) {
            return Optional.of(new MultiblockControllerDefinition(
                    Component.translatable("block.ufo.stellar_nexus_controller"),
                    StellarNexusPatternFactory.getPattern(),
                    StellarNexusPatternFactory.getDefaultCreativeStates()));
        }
        if (be instanceof QmfControllerBE) {
            return Optional.of(new MultiblockControllerDefinition(
                    Component.translatable("block.ufo.quantum_matter_fabricator_controller"),
                    QmfPatternFactory.getPattern(),
                    QmfPatternFactory.getDefaultCreativeStates()));
        }
        if (be instanceof QuantumSlicerControllerBE) {
            return Optional.of(new MultiblockControllerDefinition(
                    Component.translatable("block.ufo.quantum_slicer_controller"),
                    QuantumSlicerPatternFactory.getPattern(),
                    QuantumSlicerPatternFactory.getDefaultCreativeStates()));
        }
        if (be instanceof QuantumProcessorAssemblerControllerBE) {
            return Optional.of(new MultiblockControllerDefinition(
                    Component.translatable("block.ufo.quantum_processor_assembler_controller"),
                    QpaPatternFactory.getPattern(),
                    QpaPatternFactory.getDefaultCreativeStates()));
        }
        return Optional.empty();
    }

    public static boolean isSupportedController(BlockState state) {
        return state.is(MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get());
    }
}

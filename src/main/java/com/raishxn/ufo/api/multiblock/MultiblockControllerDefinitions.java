package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.QuantumProcessorAssemblerControllerBE;
import com.raishxn.ufo.block.entity.QuantumCryoforgeControllerBE;
import com.raishxn.ufo.block.entity.QuantumSlicerControllerBE;
import com.raishxn.ufo.block.entity.QuantumComputationNexusControllerBE;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import com.raishxn.ufo.block.entity.InfinityFabricationSingularityControllerBE;
import com.raishxn.ufo.block.entity.QmfControllerBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.block.entity.pattern.QmfPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QpaPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumCryoforgePatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumSlicerPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumComputationNexusPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumPatternFabricationMatrixPatternFactory;
import com.raishxn.ufo.block.entity.pattern.InfinityFabricationSingularityPatternFactory;
import com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Optional;

public final class MultiblockControllerDefinitions {

    public record PreviewEntry(ResourceLocation id, ItemStack iconStack, MultiblockControllerDefinition definition) {
    }

    private static final List<PreviewEntry> PREVIEW_ENTRIES = List.of(
            new PreviewEntry(
                    ResourceLocation.fromNamespaceAndPath("ufo", "stellar_nexus"),
                    MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get().asItem().getDefaultInstance(),
                    new MultiblockControllerDefinition(
                            Component.translatable("block.ufo.stellar_nexus_controller"),
                            StellarNexusPatternFactory.getPattern(),
                            StellarNexusPatternFactory.getDefaultCreativeStates())),
            new PreviewEntry(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_matter_fabricator"),
                    MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER.get().asItem().getDefaultInstance(),
                    MultiblockControllerDefinition.fromCompiled(QmfPatternFactory.getDefinition())),
            new PreviewEntry(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_slicer"),
                    MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get().asItem().getDefaultInstance(),
                    MultiblockControllerDefinition.fromCompiled(QuantumSlicerPatternFactory.getDefinition())),
            new PreviewEntry(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_processor_assembler"),
                    MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get().asItem().getDefaultInstance(),
                    MultiblockControllerDefinition.fromCompiled(QpaPatternFactory.getDefinition())),
            new PreviewEntry(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_cryoforge"),
                    MultiblockBlocks.QUANTUM_CRYOFORGE_CONTROLLER.get().asItem().getDefaultInstance(),
                    MultiblockControllerDefinition.fromCompiled(QuantumCryoforgePatternFactory.getDefinition())),
            new PreviewEntry(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_computation_nexus"),
                    MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get().asItem().getDefaultInstance(),
                    MultiblockControllerDefinition.fromCompiled(QuantumComputationNexusPatternFactory.getDefinition())),
            new PreviewEntry(
                    ResourceLocation.fromNamespaceAndPath("ufo", "quantum_pattern_fabrication_matrix"),
                    MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get().asItem().getDefaultInstance(),
                    MultiblockControllerDefinition.fromCompiled(
                            QuantumPatternFabricationMatrixPatternFactory.getDefinition())),
            new PreviewEntry(
                    ResourceLocation.fromNamespaceAndPath("ufo", "infinity_fabrication_singularity"),
                    MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get().asItem().getDefaultInstance(),
                    MultiblockControllerDefinition.fromCompiled(
                            InfinityFabricationSingularityPatternFactory.getDefinition()))
    );

    private MultiblockControllerDefinitions() {
    }

    public static List<PreviewEntry> getPreviewEntries() {
        return PREVIEW_ENTRIES;
    }

    public static Optional<MultiblockControllerDefinition> getDefinition(BlockEntity be) {
        if (be instanceof StellarNexusControllerBE) {
            return Optional.of(getPreviewEntries().get(0).definition());
        }
        if (be instanceof QmfControllerBE) {
            return Optional.of(getPreviewEntries().get(1).definition());
        }
        if (be instanceof QuantumSlicerControllerBE) {
            return Optional.of(getPreviewEntries().get(2).definition());
        }
        if (be instanceof QuantumProcessorAssemblerControllerBE) {
            return Optional.of(getPreviewEntries().get(3).definition());
        }
        if (be instanceof QuantumCryoforgeControllerBE) {
            return Optional.of(getPreviewEntries().get(4).definition());
        }
        if (be instanceof QuantumComputationNexusControllerBE) {
            return Optional.of(getPreviewEntries().get(5).definition());
        }
        if (be instanceof QuantumPatternFabricationMatrixControllerBE) {
            return Optional.of(getPreviewEntries().get(6).definition());
        }
        if (be instanceof InfinityFabricationSingularityControllerBE) {
            return Optional.of(getPreviewEntries().get(7).definition());
        }
        return Optional.empty();
    }

    public static Direction getPatternFacing(BlockEntity be, BlockState state) {
        if (be instanceof QuantumPatternFabricationMatrixControllerBE matrix) {
            return matrix.getStructureFacing();
        }
        if (be instanceof InfinityFabricationSingularityControllerBE singularity) {
            return singularity.getStructureFacing();
        }
        Direction facing;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } else if (state.hasProperty(DirectionalBlock.FACING)) {
            facing = state.getValue(DirectionalBlock.FACING);
        } else {
            facing = Direction.NORTH;
        }
        return facing;
    }

    public static boolean isSupportedController(BlockState state) {
        return state.is(MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_CRYOFORGE_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get())
                || state.is(MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get())
                || state.is(MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get());
    }
}

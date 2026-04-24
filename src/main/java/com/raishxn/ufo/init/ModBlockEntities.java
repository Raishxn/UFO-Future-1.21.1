package com.raishxn.ufo.init;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingBlockEntity;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.entity.QuantumEnergyCellBlockEntity;
import com.raishxn.ufo.block.entity.UfoEnergyCellBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream; // <<-- IMPORT ADICIONADO

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, UfoMod.MOD_ID);

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingBlockEntity>> MEGA_CRAFTING_UNITS_BE =
            BLOCK_ENTITIES.register("mega_crafting_units_be", () -> {
                final AtomicReference<BlockEntityType<CraftingBlockEntity>> typeHolder = new AtomicReference<>();
                var validBlocks = Stream.concat(
                        ModBlocks.CRAFTING_STORAGE_BLOCKS.values().stream(),
                        ModBlocks.CO_PROCESSOR_BLOCKS.values().stream()
                ).map(DeferredBlock::get).toArray(AEBaseEntityBlock[]::new);

                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new CraftingBlockEntity(typeHolder.get(), pos, state),
                        validBlocks
                ).build(null);

                typeHolder.set(type);

                for (var block : validBlocks) {
                    block.setBlockEntity(CraftingBlockEntity.class, type, null, null);
                }

                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity>> DIMENSIONAL_MATTER_ASSEMBLER_BE =
            BLOCK_ENTITIES.register("dimensional_matter_assembler", () -> {
                final java.util.concurrent.atomic.AtomicReference<BlockEntityType<com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity>> typeHolder = new java.util.concurrent.atomic.AtomicReference<>();
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity(typeHolder.get(), pos, state),
                        ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get()
                ).build(null);
                typeHolder.set(type);
                appeng.blockentity.AEBaseBlockEntity.registerBlockEntityItem(type, ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get().asItem());
                ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get().setBlockEntity(
                        com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity.class, 
                        type, 
                        null, 
                        (level, pos, state, be) -> be.serverTick()
                );
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UfoEnergyCellBlockEntity>> UFO_ENERGY_CELL_BE =
            BLOCK_ENTITIES.register("ufo_energy_cell", () -> {
                final AtomicReference<BlockEntityType<UfoEnergyCellBlockEntity>> typeHolder = new AtomicReference<>();
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new UfoEnergyCellBlockEntity(typeHolder.get(), pos, state),
                        ModBlocks.UFO_ENERGY_CELL.get()
                ).build(null);
                typeHolder.set(type);
                AEBaseBlockEntity.registerBlockEntityItem(type, ModBlocks.UFO_ENERGY_CELL.get().asItem());
                ((appeng.block.AEBaseEntityBlock<?>) ModBlocks.UFO_ENERGY_CELL.get()).setBlockEntity(
                        (Class) UfoEnergyCellBlockEntity.class,
                        (BlockEntityType) type,
                        null,
                        (level, pos, state, be) -> ((UfoEnergyCellBlockEntity) be).serverTick());
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuantumEnergyCellBlockEntity>> QUANTUM_ENERGY_CELL_BE =
            BLOCK_ENTITIES.register("quantum_energy_cell", () -> {
                final AtomicReference<BlockEntityType<QuantumEnergyCellBlockEntity>> typeHolder = new AtomicReference<>();
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new QuantumEnergyCellBlockEntity(typeHolder.get(), pos, state),
                        ModBlocks.QUANTUM_ENERGY_CELL.get()
                ).build(null);
                typeHolder.set(type);
                AEBaseBlockEntity.registerBlockEntityItem(type, ModBlocks.QUANTUM_ENERGY_CELL.get().asItem());
                ((appeng.block.AEBaseEntityBlock<?>) ModBlocks.QUANTUM_ENERGY_CELL.get()).setBlockEntity(
                        (Class) QuantumEnergyCellBlockEntity.class,
                        (BlockEntityType) type,
                        null,
                        (level, pos, state, be) -> ((QuantumEnergyCellBlockEntity) be).serverTick());
                return type;
            });

    // ═══════════════════════════════════════════════════════════
    //  QUANTUM MATTER FABRICATOR — Block Entities
    // ═══════════════════════════════════════════════════════════

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.QmfControllerBE>> QMF_CONTROLLER =
            BLOCK_ENTITIES.register("qmf_controller", () -> {
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.QmfControllerBE(pos, state),
                        com.raishxn.ufo.block.MultiblockBlocks.QUANTUM_MATTER_FABRICATOR_CONTROLLER.get()
                ).build(null);
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.QuantumSlicerControllerBE>> QUANTUM_SLICER_CONTROLLER_BE =
            BLOCK_ENTITIES.register("quantum_slicer_controller", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new com.raishxn.ufo.block.entity.QuantumSlicerControllerBE(pos, state),
                    com.raishxn.ufo.block.MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.QuantumProcessorAssemblerControllerBE>> QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER_BE =
            BLOCK_ENTITIES.register("quantum_processor_assembler_controller", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new com.raishxn.ufo.block.entity.QuantumProcessorAssemblerControllerBE(pos, state),
                    com.raishxn.ufo.block.MultiblockBlocks.QUANTUM_PROCESSOR_ASSEMBLER_CONTROLLER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.QuantumCryoforgeControllerBE>> QUANTUM_CRYOFORGE_CONTROLLER_BE =
            BLOCK_ENTITIES.register("quantum_cryoforge_controller", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new com.raishxn.ufo.block.entity.QuantumCryoforgeControllerBE(pos, state),
                    com.raishxn.ufo.block.MultiblockBlocks.QUANTUM_CRYOFORGE_CONTROLLER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.QuantumPatternHatchBE>> QUANTUM_PATTERN_HATCH_BE =
            BLOCK_ENTITIES.register("quantum_pattern_hatch", () -> {
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.QuantumPatternHatchBE(pos, state),
                        com.raishxn.ufo.block.MultiblockBlocks.QUANTUM_PATTERN_HATCH.get()
                ).build(null);
                ((appeng.block.AEBaseEntityBlock<?>) com.raishxn.ufo.block.MultiblockBlocks.QUANTUM_PATTERN_HATCH.get()).setBlockEntity(
                        (Class) appeng.blockentity.crafting.PatternProviderBlockEntity.class,
                        (BlockEntityType) type,
                        null,
                        null
                );
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.EntropicMachinePartBE>> ENTROPIC_MACHINE_PART_BE =
            BLOCK_ENTITIES.register("entropic_machine_part", () -> {
                final java.util.concurrent.atomic.AtomicReference<BlockEntityType<com.raishxn.ufo.block.entity.EntropicMachinePartBE>> typeHolder = new java.util.concurrent.atomic.AtomicReference<>();
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.EntropicMachinePartBE(typeHolder.get(), pos, state),
                        com.raishxn.ufo.block.MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get(),
                        com.raishxn.ufo.block.MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get()
                ).build(null);
                typeHolder.set(type);
                ((appeng.block.AEBaseEntityBlock<?>) com.raishxn.ufo.block.MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get()).setBlockEntity(
                        (Class) com.raishxn.ufo.block.entity.EntropicMachinePartBE.class,
                        (BlockEntityType) type,
                        null,
                        null
                );
                ((appeng.block.AEBaseEntityBlock<?>) com.raishxn.ufo.block.MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get()).setBlockEntity(
                        (Class) com.raishxn.ufo.block.entity.EntropicMachinePartBE.class,
                        (BlockEntityType) type,
                        null,
                        null
                );
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.EntropicAssemblerMatrixBE>> ENTROPIC_ASSEMBLER_MATRIX_BE =
            BLOCK_ENTITIES.register("entropic_assembler_matrix", () -> {
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.EntropicAssemblerMatrixBE(pos, state),
                        com.raishxn.ufo.block.MultiblockBlocks.ENTROPIC_ASSEMBLER_CASING.get()
                ).build(null);
                AEBaseBlockEntity.registerBlockEntityItem(type,
                        com.raishxn.ufo.block.MultiblockBlocks.ENTROPIC_ASSEMBLER_CASING.get().asItem());
                ((appeng.block.AEBaseEntityBlock<?>) com.raishxn.ufo.block.MultiblockBlocks.ENTROPIC_ASSEMBLER_CASING.get()).setBlockEntity(
                        (Class) com.raishxn.ufo.block.entity.EntropicAssemblerMatrixBE.class,
                        (BlockEntityType) type,
                        null,
                        null
                );
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE>> ENTROPIC_CONVERGENCE_CASING_BE =
            BLOCK_ENTITIES.register("entropic_convergence_casing", () -> {
                final java.util.concurrent.atomic.AtomicReference<BlockEntityType<com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE>> typeHolder = new java.util.concurrent.atomic.AtomicReference<>();
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE(typeHolder.get(), pos, state),
                        com.raishxn.ufo.block.MultiblockBlocks.ENTROPIC_CONVERGENCE_CASING.get()
                ).build(null);
                typeHolder.set(type);
                AEBaseBlockEntity.registerBlockEntityItem(type,
                        com.raishxn.ufo.block.MultiblockBlocks.ENTROPIC_CONVERGENCE_CASING.get().asItem());
                ((appeng.block.AEBaseEntityBlock<?>) com.raishxn.ufo.block.MultiblockBlocks.ENTROPIC_CONVERGENCE_CASING.get()).setBlockEntity(
                        (Class) com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE.class,
                        (BlockEntityType) type,
                        null,
                        null
                );
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE>> ENTROPIC_CONVERGENCE_ENGINE_BE =
            BLOCK_ENTITIES.register("entropic_convergence_engine", () -> {
                final java.util.concurrent.atomic.AtomicReference<BlockEntityType<com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE>> typeHolder = new java.util.concurrent.atomic.AtomicReference<>();
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE(typeHolder.get(), pos, state),
                        com.raishxn.ufo.block.MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get(),
                        com.raishxn.ufo.block.MultiblockBlocks.ENTROPIC_CONVERGENCE_ENGINE.get()
                ).build(null);
                typeHolder.set(type);
                ((appeng.block.AEBaseEntityBlock<?>) com.raishxn.ufo.block.MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get()).setBlockEntity(
                        (Class) com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE.class,
                        (BlockEntityType) type,
                        null,
                        null
                );
                ((appeng.block.AEBaseEntityBlock<?>) com.raishxn.ufo.block.MultiblockBlocks.ENTROPIC_CONVERGENCE_ENGINE.get()).setBlockEntity(
                        (Class) com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE.class,
                        (BlockEntityType) type,
                        null,
                        null
                );
                return type;
            });

    // ═══════════════════════════════════════════════════════════
    //  STELLAR NEXUS — Block Entities
    // ═══════════════════════════════════════════════════════════

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.StellarNexusControllerBE>> STELLAR_NEXUS_CONTROLLER_BE =
            BLOCK_ENTITIES.register("stellar_nexus_controller", () -> {
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.StellarNexusControllerBE(
                                ModBlockEntities.STELLAR_NEXUS_CONTROLLER_BE.get(), pos, state),
                        com.raishxn.ufo.block.MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get()
                ).build(null);
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.StellarNexusPartBE>> STELLAR_NEXUS_PART_BE =
            BLOCK_ENTITIES.register("stellar_nexus_part", () -> {
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.StellarNexusPartBE(
                                ModBlockEntities.STELLAR_NEXUS_PART_BE.get(), pos, state),
                        // Only field generators and non-AE2 structural blocks
                        com.raishxn.ufo.block.MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get(),
                        com.raishxn.ufo.block.MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get(),
                        com.raishxn.ufo.block.MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get()
                ).build(null);
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.raishxn.ufo.block.entity.MassiveOutputHatchBE>> ME_MASSIVE_OUTPUT_HATCH_BE =
            BLOCK_ENTITIES.register("me_massive_output_hatch", () -> {
                final java.util.concurrent.atomic.AtomicReference<BlockEntityType<com.raishxn.ufo.block.entity.MassiveOutputHatchBE>> typeHolder = new java.util.concurrent.atomic.AtomicReference<>();
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new com.raishxn.ufo.block.entity.MassiveOutputHatchBE(typeHolder.get(), pos, state),
                        com.raishxn.ufo.block.MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get(),
                        com.raishxn.ufo.block.MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get(),
                        com.raishxn.ufo.block.MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get(),
                        com.raishxn.ufo.block.MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get()
                ).build(null);
                typeHolder.set(type);
                // Register item mapping for AE2 wrench/network tool compatibility
                appeng.blockentity.AEBaseBlockEntity.registerBlockEntityItem(type,
                        com.raishxn.ufo.block.MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get().asItem());
                return type;
            });

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

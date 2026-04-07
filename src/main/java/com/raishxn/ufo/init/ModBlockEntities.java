package com.raishxn.ufo.init;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
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
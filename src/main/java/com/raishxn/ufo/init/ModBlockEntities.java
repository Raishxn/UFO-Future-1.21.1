package com.raishxn.ufo.init;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block; // <<-- IMPORT ADICIONADO
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList; // <<-- IMPORT ADICIONADO
import java.util.List; // <<-- IMPORT ADICIONADO
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream; // <<-- IMPORT ADICIONADO

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, UfoMod.MOD_ID);

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingBlockEntity>> MEGA_CRAFTING_UNITS_BE =
            BLOCK_ENTITIES.register("mega_crafting_units_be", () -> {
                final AtomicReference<BlockEntityType<CraftingBlockEntity>> typeHolder = new AtomicReference<>();

                // A correção já está aqui, unindo as duas listas de blocos
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
    public static final Supplier<BlockEntityType<DimensionalMatterAssemblerBlockEntity>> DIMENSIONAL_MATTER_ASSEMBLER_BE =
            BLOCK_ENTITIES.register("dimensional_matter_assembler_be", () ->
                    BlockEntityType.Builder.of(DimensionalMatterAssemblerBlockEntity::new,
                            ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
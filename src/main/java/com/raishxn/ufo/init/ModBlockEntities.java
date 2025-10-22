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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingBlockEntity>> MEGA_CRAFTING_UNITS_BE = // <<-- NOME ALTERADO PARA CLAREZA
            BLOCK_ENTITIES.register("mega_crafting_units_be", () -> { // <<-- NOME ALTERADO PARA CLAREZA
                final AtomicReference<BlockEntityType<CraftingBlockEntity>> typeHolder = new AtomicReference<>();

                // --- INÍCIO DA CORREÇÃO ---
                // 1. Juntamos as duas listas de blocos (Storages e Co-Processors) em uma só.
                var validBlocks = Stream.concat(
                        ModBlocks.CRAFTING_STORAGE_BLOCKS.values().stream(),
                        ModBlocks.CO_PROCESSOR_BLOCKS.values().stream()
                ).map(DeferredBlock::get).toArray(AEBaseEntityBlock[]::new);
                // --- FIM DA CORREÇÃO ---

                // Cria o BlockEntityType com a lista completa de blocos válidos
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new CraftingBlockEntity(typeHolder.get(), pos, state),
                        validBlocks
                ).build(null);

                typeHolder.set(type);

                // Associa a BlockEntityType a cada um dos blocos
                for (var block : validBlocks) {
                    block.setBlockEntity(CraftingBlockEntity.class, type, null, null);
                }

                return type;
            });

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
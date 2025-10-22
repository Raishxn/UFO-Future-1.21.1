package com.raishxn.ufo.init;

import appeng.block.AEBaseEntityBlock; // Import necessário
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

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, UfoMod.MOD_ID);

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingBlockEntity>> MEGA_CRAFTING_STORAGE_BE =
            BLOCK_ENTITIES.register("mega_crafting_storage_be", () -> {
                final AtomicReference<BlockEntityType<CraftingBlockEntity>> typeHolder = new AtomicReference<>();

                // Pega o array de blocos válidos
                var validBlocks = ModBlocks.CRAFTING_STORAGE_BLOCKS.values().stream()
                        .map(DeferredBlock::get)
                        .toArray(AEBaseEntityBlock[]::new);

                // Cria o BlockEntityType
                var type = BlockEntityType.Builder.of(
                        (pos, state) -> new CraftingBlockEntity(typeHolder.get(), pos, state),
                        validBlocks
                ).build(null);

                typeHolder.set(type);

                // --- O PASSO CRUCIAL QUE FALTAVA (LÓGICA DO MEGACELLS) ---
                // Depois de criar o tipo, fazemos um loop por todos os nossos blocos
                // e chamamos o método público setBlockEntity para fazer a ligação.
                for (var block : validBlocks) {
                    block.setBlockEntity(CraftingBlockEntity.class, type, null, null);
                }
                // ----------------------------------------------------

                return type;
            });

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
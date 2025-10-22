package com.raishxn.ufo.block;

import appeng.block.crafting.CraftingUnitBlock;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.custom.MegaCraftingStorageBlockItem; // <<-- 1. ADICIONE ESTE IMPORT
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.EnumMap;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(UfoMod.MOD_ID);

    public static final EnumMap<MegaCraftingStorageTier, DeferredBlock<CraftingUnitBlock>> CRAFTING_STORAGE_BLOCKS = new EnumMap<>(MegaCraftingStorageTier.class);

    static {
        for (var tier : MegaCraftingStorageTier.values()) {
            registerMegaCraftingBlock(tier);
        }
    }

    private static void registerMegaCraftingBlock(MegaCraftingStorageTier tier) {
        String registryName = tier.getRegistryId() + "_mega_crafting_storage";

        var registeredBlock = BLOCKS.register(registryName,
                () -> new CraftingUnitBlock(tier)
        );

        // 2. PASSE O TIER PARA O MÉTODO DE REGISTRO
        registerBlockItem(registryName, registeredBlock, tier);
        CRAFTING_STORAGE_BLOCKS.put(tier, registeredBlock);
    }

    // 3. ALTERE O MÉTODO PARA USAR A SUA CLASSE CUSTOMIZADA
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block, MegaCraftingStorageTier tier) {
        ModItems.ITEMS.register(name, () -> new MegaCraftingStorageBlockItem(block.get(), new Item.Properties(), tier));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
package com.raishxn.ufo.block;

// ... imports
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.EnumMap;
import com.raishxn.ufo.item.ModItems; // Adicione este import

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(UfoMod.MOD_ID);

    public static final EnumMap<MegaCraftingStorageTier, DeferredBlock<Block>> CRAFTING_STORAGE_BLOCKS = new EnumMap<>(MegaCraftingStorageTier.class);

    static {
        for (var tier : MegaCraftingStorageTier.values()) {
            registerMegaCraftingBlock(tier);
        }
    }

    private static void registerMegaCraftingBlock(MegaCraftingStorageTier tier) {
        String registryName = tier.getRegistryId() + "_mega_crafting_storage";

        DeferredBlock<Block> registeredBlock = BLOCKS.register(registryName,
                () -> new MegaCraftingStorageBlock(tier)
        );

        registerBlockItem(registryName, registeredBlock);
        CRAFTING_STORAGE_BLOCKS.put(tier, registeredBlock);
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
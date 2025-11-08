package com.raishxn.ufo.block;

import appeng.block.crafting.CraftingUnitBlock;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.item.ModItems;
// --- IMPORTS ADICIONADOS ---
import com.raishxn.ufo.item.custom.AnimatedNameBlockItem;
import com.raishxn.ufo.item.custom.MegaCoProcessorBlockItem;
import com.raishxn.ufo.item.custom.MegaCraftingStorageBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.EnumMap;

import static net.minecraft.world.item.Items.registerBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(UfoMod.MOD_ID);

    public static final EnumMap<MegaCraftingStorageTier, DeferredBlock<CraftingUnitBlock>> CRAFTING_STORAGE_BLOCKS = new EnumMap<>(MegaCraftingStorageTier.class);
    public static final EnumMap<MegaCoProcessorTier, DeferredBlock<CraftingUnitBlock>> CO_PROCESSOR_BLOCKS = new EnumMap<>(MegaCoProcessorTier.class);

    // Seus blocos base que chamam o método helper
    public static final DeferredBlock<Block> QUANTUM_LATTICE_FRAME = registerBlockWithAnimatedItem("quantum_lattice_frame",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> GRAVITON_PLATED_CASING = registerBlockWithAnimatedItem("graviton_plated_casing",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> WHITE_DWARF_FRAGMENT_BLOCK = registerBlockWithAnimatedItem("white_dwarf_fragment_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(6.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> PULSAR_FRAGMENT_BLOCK = PulsarBlockWithAnimatedItem("pulsar_fragment_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(6.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> NEUTRON_STAR_FRAGMENT_BLOCK = NeutronStarBlockWithAnimatedItem("neutron_star_fragment_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(6.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> DIMENSIONAL_MATTER_ASSEMBLER = registerBlockWithAnimatedItem("dimensional_matter_assembler",
            () -> new DimensionalMatterAssemblerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
                    .strength(50.0f, 1200.0f) // Propriedades do seu PDF
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> state.getValue(DimensionalMatterAssemblerBlock.ACTIVE) ? 10 : 0) // Emite luz quando ativo
            ));

    static {
        for (var tier : MegaCraftingStorageTier.values()) {
            registerMegaCraftingBlock(tier);
        }
        for (var tier : MegaCoProcessorTier.values()) {
            registerMegaCoProcessorBlock(tier);
        }
    }

    // --- MÉTODO HELPER ADICIONADO AQUI ---
    /**
     * Registra um bloco e seu respectivo item de bloco com um nome animado.
     */


    private static <T extends Block> DeferredBlock<T> registerBlockWithAnimatedItem(String name, java.util.function.Supplier<T> blockSupplier) {
        // Registra o bloco em si
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
        // Registra o item do bloco usando a classe AnimatedNameBlockItem
        ModItems.ITEMS.register(name, () -> new AnimatedNameBlockItem(block.get(), new Item.Properties(),
                ChatFormatting.WHITE,
                ChatFormatting.GRAY,
                ChatFormatting.DARK_GRAY,
                ChatFormatting.BLACK,
                ChatFormatting.DARK_GRAY,
                ChatFormatting.GRAY));
        return block;
    }

    private static <T extends Block> DeferredBlock<T> PulsarBlockWithAnimatedItem(String name, java.util.function.Supplier<T> blockSupplier) {
        // Registra o bloco em si
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
        // Registra o item do bloco usando a classe AnimatedNameBlockItem
        ModItems.ITEMS.register(name, () -> new AnimatedNameBlockItem(block.get(), new Item.Properties(),
                ChatFormatting.WHITE,
                ChatFormatting.GREEN,
                ChatFormatting.DARK_GREEN));
        return block;
    }

    private static <T extends Block> DeferredBlock<T> NeutronStarBlockWithAnimatedItem(String name, java.util.function.Supplier<T> blockSupplier) {
        // Registra o bloco em si
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
        // Registra o item do bloco usando a classe AnimatedNameBlockItem
        ModItems.ITEMS.register(name, () -> new AnimatedNameBlockItem(block.get(), new Item.Properties(),
                ChatFormatting.WHITE,
                ChatFormatting.BLUE,
                ChatFormatting.DARK_BLUE,
                ChatFormatting.AQUA));
        return block;
    }

    private static void registerMegaCoProcessorBlock(MegaCoProcessorTier tier) {
        String registryName = tier.getRegistryId() + "_mega_co_processor";
        var registeredBlock = BLOCKS.register(registryName, () -> new CraftingUnitBlock(tier));
        ModItems.ITEMS.register(registryName, () -> new MegaCoProcessorBlockItem(registeredBlock.get(), new Item.Properties(), tier));
        CO_PROCESSOR_BLOCKS.put(tier, registeredBlock);
    }

    private static void registerMegaCraftingBlock(MegaCraftingStorageTier tier) {
        String registryName = tier.getRegistryId() + "_mega_crafting_storage";
        var registeredBlock = BLOCKS.register(registryName, () -> new CraftingUnitBlock(tier));
        registerBlockItem(registryName, registeredBlock, tier);
        CRAFTING_STORAGE_BLOCKS.put(tier, registeredBlock);
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block, MegaCraftingStorageTier tier) {
        ModItems.ITEMS.register(name, () -> new MegaCraftingStorageBlockItem(block.get(), new Item.Properties(), tier));
    }


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
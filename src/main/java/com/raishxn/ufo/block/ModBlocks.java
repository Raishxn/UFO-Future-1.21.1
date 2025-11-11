package com.raishxn.ufo.block;

import appeng.block.crafting.CraftingUnitBlock;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.custom.DimensionalMatterAssemblerBlock;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.item.ModItems;
// --- IMPORTS ADICIONADOS ---
import com.raishxn.ufo.item.custom.AnimatedNameBlockItem;
import com.raishxn.ufo.item.custom.MegaCoProcessorBlockItem;
import com.raishxn.ufo.item.custom.MegaCraftingStorageBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
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
    public static final DeferredBlock<LiquidBlock> NEUTRON_STAR_FRAGMENT_FLUID_BLOCK = BLOCKS.register("neutron_star_fragment_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredBlock<LiquidBlock> PULSAR_FRAGMENT_FLUID_BLOCK = BLOCKS.register("pulsar_fragment_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredBlock<LiquidBlock> WHITE_DWARF_FRAGMENT_FLUID_BLOCK = BLOCKS.register("white_dwarf_fragment_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredBlock<LiquidBlock> LIQUID_STARLIGHT_FLUID_BLOCK = BLOCKS.register("liquid_starlight_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredBlock<LiquidBlock> PRIMORDIAL_MATTER_FLUID_BLOCK = BLOCKS.register("primordial_matter_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredBlock<LiquidBlock> RAW_STAR_MATTER_PLASMA_FLUID_BLOCK = BLOCKS.register("raw_star_matter_plasma_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredBlock<LiquidBlock> TRANSCENDING_MATTER_FLUID_BLOCK = BLOCKS.register("transcending_matter_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredBlock<LiquidBlock> UU_MATTER_FLUID_BLOCK = BLOCKS.register("uu_matter_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_UU_MATTER_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    public static final DeferredBlock<LiquidBlock> UU_AMPLIFIER_FLUID_BLOCK = BLOCKS.register("uu_amplifier_fluid_block",
            () -> new LiquidBlock((FlowingFluid) ModFluids.SOURCE_UU_AMPLIFIER_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> GELID_CRYOTHEUM_BLOCK = BLOCKS.register("gelid_cryotheum_block",
            () -> new LiquidBlock(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> TEMPORAL_FLUID_BLOCK = BLOCKS.register("temporal_fluid_block",
            () -> new LiquidBlock((FlowingFluid)ModFluids.SOURCE_TEMPORAL_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredBlock<LiquidBlock> SPATIAL_FLUID_BLOCK = BLOCKS.register("spatial_fluid_block",
            () -> new LiquidBlock((FlowingFluid)ModFluids.SOURCE_SPATIAL_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    static {
        for (var tier : MegaCraftingStorageTier.values()) {
            registerMegaCraftingBlock(tier);
        }
        for (var tier : MegaCoProcessorTier.values()) {
            registerMegaCoProcessorBlock(tier);
        }
    }
    private static <T extends Block> DeferredBlock<T> registerBlockWithAnimatedItem(String name, java.util.function.Supplier<T> blockSupplier) {
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
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
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
        ModItems.ITEMS.register(name, () -> new AnimatedNameBlockItem(block.get(), new Item.Properties(),
                ChatFormatting.WHITE,
                ChatFormatting.GREEN,
                ChatFormatting.DARK_GREEN));
        return block;
    }
    private static <T extends Block> DeferredBlock<T> NeutronStarBlockWithAnimatedItem(String name, java.util.function.Supplier<T> blockSupplier) {
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
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
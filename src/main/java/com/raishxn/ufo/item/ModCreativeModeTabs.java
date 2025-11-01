package com.raishxn.ufo.item;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UfoMod.MOD_ID);

    public static final Supplier<CreativeModeTab> UFO_ITEMS_TAB = CREATIVE_MODE_TAB.register("ufo_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get()))
                    .title(Component.translatable("creativetab.ufomod.ufo_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get());
                        output.accept(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get());
                        output.accept(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get());
                        output.accept(ModItems.TESSERACT_COMPONENT_MATRIX.get());
                        output.accept(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get());

                        output.accept(ModItems.UFO_HELMET.get());
                        output.accept(ModItems.UFO_CHESTPLATE.get());
                        output.accept(ModItems.UFO_LEGGINGS.get());
                        output.accept(ModItems.UFO_BOOTS.get());
                        output.accept(ModItems.UFO_STAFF);
                        output.accept(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get());
                        output.accept(ModItems.DIMENSIONAL_PROCESSOR.get());
                        output.accept(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get());
                        output.accept(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get());
                        output.accept(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get());
                        output.accept(ModItems.PULSAR_FRAGMENT_INGOT.get());
                        output.accept(ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get());
                        output.accept(ModItems.INFINITY_END_STONE_CELL.get());
                        output.accept(ModItems.INFINITY_NETHERRACK_CELL.get());
                        output.accept(ModItems.INFINITY_SAND_CELL.get());
                        output.accept(ModItems.INFINITY_LAVA_CELL.get());
                        output.accept(ModItems.INFINITY_SKY_STONE_CELL.get());
                        output.accept(ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get());

                        output.accept(ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get());
                        output.accept(ModItems.INFINITY_END_STONE_CELL.get());
                        output.accept(ModItems.INFINITY_NETHERRACK_CELL.get());
                        output.accept(ModItems.INFINITY_SAND_CELL.get());
                        output.accept(ModItems.INFINITY_LAVA_CELL.get());
                        output.accept(ModItems.INFINITY_SKY_STONE_CELL.get());
                        output.accept(ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get());

                        // --- NOVAS CÉLULAS INFINITAS (adicionais que você criou) ---
                        output.accept(ModItems.INFINITY_WATER_CELL.get());
                        output.accept(ModItems.INFINITY_COBBLESTONE_CELL.get());
                        output.accept(ModItems.INFINITY_PLUTONIUM_PELLET_CELL.get());
                        output.accept(ModItems.INFINITY_POLONIUM_PELLET_CELL.get());
                        output.accept(ModItems.INFINITY_HDPE_PELLET_CELL.get());
                        output.accept(ModItems.INFINITY_OBSIDIAN_CELL.get());
                        output.accept(ModItems.INFINITY_GRAVEL_CELL.get());
                        output.accept(ModItems.INFINITY_OAK_LOG_CELL.get());
                        output.accept(ModItems.INFINITY_GLASS_CELL.get());
                        output.accept(ModItems.INFINITY_AMETHYST_SHARD_CELL.get());
                        // Cells de corante
                        output.accept(ModItems.INFINITY_WHITE_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_ORANGE_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_MAGENTA_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_LIGHT_BLUE_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_YELLOW_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_LIME_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_PINK_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_GRAY_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_LIGHT_GRAY_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_CYAN_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_PURPLE_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_BLUE_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_BROWN_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_GREEN_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_RED_DYE_CELL.get());
                        output.accept(ModItems.INFINITY_BLACK_DYE_CELL.get());


                        // --- HOUSINGS DE CÉLULAS (de ModCellItems) ---
                        output.accept(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.get());
                        output.accept(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.get());
                        output.accept(ModCellItems.PULSAR_CELL_HOUSING.get());

                        // --- ITEM CELLS: WHITE DWARF (de ModCellItems) ---
                        output.accept(ModCellItems.ITEM_CELL_40M.get());
                        output.accept(ModCellItems.ITEM_CELL_100M.get());
                        output.accept(ModCellItems.ITEM_CELL_250M.get());
                        output.accept(ModCellItems.ITEM_CELL_750M.get());
                        output.accept(ModCellItems.ITEM_CELL_SINGULARITY.get());

                        // --- FLUID CELLS: NEUTRON STAR RESERVOIR (de ModCellItems) ---
                        output.accept(ModCellItems.FLUID_CELL_40M.get());
                        output.accept(ModCellItems.FLUID_CELL_100M.get());
                        output.accept(ModCellItems.FLUID_CELL_250M.get());
                        output.accept(ModCellItems.FLUID_CELL_750M.get());
                        output.accept(ModCellItems.FLUID_CELL_SINGULARITY.get());

                    }).build());

    public static final Supplier<CreativeModeTab> UFO_BLOCKS_TAB = CREATIVE_MODE_TAB.register("ufo_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get()))
                    .title(Component.translatable("creativetab.ufomod.ufo_blocks"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.WHITE_DWARF_FRAGMENT_BLOCK.get());
                        output.accept(ModBlocks.GRAVITON_PLATED_CASING.get());
                        output.accept(ModBlocks.QUANTUM_LATTICE_FRAME.get());
                        output.accept(ModBlocks.NEUTRON_STAR_FRAGMENT_BLOCK.get());
                        output.accept(ModBlocks.PULSAR_FRAGMENT_BLOCK.get());
                        ModBlocks.CRAFTING_STORAGE_BLOCKS.values().forEach(block -> output.accept(block.get()));
                        ModBlocks.CO_PROCESSOR_BLOCKS.values().forEach(block -> output.accept(block.get()));

                    }).build());




    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
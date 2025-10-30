package com.raishxn.ufo.datagen;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, UfoMod.MOD_ID, existingFileHelper);
    }

    private static final ResourceLocation UFO_LED_TEXTURE = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/storage_cell_led");
    private static final ResourceLocation GENERATED_PARENT = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID,"item/generated");

    @Override
        protected void registerModels() {
        basicItem(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get());
        basicItem(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get());
        basicItem(ModItems.TESSERACT_COMPONENT_MATRIX.get());
        basicItem(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get());
        basicItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get());
        basicItem(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get());
        basicItem(ModItems.DIMENSIONAL_PROCESSOR.get());
        basicItem(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get());
        basicItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get());
        basicItem(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.get());
        basicItem(ModItems.PULSAR_FRAGMENT_INGOT.get());

        // --- Infinity Cells ---
        basicItem(ModItems.INFINITY_WATER_CELL.get());
        basicItem(ModItems.INFINITY_COBBLESTONE_CELL.get());
        basicItem(ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get());
        basicItem(ModItems.INFINITY_END_STONE_CELL.get());
        basicItem(ModItems.INFINITY_NETHERRACK_CELL.get());
        basicItem(ModItems.INFINITY_SAND_CELL.get());
        basicItem(ModItems.INFINITY_LAVA_CELL.get());
        basicItem(ModItems.INFINITY_SKY_STONE_CELL.get());
        basicItem(ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get());

        // Mekanism
        basicItem(ModItems.INFINITY_PLUTONIUM_PELLET_CELL.get());
        basicItem(ModItems.INFINITY_POLONIUM_PELLET_CELL.get());
        basicItem(ModItems.INFINITY_HDPE_PELLET_CELL.get());

        // Minecraft
        basicItem(ModItems.INFINITY_OBSIDIAN_CELL.get());
        basicItem(ModItems.INFINITY_GRAVEL_CELL.get());
        basicItem(ModItems.INFINITY_OAK_LOG_CELL.get());
        basicItem(ModItems.INFINITY_GLASS_CELL.get());
        basicItem(ModItems.INFINITY_AMETHYST_SHARD_CELL.get());

        // Dyes
        basicItem(ModItems.INFINITY_WHITE_DYE_CELL.get());
        basicItem(ModItems.INFINITY_ORANGE_DYE_CELL.get());
        basicItem(ModItems.INFINITY_MAGENTA_DYE_CELL.get());
        basicItem(ModItems.INFINITY_LIGHT_BLUE_DYE_CELL.get());
        basicItem(ModItems.INFINITY_YELLOW_DYE_CELL.get());
        basicItem(ModItems.INFINITY_LIME_DYE_CELL.get());
        basicItem(ModItems.INFINITY_PINK_DYE_CELL.get());
        basicItem(ModItems.INFINITY_GRAY_DYE_CELL.get());
        basicItem(ModItems.INFINITY_LIGHT_GRAY_DYE_CELL.get());
        basicItem(ModItems.INFINITY_CYAN_DYE_CELL.get());
        basicItem(ModItems.INFINITY_PURPLE_DYE_CELL.get());
        basicItem(ModItems.INFINITY_BLUE_DYE_CELL.get());
        basicItem(ModItems.INFINITY_BROWN_DYE_CELL.get());
        basicItem(ModItems.INFINITY_GREEN_DYE_CELL.get());
        basicItem(ModItems.INFINITY_RED_DYE_CELL.get());
        basicItem(ModItems.INFINITY_BLACK_DYE_CELL.get());

        handheldItem(ModItems.UFO_AXE);
        handheldItem(ModItems.UFO_HOE);
       // handheldItem(ModItems.UFO_BOW);
        handheldItem(ModItems.UFO_GREATSWORD);
        handheldItem(ModItems.UFO_HAMMER);
        handheldItem(ModItems.UFO_SHOVEL);
        handheldItem(ModItems.UFO_SWORD);
        handheldItem(ModItems.UFO_PICKAXE);
        handheldItem(ModItems.UFO_FISHING_ROD);
        handheldItem(ModItems.UFO_STAFF);
        basicItem(ModItems.UFO_HELMET.get());
        basicItem(ModItems.UFO_CHESTPLATE.get());
        basicItem(ModItems.UFO_LEGGINGS.get());
        basicItem(ModItems.UFO_BOOTS.get());

            // Housings
            basicItem(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.getId());
            basicItem(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.getId());
            basicItem(ModCellItems.PULSAR_CELL_HOUSING.getId());

            // Components
            basicItem(ModCellItems.CELL_COMPONENT_40M.getId());
            basicItem(ModCellItems.CELL_COMPONENT_100M.getId());
            basicItem(ModCellItems.CELL_COMPONENT_250M.getId());
            basicItem(ModCellItems.CELL_COMPONENT_750M.getId());
            basicItem(ModCellItems.CELL_COMPONENT_INFINITY.getId());

            // Item Cells
            cellModel(ModCellItems.ITEM_CELL_40M, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_40M);
            cellModel(ModCellItems.ITEM_CELL_100M, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_100M);
            cellModel(ModCellItems.ITEM_CELL_250M, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_250M);
            cellModel(ModCellItems.ITEM_CELL_750M, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_750M);

            // Fluid Cells
            cellModel(ModCellItems.FLUID_CELL_40M, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_40M);
            cellModel(ModCellItems.FLUID_CELL_100M, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_100M);
            cellModel(ModCellItems.FLUID_CELL_250M, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_250M);
            cellModel(ModCellItems.FLUID_CELL_750M, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_750M);
    }

    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID,"item/" + item.getId().getPath()));
    }
    private ItemModelBuilder basicItem(DeferredHolder<Item, ? extends Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/" + item.getId().getPath()));
    }

    /**
     * Gera um modelo de célula com múltiplas camadas (housing, side, led).
     */
    private void cellModel(DeferredHolder<Item, ? extends Item> cell, DeferredHolder<Item, ? extends Item> housing, DeferredHolder<Item, ? extends Item> component) {
        withExistingParent(cell.getId().getPath(), ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/" + housing.getId().getPath())) // Camada base: o housing
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/" + component.getId().getPath())) // Camada do meio: o "side"
                .texture("layer2", UFO_LED_TEXTURE);// Camada de cima: o LED do AE2
    }
}
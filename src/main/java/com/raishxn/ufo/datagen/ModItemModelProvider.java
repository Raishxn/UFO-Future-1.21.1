package com.raishxn.ufo.datagen;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, UfoMod.MOD_ID, existingFileHelper);
    }
        @Override
        protected void registerModels() {
        basicItem(ModItems.BISMUTH.get());
        basicItem(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get());
        basicItem(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get());
        basicItem(ModItems.TESSERACT_COMPONENT_MATRIX.get());
        basicItem(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get());
        basicItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get());
        basicItem(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get());
        basicItem(ModItems.DIMENSIONAL_PROCESSOR.get());
        basicItem(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get());
        basicItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get());

            basicItem(ModItems.INFINITY_WATER_CELL.get());
            basicItem(ModItems.INFINITY_COBBLESTONE_CELL.get());
            basicItem(ModItems.INFINITY_COBBLED_DEEPSLATE_CELL.get());
            basicItem(ModItems.INFINITY_END_STONE_CELL.get());
            basicItem(ModItems.INFINITY_NETHERRACK_CELL.get());
            basicItem(ModItems.INFINITY_SAND_CELL.get());
            basicItem(ModItems.INFINITY_LAVA_CELL.get());
            basicItem(ModItems.INFINITY_SKY_STONE_CELL.get());
            basicItem(ModItems.INFINITY_ANTIMATTER_PELLET_CELL.get());

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
    }

    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID,"item/" + item.getId().getPath()));
    }
}
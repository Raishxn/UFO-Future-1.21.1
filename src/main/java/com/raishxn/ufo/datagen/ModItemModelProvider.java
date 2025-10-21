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
package com.raishxn.ufo.datagen;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, UfoMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.TRANSFORMABLE_ITEMS)
                .add(ModItems.BISMUTH.get())
                .add(Items.COAL)
                .add(Items.STICK)
                .add(Items.COMPASS);
        tag(ItemTags.SWORDS)
                .add(ModItems.UFO_SWORD.get())
                .add(ModItems.UFO_GREATSWORD.get());
        tag(ItemTags.PICKAXES)
                .add(ModItems.UFO_PICKAXE.get());
        tag(ItemTags.SHOVELS)
                .add(ModItems.UFO_SHOVEL.get());
        tag(ItemTags.AXES)
                .add(ModItems.UFO_AXE.get());
        tag(ItemTags.HOES)
                .add(ModItems.UFO_HOE.get());
        tag(ItemTags.FISHING_ENCHANTABLE)
                .add(ModItems.UFO_FISHING_ROD.get());
        tag(ItemTags.BOW_ENCHANTABLE)
                .add(ModItems.UFO_BOW.get());
        tag(ModTags.Items.STAFF)
                .add(ModItems.UFO_STAFF.get());
        tag(ItemTags.HEAD_ARMOR)
                .add(ModItems.UFO_HELMET.get());
        tag(ItemTags.CHEST_ARMOR)
                .add(ModItems.UFO_CHESTPLATE.get());
        tag(ItemTags.LEG_ARMOR)
                .add(ModItems.UFO_LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR)
                .add(ModItems.UFO_BOOTS.get());
    }
}
package com.raishxn.ufo.util;

import com.raishxn.ufo.UfoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_UFO_TOOL = createTag("needs_ufo_tool");
        public static final TagKey<Block> INCORRECT_FOR_UFO_TOOL = createTag("incorrect_for_ufo_tool");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, name));
        }
    }


    public static class Items {
        public static final TagKey<Item> TRANSFORMABLE_ITEMS = createTag("transformable_items");
        public static final TagKey<Item> STAFF = createTag("staff");
        public static final TagKey<Item> INGREDIENTS_UFO = createTag("ingredients/ufo");
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, name));
        }
    }
}
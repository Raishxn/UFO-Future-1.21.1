package com.raishxn.ufo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class AnimatedNameBlockItem extends BlockItem {

    private final ChatFormatting[] colors;

    public AnimatedNameBlockItem(Block block, Properties props, ChatFormatting... colors) {
        super(block, props);
        this.colors = colors;
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent name = Component.translatable(stack.getDescriptionId());
        String text = name.getString();

        if (this.colors == null || this.colors.length == 0) {
            return name;
        }

        MutableComponent coloredName = Component.empty();
        long time = Util.getMillis();

        for (int i = 0; i < text.length(); i++) {
            int colorIndex = (int) (i * 0.5 + time / 200.0) % this.colors.length;
            coloredName.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(this.colors[colorIndex]));
        }

        return coloredName;
    }
}
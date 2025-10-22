package com.raishxn.ufo.item.custom;

import appeng.block.AEBaseBlockItem;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class MegaCraftingStorageBlockItem extends AEBaseBlockItem {

    private final MegaCraftingStorageTier tier;

    public MegaCraftingStorageBlockItem(Block block, Properties props, MegaCraftingStorageTier tier) {
        super(block, props);
        this.tier = tier;
    }

    // O método para pegar o tier é importante para o evento
    public MegaCraftingStorageTier getTier() {
        return this.tier;
    }

    // Seu método getName com as cores continua aqui, sem problemas.
    @Override
    public Component getName(ItemStack stack) {
        // ... seu código de nome colorido ...
        MutableComponent name = Component.translatable(stack.getDescriptionId());
        String text = name.getString();
        ChatFormatting[] colors;
        switch (this.tier) {
            case STORAGE_1B: colors = new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.RED, ChatFormatting.GOLD}; break;
            case STORAGE_50B: colors = new ChatFormatting[]{ChatFormatting.DARK_BLUE, ChatFormatting.BLUE}; break;
            case STORAGE_1T: colors = new ChatFormatting[]{ChatFormatting.DARK_AQUA, ChatFormatting.AQUA, ChatFormatting.BLUE}; break;
            case STORAGE_250T: colors = new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE}; break;
            case STORAGE_1QD: colors = new ChatFormatting[]{ChatFormatting.DARK_GREEN, ChatFormatting.GREEN}; break;
            default: return name;
        }
        MutableComponent coloredName = Component.empty();
        long time = Util.getMillis();
        for (int i = 0; i < text.length(); i++) {
            int colorIndex = (int) (i * 0.5 + time / 200.0) % colors.length;
            coloredName.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(colors[colorIndex]));
        }
        return coloredName;
    }
}
package com.raishxn.ufo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Catalisador Criativo.
 * Mostra stats do modo criativo.
 */
public class DimensionalCatalystItem extends Item {

    public DimensionalCatalystItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        if (Screen.hasShiftDown()) {
            components.add(Component.literal("CREATIVE MODE CATALYST").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            components.add(Component.literal("")); // Espaçador
            components.add(Component.literal("Effects:").withStyle(ChatFormatting.AQUA));
            components.add(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Instant Crafting").withStyle(ChatFormatting.GREEN)));
            components.add(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("No Energy/Resource Cost").withStyle(ChatFormatting.GREEN)));
            components.add(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("100% Bonus Drop").withStyle(ChatFormatting.GREEN)));
            components.add(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Never Fails").withStyle(ChatFormatting.GREEN)));
            components.add(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Resets Machine Temperature").withStyle(ChatFormatting.AQUA))); //

        } else {
            components.add(Component.literal("Hold <").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("SHIFT").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                    .append(Component.literal("> for details.").withStyle(ChatFormatting.DARK_GRAY)));
        }

        super.appendHoverText(stack, context, components, flag);
    }
}
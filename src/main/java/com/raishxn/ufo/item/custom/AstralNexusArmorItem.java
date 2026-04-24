package com.raishxn.ufo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AstralNexusArmorItem extends ArmorItem {

    public AstralNexusArmorItem(Holder<ArmorMaterial> material, Type type, Item.Properties properties) {
        super(material, type, properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Infinite Armor").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Absolute immunity to damage and death").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Reflects damage at 1,000,000x").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("Night Vision, Water Breathing, Step Assist").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Creative Flight and Mekanism Radiation Immunity").withStyle(ChatFormatting.BLUE));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}

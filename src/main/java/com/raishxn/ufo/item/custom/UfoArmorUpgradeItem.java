package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.armor.UfoArmorSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class UfoArmorUpgradeItem extends Item {
    private final UfoArmorModule module;

    public UfoArmorUpgradeItem(Properties properties, UfoArmorModule module) {
        super(properties.stacksTo(1));
        this.module = module;
    }

    public UfoArmorModule module() {
        return module;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (module == null) {
            tooltip.add(Component.translatable("tooltip.ufo.upgrade_card.blank").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.ufo.module." + module.id()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.ufo.module.slot", Component.translatable(module.armorType().getName()))
                    .withStyle(ChatFormatting.DARK_GRAY));
            List<UfoArmorSetting> settings = UfoArmorSetting.forModule(module);
            if (!settings.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.ufo.module.configurable").withStyle(ChatFormatting.AQUA));
                for (UfoArmorSetting setting : settings) {
                    tooltip.add(Component.literal(" • ")
                            .append(Component.translatable(setting.translationKey()))
                            .withStyle(ChatFormatting.DARK_AQUA));
                }
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}

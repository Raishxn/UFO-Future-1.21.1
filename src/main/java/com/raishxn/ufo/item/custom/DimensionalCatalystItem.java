package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.util.UfoText;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import appeng.items.materials.UpgradeCardItem;

import java.util.List;

/**
 * Catalisador Criativo.
 * Extends UpgradeCardItem for AE2 upgrade slot compatibility.
 */
public class DimensionalCatalystItem extends UpgradeCardItem {

    public DimensionalCatalystItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return CatalystUpgradeUseHelper.tryInstallHeldCatalyst(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        if (Screen.hasShiftDown()) {
            components.add(UfoText.literal("gui.ufo.text.creative_mode_catalyst").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            components.add(UfoText.literal("")); // Espaçador
            components.add(UfoText.literal("gui.ufo.text.effects").withStyle(ChatFormatting.AQUA));
            components.add(UfoText.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(UfoText.literal("gui.ufo.text.instant_crafting").withStyle(ChatFormatting.GREEN)));
            components.add(UfoText.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(UfoText.literal("gui.ufo.text.no_energy_cost").withStyle(ChatFormatting.GREEN)));
            components.add(UfoText.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(UfoText.literal("gui.ufo.text.no_heat_generation").withStyle(ChatFormatting.GREEN)));
            components.add(UfoText.literal(" - ").withStyle(ChatFormatting.GRAY)
                    .append(UfoText.literal("gui.ufo.text.still_consumes_recipe_inputs").withStyle(ChatFormatting.AQUA)));

        } else {
            components.add(UfoText.literal("gui.ufo.text.hold").withStyle(ChatFormatting.DARK_GRAY)
                    .append(UfoText.literal("gui.ufo.text.shift").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                    .append(UfoText.literal("gui.ufo.text.for_details").withStyle(ChatFormatting.DARK_GRAY)));
        }

        super.appendHoverText(stack, context, components, flag);
    }
}

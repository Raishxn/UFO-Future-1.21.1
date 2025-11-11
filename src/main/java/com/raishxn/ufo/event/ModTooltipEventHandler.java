package com.raishxn.ufo.event;

import com.raishxn.ufo.item.custom.MegaCoProcessorBlockItem;
import com.raishxn.ufo.item.custom.MegaCraftingStorageBlockItem;
import com.raishxn.ufo.util.NumberFormattingUtil; // <-- IMPORTAR A NOVA CLASSE
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = "ufo", value = Dist.CLIENT)
public class ModTooltipEventHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof MegaCraftingStorageBlockItem item) {
            var tier = item.getTier();
            long capacity = tier.getStorageBytes();
            MutableComponent capacityLine = Component.translatable(
                    "tooltip.ufo.capacity",
                    NumberFormattingUtil.formatBytes(capacity) + "B"
            );
            capacityLine.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            event.getToolTip().add(capacityLine);
            if (Screen.hasShiftDown()) {
                MutableComponent exactCapacityLine = Component.translatable(
                        "tooltip.ufo.capacity_exact",
                        NumberFormattingUtil.formatNumberWithCommas(capacity) + " Bytes"
                );
                exactCapacityLine.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
                event.getToolTip().add(exactCapacityLine);
            } else {
                event.getToolTip().add(Component.translatable("tooltip.ufo.press_shift").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        else if (event.getItemStack().getItem() instanceof MegaCoProcessorBlockItem item) {
            var tier = item.getTier();
            String formattedThreads = tier.getDisplayName();
            MutableComponent threadsLine = Component.translatable("tooltip.ufo.accelerator_threads", formattedThreads);
            threadsLine.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            event.getToolTip().add(threadsLine);
        }
    }
}
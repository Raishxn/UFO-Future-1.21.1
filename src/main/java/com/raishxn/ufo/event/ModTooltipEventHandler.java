package com.raishxn.ufo.event;

// REMOVA O IMPORT DO AE2 QUE ESTAVA DANDO ERRO
// import appeng.api.storage.MemoryConversion;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.MegaCraftingStorageBlockItem;
import com.raishxn.ufo.util.NumberFormattingUtil; // <-- 1. IMPORTE A NOSSA NOVA CLASSE
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ModTooltipEventHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof MegaCraftingStorageBlockItem item) {

            var tier = item.getTier();

            // Linha 1: Capacidade simples (continua igual)
            MutableComponent capacityLine = Component.translatable("tooltip.ufo.capacity", tier.getDisplayName());
            capacityLine.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            event.getToolTip().add(capacityLine);

            // --- INÍCIO DA CORREÇÃO FINAL E DEFINITIVA ---
            long bytes = tier.getStorageBytes();

            // 2. Chame o nosso método estático para formatar a string
            String formattedBytes = NumberFormattingUtil.formatBytes(bytes);

            // Crie a linha completa do tooltip com a string formatada
            MutableComponent exactCapacityLine = Component.translatable("tooltip.ufo.capacity_exact", formattedBytes);
            exactCapacityLine.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
            event.getToolTip().add(exactCapacityLine);
            // --- FIM DA CORREÇÃO ---
        }
    }
}
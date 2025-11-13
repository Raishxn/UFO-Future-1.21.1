package com.raishxn.ufo.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen; // <-- IMPORT NECESSÁRIO
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Classe base para os 12 catalisadores T1-T3.
 * Cuida automaticamente da lógica da tooltip (com SHIFT).
 */
public class BaseCatalystItem extends Item {

    protected final String family;
    protected final int tier;

    public BaseCatalystItem(Properties properties, String family, int tier) {
        super(properties);
        this.family = family;
        this.tier = tier;
    }

    // Usando a assinatura de método moderna (1.21.1+)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        // 1. Verifica se o SHIFT está pressionado
        if (Screen.hasShiftDown()) {

            // --- Pega os stats base (do seu DMAThermalHandler) ---
            double baseStat = 0;
            String statName = "Unknown";
            double tierMultiplier = 1.0;
            int staticHeat = 0;

            if (tier == 2) tierMultiplier = 2.5; //
            if (tier == 3) tierMultiplier = 5.0; //

            switch (family) {
                case "matterflow" -> {
                    baseStat = -10.0; //
                    statName = "Energy Cost";
                    if (tier == 1) staticHeat = 50;   //
                    if (tier == 2) staticHeat = 100;  //
                    if (tier == 3) staticHeat = 200;  //
                }
                case "chrono" -> {
                    baseStat = 25.0; //
                    statName = "Speed";
                    if (tier == 1) staticHeat = 100;  //
                    if (tier == 2) staticHeat = 250;  //
                    if (tier == 3) staticHeat = 400;  //
                }
                case "overflux" -> {
                    baseStat = -10.0; //
                    statName = "Failure Chance";
                    if (tier == 1) staticHeat = -50;  //
                    if (tier == 2) staticHeat = -100; //
                    if (tier == 3) staticHeat = -200; //
                }
                case "quantum" -> {
                    baseStat = 10.0; //
                    statName = "Bonus Drop Chance";
                    if (tier == 1) staticHeat = 75;   //
                    if (tier == 2) staticHeat = 150;  //
                    if (tier == 3) staticHeat = 300;  //
                }
            }

            // --- Constrói a Tooltip ---
            components.add(Component.literal("Family: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(capitalize(family)).withStyle(ChatFormatting.AQUA)));
            components.add(Component.literal("Tier: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(tier)).withStyle(ChatFormatting.AQUA)));

            components.add(Component.literal("")); // Espaçador

            // Efeito Principal
            double finalStat = baseStat * tierMultiplier;
            String sign = finalStat > 0 ? "+" : "";
            ChatFormatting statColor = (finalStat > 0) ? ChatFormatting.GREEN : ChatFormatting.RED;
            String statText = String.format("%s%.1f%% %s", sign, finalStat, statName);

            components.add(Component.literal("Effect: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(statText).withStyle(statColor)));

            // Efeito Térmico
            String heatSign = staticHeat > 0 ? "+" : "";
            ChatFormatting heatColor = (staticHeat > 0) ? ChatFormatting.RED : ChatFormatting.BLUE;
            String heatText = String.format("%s%d °C Heat", heatSign, staticHeat);

            components.add(Component.literal("Thermal: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(heatText).withStyle(heatColor)));

            // Efeito de Stacking
            components.add(Component.literal("")); // Espaçador
            components.add(Component.literal("Stacking Effect (Soft Cap):").withStyle(ChatFormatting.GOLD));
            components.add(Component.literal(" 2x: ").withStyle(ChatFormatting.GRAY).append(Component.literal("175%").withStyle(ChatFormatting.WHITE))); //
            components.add(Component.literal(" 3x: ").withStyle(ChatFormatting.GRAY).append(Component.literal("225%").withStyle(ChatFormatting.WHITE))); //
            components.add(Component.literal(" 4x: ").withStyle(ChatFormatting.GRAY).append(Component.literal("250%").withStyle(ChatFormatting.WHITE))); //


        } else {
            // 2. Adiciona o prompt "Pressione SHIFT"
            components.add(Component.literal("Hold <").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("SHIFT").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                    .append(Component.literal("> for details.").withStyle(ChatFormatting.DARK_GRAY)));
        }

        super.appendHoverText(stack, context, components, flag);
    }

    // Helper para "matterflow" -> "Matterflow"
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
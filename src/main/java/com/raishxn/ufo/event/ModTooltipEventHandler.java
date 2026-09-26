package com.raishxn.ufo.event;

import com.raishxn.ufo.util.UfoText;

import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.custom.MegaCoProcessorBlockItem;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.custom.MegaCraftingStorageBlockItem;
import com.raishxn.ufo.util.NumberFormattingUtil; // <-- IMPORTAR A NOVA CLASSE
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = "ufo", value = Dist.CLIENT)
public class ModTooltipEventHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof MegaCraftingStorageBlockItem item) {
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
        else if (stack.getItem() instanceof MegaCoProcessorBlockItem item) {
            var tier = item.getTier();
            String formattedThreads = tier.getDisplayName();
            MutableComponent threadsLine = Component.translatable("tooltip.ufo.accelerator_threads", formattedThreads);
            threadsLine.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            event.getToolTip().add(threadsLine);
        }
        else if (stack.is(ModBlocks.QUANTUM_ENERGY_CELL.get().asItem())) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            boolean chargedPreview = customData != null
                    && customData.copyTag().getBoolean("ufoQuantumEnergyCellChargedPreview");
            event.getToolTip().add(UfoText.literal(chargedPreview
                    ? "Creative preview: Charged"
                    : "Creative preview: Discharged").withStyle(ChatFormatting.DARK_GRAY));
            event.getToolTip().add(Component.translatable("tooltip.ufo.stored_energy_infinite").withStyle(ChatFormatting.GRAY));
        }
        else if (stack.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().asItem())) {
            event.getToolTip().add(UfoText.literal("gui.ufo.text.universal_field_tier_mk1").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.nexus_charge_500k_ae_t_nexus_fields_must_all_match").withStyle(ChatFormatting.DARK_GRAY));
        }
        else if (stack.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get().asItem())) {
            event.getToolTip().add(UfoText.literal("gui.ufo.text.universal_field_tier_mk2").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.over_tier_universal_recipes_run_faster_and_cheaper").withStyle(ChatFormatting.DARK_GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.nexus_charge_1m_ae_t_nexus_fields_must_all_match").withStyle(ChatFormatting.DARK_GRAY));
        }
        else if (stack.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get().asItem())) {
            event.getToolTip().add(UfoText.literal("gui.ufo.text.universal_field_tier_mk3").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.required_for_stable_coolant_in_the_quantum_cryoforge").withStyle(ChatFormatting.DARK_GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.nexus_charge_2m_ae_t_nexus_fields_must_all_match").withStyle(ChatFormatting.DARK_GRAY));
        }
        else if (stack.is(MultiblockBlocks.QUANTUM_CRYOFORGE_CONTROLLER.get().asItem())) {
            event.getToolTip().add(UfoText.literal("gui.ufo.text.stable_coolant_requires_machine_tier_mk3").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.use_mk3_field_generators_in_every_field_position").withStyle(ChatFormatting.DARK_GRAY));
        }
        else if (stack.is(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get().asItem())) {
            event.getToolTip().add(UfoText.literal("gui.ufo.text.hybrid_coolant_hatch_16_000_000_mb_local_tank_me_fallbac").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.external_fluid_pipes_may_fill_it_from_any_side_connect_m").withStyle(ChatFormatting.DARK_GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.accepts_gelid_cryotheum_stable_coolant_or_bose_einstein").withStyle(ChatFormatting.DARK_GRAY));
        }
        else if (isAeHatch(stack)) {
            event.getToolTip().add(UfoText.literal("gui.ufo.text.ae2_grid_hatch_connect_me_cable_to_the_indicated_face").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.items_and_ae_are_read_from_me_storage_not_sided_pipes").withStyle(ChatFormatting.DARK_GRAY));
        }
        else if (stack.is(ModItems.STABLE_COOLANT_BUCKET.get())) {
            event.getToolTip().add(UfoText.literal("gui.ufo.text.stable_coolant_50_hu_mb_up_to_10_mb_tick").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.crafted_in_the_quantum_cryoforge_at_machine_tier_mk3").withStyle(ChatFormatting.DARK_GRAY));
        }
        else if (stack.is(ModItems.BOSE_EINSTEIN_CONDENSATE_BUCKET.get())) {
            event.getToolTip().add(UfoText.literal("gui.ufo.text.bose_einstein_condensate_200_hu_mb_up_to_10_mb_tick").withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(UfoText.literal("gui.ufo.text.extreme_tier_coolant_for_mk3_thermal_systems").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static boolean isAeHatch(ItemStack stack) {
        return stack.is(MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get().asItem())
                || stack.is(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get().asItem())
                || stack.is(MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get().asItem())
                || stack.is(MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get().asItem());
    }
}

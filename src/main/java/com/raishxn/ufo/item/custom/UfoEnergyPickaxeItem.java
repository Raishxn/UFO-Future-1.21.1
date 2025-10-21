package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.util.EnergyToolHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class UfoEnergyPickaxeItem extends PickaxeItem implements IEnergyTool, IHasModeHUD, IHasCycleableModes {

    private static final int ENERGY_COST_NORMAL = 200;
    private static final int ENERGY_COST_FAST = 2000;

    public UfoEnergyPickaxeItem(Tier pTier, Properties pProperties) {
        super(pTier, pProperties.stacksTo(1));
    }

    // --- CORREÇÃO DO NOME RAINBOW ---
    @Override
    public Component getName(ItemStack stack) {
        return IEnergyTool.super.getName(stack);
    }

    @Override
    public void cycleMode(ItemStack stack, Player player) {
        boolean isFast = stack.getOrDefault(ModDataComponents.FAST_MODE.get(), false);
        boolean newMode = !isFast;
        stack.set(ModDataComponents.FAST_MODE.get(), newMode);

        Component modeText = newMode ?
                Component.translatable("tooltip.ufo.mode.fast").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)) :
                Component.translatable("tooltip.ufo.mode.normal").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));

    }

    @Override
    public Component getModeHudComponent(ItemStack stack) {
        boolean isFast = stack.getOrDefault(ModDataComponents.FAST_MODE.get(), false);

        Component modeText = isFast ?
                Component.translatable("tooltip.ufo.mode.fast").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)) :
                Component.translatable("tooltip.ufo.mode.normal").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));

        return Component.translatable("tooltip.ufo.current_mode", modeText);
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        boolean isFast = pStack.getOrDefault(ModDataComponents.FAST_MODE.get(), false);
        IEnergyStorage energy = pStack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy != null) {
            if (isFast && energy.getEnergyStored() >= ENERGY_COST_FAST) {
                return Float.MAX_VALUE;
            } else if (!isFast && energy.getEnergyStored() >= ENERGY_COST_NORMAL) {
                return super.getDestroySpeed(pStack, pState);
            }
        }
        return 1.0F;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(getModeHudComponent(pStack));
        IEnergyTool.super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) { return EnergyToolHelper.isBarVisible(pStack); }
    @Override
    public int getBarWidth(ItemStack pStack) { return EnergyToolHelper.getBarWidth(pStack); }
    @Override
    public int getBarColor(ItemStack pStack) { return EnergyToolHelper.getBarColor(pStack); }
    @Override
    public int getEnergyPerUse() { return ENERGY_COST_NORMAL; }
}
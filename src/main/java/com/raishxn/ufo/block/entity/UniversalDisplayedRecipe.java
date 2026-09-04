package com.raishxn.ufo.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public record UniversalDisplayedRecipe(
        ItemStack itemIcon,
        FluidStack fluidIcon,
        Component label,
        long outputAmount,
        int progress,
        int maxProgress,
        int processIndex,
        boolean paused) {

    public UniversalDisplayedRecipe(
            ItemStack itemIcon,
            FluidStack fluidIcon,
            Component label,
            long outputAmount,
            int progress,
            int maxProgress) {
        this(itemIcon, fluidIcon, label, outputAmount, progress, maxProgress, -1, false);
    }

    public UniversalDisplayedRecipe {
        itemIcon = itemIcon == null ? ItemStack.EMPTY : itemIcon;
        fluidIcon = fluidIcon == null ? FluidStack.EMPTY : fluidIcon;
        label = label == null ? Component.empty() : label;
        outputAmount = Math.max(0, outputAmount);
        progress = Math.max(0, progress);
        maxProgress = Math.max(0, maxProgress);
        processIndex = Math.max(-1, processIndex);
    }
}

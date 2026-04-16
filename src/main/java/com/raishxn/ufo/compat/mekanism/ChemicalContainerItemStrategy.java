package com.raishxn.ufo.compat.mekanism;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import mekanism.api.Action;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ChemicalContainerItemStrategy implements ContainerItemStrategy<UfoMekanismKey, ItemStack> {
    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        var handler = stack.getCapability(UfoMekCapabilities.CHEMICAL.item());
        if (handler == null) {
            return null;
        }

        var chemical = handler.extractChemical(Long.MAX_VALUE, Action.SIMULATE);
        var key = UfoMekanismKey.of(chemical);
        return key == null ? null : new GenericStack(key, chemical.getAmount());
    }

    @Override
    public @Nullable ItemStack findCarriedContext(Player player, AbstractContainerMenu menu) {
        var carried = menu.getCarried();
        if (carried.getCapability(UfoMekCapabilities.CHEMICAL.item()) != null) {
            return carried;
        }

        var mainHand = player.getMainHandItem();
        if (mainHand.getCapability(UfoMekCapabilities.CHEMICAL.item()) != null) {
            return mainHand;
        }

        var offHand = player.getOffhandItem();
        return offHand.getCapability(UfoMekCapabilities.CHEMICAL.item()) != null ? offHand : null;
    }

    @Override
    public @Nullable ItemStack findPlayerSlotContext(Player player, int slot) {
        var stack = player.getInventory().getItem(slot);
        return stack.getCapability(UfoMekCapabilities.CHEMICAL.item()) != null ? stack : null;
    }

    @Override
    public long extract(ItemStack context, UfoMekanismKey what, long amount, Actionable mode) {
        var handler = context.getCapability(UfoMekCapabilities.CHEMICAL.item());
        if (handler == null) {
            return 0L;
        }
        return handler.extractChemical(what.withAmount(amount), Action.fromFluidAction(mode.getFluidAction())).getAmount();
    }

    @Override
    public long insert(ItemStack context, UfoMekanismKey what, long amount, Actionable mode) {
        var handler = context.getCapability(UfoMekCapabilities.CHEMICAL.item());
        if (handler == null) {
            return 0L;
        }
        return amount - handler.insertChemical(what.withAmount(amount), Action.fromFluidAction(mode.getFluidAction())).getAmount();
    }

    @Override
    public void playFillSound(Player player, UfoMekanismKey what) {
        player.playNotifySound(SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void playEmptySound(Player player, UfoMekanismKey what) {
        player.playNotifySound(SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public @Nullable GenericStack getExtractableContent(ItemStack context) {
        return getContainedStack(context);
    }
}

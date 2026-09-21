package com.raishxn.ufo.util;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.ids.AEComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Makes AE2's lossless overflow carrier understandable in player inventories. */
public final class RecoveryStackItems {
    private RecoveryStackItems() {
    }

    public static ItemStack wrap(GenericStack stack) {
        return wrap(stack.what(), stack.amount());
    }

    public static ItemStack wrap(AEKey key, long amount) {
        ItemStack result = GenericStack.wrapInItemStack(key, amount);
        update(result, key, amount);
        return result;
    }

    public static void update(ItemStack result, AEKey key, long amount) {
        result.set(AEComponents.WRAPPED_STACK, new GenericStack(key, amount));
        result.set(DataComponents.CUSTOM_NAME, Component.translatable(
                "item.ufo.recovery_package",
                key.getDisplayName(),
                key.formatAmount(amount, appeng.api.stacks.AmountFormat.FULL)));
    }
}

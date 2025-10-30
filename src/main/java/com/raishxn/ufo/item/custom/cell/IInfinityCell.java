// Crie este NOVO ficheiro em: src/main/java/com/raishxn/ufo/item/custom/cell/IInfinityCell.java
package com.raishxn.ufo.item.custom.cell;

import appeng.api.stacks.GenericStack;
import com.raishxn.ufo.datagen.ModDataComponents;
import net.minecraft.world.item.ItemStack;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public interface IInfinityCell {

    double getIdleDrain();

    static BigInteger getUsedBytes(ItemStack stack) {
        String val = stack.get(ModDataComponents.CELL_BYTES_BIG);
        return val == null ? BigInteger.ZERO : new BigInteger(val);
    }

    static void setUsedBytes(ItemStack stack, BigInteger usedBytes) {
        if (usedBytes.signum() <= 0) {
            stack.remove(ModDataComponents.CELL_BYTES_BIG);
        } else {
            stack.set(ModDataComponents.CELL_BYTES_BIG, usedBytes.toString());
        }
    }

    static int getUsedTypes(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CELL_TYPES_USED, 0);
    }

    static void setUsedTypes(ItemStack stack, int usedTypes) {
        if (usedTypes <= 0) {
            stack.remove(ModDataComponents.CELL_TYPES_USED);
        } else {
            stack.set(ModDataComponents.CELL_TYPES_USED, usedTypes);
        }
    }

    static List<GenericStack> getTooltipShowStacks(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CELL_TOOLTIP_STACKS, Collections.emptyList());
    }

    static void setTooltipShowStacks(ItemStack stack, List<GenericStack> showStacks) {
        if (showStacks == null || showStacks.isEmpty()) {
            stack.remove(ModDataComponents.CELL_TOOLTIP_STACKS);
        } else {
            stack.set(ModDataComponents.CELL_TOOLTIP_STACKS, showStacks);
        }
    }
}
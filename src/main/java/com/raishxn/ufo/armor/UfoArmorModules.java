package com.raishxn.ufo.armor;

import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class UfoArmorModules {
    private UfoArmorModules() {}

    public static ItemStack stack(Player player, UfoArmorModule module) {
        ItemStack stack = player.getItemBySlot(module.armorType().getSlot());
        return UfoArmorItem.isUfoArmorPiece(stack) ? stack : ItemStack.EMPTY;
    }

    public static boolean active(Player player, UfoArmorModule module) {
        if (!UfoArmorItem.hasFullUfoSet(player)) return false;
        ItemStack stack = stack(player, module);
        return UfoArmorItem.isModuleEnabled(stack, module)
                && (player.isCreative() || stack.getOrDefault(ModDataComponents.ENERGY.get(), 0) >= module.energyCost());
    }

    public static boolean consume(Player player, UfoArmorModule module) {
        return consume(player, module, module.energyCost());
    }

    public static boolean consume(Player player, UfoArmorModule module, int amount) {
        if (player.isCreative()) return true;
        ItemStack stack = stack(player, module);
        if (!UfoArmorItem.isModuleEnabled(stack, module)) return false;
        int stored = stack.getOrDefault(ModDataComponents.ENERGY.get(), 0);
        if (stored < amount) return false;
        stack.set(ModDataComponents.ENERGY.get(), stored - amount);
        return true;
    }
}

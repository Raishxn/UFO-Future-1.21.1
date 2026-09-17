package com.raishxn.ufo.armor;

import com.raishxn.ufo.UFOConfig;
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

    /**
     * Server-side read of a module setting with the operator cap applied. The client reads settings
     * uncapped so the configuration UI can show the full legal range; the server is what decides how
     * much of that range is actually granted, and this is the read path its effects use.
     */
    public static int cappedSetting(Player player, UfoArmorModule module, UfoArmorSetting setting) {
        return cappedSetting(stack(player, module), setting);
    }

    /** Server-side read of a setting from an already resolved armor stack, with the operator cap. */
    public static int cappedSetting(ItemStack stack, UfoArmorSetting setting) {
        return UFOConfig.clampArmorSetting(setting, UfoArmorItem.moduleSetting(stack, setting));
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

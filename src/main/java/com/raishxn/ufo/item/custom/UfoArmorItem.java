package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.armor.UfoArmorSetting;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.ModArmor;
import com.raishxn.ufo.util.EnergyToolHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;
import java.util.ArrayList;
import java.util.EnumSet;

// --- MUDANÇA 1: Implementar a interface IEnergyTool ---
public class UfoArmorItem extends ArmorItem implements IEnergyTool {

    public UfoArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    // --- MUDANÇA 2: Adicionar o método getName para o efeito arco-íris ---
    @Override
    public Component getName(ItemStack stack) {
        return IEnergyTool.super.getName(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    public static boolean hasActiveFlightSet(Player player) {
        if (!hasFullUfoSet(player)) return false;
        ItemStack chest = player.getItemBySlot(Type.CHESTPLATE.getSlot());
        return isModuleEnabled(chest, UfoArmorModule.VOID_FLIGHT)
                && chest.getOrDefault(ModDataComponents.ENERGY.get(), 0) >= UfoArmorModule.VOID_FLIGHT.energyCost();
    }

    public static boolean hasFullUfoSet(Player player) {
        return player.getItemBySlot(Type.HELMET.getSlot()).is(ModArmor.UFO_HELMET.get())
                && player.getItemBySlot(Type.CHESTPLATE.getSlot()).is(ModArmor.UFO_CHESTPLATE.get())
                && player.getItemBySlot(Type.LEGGINGS.getSlot()).is(ModArmor.UFO_LEGGINGS.get())
                && player.getItemBySlot(Type.BOOTS.getSlot()).is(ModArmor.UFO_BOOTS.get());
    }

    public static boolean isUfoArmorPiece(ItemStack stack) {
        return stack.is(ModArmor.UFO_HELMET.get()) || stack.is(ModArmor.UFO_CHESTPLATE.get())
                || stack.is(ModArmor.UFO_LEGGINGS.get()) || stack.is(ModArmor.UFO_BOOTS.get());
    }

    public static EnumSet<UfoArmorModule> installedModules(ItemStack stack) {
        EnumSet<UfoArmorModule> modules = EnumSet.noneOf(UfoArmorModule.class);
        for (String id : stack.getOrDefault(ModDataComponents.ARMOR_MODULES.get(), List.<String>of())) {
            UfoArmorModule module = UfoArmorModule.byId(id);
            if (module != null) modules.add(module);
        }
        return modules;
    }

    public static boolean isModuleEnabled(ItemStack stack, UfoArmorModule module) {
        return installedModules(stack).contains(module)
                && !stack.getOrDefault(ModDataComponents.DISABLED_ARMOR_MODULES.get(), List.<String>of()).contains(module.id());
    }

    public static boolean installModule(ItemStack stack, UfoArmorModule module) {
        if (!isUfoArmorPiece(stack) || !(stack.getItem() instanceof UfoArmorItem armor)
                || armor.getType() != module.armorType()) return false;
        EnumSet<UfoArmorModule> installed = installedModules(stack);
        if (installed.contains(module) || installed.size() >= UfoArmorModule.capacity(armor.getType())) return false;
        List<String> ids = new ArrayList<>(stack.getOrDefault(ModDataComponents.ARMOR_MODULES.get(), List.<String>of()));
        ids.add(module.id());
        stack.set(ModDataComponents.ARMOR_MODULES.get(), List.copyOf(ids));
        return true;
    }

    public static boolean removeModule(ItemStack stack, UfoArmorModule module) {
        List<String> ids = new ArrayList<>(stack.getOrDefault(ModDataComponents.ARMOR_MODULES.get(), List.<String>of()));
        if (!ids.remove(module.id())) return false;
        stack.set(ModDataComponents.ARMOR_MODULES.get(), List.copyOf(ids));
        List<String> disabled = new ArrayList<>(stack.getOrDefault(ModDataComponents.DISABLED_ARMOR_MODULES.get(), List.<String>of()));
        disabled.remove(module.id());
        stack.set(ModDataComponents.DISABLED_ARMOR_MODULES.get(), List.copyOf(disabled));
        List<String> settings = new ArrayList<>(stack.getOrDefault(
                ModDataComponents.ARMOR_MODULE_SETTINGS.get(), List.<String>of()));
        for (UfoArmorSetting setting : UfoArmorSetting.forModule(module)) {
            settings.removeIf(entry -> entry.startsWith(setting.id() + "="));
        }
        stack.set(ModDataComponents.ARMOR_MODULE_SETTINGS.get(), List.copyOf(settings));
        return true;
    }

    public static boolean toggleModule(ItemStack stack, UfoArmorModule module) {
        if (!installedModules(stack).contains(module)) return false;
        List<String> disabled = new ArrayList<>(stack.getOrDefault(ModDataComponents.DISABLED_ARMOR_MODULES.get(), List.<String>of()));
        if (!disabled.remove(module.id())) disabled.add(module.id());
        stack.set(ModDataComponents.DISABLED_ARMOR_MODULES.get(), List.copyOf(disabled));
        return true;
    }

    public static int moduleSetting(ItemStack stack, UfoArmorSetting setting) {
        String prefix = setting.id() + "=";
        for (String entry : stack.getOrDefault(ModDataComponents.ARMOR_MODULE_SETTINGS.get(), List.<String>of())) {
            if (!entry.startsWith(prefix)) continue;
            try {
                return setting.clamp(Integer.parseInt(entry.substring(prefix.length())));
            } catch (NumberFormatException ignored) {
                break;
            }
        }
        return setting.defaultValue();
    }

    public static boolean setModuleSetting(ItemStack stack, UfoArmorSetting setting, int value) {
        if (!isModuleEnabled(stack, setting.module())) return false;
        String prefix = setting.id() + "=";
        List<String> entries = new ArrayList<>(stack.getOrDefault(
                ModDataComponents.ARMOR_MODULE_SETTINGS.get(), List.<String>of()));
        entries.removeIf(entry -> entry.startsWith(prefix));
        entries.add(prefix + setting.clamp(value));
        stack.set(ModDataComponents.ARMOR_MODULE_SETTINGS.get(), List.copyOf(entries));
        return true;
    }

    // --- MÉTODOS VISUAIS E DA INTERFACE ---
    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        return EnergyToolHelper.getBarWidth(pStack);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return EnergyToolHelper.getBarColor(pStack);
    }

    // --- MUDANÇA 3: Adicionar o método getEnergyPerUse para satisfazer a interface ---
    @Override
    public int getEnergyPerUse() {
        return 0; // O consumo de energia da armadura é por tick, não por uso.
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        if (Screen.hasShiftDown()) {
            IEnergyStorage energyStorage = pStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energyStorage != null) {
                String energyText = String.format("%,d / %,d RF", energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored());
                pTooltipComponents.add(Component.literal(energyText).withStyle(ChatFormatting.GRAY));
            }
            EnumSet<UfoArmorModule> modules = installedModules(pStack);
            pTooltipComponents.add(Component.translatable("tooltip.ufo.armor.modules", modules.size(),
                    UfoArmorModule.capacity(getType())).withStyle(ChatFormatting.AQUA));
            for (UfoArmorModule module : modules) {
                ChatFormatting color = isModuleEnabled(pStack, module) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
                pTooltipComponents.add(Component.literal(" • ")
                        .append(Component.translatable(module.translationKey())).withStyle(color));
            }
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.ufo.press_shift").withStyle(ChatFormatting.AQUA));
            pTooltipComponents.add(Component.translatable("tooltip.ufo.armor.open_config").withStyle(ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}

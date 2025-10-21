package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.util.UfoPersistentEnergyStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;
import java.util.function.Supplier;

public interface IEnergyTool {

    List<Supplier<? extends Item>> TOOL_CYCLE = List.of(
            ModItems.UFO_STAFF, ModItems.UFO_SWORD, ModItems.UFO_PICKAXE, ModItems.UFO_AXE,
            ModItems.UFO_SHOVEL, ModItems.UFO_HOE, ModItems.UFO_HAMMER, ModItems.UFO_GREATSWORD,
            ModItems.UFO_FISHING_ROD, ModItems.UFO_BOW
    );

    int getEnergyPerUse();

    default boolean consumeEnergy(ItemStack pStack) {
        IEnergyStorage energyStorage = pStack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null && energyStorage.getEnergyStored() >= getEnergyPerUse()) {
            energyStorage.extractEnergy(getEnergyPerUse(), false);
            return true;
        }
        return false;
    }

    default void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        if (Screen.hasShiftDown()) {
            IEnergyStorage energyStorage = pStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energyStorage != null) {
                String energyText = String.format("%,d / %,d RF", energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored());
                pTooltipComponents.add(Component.literal(energyText).withStyle(ChatFormatting.GRAY));
            }
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.ufo.press_shift").withStyle(ChatFormatting.AQUA));
        }
    }

    // No arquivo IEnergyTool.java

    // --- CORREÇÃO 2: O método agora aceita um booleano 'forward' ---
    // No arquivo IEnergyTool.java

    // O método agora é 'void' porque a rede o chama, ele não precisa retornar um valor.
// E ele aceita um booleano para a direção.
    default void transformTool(Level level, Player player, InteractionHand hand, boolean forward) {
        if (!level.isClientSide()) {
            ItemStack currentStack = player.getItemInHand(hand);
            int currentIndex = currentStack.getOrDefault(ModDataComponents.TOOL_MODE_INDEX.get(), 0);

            // Lógica para ciclar para frente ou para trás
            int nextIndex;
            if (forward) {
                nextIndex = (currentIndex + 1) % TOOL_CYCLE.size(); // Próximo
            } else {
                nextIndex = (currentIndex - 1 + TOOL_CYCLE.size()) % TOOL_CYCLE.size(); // Anterior
            }

            Item nextItem = TOOL_CYCLE.get(nextIndex).get();
            ItemStack nextStack = new ItemStack(nextItem);

            // (O resto da lógica para transferir energia e dados continua a mesma)
            IEnergyStorage oldEnergy = currentStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (oldEnergy != null) {
                IEnergyStorage newEnergy = nextStack.getCapability(Capabilities.EnergyStorage.ITEM);
                if (newEnergy instanceof UfoPersistentEnergyStorage persistentEnergy) {
                    persistentEnergy.setEnergy(oldEnergy.getEnergyStored());
                }
            }

            CustomData customData = currentStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                nextStack.set(DataComponents.CUSTOM_DATA, customData);
            }

            nextStack.set(ModDataComponents.TOOL_MODE_INDEX.get(), nextIndex);
            player.setItemInHand(hand, nextStack);
        }
    }

    default Component getName(ItemStack stack) {
        MutableComponent name = Component.translatable(stack.getDescriptionId());
        String text = name.getString();

        ChatFormatting[] rainbowColors = new ChatFormatting[]{
                ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
                ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.BLUE,
                ChatFormatting.LIGHT_PURPLE
        };

        MutableComponent rainbowName = Component.empty();
        long time = Util.getMillis();

        for (int i = 0; i < text.length(); i++) {
            int colorIndex = (int) (i * 0.5 + time / 200.0) % rainbowColors.length;
            rainbowName.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(rainbowColors[colorIndex]));
        }

        return rainbowName;
    }
}
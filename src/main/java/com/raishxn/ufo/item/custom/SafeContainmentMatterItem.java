package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ComponentItemHandler;

import java.util.List;

public class SafeContainmentMatterItem extends Item {
    public SafeContainmentMatterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack ccmStack = player.getItemInHand(hand); // Podia renomear para scmStack, mas ccmStack funciona
        if (level.isClientSide) return InteractionResultHolder.success(ccmStack);

        ComponentItemHandler handler = new ComponentItemHandler(ccmStack, ModDataComponents.SAVED_INVENTORY.get(), 1);
        ItemStack inside = handler.getStackInSlot(0);

        ItemStack otherHandStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

        if (inside.isEmpty()) {
            if (!otherHandStack.isEmpty() && otherHandStack.is(ModTags.Items.HAZARDOUS)) {
                ItemStack inserted = handler.insertItem(0, otherHandStack.copy(), false);
                if (inserted.getCount() < otherHandStack.getCount()) {
                    player.setItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, inserted);
                    // Já está em inglês
                    player.displayClientMessage(Component.literal("Item contained securely.").withStyle(ChatFormatting.GREEN), true);
                    return InteractionResultHolder.consume(ccmStack);
                }
            } else if (!otherHandStack.isEmpty()) {
                // Já está em inglês
                player.displayClientMessage(Component.literal("This item does not require containment.").withStyle(ChatFormatting.RED), true);
            }
        } else {
            if (otherHandStack.isEmpty()) {
                player.setItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, inside.copy());
                handler.setStackInSlot(0, ItemStack.EMPTY);
                // Já está em inglês
                player.displayClientMessage(Component.literal("WARNING: Hazardous item removed from containment!").withStyle(ChatFormatting.GOLD), true);
                return InteractionResultHolder.consume(ccmStack);
            } else {
                // Já está em inglês
                player.displayClientMessage(Component.literal("Empty your other hand to remove the item.").withStyle(ChatFormatting.RED), true);
            }
        }

        return InteractionResultHolder.pass(ccmStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.ufo.scm").withStyle(ChatFormatting.GRAY));

        ComponentItemHandler handler = new ComponentItemHandler(stack, ModDataComponents.SAVED_INVENTORY.get(), 1);
        ItemStack inside = handler.getStackInSlot(0);
        if (!inside.isEmpty()) {
            // MUDANÇA: Traduzido de "Contém: " para "Contains: "
            tooltipComponents.add(Component.literal("Contains: ").append(inside.getHoverName()).withStyle(ChatFormatting.RED));
        }
    }
}
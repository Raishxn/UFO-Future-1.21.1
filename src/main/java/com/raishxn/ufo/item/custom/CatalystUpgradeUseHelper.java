package com.raishxn.ufo.item.custom;

import com.raishxn.ufo.util.UfoText;

import appeng.api.upgrades.IUpgradeableObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

final class CatalystUpgradeUseHelper {

    private CatalystUpgradeUseHelper() {
    }

    static InteractionResult tryInstallHeldCatalyst(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof IUpgradeableObject upgradeable)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        var upgrades = upgradeable.getUpgrades();
        if (upgrades == null) {
            return InteractionResult.PASS;
        }

        ItemStack heldStack = context.getItemInHand();
        ItemStack toInsert = heldStack.copyWithCount(1);
        ItemStack remainder = upgrades.addItems(toInsert);
        if (!remainder.isEmpty()) {
            player.displayClientMessage(UfoText.literal("gui.ufo.text.this_controller_cannot_accept_that_catalyst_or_its_catal")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.sidedSuccess(false);
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }
        blockEntity.setChanged();
        player.displayClientMessage(UfoText.literal("gui.ufo.text.catalyst_installed")
                .withStyle(ChatFormatting.GREEN), true);
        return InteractionResult.sidedSuccess(false);
    }
}

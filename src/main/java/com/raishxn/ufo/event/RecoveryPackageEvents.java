package com.raishxn.ufo.event;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.util.RecoveryStackItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Lets a compact emergency recovery package be safely returned to an ME network. */
@EventBusSubscriber(modid = UfoMod.MOD_ID)
public final class RecoveryPackageEvents {
    private RecoveryPackageEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        GenericStack recovered = GenericStack.unwrapItemStack(event.getItemStack());
        if (recovered == null || recovered.amount() <= 0L) {
            return;
        }

        // Cancel on both sides so an ME terminal does not also open underneath the package.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        IInWorldGridNodeHost host = event.getLevel().getCapability(
                AECapabilities.IN_WORLD_GRID_NODE_HOST, event.getPos());
        IGridNode node = host == null ? null : host.getGridNode(event.getFace());
        IGrid grid = node == null ? null : node.getGrid();
        if (grid == null || !node.isActive()) {
            player.displayClientMessage(Component.translatable("message.ufo.recovery.no_network")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        long inserted = grid.getStorageService().getInventory().insert(
                recovered.what(), recovered.amount(), Actionable.MODULATE, IActionSource.ofPlayer(player));
        if (inserted <= 0L) {
            player.displayClientMessage(Component.translatable("message.ufo.recovery.no_space")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        long remaining = recovered.amount() - inserted;
        if (remaining <= 0L) {
            event.getItemStack().shrink(1);
        } else {
            RecoveryStackItems.update(event.getItemStack(), recovered.what(), remaining);
        }
        player.displayClientMessage(Component.translatable(
                "message.ufo.recovery.inserted",
                recovered.what().formatAmount(inserted, appeng.api.stacks.AmountFormat.FULL),
                recovered.what().getDisplayName()).withStyle(ChatFormatting.GREEN), true);
    }
}

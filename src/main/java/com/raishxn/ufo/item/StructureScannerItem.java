package com.raishxn.ufo.item;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockAutoBuildService;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.StructureTerminalOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StructureScannerItem extends Item {

    public StructureScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            ClientProxy.openSettings(hand, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IMultiblockController controller) {
            MultiblockControllerDefinition definition = StructureTerminalOps.definitionOf(be);
            if (definition == null) {
                return InteractionResult.PASS;
            }
            var facing = MultiblockControllerDefinitions.getPatternFacing(be, level.getBlockState(pos));

            if (level.isClientSide) {
                if (!StructureTerminalOps.isFormed(definition.pattern(), level, pos, facing)) {
                    var mismatches = StructureTerminalOps.findMismatches(definition.pattern(), level, pos, facing);
                    int maxHighlight = Math.min(mismatches.size(), 50);
                    for (int i = 0; i < maxHighlight; i++) {
                        ClientProxy.highlight(mismatches.get(i), 15000);
                    }
                }
                return InteractionResult.sidedSuccess(true);
            }

            if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            if (!player.mayUseItemAt(pos, context.getClickedFace(), stack) || !level.mayInteract(player, pos)) {
                serverPlayer.displayClientMessage(Component.translatable("message.ufo.terminal.denied")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            StructureScannerSettings settings = toScannerSettings(stack);
            if (settings.mode() == StructureScannerSettings.Mode.SCAN) {
                controller.scanStructure(level);
                if (controller.isAssembled()) {
                    serverPlayer.displayClientMessage(Component.translatable("message.ufo.structure_formed")
                            .withStyle(ChatFormatting.GREEN), true);
                } else {
                    serverPlayer.sendSystemMessage(definition.name().copy()
                            .append(Component.translatable("message.ufo.terminal.incomplete")
                                    .withStyle(ChatFormatting.RED)));
                }
            } else {
                MultiblockAutoBuildService.start(serverPlayer, be, settings, stack);
            }
            return InteractionResult.sidedSuccess(false);
        }

        var gridHost = level.getCapability(appeng.api.AECapabilities.IN_WORLD_GRID_NODE_HOST, pos, null);
        boolean isWap = level.getBlockEntity(pos) instanceof appeng.api.implementations.blockentities.IWirelessAccessPoint;
        if (gridHost == null || (!isWap && !StructureTerminalOps.hasExposedGridNode(gridHost))) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            StructureTerminalSettings.setBoundPos(stack, new GlobalPos(level.dimension(), pos));
            if (isWap) {
                StructureScannerAe2Link.link(stack, level, pos);
                player.displayClientMessage(Component.translatable("message.ufo.terminal.wap_bound", pos.toShortString()), true);
            } else {
                player.displayClientMessage(Component.translatable("message.ufo.terminal.bound", pos.toShortString()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Maps the terminal UI state onto the guarded auto-build settings used by the server flow. */
    private static StructureScannerSettings toScannerSettings(ItemStack stack) {
        boolean dismantle = StructureTerminalSettings.getDismantleMode(stack);
        boolean replace = StructureTerminalSettings.getReplaceMode(stack);
        StructureScannerSettings.Mode mode = dismantle
                ? StructureScannerSettings.Mode.DEMOLISH
                : replace ? StructureScannerSettings.Mode.REPLACE : StructureScannerSettings.Mode.BUILD;
        return new StructureScannerSettings(mode, true, StructureTerminalSettings.getFieldTier(stack),
                StructureTerminalSettings.getAeMode(stack));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.ufo.terminal.tooltip.0").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.ufo.terminal.tooltip.1").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("item.ufo.terminal.tooltip.2").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("tooltip.ufo.structure_scanner.creative_tooltip").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static final class ClientProxy {
        private static void openSettings(InteractionHand hand, ItemStack stack) {
            net.minecraft.client.Minecraft.getInstance().setScreen(
                    new com.raishxn.ufo.screen.StructureScannerScreen(hand, stack));
        }

        private static void highlight(BlockPos pos, long duration) {
            com.raishxn.ufo.client.render.StructureHighlightRenderer.highlight(pos, duration);
        }
    }
}

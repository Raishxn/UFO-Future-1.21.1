package com.raishxn.ufo.item;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
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
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;

public class StructureScannerItem extends Item {

    public StructureScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) ClientProxy.openSettings(hand, stack);
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

        BlockEntity be = level.getBlockEntity(pos);
        if (StructureScannerAe2Link.isAccessPoint(be)) {
            if (!level.isClientSide) {
                StructureScannerAe2Link.link(context.getItemInHand(), level, pos);
                player.displayClientMessage(Component.translatable(
                        "message.ufo.structure_scanner.ae_linked", pos.getX(), pos.getY(), pos.getZ())
                        .withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(be instanceof IMultiblockController controller)) {
            return InteractionResult.PASS;
        }

        Optional<MultiblockControllerDefinition> definitionOpt = MultiblockControllerDefinitions.getDefinition(be);
        if (definitionOpt.isEmpty()) {
            return InteractionResult.PASS;
        }

        MultiblockControllerDefinition definition = definitionOpt.get();
        var state = level.getBlockState(pos);
        var facing = MultiblockControllerDefinitions.getPatternFacing(be, state);
        StructureScannerSettings settings = StructureScannerSettings.read(context.getItemInHand());

        if (player.isShiftKeyDown() && settings.mode() != StructureScannerSettings.Mode.SCAN) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                com.raishxn.ufo.api.multiblock.MultiblockAutoBuildService.start(
                        serverPlayer, be, settings, context.getItemInHand());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        MultiblockPattern.MatchResult result = definition.pattern().match(level, pos, facing);

        if (result.isValid()) {
            if (!level.isClientSide) {
                controller.scanStructure(level);
                if (controller.isAssembled()) {
                    player.displayClientMessage(Component.translatable("message.ufo.structure_formed").withStyle(ChatFormatting.GREEN), true);
                } else {
                    player.sendSystemMessage(definition.name().copy()
                            .append(Component.literal(": structure shape is valid, but extra controller validation failed.").withStyle(ChatFormatting.RED)));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        List<MultiblockPattern.PatternError> errors = result.allErrors();
        if (errors != null && !errors.isEmpty()) {
            if (!level.isClientSide) {
                int shown = Math.min(errors.size(), 10);
                player.sendSystemMessage(definition.name().copy()
                        .append(Component.literal(": " + errors.size() + " block(s) missing or misplaced.").withStyle(ChatFormatting.RED)));
                for (int i = 0; i < shown; i++) {
                    MultiblockPattern.PatternError error = errors.get(i);
                    BlockPos errorPos = error.pos();
                    Component message = Component.literal("  [" + errorPos.getX() + ", " + errorPos.getY() + ", " + errorPos.getZ() + "] Expected: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(error.expected().copy().withStyle(ChatFormatting.YELLOW));
                    player.sendSystemMessage(message);
                }
                if (errors.size() > shown) {
                    player.sendSystemMessage(Component.literal("  ... and " + (errors.size() - shown) + " more.").withStyle(ChatFormatting.GRAY));
                }
            } else {
                int maxHighlight = Math.min(errors.size(), 50);
                for (int i = 0; i < maxHighlight; i++) {
                    ClientProxy.highlight(errors.get(i).pos(), 15000);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        Optional<MultiblockPattern.PatternError> errOpt = result.error();
        if (errOpt.isPresent()) {
            MultiblockPattern.PatternError error = errOpt.get();
            BlockPos errorPos = error.pos();
            if (!level.isClientSide) {
                Component message = Component.translatable("message.ufo.structure_error",
                        errorPos.getX(), errorPos.getY(), errorPos.getZ(),
                        error.expected().copy().withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(message);
            } else {
                ClientProxy.highlight(errorPos, 15000);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.ufo.structure_scanner.tooltip.0").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.ufo.structure_scanner.tooltip.1").withStyle(ChatFormatting.DARK_GRAY));
        StructureScannerSettings settings = StructureScannerSettings.read(stack);
        tooltipComponents.add(Component.translatable("item.ufo.structure_scanner.tooltip.configure").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("item.ufo.structure_scanner.tooltip.execute").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("item.ufo.structure_scanner.tooltip.mode",
                Component.translatable("gui.ufo.structure_scanner.mode."
                        + settings.mode().name().toLowerCase(java.util.Locale.ROOT))).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.translatable("item.ufo.structure_scanner.tooltip.field", settings.fieldTier())
                .withStyle(ChatFormatting.AQUA));
        var linked = StructureScannerAe2Link.linkedPosition(stack);
        tooltipComponents.add(Component.translatable(linked == null
                        ? "item.ufo.structure_scanner.tooltip.ae_unlinked"
                        : "item.ufo.structure_scanner.tooltip.ae_linked",
                linked == null ? "" : linked.pos().getX(),
                linked == null ? "" : linked.pos().getY(),
                linked == null ? "" : linked.pos().getZ()).withStyle(
                linked == null ? ChatFormatting.RED : ChatFormatting.GREEN));
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

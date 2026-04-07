package com.raishxn.ufo.item;

import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
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

public class StructureScannerItem extends Item {

    public StructureScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StellarNexusControllerBE controller) {
            MultiblockPattern pattern = com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory.getPattern();
            net.minecraft.core.Direction facing = net.minecraft.core.Direction.NORTH;
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
            if (state.hasProperty(net.minecraft.world.level.block.DirectionalBlock.FACING)) {
                facing = state.getValue(net.minecraft.world.level.block.DirectionalBlock.FACING);
            }
            MultiblockPattern.MatchResult result = pattern.match(level, pos, facing);

            if (result.isValid()) {
                if (!level.isClientSide) {
                    // Trigger a real structure scan on the controller
                    controller.scanStructure(level);
                    if (controller.isAssembled()) {
                        player.displayClientMessage(Component.translatable("message.ufo.structure_formed").withStyle(ChatFormatting.GREEN), true);
                    } else {
                        // Pattern matched but hatch validation failed
                        player.sendSystemMessage(Component.literal("§e[Stellar Nexus] §cStructure shape is valid, but hatch requirements are not met!"));
                        player.sendSystemMessage(Component.literal("§7Required: exactly 1 of each: ME Output Hatch, ME Fluid Hatch, ME Input Hatch, AE Energy Hatch, Stellar Fuel Hatch."));
                    }
                }
            } else {
                if (player.isCreative() && player.isShiftKeyDown()) {
                    if (!level.isClientSide) {
                        pattern.assembleAsCreative(level, pos, facing, com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory.getDefaultCreativeStates());
                        controller.scanStructure(level);
                        player.displayClientMessage(Component.literal("§d[Stellar Nexus] §7Instant Auto-Build completed!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    }
                } else {
                    // Collect ALL errors from the pattern and report them
                    List<MultiblockPattern.PatternError> errors = result.allErrors();
                    if (errors != null && !errors.isEmpty()) {
                        if (!level.isClientSide) {
                            int shown = Math.min(errors.size(), 10); // Cap at 10 to avoid chat spam
                            player.sendSystemMessage(Component.literal("§e[Stellar Nexus] §c" + errors.size() + " block(s) missing or misplaced:"));
                            for (int i = 0; i < shown; i++) {
                                MultiblockPattern.PatternError error = errors.get(i);
                                BlockPos errorPos = error.pos();
                                Component message = Component.literal("  §7• [" +
                                        errorPos.getX() + ", " + errorPos.getY() + ", " + errorPos.getZ() + "] §fExpected: ")
                                        .append(error.expected().copy().withStyle(ChatFormatting.YELLOW));
                                player.sendSystemMessage(message);
                            }
                            if (errors.size() > shown) {
                                player.sendSystemMessage(Component.literal("  §7... and " + (errors.size() - shown) + " more."));
                            }
                        } else {
                            // CLIENT SIDE: Highlight ALL missing block positions
                            if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                                int maxHighlight = Math.min(errors.size(), 50);
                                for (int i = 0; i < maxHighlight; i++) {
                                    ClientProxy.highlight(errors.get(i).pos(), 15000);
                                }
                            }
                        }
                    } else {
                        // Fallback: single error from the result
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
                                if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                                    ClientProxy.highlight(errorPos, 15000);
                                }
                            }
                        }
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.ufo.structure_scanner.tooltip.0").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.ufo.structure_scanner.tooltip.1").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.literal("§eSneak + Right Click in Creative to Auto-Build!"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static class ClientProxy {
        static void highlight(BlockPos pos, long duration) {
            com.raishxn.ufo.client.render.StructureHighlightRenderer.highlight(pos, duration);
        }
    }
}

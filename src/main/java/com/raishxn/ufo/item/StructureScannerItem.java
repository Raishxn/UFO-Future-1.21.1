package com.raishxn.ufo.item;

import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.client.render.StructureHighlightRenderer;
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
        if (be instanceof StellarNexusControllerBE) {
            MultiblockPattern pattern = com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory.getPattern();
            net.minecraft.core.Direction facing = net.minecraft.core.Direction.NORTH;
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                facing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            }
            MultiblockPattern.MatchResult result = pattern.match(level, pos, facing);

            if (result.isValid()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.ufo.structure_formed").withStyle(ChatFormatting.GREEN), true);
                }
            } else {
                if (player.isCreative() && player.isShiftKeyDown()) {
                    if (!level.isClientSide) {
                        pattern.assembleAsCreative(level, pos, facing, com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory.getDefaultCreativeStates());
                        player.displayClientMessage(Component.literal("§d[Stellar Nexus] §7Instant Auto-Build completed!").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    }
                } else {
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
                            // CLIENT SIDE: Send coordinates to visualizer!
                            StructureHighlightRenderer.highlight(errorPos, 15000); 
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
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
}

package com.raishxn.ufo.block;

import com.mojang.serialization.MapCodec;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockCasingStyle;
import com.raishxn.ufo.block.entity.AbstractSimpleMultiblockControllerBE;
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.init.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** The single cable-facing ME endpoint shared by endgame quantum multiblocks. */
public final class QuantumGridLinkBlock extends DirectionalBlock implements EntityBlock {
    public static final MapCodec<QuantumGridLinkBlock> CODEC = simpleCodec(QuantumGridLinkBlock::new);
    public static final EnumProperty<MultiblockCasingStyle> CASING_STYLE =
            EnumProperty.create("casing_style", MultiblockCasingStyle.class);

    public QuantumGridLinkBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CASING_STYLE, MultiblockCasingStyle.DEFAULT));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CASING_STYLE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuantumGridLinkBE(ModBlockEntities.QUANTUM_GRID_LINK_BE.get(), pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof QuantumGridLinkBE link) {
            Component status = link.isNetworkReady()
                    ? Component.translatable("message.ufo.quantum_grid_link.online").withStyle(ChatFormatting.GREEN)
                    : link.isLinked()
                            ? Component.translatable("message.ufo.quantum_grid_link.offline").withStyle(ChatFormatting.RED)
                            : Component.translatable("message.ufo.quantum_grid_link.unlinked").withStyle(ChatFormatting.GRAY);
            player.displayClientMessage(getName().copy().append(": ").append(status), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof QuantumGridLinkBE link) {
            BlockPos controllerPos = link.getControllerPos();
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof IMultiblockController controller) {
                controller.removePart(pos);
                markControllerDirty(level, controllerPos);
            }
            link.unlinkForRemoval();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock,
                                BlockPos changedPos, boolean moving) {
        super.neighborChanged(state, level, pos, changedBlock, changedPos, moving);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof QuantumGridLinkBE link) {
            link.refreshGridConnection();
            if (link.getControllerPos() != null) markControllerDirty(level, link.getControllerPos());
        }
    }

    private static void markControllerDirty(Level level, BlockPos controllerPos) {
        BlockEntity entity = level.getBlockEntity(controllerPos);
        if (entity instanceof AbstractSimpleMultiblockControllerBE controller) {
            controller.markStructureDirty();
        } else if (entity instanceof StellarNexusControllerBE controller) {
            controller.markStructureDirty();
        } else if (entity instanceof IMultiblockController controller) {
            controller.scanStructure(level);
        }
    }
}

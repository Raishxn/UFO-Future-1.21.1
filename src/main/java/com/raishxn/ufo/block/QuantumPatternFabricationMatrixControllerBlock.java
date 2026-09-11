package com.raishxn.ufo.block;

import appeng.api.orientation.IOrientableBlock;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.mojang.serialization.MapCodec;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import com.raishxn.ufo.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Structural controller for the Quantum Pattern Fabrication Matrix. */
public final class QuantumPatternFabricationMatrixControllerBlock extends DirectionalBlock
        implements EntityBlock, IOrientableBlock {
    public static final MapCodec<QuantumPatternFabricationMatrixControllerBlock> CODEC =
            simpleCodec(QuantumPatternFabricationMatrixControllerBlock::new);
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public QuantumPatternFabricationMatrixControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, POWERED);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                com.raishxn.ufo.client.GhostHologramRenderer.toggleHologram(
                        pos, state.getValue(FACING).getClockWise());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof QuantumPatternFabricationMatrixControllerBE controller) {
            MenuOpener.open(com.raishxn.ufo.init.ModMenus.QUANTUM_PATTERN_FABRICATION_MATRIX_MENU.get(),
                    serverPlayer, MenuLocators.forBlockEntity(controller));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuantumPatternFabricationMatrixControllerBE(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER_BE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) ->
                ((QuantumPatternFabricationMatrixControllerBE) blockEntity).serverTick();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock,
                                BlockPos changedPos, boolean moving) {
        super.neighborChanged(state, level, pos, changedBlock, changedPos, moving);
        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof QuantumPatternFabricationMatrixControllerBE controller) {
            controller.markStructureDirty();
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof QuantumPatternFabricationMatrixControllerBE controller) {
            controller.onControllerBroken();
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}

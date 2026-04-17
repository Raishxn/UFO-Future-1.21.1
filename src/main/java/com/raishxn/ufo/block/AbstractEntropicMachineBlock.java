package com.raishxn.ufo.block;

import appeng.block.AEBaseEntityBlock;
import com.mojang.serialization.MapCodec;
import com.raishxn.ufo.api.multiblock.EntropicMachineLocator;
import com.raishxn.ufo.api.multiblock.IEntropicMachineController;
import com.raishxn.ufo.block.entity.AbstractEntropicMachineBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractEntropicMachineBlock<T extends AbstractEntropicMachineBE> extends AEBaseEntityBlock<T> {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    protected AbstractEntropicMachineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED, POWERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        IEntropicMachineController controller = EntropicMachineLocator.findController(level, pos);
        if (!(controller instanceof AbstractEntropicMachineBE be)) {
            return InteractionResult.PASS;
        }

        if (!controller.isAssembled()) {
            return InteractionResult.PASS;
        }

        if (!controller.isNetworkConnected()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            player.openMenu(be, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, changedBlock, changedPos, isMoving);
        if (!level.isClientSide()) {
            EntropicMachineLocator.markNearbyDirty(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            EntropicMachineLocator.markNearbyDirty(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <B extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityTicker<B> getTicker(Level level, BlockState state, BlockEntityType<B> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, blockState, be) -> {
            if (be instanceof AbstractEntropicMachineBE machine) {
                machine.serverTick();
            }
        };
    }

    @Override
    protected abstract MapCodec<? extends AEBaseEntityBlock<T>> codec();
}

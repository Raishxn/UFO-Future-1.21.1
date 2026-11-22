package com.raishxn.ufo.block;

import com.raishxn.ufo.api.multiblock.EntropicMachineLocator;
import com.raishxn.ufo.api.multiblock.IEntropicMachineController;
import com.raishxn.ufo.block.entity.EntropicMachinePartBE;
import com.raishxn.ufo.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EntropicMachineShellBlock extends Block implements EntityBlock {
    public EntropicMachineShellBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public EntropicMachinePartBE newBlockEntity(BlockPos pos, BlockState state) {
        return new EntropicMachinePartBE(ModBlockEntities.ENTROPIC_MACHINE_PART_BE.get(), pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        IEntropicMachineController controller = EntropicMachineLocator.findController(level, pos);
        if (controller == null || !controller.isAssembled() || !controller.isNetworkConnected()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && controller instanceof net.minecraft.world.MenuProvider menuProvider) {
            player.openMenu(menuProvider, controller.getControllerPos());
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            EntropicMachineLocator.markNearbyDirty(level, pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}

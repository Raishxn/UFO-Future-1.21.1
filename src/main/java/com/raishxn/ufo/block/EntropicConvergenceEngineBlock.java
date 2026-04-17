package com.raishxn.ufo.block;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.CraftingUnitType;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.raishxn.ufo.api.multiblock.EntropicMachineLocator;
import com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EntropicConvergenceEngineBlock extends AbstractCraftingUnitBlock<EntropicConvergenceEngineBE> {
    public EntropicConvergenceEngineBlock() {
        super(metalProps(), CraftingUnitType.UNIT);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof EntropicConvergenceEngineBE be) {
            if (!be.isGuiAssembled()) {
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            if (!level.isClientSide()) {
                MenuOpener.open(com.raishxn.ufo.init.ModMenus.ENTROPIC_CONVERGENCE_ENGINE_MENU.get(), player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);
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
    public EntropicConvergenceEngineBE newBlockEntity(BlockPos pos, BlockState state) {
        return new EntropicConvergenceEngineBE(pos, state);
    }
}

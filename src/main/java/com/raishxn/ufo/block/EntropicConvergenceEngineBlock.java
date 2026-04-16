package com.raishxn.ufo.block;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.CraftingUnitType;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.raishxn.ufo.block.entity.EntropicConvergenceEngineBE;
import com.raishxn.ufo.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

    @Nullable
    @Override
    public EntropicConvergenceEngineBE newBlockEntity(BlockPos pos, BlockState state) {
        return new EntropicConvergenceEngineBE(pos, state);
    }

    @Nullable
    @Override
    public <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == ModBlockEntities.ENTROPIC_CONVERGENCE_ENGINE_BE.get()
                ? (lvl, pos, blockState, be) -> ((EntropicConvergenceEngineBE) be).serverTick()
                : null;
    }
}

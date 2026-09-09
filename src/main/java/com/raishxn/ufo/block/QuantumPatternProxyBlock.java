package com.raishxn.ufo.block;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.block.entity.AbstractSimpleMultiblockControllerBE;
import com.raishxn.ufo.block.entity.QuantumPatternProxyBE;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Lightweight multiblock endpoint for a remote Quantum Pattern Buffer. */
public final class QuantumPatternProxyBlock extends Block implements EntityBlock {
    public QuantumPatternProxyBlock() {
        super(BlockBehaviour.Properties.of().strength(25.0f, 600.0f).requiresCorrectToolForDrops());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuantumPatternProxyBE(ModBlockEntities.QUANTUM_PATTERN_PROXY_BE.get(), pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof QuantumPatternProxyBE proxy) {
            player.displayClientMessage(Component.translatable(proxy.isLinked()
                    ? "message.ufo.pattern_proxy.linked"
                    : "message.ufo.pattern_proxy.unlinked"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof QuantumPatternProxyBE proxy) {
            BlockPos controllerPos = proxy.getControllerPos();
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof IMultiblockController controller) {
                controller.removePart(pos);
                markControllerDirty(level, controllerPos);
            }
            proxy.unlinkForRemoval();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof QuantumPatternProxyBE proxy
                && proxy.getControllerPos() != null) {
            markControllerDirty(level, proxy.getControllerPos());
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

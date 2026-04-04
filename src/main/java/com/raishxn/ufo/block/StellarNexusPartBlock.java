package com.raishxn.ufo.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.block.entity.StellarNexusPartBE;
import com.raishxn.ufo.init.ModBlockEntities;

/**
 * Generic structural block for the Stellar Nexus multiblock.
 * <p>
 * This class is reused for various "parts" of the structure:
 * casings, hatches, field generators, coolant matrices, etc.
 * Each instance gets its own unique registry ID and texture, but
 * they all share the same block entity type and linking logic.
 */
public class StellarNexusPartBlock extends Block implements net.minecraft.world.level.block.EntityBlock {

    public StellarNexusPartBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StellarNexusPartBE(ModBlockEntities.STELLAR_NEXUS_PART_BE.get(), pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof StellarNexusPartBE part) {
                // Notify the controller that this part has been removed
                BlockPos controllerPos = part.getControllerPos();
                if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof IMultiblockController controller) {
                    controller.removePart(pos);
                }
                part.unlinkFromController();
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, changedBlock, changedPos, isMoving);
        // Propagate neighbour changes to the controller for re-scan
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StellarNexusPartBE part) {
            BlockPos controllerPos = part.getControllerPos();
            if (controllerPos != null
                    && level.getBlockEntity(controllerPos) instanceof com.raishxn.ufo.block.entity.StellarNexusControllerBE cbe) {
                cbe.markStructureDirty();
            }
        }
    }
}

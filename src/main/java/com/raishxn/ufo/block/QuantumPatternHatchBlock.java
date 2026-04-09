package com.raishxn.ufo.block;

import appeng.block.crafting.PatternProviderBlock;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.block.entity.QuantumPatternHatchBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class QuantumPatternHatchBlock extends PatternProviderBlock {

    public QuantumPatternHatchBlock() {
        super();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof QuantumPatternHatchBE hatch) {
            var controllerPos = hatch.getControllerPos();
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof IMultiblockController controller) {
                controller.removePart(pos);
                controller.scanStructure(level);
            }
            hatch.unlinkFromController();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof QuantumPatternHatchBE hatch) {
            var controllerPos = hatch.getControllerPos();
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof IMultiblockController controller) {
                controller.scanStructure(level);
            }
        }
    }
}

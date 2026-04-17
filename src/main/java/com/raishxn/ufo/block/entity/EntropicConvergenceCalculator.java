package com.raishxn.ufo.block.entity;

import appeng.api.networking.IGrid;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.me.cluster.MBCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.raishxn.ufo.api.multiblock.FieldTieredCubeValidator;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.mixin.InvokerCraftingCPUCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Iterator;

public final class EntropicConvergenceCalculator {
    private static final int SEARCH_RADIUS = FieldTieredCubeValidator.OUTER_SIZE - 1;
    private final EntropicConvergenceEngineBE target;

    public EntropicConvergenceCalculator(EntropicConvergenceEngineBE target) {
        this.target = target;
    }

    public void calculateMultiblock(ServerLevel level, BlockPos loc) {
        if (MBCalculator.isModificationInProgress()) {
            return;
        }

        var currentCluster = this.target.getCluster();
        if (currentCluster != null && currentCluster.isDestroyed()) {
            return;
        }

        var result = FieldTieredCubeValidator.findMatchingCube(level, loc,
                (state, testLevel, pos) -> state.is(MultiblockBlocks.ENTROPIC_CONVERGENCE_CASING.get()));

        if (result.isEmpty() || !result.get().valid() || !result.get().shellPositions().contains(loc)) {
            this.target.clearCalculatedStructure();
            this.target.disconnect(true);
            return;
        }

        var validation = result.get();
        boolean updateGrid = false;
        CraftingCPUCluster cluster = this.target.getCluster();

        try {
            if (cluster == null
                    || !cluster.getBoundsMin().equals(validation.minCorner())
                    || !cluster.getBoundsMax().equals(validation.maxCorner())) {
                cluster = new CraftingCPUCluster(validation.minCorner(), validation.maxCorner());
                MBCalculator.setModificationInProgress(cluster);
                this.updateBlockEntities(cluster, level, validation);
                updateGrid = true;
            } else {
                MBCalculator.setModificationInProgress(cluster);
                this.refreshBlockEntities(cluster, level, validation);
            }

            cluster.updateStatus(updateGrid);
        } finally {
            MBCalculator.setModificationInProgress(null);
        }
    }

    public void updateMultiblockAfterNeighborUpdate(ServerLevel level, BlockPos loc, BlockPos changedPos) {
        boolean recheck;
        CraftingCPUCluster cluster = this.target.getCluster();

        if (cluster != null) {
            recheck = isWithinBounds(changedPos, cluster.getBoundsMin(), cluster.getBoundsMax())
                    || this.isRelevantBlock(level.getBlockEntity(changedPos));
        } else {
            recheck = true;
        }

        if (recheck) {
            calculateMultiblock(level, loc);
        }
    }

    private void updateBlockEntities(CraftingCPUCluster cluster, ServerLevel level,
            FieldTieredCubeValidator.ValidationResult validation) {
        for (BlockPos shellPos : validation.shellPositions()) {
            BlockEntity blockEntity = level.getBlockEntity(shellPos);
            if (blockEntity instanceof EntropicConvergenceEngineBE convergence) {
                convergence.applyCalculatedStructure(cluster, validation);
                ((InvokerCraftingCPUCluster) (Object) cluster).ufo$addBlockEntity(convergence);
            }
        }

        ((InvokerCraftingCPUCluster) (Object) cluster).ufo$done();
        postCpuChange(cluster);
    }

    private void refreshBlockEntities(CraftingCPUCluster cluster, ServerLevel level,
            FieldTieredCubeValidator.ValidationResult validation) {
        for (BlockPos shellPos : validation.shellPositions()) {
            BlockEntity blockEntity = level.getBlockEntity(shellPos);
            if (blockEntity instanceof EntropicConvergenceEngineBE convergence) {
                convergence.applyCalculatedStructure(cluster, validation);
            }
        }
    }

    private void postCpuChange(CraftingCPUCluster cluster) {
        Iterator<appeng.blockentity.crafting.CraftingBlockEntity> iterator = cluster.getBlockEntities();
        while (iterator.hasNext()) {
            var blockEntity = iterator.next();
            var node = blockEntity.getGridNode();
            if (node != null) {
                IGrid grid = node.getGrid();
                if (grid != null) {
                    grid.postEvent(new GridCraftingCpuChange(node));
                }
                return;
            }
        }
    }

    private boolean isRelevantBlock(BlockEntity blockEntity) {
        return blockEntity instanceof EntropicConvergenceEngineBE;
    }

    public static void markNearbyDirty(ServerLevel level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -SEARCH_RADIUS, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS))) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof EntropicConvergenceEngineBE convergence) {
                convergence.scanStructure(level);
            }
        }
    }

    private static boolean isWithinBounds(BlockPos pos, BlockPos min, BlockPos max) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return x >= min.getX() && y >= min.getY() && z >= min.getZ()
                && x <= max.getX() && y <= max.getY() && z <= max.getZ();
    }
}

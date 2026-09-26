package com.raishxn.ufo.api.multiblock;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import appeng.api.networking.IInWorldGridNodeHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only Structure Terminal helpers: mismatch scanning and grid node discovery.
 * All block placement and removal goes through {@link MultiblockAutoBuildService}.
 */
public final class StructureTerminalOps {

    private StructureTerminalOps() {
    }

    public static boolean isFormed(MultiblockPattern pattern, Level level, BlockPos pos, Direction facing) {
        return findMismatches(pattern, level, pos, facing).isEmpty();
    }

    public static List<BlockPos> findMismatches(MultiblockPattern pattern, Level level, BlockPos pos, Direction facing) {
        List<BlockPos> mismatches = new ArrayList<>();
        Set<BlockPos> seen = new LinkedHashSet<>();
        for (MultiblockPattern.PatternError error : pattern.match(level, pos, facing).allErrors()) {
            BlockPos at = error.pos().immutable();
            if (seen.add(at)) {
                mismatches.add(at);
            }
        }
        return mismatches;
    }

    public static boolean hasExposedGridNode(IInWorldGridNodeHost host) {
        for (Direction dir : Direction.values()) {
            if (host.getGridNode(dir) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static MultiblockControllerDefinition definitionOf(BlockEntity be) {
        return MultiblockControllerDefinitions.getDefinition(be).orElse(null);
    }
}

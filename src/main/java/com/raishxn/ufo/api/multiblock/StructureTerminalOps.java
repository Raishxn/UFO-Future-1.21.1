package com.raishxn.ufo.api.multiblock;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import appeng.api.AECapabilities;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.storage.MEStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    /** Select only currently matching structural blocks; never select foreign blocks or the controller. */
    public static DemolitionScan scanDemolition(MultiblockControllerDefinition definition, Level level,
                                                BlockPos controllerPos, Direction facing) {
        MultiblockPattern pattern = definition.pattern();
        List<DemolitionTarget> targets = new ArrayList<>();
        char[][][] template = pattern.getPattern();
        for (int y = 0; y < template.length; y++) {
            for (int z = 0; z < template[y].length; z++) {
                for (int x = 0; x < template[y][z].length; x++) {
                    char symbol = template[y][z][x];
                    BlockState expected = definition.defaultCreativeStates().get(symbol);
                    if (symbol == pattern.getControllerChar() || expected == null || expected.isAir()) continue;
                    BlockPos world = MultiblockPattern.getRotatedPos(controllerPos,
                            x - pattern.getControllerCol(), y - pattern.getControllerLayer(),
                            z - pattern.getControllerRow(), facing);
                    if (!level.isInWorldBounds(world) || !level.isLoaded(world)) {
                        return new DemolitionScan(false, List.of());
                    }
                    BlockState current = level.getBlockState(world);
                    if (!current.isAir() && pattern.matchesSlot(symbol, current, level, world)) {
                        targets.add(new DemolitionTarget(world.immutable(), current));
                    }
                }
            }
        }
        return new DemolitionScan(true, targets);
    }

    public record DemolitionTarget(BlockPos position, BlockState state) { }
    public record DemolitionScan(boolean available, List<DemolitionTarget> targets) {
        public DemolitionScan {
            targets = List.copyOf(targets);
        }
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
    public static MEStorage findMeStorage(Level level, @Nullable GlobalPos bound) {
        if (bound == null || bound.dimension() != level.dimension() || !level.isLoaded(bound.pos())) {
            return null;
        }
        if (level.getBlockEntity(bound.pos()) instanceof IWirelessAccessPoint wap) {
            IGrid grid = wap.getGrid();
            if (grid == null || !wap.isActive()) {
                return null;
            }
            return grid.getStorageService().getInventory();
        }
        var host = level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, bound.pos(), null);
        if (host == null) {
            return null;
        }
        IGridNode node = null;
        for (Direction dir : Direction.values()) {
            node = host.getGridNode(dir);
            if (node != null) {
                break;
            }
        }
        if (node == null || !node.isActive() || node.getGrid() == null) {
            return null;
        }
        return node.getGrid().getStorageService().getInventory();
    }

    @Nullable
    public static MultiblockControllerDefinition definitionOf(BlockEntity be) {
        return MultiblockControllerDefinitions.getDefinition(be).orElse(null);
    }
}

package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.block.entity.AbstractEntropicMachineBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EntropicMachineLocator {
    private static final int SEARCH_RADIUS = FieldTieredCubeValidator.OUTER_SIZE - 1;

    private EntropicMachineLocator() {
    }

    @Nullable
    public static IEntropicMachineController findController(Level level, BlockPos origin) {
        IEntropicMachineController best = null;
        BlockPos bestPos = null;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -SEARCH_RADIUS, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof IEntropicMachineController controller) || !controller.canProxyInteract(origin)) {
                continue;
            }

            if (best == null || compare(pos, bestPos) < 0) {
                best = controller;
                bestPos = pos.immutable();
            }
        }

        return best;
    }

    public static void markNearbyDirty(Level level, BlockPos origin) {
        if (level.isClientSide()) {
            return;
        }

        List<AbstractEntropicMachineBE> nearbyMachines = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -SEARCH_RADIUS, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractEntropicMachineBE machine) {
                nearbyMachines.add(machine);
            }
        }

        if (nearbyMachines.isEmpty()) {
            return;
        }

        for (AbstractEntropicMachineBE machine : nearbyMachines) {
            machine.clearStructureState();
        }

        Map<StructureKey, FieldTieredCubeValidator.ValidationResult> matches = new LinkedHashMap<>();
        for (AbstractEntropicMachineBE machine : nearbyMachines) {
            var result = machine.findStructure(level);
            if (result != null && result.valid()) {
                matches.putIfAbsent(new StructureKey(machine.getClass(), result.origin()), result);
            }
        }

        for (var entry : matches.entrySet()) {
            StructureKey key = entry.getKey();
            var result = entry.getValue();

            for (BlockPos shellPos : result.shellPositions()) {
                BlockEntity be = level.getBlockEntity(shellPos);
                if (be instanceof AbstractEntropicMachineBE machine
                        && machine.getClass() == key.machineClass()) {
                    machine.applyStructure(result);
                }
            }
        }
    }

    private record StructureKey(Class<?> machineClass, BlockPos origin) {
    }

    private static int compare(@Nullable BlockPos a, @Nullable BlockPos b) {
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        if (a.getY() != b.getY()) {
            return Integer.compare(a.getY(), b.getY());
        }
        if (a.getZ() != b.getZ()) {
            return Integer.compare(a.getZ(), b.getZ());
        }
        return Integer.compare(a.getX(), b.getX());
    }
}

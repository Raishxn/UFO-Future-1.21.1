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

        // Machines whose cached anchor still validates are left untouched:
        // no clear, no re-apply, no NBT/visual churn, no progress loss.
        // Machines of the same class sharing an anchor validate once.
        record AnchorKey(Class<?> machineClass, BlockPos anchor) {
        }
        Map<AnchorKey, Boolean> anchorValidity = new LinkedHashMap<>();
        List<AbstractEntropicMachineBE> brokenMachines = new ArrayList<>();
        for (AbstractEntropicMachineBE machine : nearbyMachines) {
            boolean intact = false;
            if (machine.isAssembled() && machine.getAnchorPos() != null) {
                intact = anchorValidity.computeIfAbsent(
                        new AnchorKey(machine.getClass(), machine.getAnchorPos()),
                        key -> machine.validateCurrentAnchor(level) != null);
            }
            if (!intact) {
                brokenMachines.add(machine);
            }
        }

        if (brokenMachines.isEmpty()) {
            return;
        }

        for (AbstractEntropicMachineBE machine : brokenMachines) {
            machine.clearStructureState();
        }

        // Valid cubes are searched once per machine class and shared with every
        // broken machine of that class contained in the result.
        record ClassResult(Class<?> machineClass, FieldTieredCubeValidator.ValidationResult result) {
        }
        List<ClassResult> foundResults = new ArrayList<>();
        for (AbstractEntropicMachineBE machine : brokenMachines) {
            FieldTieredCubeValidator.ValidationResult result = null;
            for (ClassResult candidate : foundResults) {
                if (candidate.machineClass() == machine.getClass()
                        && candidate.result().shellPositions().contains(machine.getBlockPos())) {
                    result = candidate.result();
                    break;
                }
            }

            if (result == null) {
                result = machine.findStructure(level);
                if (result != null && result.valid()) {
                    foundResults.add(new ClassResult(machine.getClass(), result));
                }
            }

            if (result != null && result.valid()) {
                machine.applyStructure(result);
            }
        }
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

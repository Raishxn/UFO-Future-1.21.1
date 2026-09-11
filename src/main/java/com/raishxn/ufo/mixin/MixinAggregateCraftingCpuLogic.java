package com.raishxn.ufo.mixin;

import appeng.api.networking.energy.IEnergyService;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.raishxn.ufo.crafting.AggregateCraftingExecutor;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Routes UFO aggregate providers around AE2's one-Java-call-per-copy execution loop. */
@Mixin(value = CraftingCpuLogic.class, priority = 3000, remap = false)
public abstract class MixinAggregateCraftingCpuLogic {
    private static final int UFO_MAX_AGGREGATE_OPERATIONS_PER_TICK = 128;

    @Shadow private ExecutingCraftingJob job;
    @Shadow @Final CraftingCPUCluster cluster;
    @Shadow public abstract ListCraftingInventory getInventory();

    @Unique private long ufo$aggregateTick = Long.MIN_VALUE;
    @Unique private int ufo$aggregateOperations;
    @Unique private int ufo$aggregateScanOffset;

    @WrapOperation(
            method = "tickCraftingLogic",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/crafting/execution/CraftingCpuLogic;executeCrafting"
                            + "(ILappeng/me/service/CraftingService;Lappeng/api/networking/energy/IEnergyService;"
                            + "Lnet/minecraft/world/level/Level;)I"))
    private int ufo$executeAggregate(CraftingCpuLogic self, int remainingOperations,
            CraftingService craftingService, IEnergyService energyService, Level level,
            Operation<Integer> original) {
        long tick = level.getGameTime();
        if (ufo$aggregateTick != tick) {
            ufo$aggregateTick = tick;
            ufo$aggregateOperations = 0;
        }

        int aggregateBudget = Math.min(remainingOperations,
                UFO_MAX_AGGREGATE_OPERATIONS_PER_TICK - ufo$aggregateOperations);
        if (job != null && aggregateBudget > 0) {
            AccessorExecutingCraftingJob jobAccess = (AccessorExecutingCraftingJob) job;
            InvokerElapsedTimeTracker tracker = (InvokerElapsedTimeTracker) jobAccess.ufo$getTimeTracker();
            AggregateCraftingExecutor.Result result = AggregateCraftingExecutor.dispatch(
                    aggregateBudget,
                    ufo$aggregateScanOffset,
                    craftingService,
                    energyService,
                    level,
                    getInventory(),
                    jobAccess.ufo$getTasks(),
                    jobAccess.ufo$getWaitingFor(),
                    task -> ((AccessorCraftingTaskProgress) task).ufo$getValue(),
                    (task, value) -> ((AccessorCraftingTaskProgress) task).ufo$setValue(value),
                    tracker::ufo$addMaxItems,
                    cluster::markDirty);
            ufo$aggregateScanOffset = result.nextOffset();
            if (result.consumedOperations() > 0) {
                ufo$aggregateOperations += result.consumedOperations();
                return result.consumedOperations();
            }
            // An aggregate provider owns at least one pending task but cannot accept it yet
            // (for example, all 128 persistent routes are occupied). Do not fall through to
            // AE2's copy-by-copy loop, which would undo the bounded-work guarantee.
            if (result.sawAggregateProvider()) {
                return 0;
            }
        }

        return original.call(self, remainingOperations, craftingService, energyService, level);
    }
}

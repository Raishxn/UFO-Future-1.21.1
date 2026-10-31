package com.raishxn.ufo.mixin;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.crafting.AggregateCraftingExecutor;
import com.raishxn.ufo.crafting.CraftingCpuTickBudget;
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

    @Unique private static final CraftingCpuTickBudget UFO_TICK_BUDGET =
            new CraftingCpuTickBudget(System::nanoTime);

    @Shadow private ExecutingCraftingJob job;
    @Shadow @Final CraftingCPUCluster cluster;
    @Shadow public abstract ListCraftingInventory getInventory();

    @Unique private long ufo$aggregateTick = Long.MIN_VALUE;
    @Unique private int ufo$aggregateOperations;
    @Unique private int ufo$aggregateScanOffset;
    @Unique private int ufo$pushAttempts;

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
            ufo$pushAttempts = 0;
        }
        var server = level.getServer();
        UFO_TICK_BUDGET.beginTick(server != null ? server.getTickCount() : tick,
                UFOConfig.craftingCpuTickBudgetMillis() * 1_000_000L);

        int aggregateBudget = Math.min(remainingOperations,
                UFO_MAX_AGGREGATE_OPERATIONS_PER_TICK - ufo$aggregateOperations);
        // Every CPU on every grid passes through here each tick; skip the task probing on grids
        // that cannot hold an aggregate provider. A CPU without a grid (virtual Nexus job CPUs)
        // keeps the probing.
        if (job != null && aggregateBudget > 0
                && AggregateCraftingExecutor.mayHaveAggregateProviders(cluster.getGrid())) {
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
        }

        // Nothing aggregate-dispatched this tick: fall through to AE2's copy-by-copy
        // loop. A busy aggregate provider rejects vanilla pushes without queueing
        // (pushPattern -> pushAggregate returns the full leftover), and any normal
        // provider for the same pattern keeps making progress instead of deadlocking
        // behind a full aggregate route queue.
        // AE2 re-enters this call until the window is spent, so returning 0 is what ends the tick.
        if (!UFO_TICK_BUDGET.mayAttempt(ufo$pushAttempts)) return 0;
        return original.call(self, remainingOperations, craftingService, energyService, level);
    }

    /**
     * Stops a single executeCrafting call mid-loop. Refused pushes count too: scanning many full
     * providers costs as much as pushing, and it spends no AE2 operations.
     */
    @WrapOperation(
            method = "executeCrafting",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/networking/crafting/ICraftingProvider;pushPattern"
                            + "(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z"))
    private boolean ufo$budgetPush(ICraftingProvider provider, IPatternDetails details, KeyCounter[] inputs,
            Operation<Boolean> original) {
        if (!UFO_TICK_BUDGET.mayAttempt(ufo$pushAttempts)) return false;
        ufo$pushAttempts++;
        return original.call(provider, details, inputs);
    }
}

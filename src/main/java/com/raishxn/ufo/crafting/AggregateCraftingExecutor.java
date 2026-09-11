package com.raishxn.ufo.crafting;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.service.CraftingService;
import com.raishxn.ufo.api.crafting.IAggregateCraftingProvider;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.ToLongFunction;
import net.minecraft.world.level.Level;

/** Executes homogeneous crafting work in O(pattern inputs + outputs), independent of copy count. */
public final class AggregateCraftingExecutor {
    private static final int MAX_TASK_PROBES = 256;
    private static final long MAX_CALL_NANOS = 2_000_000L;

    private AggregateCraftingExecutor() {
    }

    public static Result dispatch(
            int operationBudget,
            int startOffset,
            CraftingService craftingService,
            IEnergyService energyService,
            Level level,
            ListCraftingInventory inventory,
            Map<IPatternDetails, ?> rawTasks,
            ListCraftingInventory waitingFor,
            ToLongFunction<Object> taskValueGetter,
            ObjLongConsumer<Object> taskValueSetter,
            BiConsumer<Long, AEKeyType> addContainerMaximum,
            Runnable markDirty) {
        if (operationBudget <= 0 || rawTasks.isEmpty()) return new Result(0, startOffset, false);

        @SuppressWarnings("unchecked")
        Map<IPatternDetails, Object> tasks = (Map<IPatternDetails, Object>) rawTasks;
        int size = tasks.size();
        int offset = Math.floorMod(startOffset, Math.max(1, size));
        Iterator<Map.Entry<IPatternDetails, Object>> iterator = tasks.entrySet().iterator();
        for (int skipped = 0; skipped < offset && iterator.hasNext(); skipped++) iterator.next();

        long deadline = System.nanoTime() + MAX_CALL_NANOS;
        int probes = 0;
        int consumedOperations = 0;
        boolean sawAggregateProvider = false;

        while (iterator.hasNext() && probes < MAX_TASK_PROBES
                && consumedOperations < operationBudget && System.nanoTime() < deadline) {
            Map.Entry<IPatternDetails, Object> task = iterator.next();
            probes++;
            long remainingTaskCopies = taskValueGetter.applyAsLong(task.getValue());
            if (remainingTaskCopies <= 0) {
                iterator.remove();
                continue;
            }

            IPatternDetails details = task.getKey();
            IAggregateCraftingProvider aggregateProvider = null;
            long providerCapacity = 0L;
            int providerPriority = Integer.MIN_VALUE;
            for (ICraftingProvider provider : craftingService.getProviders(details)) {
                if (!(provider instanceof IAggregateCraftingProvider candidate)) continue;
                sawAggregateProvider = true;
                if (candidate.isBusy()) continue;
                long capacity;
                try {
                    capacity = Math.max(0L, candidate.getAggregateCapacity(details));
                } catch (RuntimeException ignored) {
                    continue;
                }
                int priority = candidate.getAggregatePriority();
                if (priority > providerPriority || priority == providerPriority && capacity > providerCapacity) {
                    aggregateProvider = candidate;
                    providerCapacity = capacity;
                    providerPriority = priority;
                }
            }
            if (aggregateProvider == null || providerCapacity <= 1L || remainingTaskCopies <= 1L) continue;

            int operationCost = Math.max(1, aggregateProvider.getAggregateOperationCost());
            if (operationCost > operationBudget - consumedOperations) continue;

            KeyCounter expectedOutputs = new KeyCounter();
            KeyCounter expectedContainers = new KeyCounter();
            KeyCounter[] oneCopyInputs = CraftingCpuHelper.extractPatternInputs(
                    details, inventory, level, expectedOutputs, expectedContainers);
            if (oneCopyInputs == null) continue;

            Map<AEKey, Long> perCopyRequirements = aggregateRequirements(oneCopyInputs);
            long copies = Math.min(remainingTaskCopies, providerCapacity);
            for (Map.Entry<AEKey, Long> requirement : perCopyRequirements.entrySet()) {
                long perCopy = requirement.getValue();
                copies = Math.min(copies, Long.MAX_VALUE / perCopy);
                long availableAfterFirst = inventory.extract(
                        requirement.getKey(), Long.MAX_VALUE, Actionable.SIMULATE);
                copies = Math.min(copies, saturatingAdd(1L, availableAfterFirst / perCopy));
            }

            double energyMultiplier = aggregateProvider.getAggregateEnergyMultiplier();
            if (!(energyMultiplier > 0.0D) || !Double.isFinite(energyMultiplier)) energyMultiplier = 1.0D;
            double powerPerCopy = CraftingCpuHelper.calculatePatternPower(oneCopyInputs) * energyMultiplier;
            if (powerPerCopy > 0.0D && copies > 0L) {
                double requestedPower = powerPerCopy * copies;
                double availablePower = energyService.extractAEPower(
                        requestedPower, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                if (availablePower < requestedPower - 0.01D) {
                    copies = Math.min(copies, floorPositive(availablePower / powerPerCopy));
                }
            }

            if (copies <= 0L) {
                CraftingCpuHelper.reinjectPatternInputs(inventory, oneCopyInputs);
                continue;
            }

            Map<AEKey, Long> additionallyExtracted = new LinkedHashMap<>();
            if (!extractAdditionalCopies(inventory, perCopyRequirements, copies - 1L, additionallyExtracted)) {
                reinject(inventory, additionallyExtracted);
                CraftingCpuHelper.reinjectPatternInputs(inventory, oneCopyInputs);
                continue;
            }

            long leftover;
            try {
                leftover = aggregateProvider.pushAggregate(details, oneCopyInputs, copies);
            } catch (RuntimeException ignored) {
                leftover = copies;
            }
            if (leftover < 0L || leftover > copies) leftover = copies;
            long accepted = copies - leftover;
            if (leftover > 0L) reinjectCopies(inventory, perCopyRequirements, leftover);
            if (accepted <= 0L) continue;

            if (powerPerCopy > 0.0D) {
                energyService.extractAEPower(
                        powerPerCopy * accepted, Actionable.MODULATE, PowerMultiplier.CONFIG);
            }
            for (var output : expectedOutputs) {
                waitingFor.insert(output.getKey(), saturatingMultiply(output.getLongValue(), accepted),
                        Actionable.MODULATE);
            }
            for (var container : expectedContainers) {
                long amount = saturatingMultiply(container.getLongValue(), accepted);
                waitingFor.insert(container.getKey(), amount, Actionable.MODULATE);
                addContainerMaximum.accept(amount, container.getKey().getType());
            }

            long newValue = remainingTaskCopies - accepted;
            taskValueSetter.accept(task.getValue(), newValue);
            if (newValue <= 0L) iterator.remove();
            markDirty.run();
            consumedOperations += operationCost;
        }

        int nextOffset = tasks.isEmpty() ? 0 : Math.floorMod(offset + Math.max(1, probes), tasks.size());
        return new Result(consumedOperations, nextOffset, sawAggregateProvider);
    }

    private static Map<AEKey, Long> aggregateRequirements(KeyCounter[] oneCopyInputs) {
        Map<AEKey, Long> requirements = new LinkedHashMap<>();
        for (KeyCounter slot : oneCopyInputs) {
            if (slot == null) continue;
            for (var entry : slot) {
                if (entry.getKey() != null && entry.getLongValue() > 0L) {
                    requirements.merge(entry.getKey(), entry.getLongValue(),
                            AggregateCraftingExecutor::saturatingAdd);
                }
            }
        }
        return requirements;
    }

    private static boolean extractAdditionalCopies(ListCraftingInventory inventory,
            Map<AEKey, Long> requirements, long additionalCopies, Map<AEKey, Long> extracted) {
        if (additionalCopies <= 0L) return true;
        for (Map.Entry<AEKey, Long> requirement : requirements.entrySet()) {
            long amount = saturatingMultiply(requirement.getValue(), additionalCopies);
            long taken = inventory.extract(requirement.getKey(), amount, Actionable.MODULATE);
            if (taken > 0L) extracted.put(requirement.getKey(), taken);
            if (taken != amount) return false;
        }
        return true;
    }

    private static void reinjectCopies(ListCraftingInventory inventory,
            Map<AEKey, Long> requirements, long copies) {
        for (Map.Entry<AEKey, Long> requirement : requirements.entrySet()) {
            inventory.insert(requirement.getKey(), saturatingMultiply(requirement.getValue(), copies),
                    Actionable.MODULATE);
        }
    }

    private static void reinject(ListCraftingInventory inventory, Map<AEKey, Long> stacks) {
        for (Map.Entry<AEKey, Long> stack : stacks.entrySet()) {
            inventory.insert(stack.getKey(), stack.getValue(), Actionable.MODULATE);
        }
    }

    private static long floorPositive(double value) {
        if (!(value > 0.0D)) return 0L;
        return value >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) Math.floor(value);
    }

    private static long saturatingAdd(long left, long right) {
        if (left <= 0L) return Math.max(0L, right);
        if (right <= 0L) return left;
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    public record Result(int consumedOperations, int nextOffset, boolean sawAggregateProvider) {
    }
}

package com.raishxn.ufo.crafting;

import java.util.function.LongSupplier;

/**
 * Server-thread time budget shared by every AE2 crafting CPU during one server tick.
 *
 * <p>AE2 spends {@code coProcessors + 1} operations per four-tick window, and mega co-processors make
 * that number far larger than a tick can absorb when pushes reach slow external inventories. A
 * per-push cost is unknown in advance, so the budget is measured in time instead of operations:
 * cheap pushes keep full throughput and expensive ones stop at the deadline. Unspent operations
 * simply resume on the next tick.
 *
 * <p>Each CPU keeps a small attempt allowance past the deadline, so a CPU ticked late in the set
 * still makes progress when earlier CPUs consume the whole budget.
 */
public final class CraftingCpuTickBudget {
    /** Attempts every CPU may make per tick once the shared budget is exhausted. */
    public static final int MIN_ATTEMPTS_PER_CPU = 64;

    private final LongSupplier nanoClock;
    private long tick = Long.MIN_VALUE;
    private long deadlineNanos;

    public CraftingCpuTickBudget(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
    }

    /** Opens the budget on the first call of {@code currentTick}; later calls keep the deadline. */
    public void beginTick(long currentTick, long budgetNanos) {
        if (tick != currentTick) {
            tick = currentTick;
            deadlineNanos = nanoClock.getAsLong() + budgetNanos;
        }
    }

    /** True while a CPU that already made {@code cpuAttempts} attempts this tick may try another. */
    public boolean mayAttempt(int cpuAttempts) {
        return cpuAttempts < MIN_ATTEMPTS_PER_CPU || nanoClock.getAsLong() - deadlineNanos < 0L;
    }
}

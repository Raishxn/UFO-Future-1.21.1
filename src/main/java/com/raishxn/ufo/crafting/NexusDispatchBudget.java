package com.raishxn.ufo.crafting;

/**
 * Pure policy for the Quantum Computation Nexus shared dispatch budget.
 *
 * <p>AE2 meters a crafting CPU's pattern operations over a rolling four-tick window: a CPU with
 * {@code n} co-processors executes at most {@code n + 1} patterns per window. The Nexus keeps that
 * accounting per virtual job CPU, but shares the installed lane count between every active job
 * instead of duplicating it. The per-tick ceiling bounds pools that would otherwise exceed the
 * server's safe dispatch rate; it must never make the Nexus slower than the equivalent stand-alone
 * CPUs the player would otherwise build.
 *
 * <p>Energy throttling is opt-in and has no effect while pattern energy is waived.
 */
final class NexusDispatchBudget {
    /** AE2 accumulates {@code usedOps} over the current tick plus the previous three. */
    static final int OPERATION_WINDOW_TICKS = 4;

    private NexusDispatchBudget() {
    }

    /**
     * Returns the shared four-tick window the pool may distribute this tick.
     *
     * @param storedPower           current grid buffer, as reported by AE2
     * @param maxStoredPower        grid buffer capacity; values {@code <= 0} disable energy throttling
     * @param installedCoProcessors lanes installed in the Nexus
     * @param jobCount              active shared job CPUs
     * @param maxDispatchesPerTick  operator ceiling, expressed as an average per-tick dispatch count
     * @param throttleByEnergy      whether low buffers reduce the window; disabled keeps AE2 behavior
     */
    static int windowFor(double storedPower, double maxStoredPower, int installedCoProcessors,
                         int jobCount, int maxDispatchesPerTick, boolean throttleByEnergy) {
        if (jobCount <= 0) return 0;
        // Every active job is an AE2 CPU and gets its own base operation per window, exactly
        // like a stand-alone CPU with no accelerators.
        long installedWindow = (long) Math.max(0, installedCoProcessors) + jobCount;
        long ceiling = Math.max(1L, maxDispatchesPerTick) * OPERATION_WINDOW_TICKS;
        int window = (int) Math.min(Math.min(installedWindow, ceiling), Integer.MAX_VALUE);
        if (!throttleByEnergy || maxStoredPower <= 0.0D) return window;
        // Keep at least one lane per active job when the buffer is critical. The old policy
        // collapsed the entire pool to a single dispatch and starved every other job.
        int minimum = Math.max(1, Math.min(window, jobCount));
        double ratio = storedPower <= 0.0D ? 0.0D : storedPower / maxStoredPower;
        if (ratio < 0.10D) return minimum;
        if (ratio < 0.25D) return Math.max(minimum, window / 8);
        if (ratio < 0.50D) return Math.max(minimum, window / 2);
        return window;
    }
}

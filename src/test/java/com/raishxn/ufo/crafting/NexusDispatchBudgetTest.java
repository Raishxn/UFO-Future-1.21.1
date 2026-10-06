package com.raishxn.ufo.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NexusDispatchBudgetTest {
    private static final int DEFAULT_CEILING = 16_384;
    private static final int INSTALLED_LANES = 16_384;
    private static final int JOBS = 4;
    /** Installed lanes plus one base operation per active job. */
    private static final int SHARED_WINDOW = INSTALLED_LANES + JOBS;

    @Test
    void installedLanesAreSharedBetweenJobsInsteadOfBeingCappedForTheWholePool() {
        // Four jobs and four COPROCESSOR_50M modules: the same hardware as four 4,096-lane CPUs.
        // Each job must receive 4,097 lanes, not 512 from a pool-wide 2,048 cap.
        int window = NexusDispatchBudget.windowFor(1_000_000.0D, 1_000_000.0D,
                INSTALLED_LANES, JOBS, DEFAULT_CEILING, true);
        assertEquals(SHARED_WINDOW, window);
        assertEquals(4_097, window / JOBS);
    }

    @Test
    void perTickCeilingBoundsHugePools() {
        int window = NexusDispatchBudget.windowFor(1_000_000.0D, 1_000_000.0D,
                Integer.MAX_VALUE - 1, 1, DEFAULT_CEILING, true);
        assertEquals(DEFAULT_CEILING * NexusDispatchBudget.OPERATION_WINDOW_TICKS, window);
    }

    @Test
    void installedWindowIncludesTheBaseOperationOfEveryJob() {
        int window = NexusDispatchBudget.windowFor(1_000_000.0D, 1_000_000.0D, 8, JOBS, DEFAULT_CEILING, true);
        assertEquals(12, window);
    }

    @Test
    void criticalEnergyKeepsOneLanePerJobInsteadOfOneForTheWholePool() {
        // 1% of the buffer used to collapse the entire pool to a single dispatch.
        int window = NexusDispatchBudget.windowFor(1_000_000.0D, 100_000_000.0D,
                INSTALLED_LANES, JOBS, DEFAULT_CEILING, true);
        assertEquals(JOBS, window);
    }

    @Test
    void energyThrottleScalesTheDistributedWindow() {
        assertEquals(SHARED_WINDOW / 2,
                NexusDispatchBudget.windowFor(300_000_000.0D, 1_000_000_000.0D,
                        INSTALLED_LANES, JOBS, DEFAULT_CEILING, true));
        assertEquals(SHARED_WINDOW / 8,
                NexusDispatchBudget.windowFor(200_000_000.0D, 1_000_000_000.0D,
                        INSTALLED_LANES, JOBS, DEFAULT_CEILING, true));
        assertEquals(SHARED_WINDOW,
                NexusDispatchBudget.windowFor(500_000_000.0D, 1_000_000_000.0D,
                        INSTALLED_LANES, JOBS, DEFAULT_CEILING, true));
    }

    @Test
    void disabledEnergyThrottleKeepsTheFullWindow() {
        // Default server policy: AE2 does not throttle dispatch by stored energy, and neither
        // does the Nexus unless the operator opts in.
        assertEquals(SHARED_WINDOW,
                NexusDispatchBudget.windowFor(1_000_000.0D, 100_000_000.0D,
                        INSTALLED_LANES, JOBS, DEFAULT_CEILING, false));
    }

    @Test
    void networksWithoutStorageAreNotThrottled() {
        assertEquals(SHARED_WINDOW,
                NexusDispatchBudget.windowFor(0.0D, 0.0D, INSTALLED_LANES, JOBS, DEFAULT_CEILING, true));
    }

    @Test
    void noJobsMeansNoWindow() {
        assertEquals(0, NexusDispatchBudget.windowFor(1.0D, 1.0D, INSTALLED_LANES, 0, DEFAULT_CEILING, true));
    }
}

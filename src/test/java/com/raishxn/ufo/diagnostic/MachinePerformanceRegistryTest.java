package com.raishxn.ufo.diagnostic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachinePerformanceRegistryTest {
    private static final MachineMetricKey KEY = new MachineMetricKey("minecraft:overworld", 42L, "TestMachine");

    @BeforeEach
    void resetRegistry() {
        MachinePerformanceRegistry.INSTANCE.reset();
    }

    @Test
    void aggregatesCountersAndPercentiles() {
        var registry = MachinePerformanceRegistry.INSTANCE;
        for (int i = 1; i <= 100; i++) {
            registry.recordTick(KEY, i, i);
        }
        registry.recordScan(KEY, 500, 27, 100);
        registry.recordStorageOperation(KEY, 100);
        registry.recordStorageOperation(KEY, 100);
        registry.recordEnergyTransfer(KEY, 1_000, 750, 100);
        registry.recordSync(KEY, 123, 100);

        var snapshot = registry.snapshot(KEY.dimension(), KEY.blockPos()).orElseThrow();
        assertEquals(100, snapshot.tickCount());
        assertEquals(50, snapshot.averageTickNanos());
        assertEquals(95, snapshot.p95TickNanos());
        assertEquals(99, snapshot.p99TickNanos());
        assertEquals(1, snapshot.scanCount());
        assertEquals(27, snapshot.blocksTested());
        assertEquals(2, snapshot.storageOperations());
        assertEquals(1, snapshot.energyTransferAttempts());
        assertEquals(1_000, snapshot.energyRequested());
        assertEquals(750, snapshot.energyAccepted());
        assertEquals(1, snapshot.syncEvents());
        assertEquals(123, snapshot.syncBytes());
    }

    @Test
    void keepsOnlyTheMostRecentTickSamples() {
        var registry = MachinePerformanceRegistry.INSTANCE;
        for (int i = 0; i < MachinePerformanceRegistry.SAMPLE_CAPACITY; i++) {
            registry.recordTick(KEY, 10, i);
        }
        registry.recordTick(KEY, 1_000, MachinePerformanceRegistry.SAMPLE_CAPACITY);

        var snapshot = registry.snapshot(KEY.dimension(), KEY.blockPos()).orElseThrow();
        assertEquals(MachinePerformanceRegistry.SAMPLE_CAPACITY + 1L, snapshot.tickCount());
        assertTrue(snapshot.p99TickNanos() >= 10);
    }

    @Test
    void resetRemovesAllEntries() {
        MachinePerformanceRegistry.INSTANCE.recordTick(KEY, 10, 1);
        MachinePerformanceRegistry.INSTANCE.reset();
        assertTrue(MachinePerformanceRegistry.INSTANCE.snapshots().isEmpty());
    }

    @Test
    void saturatesAccumulatedTickTimeInsteadOfOverflowing() {
        var registry = MachinePerformanceRegistry.INSTANCE;
        registry.recordTick(KEY, Long.MAX_VALUE, 1);
        registry.recordTick(KEY, Long.MAX_VALUE, 2);

        var snapshot = registry.snapshot(KEY.dimension(), KEY.blockPos()).orElseThrow();
        assertEquals(Long.MAX_VALUE / 2, snapshot.averageTickNanos());
        assertEquals(Long.MAX_VALUE, snapshot.p99TickNanos());
    }

    @Test
    void prunesMachinesThatHaveNotBeenSeenForTwentyMinutes() {
        var registry = MachinePerformanceRegistry.INSTANCE;
        registry.recordTick(KEY, 10, 0);
        registry.recordTick(new MachineMetricKey("minecraft:overworld", 99L, "ActiveMachine"), 10, 24_064);

        assertTrue(registry.snapshot(KEY.dimension(), KEY.blockPos()).isEmpty());
        assertEquals(1, registry.snapshots().size());
    }
}

package com.raishxn.ufo.diagnostic;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MachinePerformanceRegistry {
    public static final MachinePerformanceRegistry INSTANCE = new MachinePerformanceRegistry();
    static final int SAMPLE_CAPACITY = 256;
    private static final long STALE_TICKS = 20L * 60L * 20L;

    private final Map<MachineMetricKey, MutableMetrics> entries = new LinkedHashMap<>();

    public synchronized void recordTick(MachineMetricKey key, long nanos, long gameTime) {
        if (key == null || nanos < 0) return;
        metrics(key, gameTime).recordTick(nanos);
        prune(gameTime);
    }

    public synchronized void recordScan(MachineMetricKey key, long nanos, long blocksTested, long gameTime) {
        if (key == null || nanos < 0 || blocksTested < 0) return;
        MutableMetrics metrics = metrics(key, gameTime);
        metrics.scanCount = saturatedAdd(metrics.scanCount, 1);
        metrics.scanNanos = saturatedAdd(metrics.scanNanos, nanos);
        metrics.blocksTested = saturatedAdd(metrics.blocksTested, blocksTested);
    }

    public synchronized void recordStorageOperation(MachineMetricKey key, long gameTime) {
        if (key == null) return;
        MutableMetrics metrics = metrics(key, gameTime);
        metrics.storageOperations = saturatedAdd(metrics.storageOperations, 1);
    }

    public synchronized void recordEnergyTransfer(
            MachineMetricKey key, long requested, long accepted, long gameTime) {
        if (key == null || requested < 0L || accepted < 0L) return;
        MutableMetrics metrics = metrics(key, gameTime);
        metrics.energyTransferAttempts = saturatedAdd(metrics.energyTransferAttempts, 1L);
        metrics.energyRequested = saturatedAdd(metrics.energyRequested, requested);
        metrics.energyAccepted = saturatedAdd(metrics.energyAccepted, Math.min(requested, accepted));
    }

    public synchronized void recordSync(MachineMetricKey key, long bytes, long gameTime) {
        if (key == null || bytes < 0) return;
        MutableMetrics metrics = metrics(key, gameTime);
        metrics.syncEvents = saturatedAdd(metrics.syncEvents, 1);
        metrics.syncBytes = saturatedAdd(metrics.syncBytes, bytes);
    }

    public synchronized List<Snapshot> snapshots() {
        return entries.entrySet().stream()
                .map(entry -> entry.getValue().snapshot(entry.getKey()))
                .sorted(Comparator.comparing((Snapshot snapshot) -> snapshot.key().machineType())
                        .thenComparing(snapshot -> snapshot.key().dimension())
                        .thenComparingLong(snapshot -> snapshot.key().blockPos()))
                .toList();
    }

    public synchronized Optional<Snapshot> snapshot(String dimension, long blockPos) {
        return entries.entrySet().stream()
                .filter(entry -> entry.getKey().dimension().equals(dimension)
                        && entry.getKey().blockPos() == blockPos)
                .map(entry -> entry.getValue().snapshot(entry.getKey()))
                .findFirst();
    }

    public synchronized void reset() {
        entries.clear();
    }

    private MutableMetrics metrics(MachineMetricKey key, long gameTime) {
        MutableMetrics metrics = entries.computeIfAbsent(key, ignored -> new MutableMetrics());
        metrics.lastSeenTick = gameTime;
        return metrics;
    }

    private void prune(long gameTime) {
        if ((gameTime & 255L) != 0L) return;
        entries.entrySet().removeIf(entry -> gameTime - entry.getValue().lastSeenTick > STALE_TICKS);
    }

    private static long saturatedAdd(long left, long right) {
        if (right <= 0) return left;
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    public record Snapshot(
            MachineMetricKey key,
            long tickCount,
            long averageTickNanos,
            long p95TickNanos,
            long p99TickNanos,
            long scanCount,
            long scanNanos,
            long blocksTested,
            long storageOperations,
            long energyTransferAttempts,
            long energyRequested,
            long energyAccepted,
            long syncEvents,
            long syncBytes,
            long lastSeenTick) {
    }

    private static final class MutableMetrics {
        private final long[] tickSamples = new long[SAMPLE_CAPACITY];
        private int sampleCount;
        private int sampleCursor;
        private long tickCount;
        private long totalTickNanos;
        private long scanCount;
        private long scanNanos;
        private long blocksTested;
        private long storageOperations;
        private long energyTransferAttempts;
        private long energyRequested;
        private long energyAccepted;
        private long syncEvents;
        private long syncBytes;
        private long lastSeenTick;

        private void recordTick(long nanos) {
            tickSamples[sampleCursor] = nanos;
            sampleCursor = (sampleCursor + 1) % tickSamples.length;
            sampleCount = Math.min(sampleCount + 1, tickSamples.length);
            tickCount = saturatedAdd(tickCount, 1);
            totalTickNanos = saturatedAdd(totalTickNanos, nanos);
        }

        private Snapshot snapshot(MachineMetricKey key) {
            long[] sorted = Arrays.copyOf(tickSamples, sampleCount);
            Arrays.sort(sorted);
            return new Snapshot(
                    key,
                    tickCount,
                    tickCount == 0 ? 0 : totalTickNanos / tickCount,
                    percentile(sorted, 0.95D),
                    percentile(sorted, 0.99D),
                    scanCount,
                    scanNanos,
                    blocksTested,
                    storageOperations,
                    energyTransferAttempts,
                    energyRequested,
                    energyAccepted,
                    syncEvents,
                    syncBytes,
                    lastSeenTick);
        }

        private static long percentile(long[] sorted, double percentile) {
            if (sorted.length == 0) return 0;
            int index = Math.max(0, (int) Math.ceil(sorted.length * percentile) - 1);
            return sorted[Math.min(index, sorted.length - 1)];
        }
    }
}

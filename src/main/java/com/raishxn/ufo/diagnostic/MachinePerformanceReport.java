package com.raishxn.ufo.diagnostic;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/** On-demand report; no serialization work is added to machine ticks. */
public final class MachinePerformanceReport {
    private MachinePerformanceReport() {}

    public static String toJson(String scenario, String capturedAt,
                                List<MachinePerformanceRegistry.Snapshot> snapshots) {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var root = new JsonObject();
        root.addProperty("schemaVersion", 1);
        root.addProperty("scenario", scenario);
        root.addProperty("capturedAtUtc", capturedAt);
        root.addProperty("timingUnit", "nanoseconds");
        root.addProperty("energyUnit", "AE (instrumented transfers only; not FE)");
        root.addProperty("measurementWindow", "Counters and average since reset/entry creation; p95/p99 over last up to 256 recorded ticks. Not wall-clock TPS.");
        var machines = new JsonArray();
        for (var snapshot : snapshots) {
            var machine = gson.toJsonTree(snapshot).getAsJsonObject();
            machine.addProperty("percentileSamples", Math.min(snapshot.tickCount(), MachinePerformanceRegistry.SAMPLE_CAPACITY));
            // A missing denominator means unknown, not zero throughput.
            if (snapshot.tickCount() > 0 && snapshot.energyTransferAttempts() > 0) {
                machine.addProperty("acceptedAePerRecordedTick",
                        (double) snapshot.energyAccepted() / snapshot.tickCount());
            }
            if (snapshot.energyRequested() > 0) {
                machine.addProperty("energyAcceptanceRatio",
                        (double) snapshot.energyAccepted() / snapshot.energyRequested());
            }
            machines.add(machine);
        }
        root.add("machines", machines);
        return gson.toJson(root) + "\n";
    }
}

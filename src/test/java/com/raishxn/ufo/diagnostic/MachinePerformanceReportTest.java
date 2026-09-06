package com.raishxn.ufo.diagnostic;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MachinePerformanceReportTest {
    @Test
    void preservesLongCountersAndDistinguishesTickThroughputFromAcceptance() {
        var registry = new MachinePerformanceRegistry();
        var key = new MachineMetricKey("minecraft:overworld", Long.MAX_VALUE, "Stellar");
        registry.recordTick(key, 1000, 1);
        registry.recordTick(key, 3000, 2);
        registry.recordEnergyTransfer(key, 6_000_000_000L, 3_000_000_000L, 2);
        var report = JsonParser.parseString(MachinePerformanceReport.toJson("safe\"mode", "now", registry.snapshots())).getAsJsonObject();
        assertEquals("safe\"mode", report.get("scenario").getAsString());
        var machine = report.getAsJsonArray("machines").get(0).getAsJsonObject();
        assertEquals(6_000_000_000L, machine.get("energyRequested").getAsLong());
        assertEquals(Long.MAX_VALUE, machine.getAsJsonObject("key").get("blockPos").getAsLong());
        assertEquals(1_500_000_000D, machine.get("acceptedAePerRecordedTick").getAsDouble());
        assertEquals(0.5D, machine.get("energyAcceptanceRatio").getAsDouble());
        assertEquals(2, machine.get("percentileSamples").getAsInt());
    }

    @Test
    void missingTickAndRequestSamplesAreNotReportedAsMeasuredZeroRates() {
        var registry = new MachinePerformanceRegistry();
        registry.recordScan(new MachineMetricKey("test", 0, "test"), 1, 1, 1);
        var report = JsonParser.parseString(MachinePerformanceReport.toJson("idle", "now", registry.snapshots())).getAsJsonObject();
        var machine = report.getAsJsonArray("machines").get(0).getAsJsonObject();
        assertFalse(machine.has("acceptedAePerRecordedTick"));
        assertFalse(machine.has("energyAcceptanceRatio"));
        assertTrue(JsonParser.parseString(MachinePerformanceReport.toJson("empty", "now", List.of()))
                .getAsJsonObject().getAsJsonArray("machines").isEmpty());
    }
}

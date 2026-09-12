package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExternalEnergyBufferTest {
    @Test void aggregateCraftingUsesOnlyBufferedEnergyIncludingFractionalCosts() {
        var buffer = new ExternalEnergyBuffer(100);
        assertEquals(0D, buffer.extractAe(10D, 1D, false));
        assertEquals(5, buffer.receiveFe(5, 0.5D, false));
        assertEquals(2.5D, buffer.extractAe(10D, 1D, true));
        assertEquals(2.5D, buffer.stored());
        assertEquals(0.75D, buffer.extractAe(0.75D, 2D, false));
        assertEquals(1D, buffer.stored());
        var restored = new ExternalEnergyBuffer(100);
        restored.restore(buffer.stored());
        assertEquals(1D, restored.extractAe(10D, 1D, false));
        assertEquals(0D, restored.extractAe(10D, 1D, false));
        assertEquals(0D, restored.extractAe(Double.POSITIVE_INFINITY, 1D, false));
        restored.receiveFe(4, 0.5D, false);
        assertEquals(2D, restored.extractAe(10D, 1D, false));
    }

    @Test void depletionWaitsForExplicitExternalRefill() {
        var buffer = new ExternalEnergyBuffer(100);
        var ports = new com.raishxn.ufocore.api.port.EnergyPortGroup(java.util.List.of(buffer::extract));
        assertEquals(0, ports.extract(100, false));
        assertEquals(40, buffer.receiveFe(40, 0.5, false));
        assertEquals(20, ports.extract(100, true));
        assertEquals(20, buffer.stored());
        assertEquals(20, ports.extract(100, false));
        assertEquals(0, ports.extract(100, false));
        assertEquals(0, ports.extract(100, true));
        assertEquals(12, buffer.receiveFe(12, 0.5, false));
        assertEquals(6, ports.extract(100, false));
    }

    @Test void preservesFractionalConversionAndSimulation() {
        var buffer = new ExternalEnergyBuffer(10);
        assertEquals(3, buffer.receiveFe(3, 0.5, true));
        assertEquals(0, buffer.stored());
        buffer.receiveFe(3, 0.5, false);
        assertEquals(1, buffer.extract(10, true));
        assertEquals(1.5, buffer.stored());
        assertEquals(1, buffer.extract(10, false));
        var restored = new ExternalEnergyBuffer(10);
        restored.restore(buffer.stored());
        restored.receiveFe(1, 0.5, false);
        assertEquals(1, restored.extract(10, false));
        assertEquals(0, restored.stored());
    }
    @Test void appliesConfiguredConsumptionMultiplier() {
        var buffer = new ExternalEnergyBuffer(100);
        buffer.receiveFe(19, 0.5, false);
        assertEquals(4, buffer.extract(20, 2, true));
        assertEquals(9.5, buffer.stored());
        assertEquals(4, buffer.extract(20, 2, false));
        assertEquals(1.5, buffer.stored());
        assertEquals(3, buffer.extract(20, 0.5, false));
        assertEquals(0, buffer.stored());
        assertEquals(0, buffer.extract(20, Double.NaN, false));
        assertEquals(0, buffer.extract(20, 0, false));
    }

    @Test void capsAcceptanceWithoutOverflowOrNegativeTransfers() {
        var buffer = new ExternalEnergyBuffer(1_000_000_000L);
        assertEquals(2_000_000_000, buffer.receiveFe(Integer.MAX_VALUE, 0.5, false));
        assertEquals(0, buffer.receiveFe(1, 0.5, false));
        assertEquals(1_000_000_000L, buffer.extract(Long.MAX_VALUE, false));
        assertEquals(0, buffer.receiveFe(-1, 0.5, false));
        assertEquals(0, buffer.receiveFe(10, Double.NaN, false));
        buffer.restore(Double.POSITIVE_INFINITY);
        assertEquals(0, buffer.stored());
    }
}

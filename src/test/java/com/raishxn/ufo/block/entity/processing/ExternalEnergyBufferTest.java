package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExternalEnergyBufferTest {
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

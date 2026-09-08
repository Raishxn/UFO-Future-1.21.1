package com.raishxn.ufo.wireless;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuantumTransferTest {
    @Test void neverExtractsWhenDestinationIsFull() {
        assertEquals(new QuantumTransfer.Result(0, 0), QuantumTransfer.move(100, n -> 0,
                n -> { fail("Must not extract"); return n; }, n -> { fail("Must not insert"); return n; }));
    }

    @Test void retainsMaterialWhenDestinationChangesAfterSimulation() {
        long[] source = {1_000};
        var result = QuantumTransfer.move(500, n -> 300, n -> { source[0] -= n; return n; }, n -> 120);
        assertEquals(700, source[0]);
        assertEquals(120, result.inserted());
        assertEquals(180, result.retained());
        assertEquals(1_000, source[0] + result.inserted() + result.retained());
    }

    @Test void neverInsertsMoreThanTheSourceActuallyProvided() {
        var result = QuantumTransfer.move(500, n -> n, n -> 16, n -> {
            assertEquals(16, n); return n;
        });
        assertEquals(new QuantumTransfer.Result(16, 0), result);
    }

    @Test void preservesBulkAmountsAndRejectsEmptyRequests() {
        long amount = (long) Integer.MAX_VALUE * 8;
        assertEquals(new QuantumTransfer.Result(amount, 0), QuantumTransfer.move(amount, n -> n, n -> n, n -> n));
        assertEquals(new QuantumTransfer.Result(0, 0), QuantumTransfer.move(-1,
                n -> { fail("Must not simulate"); return n; }, n -> n, n -> n));
    }
}

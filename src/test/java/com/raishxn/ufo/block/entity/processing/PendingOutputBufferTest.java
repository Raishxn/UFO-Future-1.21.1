package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingOutputBufferTest {
    @Test
    void preservesRemainderAcrossPartialInsertions() {
        PendingOutputBuffer<String> buffer = new PendingOutputBuffer<>();
        buffer.prepare(List.of(new TransactionalAmountLedger.Entry<>("matter", 100L)));

        assertEquals(30L, buffer.drain((key, requested) -> 30L));
        assertEquals(List.of(new TransactionalAmountLedger.Entry<>("matter", 70L)), buffer.snapshot());
        assertTrue(buffer.hasPending());

        assertEquals(70L, buffer.drain((key, requested) -> requested));
        assertFalse(buffer.hasPending());
        assertFalse(buffer.isPrepared());
    }

    @Test
    void preparingAgainCannotDuplicateAnUnfinishedOutput() {
        PendingOutputBuffer<String> buffer = new PendingOutputBuffer<>();
        var outputs = List.of(new TransactionalAmountLedger.Entry<>("star", 25L));

        buffer.prepare(outputs);
        buffer.drain((key, requested) -> 5L);
        buffer.prepare(outputs);

        assertEquals(List.of(new TransactionalAmountLedger.Entry<>("star", 20L)), buffer.snapshot());
    }

    @Test
    void clampsBrokenInserterResultsAndRestoresPendingState() {
        PendingOutputBuffer<String> buffer = new PendingOutputBuffer<>();
        buffer.restore(List.of(new TransactionalAmountLedger.Entry<>("fluid", 40L)), false);

        assertTrue(buffer.isPrepared());
        assertEquals(0L, buffer.drain((key, requested) -> -5L));
        assertEquals(40L, buffer.drain((key, requested) -> Long.MAX_VALUE));
        assertFalse(buffer.hasPending());
    }
}

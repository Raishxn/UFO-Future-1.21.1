package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyedTransferBatchTest {
    @Test
    void equalKeysUseOneTransferAndPreserveRequestOrder() {
        KeyedTransferBatch<String, String> batch = new KeyedTransferBatch<>();
        batch.add("iron", "first", 4);
        batch.add("iron", "second", 6);
        AtomicInteger calls = new AtomicInteger();

        var allocations = batch.execute((key, amount) -> {
            calls.incrementAndGet();
            assertEquals("iron", key);
            assertEquals(10, amount);
            return 7;
        });

        assertEquals(1, calls.get());
        assertEquals(List.of(
                new KeyedTransferBatch.Allocation<>("iron", "first", 4L),
                new KeyedTransferBatch.Allocation<>("iron", "second", 3L)), allocations);
    }

    @Test
    void differentKeysRetainFirstSeenOrder() {
        KeyedTransferBatch<String, Integer> batch = new KeyedTransferBatch<>();
        batch.add("b", 1, 2);
        batch.add("a", 2, 3);
        List<String> visited = new ArrayList<>();

        batch.execute((key, amount) -> {
            visited.add(key);
            return amount;
        });

        assertEquals(List.of("b", "a"), visited);
        assertEquals(2, batch.keyCount());
    }

    @Test
    void transferResultsAreClampedToTheRequest() {
        KeyedTransferBatch<String, String> batch = new KeyedTransferBatch<>();
        batch.add("key", "target", 5);

        assertEquals(5, batch.execute((key, amount) -> Long.MAX_VALUE).getFirst().amount());
        assertTrue(batch.execute((key, amount) -> -1).isEmpty());
    }

    @Test
    void aggregateAmountSaturatesWithoutOverflow() {
        KeyedTransferBatch<String, String> batch = new KeyedTransferBatch<>();
        batch.add("key", "first", Long.MAX_VALUE);
        batch.add("key", "second", 10);

        var allocations = batch.execute((key, amount) -> {
            assertEquals(Long.MAX_VALUE, amount);
            return amount;
        });

        assertEquals(1, allocations.size());
        assertEquals(Long.MAX_VALUE, allocations.getFirst().amount());
    }

    @Test
    void emptyAndNonPositiveRequestsDoNotCallStorage() {
        KeyedTransferBatch<String, String> batch = new KeyedTransferBatch<>();
        batch.add("key", "target", 0);
        batch.add("key", "target", -1);

        assertTrue(batch.isEmpty());
        assertTrue(batch.execute((key, amount) -> {
            throw new AssertionError("must not execute");
        }).isEmpty());
    }
}

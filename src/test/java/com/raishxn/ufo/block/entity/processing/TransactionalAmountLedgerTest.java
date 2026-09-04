package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionalAmountLedgerTest {
    @Test
    void mergesAndPartiallyConsumesAmountsByExactKey() {
        TransactionalAmountLedger<String> ledger = new TransactionalAmountLedger<>();

        ledger.add("stone", 40L);
        ledger.add("stone", 2L);
        ledger.add("dirt", 7L);
        ledger.consume("stone", 10L);

        assertEquals(List.of(
                new TransactionalAmountLedger.Entry<>("stone", 32L),
                new TransactionalAmountLedger.Entry<>("dirt", 7L)), ledger.snapshot());

        ledger.consume("stone", Long.MAX_VALUE);
        ledger.consume("dirt", 7L);
        assertTrue(ledger.isEmpty());
    }

    @Test
    void ignoresInvalidAmountsAndUnknownKeys() {
        TransactionalAmountLedger<String> ledger = new TransactionalAmountLedger<>();

        ledger.add("stone", 5L);
        ledger.add("stone", 0L);
        ledger.add("dirt", -1L);
        ledger.consume("missing", 99L);
        ledger.consume("stone", -1L);

        assertEquals(List.of(new TransactionalAmountLedger.Entry<>("stone", 5L)), ledger.snapshot());
        assertFalse(ledger.isEmpty());
    }

    @Test
    void saturatesInsteadOfOverflowing() {
        TransactionalAmountLedger<String> ledger = new TransactionalAmountLedger<>();

        ledger.add("matter", Long.MAX_VALUE - 2L);
        ledger.add("matter", 10L);

        assertEquals(Long.MAX_VALUE, ledger.snapshot().getFirst().amount());
    }
}

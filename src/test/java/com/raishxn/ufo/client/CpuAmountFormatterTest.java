package com.raishxn.ufo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CpuAmountFormatterTest {
    @Test
    void formatsInfiniteStorageConsistently() {
        assertEquals("∞", CpuAmountFormatter.storage(Long.MAX_VALUE));
        assertEquals("∞", CpuAmountFormatter.storage(Long.MAX_VALUE - 16));
        assertEquals("8EiB", CpuAmountFormatter.storage(Long.MAX_VALUE - 17));
    }

    @Test
    void formatsFiniteStorageWithoutIntegerTruncation() {
        assertEquals("2GiB", CpuAmountFormatter.storage(2L * 1024 * 1024 * 1024));
        assertEquals("1TiB", CpuAmountFormatter.storage(1L << 40));
    }

    @Test
    void formatsThreadMarkers() {
        assertEquals("∞", CpuAmountFormatter.threads(Integer.MAX_VALUE - 1L));
        assertEquals("≥2.15G", CpuAmountFormatter.threads(Integer.MAX_VALUE - 2L));
        assertEquals("50M", CpuAmountFormatter.threads(50_000_000));
    }
}

package com.raishxn.ufo.item.custom.cell;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigCellCapacityMathTest {
    @Test
    void poolsAmountsAndRoundsOnlyOncePerCell() {
        assertEquals(BigInteger.valueOf(18),
                BigCellCapacityMath.usedBytes(BigInteger.valueOf(9), 2, 8, 8));
        assertEquals(BigInteger.valueOf(16),
                BigCellCapacityMath.usedBytes(BigInteger.ZERO, 2, 8, 8));
    }

    @Test
    void accountsForTypeOverheadBeforeAcceptingInsertion() {
        assertEquals(736, BigCellCapacityMath.acceptedInsert(
                BigInteger.ZERO, 0, true, 1_000, 100, 8, 8, 63));
        assertEquals(0, BigCellCapacityMath.acceptedInsert(
                BigInteger.valueOf(700), 1, true, 100, 100, 8, 8, 63));
        assertEquals(36, BigCellCapacityMath.acceptedInsert(
                BigInteger.valueOf(700), 1, false, 100, 100, 8, 8, 63));
    }

    @Test
    void preventsTypeLimitAndLongMultiplicationOverflow() {
        assertEquals(0, BigCellCapacityMath.acceptedInsert(
                BigInteger.ONE, 63, true, 1, 1_000, 8, 8, 63));
        assertEquals(Long.MAX_VALUE, BigCellCapacityMath.acceptedInsert(
                BigInteger.ZERO, 0, false, Long.MAX_VALUE,
                Long.MAX_VALUE - 1, Integer.MAX_VALUE, 0, 63));
    }

    @Test
    void infiniteCellsAcceptTheRequestedLongAmount() {
        assertEquals(Long.MAX_VALUE, BigCellCapacityMath.acceptedInsert(
                BigInteger.TEN.pow(100), 8_192, false, Long.MAX_VALUE,
                Long.MAX_VALUE, 8, 8, 8_192));
    }
}

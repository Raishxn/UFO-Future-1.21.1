package com.raishxn.ufo.item.custom.cell;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BigIntegerLimitsTest {
    @Test
    void acceptsLargestConfiguredPositiveValue() {
        BigInteger value = BigInteger.ONE.shiftLeft(BigIntegerLimits.MAX_BITS).subtract(BigInteger.ONE);

        assertEquals(value, BigIntegerLimits.parseNonNegativeDecimal(value.toString()));
        assertEquals(value, BigIntegerLimits.fromSignedBytes(value.toByteArray()));
    }

    @Test
    void rejectsNegativeMalformedAndOversizedValues() {
        assertThrows(IllegalArgumentException.class,
                () -> BigIntegerLimits.parseNonNegativeDecimal("-1"));
        assertThrows(IllegalArgumentException.class,
                () -> BigIntegerLimits.parseNonNegativeDecimal("not-a-number"));
        assertThrows(IllegalArgumentException.class,
                () -> BigIntegerLimits.requireNonNegativeAndBounded(BigInteger.ONE.shiftLeft(4096)));
        assertThrows(IllegalArgumentException.class,
                () -> BigIntegerLimits.fromSignedBytes(new byte[BigIntegerLimits.MAX_ENCODED_BYTES + 1]));
    }

    @Test
    void rejectsEmptyRepresentations() {
        assertThrows(IllegalArgumentException.class,
                () -> BigIntegerLimits.parseNonNegativeDecimal(""));
        assertThrows(IllegalArgumentException.class,
                () -> BigIntegerLimits.fromSignedBytes(new byte[0]));
    }
}

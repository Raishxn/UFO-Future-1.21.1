package com.raishxn.ufo.item.custom.cell;

import java.math.BigInteger;

/** Limits BigInteger values read from components, network packets and world data. */
public final class BigIntegerLimits {
    public static final int MAX_BITS = 4096;
    public static final int MAX_DECIMAL_CHARS = 1234;
    public static final int MAX_ENCODED_BYTES = 513;

    private BigIntegerLimits() {
    }

    public static BigInteger parseNonNegativeDecimal(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_DECIMAL_CHARS) {
            throw new IllegalArgumentException("BigInteger decimal representation is empty or too large");
        }
        final BigInteger parsed;
        try {
            parsed = new BigInteger(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid BigInteger decimal representation", exception);
        }
        return requireNonNegativeAndBounded(parsed);
    }

    public static BigInteger fromSignedBytes(byte[] value) {
        if (value == null || value.length == 0 || value.length > MAX_ENCODED_BYTES) {
            throw new IllegalArgumentException("BigInteger byte representation is empty or too large");
        }
        return requireNonNegativeAndBounded(new BigInteger(value));
    }

    public static BigInteger requireNonNegativeAndBounded(BigInteger value) {
        if (value == null || value.signum() < 0 || value.bitLength() > MAX_BITS) {
            throw new IllegalArgumentException("BigInteger must be non-negative and at most " + MAX_BITS + " bits");
        }
        return value;
    }
}

package com.raishxn.ufo.item.custom.cell;

import java.math.BigInteger;

/** Pure capacity calculations for BigInteger-backed AE2 cells. */
public final class BigCellCapacityMath {
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private BigCellCapacityMath() {
    }

    public static BigInteger usedBytes(BigInteger totalAmount, int storedTypes,
                                       long amountPerByte, int bytesPerType) {
        requireNonNegative(totalAmount, "totalAmount");
        if (storedTypes < 0 || amountPerByte <= 0 || bytesPerType < 0) {
            throw new IllegalArgumentException("Invalid cell capacity parameters");
        }

        BigInteger valueBytes = ceilDiv(totalAmount, BigInteger.valueOf(amountPerByte));
        BigInteger typeBytes = BigInteger.valueOf(bytesPerType).multiply(BigInteger.valueOf(storedTypes));
        return valueBytes.add(typeBytes);
    }

    public static long acceptedInsert(BigInteger totalAmount, int storedTypes, boolean newType,
                                      long requested, long maxBytes, long amountPerByte,
                                      int bytesPerType, int maxTypes) {
        requireNonNegative(totalAmount, "totalAmount");
        if (requested <= 0) return 0;
        if (storedTypes < 0 || amountPerByte <= 0 || bytesPerType < 0 || maxTypes < 0) {
            throw new IllegalArgumentException("Invalid cell capacity parameters");
        }
        if (newType && storedTypes >= maxTypes) return 0;
        if (maxBytes == Long.MAX_VALUE) return requested;
        if (maxBytes < 0) return 0;

        int resultingTypes = newType ? storedTypes + 1 : storedTypes;
        BigInteger usableBytes = BigInteger.valueOf(maxBytes)
                .subtract(BigInteger.valueOf(bytesPerType).multiply(BigInteger.valueOf(resultingTypes)));
        if (usableBytes.signum() <= 0) return 0;

        BigInteger amountCapacity = usableBytes.multiply(BigInteger.valueOf(amountPerByte));
        BigInteger headroom = amountCapacity.subtract(totalAmount);
        if (headroom.signum() <= 0) return 0;
        return headroom.min(BigInteger.valueOf(requested)).longValueExact();
    }

    public static BigInteger remainingAmount(BigInteger totalAmount, int resultingTypes,
                                             long maxBytes, long amountPerByte, int bytesPerType) {
        requireNonNegative(totalAmount, "totalAmount");
        if (maxBytes == Long.MAX_VALUE) return LONG_MAX;
        if (maxBytes < 0 || resultingTypes < 0 || amountPerByte <= 0 || bytesPerType < 0) {
            return BigInteger.ZERO;
        }
        BigInteger usableBytes = BigInteger.valueOf(maxBytes)
                .subtract(BigInteger.valueOf(bytesPerType).multiply(BigInteger.valueOf(resultingTypes)));
        if (usableBytes.signum() <= 0) return BigInteger.ZERO;
        return usableBytes.multiply(BigInteger.valueOf(amountPerByte)).subtract(totalAmount).max(BigInteger.ZERO);
    }

    public static long clampToLong(BigInteger value) {
        if (value == null || value.signum() <= 0) return 0;
        return value.compareTo(LONG_MAX) >= 0 ? Long.MAX_VALUE : value.longValue();
    }

    private static BigInteger ceilDiv(BigInteger dividend, BigInteger divisor) {
        if (dividend.signum() == 0) return BigInteger.ZERO;
        return dividend.add(divisor.subtract(BigInteger.ONE)).divide(divisor);
    }

    private static void requireNonNegative(BigInteger value, String name) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
    }
}

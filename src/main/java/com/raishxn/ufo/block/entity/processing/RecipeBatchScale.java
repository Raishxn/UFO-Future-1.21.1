package com.raishxn.ufo.block.entity.processing;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Exact rational scale used when AE2 encodes a proportional sub-batch of a
 * large multiblock recipe. Input and promised-output amounts must remain exact;
 * energy is rounded up so a smaller batch can never become free.
 */
public record RecipeBatchScale(long numerator, long denominator) {
    public static final RecipeBatchScale FULL = new RecipeBatchScale(1L, 1L);

    public RecipeBatchScale {
        if (numerator <= 0L || denominator <= 0L) {
            throw new IllegalArgumentException("Recipe batch scale must be positive");
        }
        long divisor = gcd(numerator, denominator);
        numerator /= divisor;
        denominator /= divisor;
    }

    public static Optional<RecipeBatchScale> between(long encodedAmount, long recipeAmount) {
        if (encodedAmount <= 0L || recipeAmount <= 0L) {
            return Optional.empty();
        }
        return Optional.of(new RecipeBatchScale(encodedAmount, recipeAmount));
    }

    public boolean matches(long encodedAmount, long recipeAmount) {
        return exact(recipeAmount).filter(value -> value == encodedAmount).isPresent();
    }

    public Optional<Long> exact(long recipeAmount) {
        if (recipeAmount < 0L) {
            return Optional.empty();
        }
        BigInteger product = BigInteger.valueOf(recipeAmount).multiply(BigInteger.valueOf(numerator));
        BigInteger[] result = product.divideAndRemainder(BigInteger.valueOf(denominator));
        if (result[1].signum() != 0 || result[0].signum() < 0
                || result[0].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            return Optional.empty();
        }
        return Optional.of(result[0].longValue());
    }

    public long ceil(long recipeAmount) {
        if (recipeAmount <= 0L) {
            return 0L;
        }
        BigInteger product = BigInteger.valueOf(recipeAmount).multiply(BigInteger.valueOf(numerator));
        BigInteger divisor = BigInteger.valueOf(denominator);
        BigInteger[] result = product.divideAndRemainder(divisor);
        BigInteger rounded = result[1].signum() == 0 ? result[0] : result[0].add(BigInteger.ONE);
        return rounded.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                ? Long.MAX_VALUE
                : rounded.longValue();
    }

    private static long gcd(long left, long right) {
        while (right != 0L) {
            long remainder = left % right;
            left = right;
            right = remainder;
        }
        return left;
    }
}

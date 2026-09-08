package com.raishxn.ufo.wireless;

import java.util.function.LongUnaryOperator;

/** A transfer retains any extracted amount the destination unexpectedly rejects. */
public final class QuantumTransfer {
    private QuantumTransfer() {}
    public record Result(long inserted, long retained) {
        public long extracted() { return inserted + retained; }
    }

    public static Result move(long requested, LongUnaryOperator simulateInsertion,
            LongUnaryOperator extract, LongUnaryOperator insert) {
        if (requested <= 0) return new Result(0, 0);
        long accepted = Math.max(0, Math.min(requested, simulateInsertion.applyAsLong(requested)));
        if (accepted == 0) return new Result(0, 0);
        long extracted = extract.applyAsLong(accepted);
        if (extracted <= 0) return new Result(0, 0);
        long inserted = Math.max(0, Math.min(extracted, insert.applyAsLong(extracted)));
        return new Result(inserted, extracted - inserted);
    }
}

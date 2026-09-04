package com.raishxn.ufo.block.entity.processing;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Exact, retryable output accounting. Preparing twice is intentionally a no-op
 * so a completion tick cannot duplicate outputs after a partial insertion.
 */
public final class PendingOutputBuffer<K> {
    private final TransactionalAmountLedger<K> pending = new TransactionalAmountLedger<>();
    private boolean prepared;

    public void prepare(List<TransactionalAmountLedger.Entry<K>> outputs) {
        if (this.prepared) {
            return;
        }
        for (var output : outputs) {
            if (output != null) {
                this.pending.add(output.key(), output.amount());
            }
        }
        this.prepared = true;
    }

    public void restore(List<TransactionalAmountLedger.Entry<K>> outputs, boolean prepared) {
        clear();
        for (var output : outputs) {
            if (output != null) {
                this.pending.add(output.key(), output.amount());
            }
        }
        this.prepared = prepared || !this.pending.isEmpty();
    }

    public long drain(BiFunction<K, Long, Long> inserter) {
        long acceptedTotal = 0L;
        for (var output : this.pending.snapshot()) {
            long accepted = inserter.apply(output.key(), output.amount());
            accepted = Math.max(0L, Math.min(output.amount(), accepted));
            this.pending.consume(output.key(), accepted);
            acceptedTotal = saturatedAdd(acceptedTotal, accepted);
        }
        if (this.pending.isEmpty()) {
            this.prepared = false;
        }
        return acceptedTotal;
    }

    public List<TransactionalAmountLedger.Entry<K>> snapshot() {
        return this.pending.snapshot();
    }

    public boolean hasPending() {
        return !this.pending.isEmpty();
    }

    public boolean isPrepared() {
        return this.prepared;
    }

    public void clear() {
        this.pending.clear();
        this.prepared = false;
    }

    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }
}

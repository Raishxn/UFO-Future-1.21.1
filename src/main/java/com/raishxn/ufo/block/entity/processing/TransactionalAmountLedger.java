package com.raishxn.ufo.block.entity.processing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TransactionalAmountLedger<K> {
    private final Map<K, Long> amounts = new LinkedHashMap<>();

    public void add(K key, long amount) {
        if (key == null || amount <= 0L) {
            return;
        }
        this.amounts.merge(key, amount, TransactionalAmountLedger::saturatedAdd);
    }

    public void consume(K key, long amount) {
        if (key == null || amount <= 0L) {
            return;
        }
        this.amounts.computeIfPresent(key, (ignored, stored) -> {
            long remaining = stored - Math.min(stored, amount);
            return remaining > 0L ? remaining : null;
        });
    }

    public List<Entry<K>> snapshot() {
        List<Entry<K>> result = new ArrayList<>(this.amounts.size());
        this.amounts.forEach((key, amount) -> result.add(new Entry<>(key, amount)));
        return List.copyOf(result);
    }

    public boolean isEmpty() {
        return this.amounts.isEmpty();
    }

    public void clear() {
        this.amounts.clear();
    }

    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    public record Entry<K>(K key, long amount) {
    }
}

package com.raishxn.ufo.block.entity.processing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregates transfer requests by key while retaining deterministic request
 * order for partial-result distribution.
 */
public final class KeyedTransferBatch<K, T> {
    private final Map<K, List<Request<T>>> requests = new LinkedHashMap<>();

    public void add(K key, T target, long amount) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(target, "target");
        if (amount <= 0L) {
            return;
        }
        this.requests.computeIfAbsent(key, ignored -> new ArrayList<>())
                .add(new Request<>(target, amount));
    }

    public boolean isEmpty() {
        return this.requests.isEmpty();
    }

    public int keyCount() {
        return this.requests.size();
    }

    public List<Allocation<K, T>> execute(Transfer<K> transfer) {
        Objects.requireNonNull(transfer, "transfer");
        List<Allocation<K, T>> allocations = new ArrayList<>();
        this.requests.forEach((key, keyedRequests) -> {
            long requested = 0L;
            for (Request<T> request : keyedRequests) {
                requested = saturatedAdd(requested, request.amount());
            }

            long transferred = Math.max(0L, Math.min(requested, transfer.transfer(key, requested)));
            for (Request<T> request : keyedRequests) {
                if (transferred <= 0L) {
                    break;
                }
                long allocated = Math.min(request.amount(), transferred);
                allocations.add(new Allocation<>(key, request.target(), allocated));
                transferred -= allocated;
            }
        });
        return List.copyOf(allocations);
    }

    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    @FunctionalInterface
    public interface Transfer<K> {
        long transfer(K key, long requested);
    }

    private record Request<T>(T target, long amount) {
    }

    public record Allocation<K, T>(K key, T target, long amount) {
    }
}

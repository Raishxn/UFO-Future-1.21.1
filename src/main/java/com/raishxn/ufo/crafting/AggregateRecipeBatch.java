package com.raishxn.ufo.crafting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToLongFunction;

/** Scales a validated recipe while reserving unchanged container ingredients only once. */
public record AggregateRecipeBatch<K>(Map<K, Long> consumedInputs,
                                      Map<K, Long> scaledProducts,
                                      Map<K, Long> reservedProducts) {
    public static <K> AggregateRecipeBatch<K> of(Map<K, Long> inputs, Map<K, Long> outputs,
                                                Map<K, Long> containers) {
        Map<K, Long> consumed = new LinkedHashMap<>(inputs);
        Map<K, Long> scaled = new LinkedHashMap<>(outputs);
        Map<K, Long> reserved = new LinkedHashMap<>();
        containers.forEach((key, amount) -> {
            // Only an identical returned key is reusable. Buckets and damaged tools still
            // consume their original input and produce a distinct remainder for every copy.
            long reusable = Math.min(inputs.getOrDefault(key, 0L), amount);
            if (reusable > 0L) {
                long remaining = consumed.get(key) - reusable;
                if (remaining == 0L) consumed.remove(key);
                else consumed.put(key, remaining);
                reserved.put(key, reusable);
            }
            long perCopy = amount - reusable;
            if (perCopy > 0L) scaled.merge(key, perCopy, Math::addExact);
        });
        return new AggregateRecipeBatch<>(Map.copyOf(consumed), Map.copyOf(scaled), Map.copyOf(reserved));
    }

    /** The first complete copy, including catalysts, has already been extracted. */
    public long maximumCopies(ToLongFunction<K> availableAfterFirst) {
        // A recipe with no consumed ingredient must never become an unbounded free loop.
        if (consumedInputs.isEmpty()) return 1L;
        long copies = Long.MAX_VALUE;
        for (var input : consumedInputs.entrySet()) {
            long additional = Math.max(0L, availableAfterFirst.applyAsLong(input.getKey())) / input.getValue();
            copies = Math.min(copies, additional == Long.MAX_VALUE ? Long.MAX_VALUE : additional + 1L);
            copies = Math.min(copies, Long.MAX_VALUE / input.getValue());
        }
        for (var output : scaledProducts.entrySet()) {
            copies = Math.min(copies,
                    (Long.MAX_VALUE - reservedProducts.getOrDefault(output.getKey(), 0L)) / output.getValue());
        }
        return copies;
    }

    public Map<K, Long> products(long copies) {
        if (copies <= 0L) throw new IllegalArgumentException("copies");
        Map<K, Long> products = new LinkedHashMap<>(reservedProducts);
        scaledProducts.forEach((key, amount) -> products.merge(key,
                Math.multiplyExact(amount, copies), Math::addExact));
        return products;
    }
}

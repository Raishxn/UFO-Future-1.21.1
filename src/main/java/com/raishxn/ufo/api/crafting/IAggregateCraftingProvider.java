package com.raishxn.ufo.api.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;

/**
 * UFO-native contract for providers that can execute many identical pattern copies as one
 * aggregate operation. Implementations return the number of copies they did not accept.
 */
public interface IAggregateCraftingProvider extends ICraftingProvider {
    /** Advisory copy capacity for the supplied pattern. */
    default long getAggregateCapacity(IPatternDetails details) {
        return isBusy() ? 0L : Long.MAX_VALUE;
    }

    /**
     * Executes up to {@code maxCopies}. The supplied inputs describe exactly one pattern copy and
     * are borrowed read-only; the crafting CPU has already extracted inputs for every offered copy.
     */
    long pushAggregate(IPatternDetails details, KeyCounter[] oneCopyInputs, long maxCopies);

    @Override
    default boolean pushPattern(IPatternDetails details, KeyCounter[] oneCopyInputs) {
        return pushAggregate(details, oneCopyInputs, 1L) == 0L;
    }
}

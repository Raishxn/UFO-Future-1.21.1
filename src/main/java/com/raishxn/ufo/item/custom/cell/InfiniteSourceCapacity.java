package com.raishxn.ufo.item.custom.cell;

/** Display quantity shared by infinite AE sources; extraction remains unlimited. */
public final class InfiniteSourceCapacity {
    private InfiniteSourceCapacity() {
    }

    public static long advertisedAmount() {
        // Match AE2's CreativeCellInventory. KeyCounter sums mounted inventories
        // with ordinary long addition: Long.MAX_VALUE plus any external stock
        // wraps negative, causing the terminal to hide that resource. Leave
        // headroom for other cells and external sources instead.
        return Integer.MAX_VALUE;
    }
}

package com.raishxn.ufo.item.custom.cell;

/** Single saturation policy shared by infinite AE sources and their regression tests. */
public final class InfiniteSourceCapacity {
    private InfiniteSourceCapacity() {
    }

    public static long advertisedAmount() {
        return Long.MAX_VALUE;
    }
}

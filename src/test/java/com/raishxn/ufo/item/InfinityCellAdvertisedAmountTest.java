package com.raishxn.ufo.item;

import com.raishxn.ufo.item.custom.cell.InfiniteSourceCapacity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfinityCellAdvertisedAmountTest {
    @Test
    void advertisesTheLargestAmountSupportedByAe2() {
        assertEquals(Long.MAX_VALUE, InfiniteSourceCapacity.advertisedAmount());
    }
}

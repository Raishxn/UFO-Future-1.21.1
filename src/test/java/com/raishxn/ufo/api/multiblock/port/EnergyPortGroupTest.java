package com.raishxn.ufo.api.multiblock.port;

import com.raishxn.ufocore.api.port.EnergyPortGroup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyPortGroupTest {
    @Test
    void aggregatesPartialDeliveryWithoutExceedingRequest() {
        EnergyPortGroup group = new EnergyPortGroup(List.of(
                (amount, simulate) -> Math.min(amount, 30L),
                (amount, simulate) -> Math.min(amount, 50L),
                (amount, simulate) -> amount));

        assertEquals(100L, group.extract(100L, false));
    }

    @Test
    void forwardsSimulationAndRemainingAmount() {
        List<String> calls = new ArrayList<>();
        EnergyPortGroup group = new EnergyPortGroup(List.of(
                (amount, simulate) -> {
                    calls.add(amount + ":" + simulate);
                    return 40L;
                },
                (amount, simulate) -> {
                    calls.add(amount + ":" + simulate);
                    return amount;
                }));

        assertEquals(75L, group.extract(75L, true));
        assertEquals(List.of("75:true", "35:true"), calls);
    }

    @Test
    void emptyGroupAndInvalidRequestsExtractNothing() {
        assertEquals(0L, EnergyPortGroup.empty().extract(10L, false));
        assertEquals(0L, new EnergyPortGroup(List.of((amount, simulate) -> amount)).extract(0L, false));
    }
}

package com.raishxn.ufo.api.multiblock.port;

import com.raishxn.ufocore.api.port.FluidPortGroup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluidPortGroupTest {
    @Test
    void aggregatesPartialDeliveryWithoutExceedingRequest() {
        FluidPortGroup<String> group = new FluidPortGroup<>(List.of(
                (fluid, amount, simulate) -> Math.min(amount, 30L),
                (fluid, amount, simulate) -> Math.min(amount, 50L),
                (fluid, amount, simulate) -> amount + 100L));

        assertEquals(100L, group.extract("ufo:stable_coolant", 100L, false));
    }

    @Test
    void forwardsKeySimulationAndRemainingAmount() {
        List<String> calls = new ArrayList<>();
        FluidPortGroup<String> group = new FluidPortGroup<>(List.of(
                (fluid, amount, simulate) -> {
                    calls.add(fluid + ":" + amount + ":" + simulate);
                    return 40L;
                },
                (fluid, amount, simulate) -> {
                    calls.add(fluid + ":" + amount + ":" + simulate);
                    return amount;
                }));

        assertEquals(75L, group.extract("ufo:temporal_fluid", 75L, true));
        assertEquals(List.of(
                "ufo:temporal_fluid:75:true",
                "ufo:temporal_fluid:35:true"), calls);
    }

    @Test
    void emptyGroupAndInvalidRequestsExtractNothing() {
        assertEquals(0L, FluidPortGroup.<String>empty().extract("ufo:coolant", 10L, false));
        assertEquals(0L, new FluidPortGroup<String>(List.of()).extract(null, 10L, false));
        assertEquals(0L, new FluidPortGroup<String>(List.of()).extract("ufo:coolant", 0L, false));
    }
}

package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleTankFluidReservationTest {
    @Test
    void aggregatesAllCompatibleRequirements() {
        OptionalLong reserved = SingleTankFluidReservation.reserve(250, List.of(
                new SingleTankFluidReservation.Demand(true, 100),
                new SingleTankFluidReservation.Demand(true, 150)));

        assertEquals(OptionalLong.of(250), reserved);
    }

    @Test
    void rejectsWhenAggregateExceedsAvailableFluid() {
        OptionalLong reserved = SingleTankFluidReservation.reserve(150, List.of(
                new SingleTankFluidReservation.Demand(true, 100),
                new SingleTankFluidReservation.Demand(true, 100)));

        assertTrue(reserved.isEmpty());
    }

    @Test
    void rejectsRequirementForAnotherFluid() {
        OptionalLong reserved = SingleTankFluidReservation.reserve(1_000, List.of(
                new SingleTankFluidReservation.Demand(true, 100),
                new SingleTankFluidReservation.Demand(false, 100)));

        assertTrue(reserved.isEmpty());
    }

    @Test
    void rejectsInvalidAmountsAndOverflow() {
        assertTrue(SingleTankFluidReservation.reserve(100, List.of(
                new SingleTankFluidReservation.Demand(true, 0))).isEmpty());
        assertTrue(SingleTankFluidReservation.reserve(Long.MAX_VALUE, List.of(
                new SingleTankFluidReservation.Demand(true, Long.MAX_VALUE),
                new SingleTankFluidReservation.Demand(true, 1))).isEmpty());
    }

    @Test
    void acceptsRecipeWithoutFluidRequirements() {
        assertEquals(OptionalLong.of(0), SingleTankFluidReservation.reserve(0, List.of()));
    }
}

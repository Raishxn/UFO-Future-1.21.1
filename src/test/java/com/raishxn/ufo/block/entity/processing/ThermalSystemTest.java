package com.raishxn.ufo.block.entity.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ThermalSystemTest {
    @Test
    void plansStrongCoolantWithoutOverconsuming() {
        var profile = new ThermalSystem.CoolantProfile(100L, 1L, 10L);

        assertEquals(new ThermalSystem.CoolingPlan(3L, 250L),
                ThermalSystem.planCooling(250L, 10L, profile));
    }

    @Test
    void supportsMoreThanOneMillibucketPerHeatUnit() {
        var profile = new ThermalSystem.CoolantProfile(1L, 120L, 1_000L);

        assertEquals(new ThermalSystem.CoolingPlan(360L, 3L),
                ThermalSystem.planCooling(3L, 1_000L, profile));
        assertEquals(new ThermalSystem.CoolingPlan(0L, 0L),
                ThermalSystem.planCooling(3L, 119L, profile));
    }

    @Test
    void capsPlanByAvailabilityAndFlow() {
        var profile = new ThermalSystem.CoolantProfile(4L, 100L, 100L);

        assertEquals(new ThermalSystem.CoolingPlan(50L, 2L),
                ThermalSystem.planCooling(10L, 50L, profile));
        assertEquals(new ThermalSystem.CoolingPlan(100L, 4L),
                ThermalSystem.planCooling(10L, 1_000L, profile));
    }

    @Test
    void rejectsInvalidProfilesAndSaturatesMultiplication() {
        assertEquals(0L, ThermalSystem.coolingFromExtracted(
                10L, new ThermalSystem.CoolantProfile(0L, 1L, 10L)));
        assertEquals(Long.MAX_VALUE, ThermalSystem.coolingFromExtracted(
                Long.MAX_VALUE, new ThermalSystem.CoolantProfile(Long.MAX_VALUE, 1L, Long.MAX_VALUE)));
    }

    @Test
    void passiveDissipationClampsAtZeroAndRejectsNegativeRates() {
        assertEquals(599L, ThermalSystem.dissipate(600L, 1L));
        assertEquals(0L, ThermalSystem.dissipate(3L, 10L));
        assertEquals(600L, ThermalSystem.dissipate(600L, 0L));
        assertEquals(600L, ThermalSystem.dissipate(600L, -1L));
        assertEquals(0L, ThermalSystem.dissipate(-1L, 10L));
    }
}

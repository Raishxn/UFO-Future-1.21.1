package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure coverage of the shared coolant tuning tables introduced by L-0040,
 * including equivalence between ThermalSystem.planCooling and the DMA's legacy
 * ad-hoc math for representative boundary cases.
 */
class CoolantTuningTest {

    private static long legacyDmaConsumption(ThermalSystem.CoolantProfile profile, long temperature, long available) {
        // Replicates the pre-L-0040 DMA math: heat/mB profiles capped at 10 mB/tick
        // with floor division, and mB/heat profiles capped at one bucket.
        if (profile.millibucketDenominator() == 1L) {
            long amount = Math.min(10L, available);
            long possibleHeat = amount * profile.heatNumerator();
            if (temperature < possibleHeat) {
                amount = Math.max(1L, temperature / profile.heatNumerator());
            }
            return amount;
        }
        long amount = Math.min(available, 1000L);
        long possibleHeat = amount * profile.heatNumerator() / profile.millibucketDenominator();
        long heatCooled = Math.min(temperature, possibleHeat);
        return heatCooled * profile.millibucketDenominator();
    }

    @Test
    void parallelTablePreservesLegacyBalance() {
        assertEquals(100L, CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.TEMPORAL).heatNumerator());
        assertEquals(1L, CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.TEMPORAL).millibucketDenominator());
        assertEquals(10L, CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.TEMPORAL).maxFlowPerTick());

        assertEquals(50L, CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.STABLE).heatNumerator());

        // Gelid contract validated in L-0013: 120 mB per heat unit.
        assertEquals(1L, CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.GELID).heatNumerator());
        assertEquals(120L, CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.GELID).millibucketDenominator());
        assertEquals(1000L, CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.GELID).maxFlowPerTick());

        // Starlight was never a recognized parallel coolant: generic fallback applies.
        assertEquals(CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.GENERIC),
                CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.STARLIGHT));
        assertEquals(15L, CoolantTuning.parallelProfile(CoolantTuning.CoolantKind.GENERIC).heatNumerator());
    }

    @Test
    void dmaTablePreservesLegacyBalance() {
        assertEquals(30L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.STARLIGHT).heatNumerator());
        assertEquals(1L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.STARLIGHT).millibucketDenominator());
        assertEquals(10L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.STARLIGHT).maxFlowPerTick());

        // Starter coolant contract: 24 mB per heat unit, at most one bucket per tick.
        assertEquals(1L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.GELID).heatNumerator());
        assertEquals(24L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.GELID).millibucketDenominator());
        assertEquals(1000L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.GELID).maxFlowPerTick());

        assertEquals(100L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.TEMPORAL).heatNumerator());
        assertEquals(50L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.STABLE).heatNumerator());
        assertEquals(15L, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.GENERIC).heatNumerator());
    }

    @Test
    void dmaPlanMatchesLegacyMathOnCostAndRemoval() {
        long[][] cases = {
                {100, 1000}, // gelid boundary: full bucket
                {100, 500}, // gelid: available limits cooling
                {99, 10}, // temporal: legacy floor rounds to one mB
                {150, 10}, // temporal: legacy leaves residue
                {7, 10}, // generic: legacy overshoots by rounding up to one mB
                {60, 10}, // stable: exact multiple
                {1, 10}, // minimum heat
                {10000, 10}, // maximum DMA temperature
        };
        CoolantTuning.CoolantKind[] kinds = {
                CoolantTuning.CoolantKind.GELID,
                CoolantTuning.CoolantKind.GELID,
                CoolantTuning.CoolantKind.TEMPORAL,
                CoolantTuning.CoolantKind.TEMPORAL,
                CoolantTuning.CoolantKind.GENERIC,
                CoolantTuning.CoolantKind.STABLE,
                CoolantTuning.CoolantKind.TEMPORAL,
                CoolantTuning.CoolantKind.STABLE,
        };

        for (int i = 0; i < cases.length; i++) {
            long temperature = cases[i][0];
            long available = cases[i][1];
            var profile = CoolantTuning.dmaProfile(kinds[i]);

            var plan = ThermalSystem.planCooling(temperature, available, profile);
            long legacyConsumption = legacyDmaConsumption(profile, temperature, available);

            assertTrue(plan.heatRemoved() > 0L, "case " + i + " must cool something");
            assertTrue(plan.heatRemoved() <= temperature, "case " + i + " must not overcool");
            assertTrue(plan.requestedMillibuckets() <= available, "case " + i + " must respect availability");
            // The legacy math under-consumed on residue ticks (floor division) and
            // needed an extra tick for the remainder; the shared planner finishes in
            // one tick, so its per-tick cost is within one mB of the legacy step and
            // the aggregate mB-per-heat cost is identical.
            assertTrue(plan.requestedMillibuckets() >= legacyConsumption,
                    "case " + i + " must not consume less than the legacy step");
            assertTrue(plan.requestedMillibuckets() <= legacyConsumption + 1L,
                    "case " + i + " must not overpay the legacy step");
        }
    }

    @Test
    void dmaPlanIsExactInsteadOfOvershooting() {
        // Legacy math subtracted amount * heatPerMB even when it exceeded the
        // temperature (clamped afterwards); the shared planner removes exactly the
        // planned heat with the same mB cost.
        var plan = ThermalSystem.planCooling(99, 10, CoolantTuning.dmaProfile(CoolantTuning.CoolantKind.TEMPORAL));
        assertEquals(1L, plan.requestedMillibuckets());
        assertEquals(99L, plan.heatRemoved());
    }
}

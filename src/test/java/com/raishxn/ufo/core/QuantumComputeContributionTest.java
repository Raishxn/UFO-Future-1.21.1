package com.raishxn.ufo.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class QuantumComputeContributionTest {
    @Test
    void megaUnitsExposeExactNexusContributions() {
        assertEquals(BigInteger.valueOf(1024L).pow(5),
                MegaCraftingStorageTier.STORAGE_1QD.quantumComputeContribution().storageBytes().asBigInteger());
        assertEquals(BigInteger.valueOf(2_000_000_000L),
                MegaCoProcessorTier.COPROCESSOR_2B.quantumComputeContribution().parallelLanes().asBigInteger());

        var combined = MegaCoProcessorTier.COPROCESSOR_2B.quantumComputeContribution()
                .add(MegaCoProcessorTier.COPROCESSOR_2B.quantumComputeContribution());
        assertEquals(BigInteger.valueOf(4_000_000_000L), combined.parallelLanes().asBigInteger());
    }
}

package com.raishxn.ufo.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class QuantumComputeContributionTest {
    @Test
    void megaUnitsExposeExactNexusContributions() {
        assertEquals(BigInteger.valueOf(1024L).pow(4),
                MegaCraftingStorageTier.STORAGE_1QD.quantumComputeContribution().storageBytes().asBigInteger());
        assertEquals(BigInteger.valueOf(65_536L),
                MegaCoProcessorTier.COPROCESSOR_2B.quantumComputeContribution().parallelLanes().asBigInteger());

        var combined = MegaCoProcessorTier.COPROCESSOR_2B.quantumComputeContribution()
                .add(MegaCoProcessorTier.COPROCESSOR_2B.quantumComputeContribution());
        assertEquals(BigInteger.valueOf(131_072L), combined.parallelLanes().asBigInteger());
    }

    @Test
    void megaUnitTiersFollowTheApprovedEndgameBalance() {
        long gib = 1024L * 1024L * 1024L;
        assertEquals(4L * gib, MegaCraftingStorageTier.STORAGE_1B.getStorageBytes());
        assertEquals(16L * gib, MegaCraftingStorageTier.STORAGE_50B.getStorageBytes());
        assertEquals(64L * gib, MegaCraftingStorageTier.STORAGE_1T.getStorageBytes());
        assertEquals(256L * gib, MegaCraftingStorageTier.STORAGE_250T.getStorageBytes());
        assertEquals(1024L * gib, MegaCraftingStorageTier.STORAGE_1QD.getStorageBytes());

        assertEquals(4_096, MegaCoProcessorTier.COPROCESSOR_50M.getAcceleratorThreads());
        assertEquals(8_192, MegaCoProcessorTier.COPROCESSOR_150M.getAcceleratorThreads());
        assertEquals(16_384, MegaCoProcessorTier.COPROCESSOR_300M.getAcceleratorThreads());
        assertEquals(32_768, MegaCoProcessorTier.COPROCESSOR_750M.getAcceleratorThreads());
        assertEquals(65_536, MegaCoProcessorTier.COPROCESSOR_2B.getAcceleratorThreads());

        // The first UFO tier remains decisively above the strongest installed addon references:
        // 1 GiB effective storage and 1,024 parallel threads.
        assertTrue(MegaCraftingStorageTier.STORAGE_1B.getStorageBytes() >= 4L * gib);
        assertTrue(MegaCoProcessorTier.COPROCESSOR_50M.getAcceleratorThreads() >= 4 * 1_024);
    }
}

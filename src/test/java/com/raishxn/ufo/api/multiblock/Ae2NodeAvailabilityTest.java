package com.raishxn.ufo.api.multiblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae2NodeAvailabilityTest {

    @Test
    void requiresPresentActiveAndPoweredGridNode() {
        assertTrue(Ae2NodeAvailability.isUsable(true, true, true, true));
        assertFalse(Ae2NodeAvailability.isUsable(false, true, true, true));
        assertFalse(Ae2NodeAvailability.isUsable(true, false, true, true));
        assertFalse(Ae2NodeAvailability.isUsable(true, true, false, true));
        assertFalse(Ae2NodeAvailability.isUsable(true, true, true, false));
    }
}

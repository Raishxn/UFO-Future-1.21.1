package com.raishxn.ufo.wireless;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class WirelessBlockingInputsTest {
    @Test void otherRecipeInputsStillBlockTheDestination() {
        var blocked = WirelessBlockingInputs.collect(Set.of("blizz", "snowball", "cryotheum", "water"),
                Set.of("cryotheum", "water"));
        assertTrue(blocked.contains("blizz"));
        assertTrue(blocked.contains("snowball"));
    }
    @Test void actualSubstitutionsAreIncludedWithoutChangingProviderCache() {
        var configured = new HashSet<>(Set.of("oak", "water"));
        var blocked = WirelessBlockingInputs.collect(configured, Set.of("birch", "water"));
        assertTrue(blocked.contains("birch"));
        assertEquals(Set.of("oak", "water"), configured);
    }
    @Test void unrelatedCoolantAndOutputDoNotBecomeBlockingInputs() {
        var blocked = WirelessBlockingInputs.collect(Set.of("cryotheum", "water"), Set.of("cryotheum", "water"));
        assertFalse(blocked.contains("gelid_coolant"));
        assertFalse(blocked.contains("unrelated_output"));
    }
}

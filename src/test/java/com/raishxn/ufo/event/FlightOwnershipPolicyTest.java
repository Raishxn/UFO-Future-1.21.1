package com.raishxn.ufo.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightOwnershipPolicyTest {
    @Test
    void claimsOwnershipOnlyWhenUfoActuallyGrantsFlight() {
        assertEquals(new FlightOwnershipPolicy.Decision(true, true, false),
                FlightOwnershipPolicy.decide(true, false, false, false));
        assertEquals(new FlightOwnershipPolicy.Decision(false, false, false),
                FlightOwnershipPolicy.decide(true, true, false, false));
    }

    @Test
    void preservesOwnershipWhileAnyUfoFlightSourceRemainsActive() {
        assertEquals(new FlightOwnershipPolicy.Decision(true, false, false),
                FlightOwnershipPolicy.decide(true, true, true, false));
    }

    @Test
    void revokesOnlyFlightThatUfoStillOwns() {
        assertEquals(new FlightOwnershipPolicy.Decision(false, false, true),
                FlightOwnershipPolicy.decide(false, true, true, false));
        assertEquals(new FlightOwnershipPolicy.Decision(false, false, false),
                FlightOwnershipPolicy.decide(false, true, false, false));
        assertEquals(new FlightOwnershipPolicy.Decision(false, false, false),
                FlightOwnershipPolicy.decide(false, false, true, false));
    }

    @Test
    void creativeAndSpectatorStyleModesRelinquishOwnershipWithoutRevoking() {
        assertEquals(new FlightOwnershipPolicy.Decision(false, false, false),
                FlightOwnershipPolicy.decide(false, true, true, true));
        assertEquals(new FlightOwnershipPolicy.Decision(false, false, false),
                FlightOwnershipPolicy.decide(true, true, true, true));
    }

    @Test
    void legacyOwnershipIsDiscardedAsUntrusted() {
        assertFalse(FlightOwnershipPolicy.trustedStoredOwnership(0, true));
        assertFalse(FlightOwnershipPolicy.trustedStoredOwnership(
                FlightOwnershipPolicy.CURRENT_VERSION, false));
        assertTrue(FlightOwnershipPolicy.trustedStoredOwnership(
                FlightOwnershipPolicy.CURRENT_VERSION, true));
    }
}

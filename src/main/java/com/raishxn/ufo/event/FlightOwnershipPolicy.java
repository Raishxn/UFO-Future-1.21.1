package com.raishxn.ufo.event;

/**
 * Pure ownership policy for the shared player flight ability.
 */
public final class FlightOwnershipPolicy {
    public static final int CURRENT_VERSION = 1;

    private FlightOwnershipPolicy() {
    }

    public static boolean trustedStoredOwnership(int policyVersion, boolean storedOwnership) {
        return policyVersion >= CURRENT_VERSION && storedOwnership;
    }

    public static Decision decide(boolean ufoSourceActive, boolean mayFly, boolean ownedByUfo,
                                  boolean protectedGameMode) {
        if (protectedGameMode) {
            return new Decision(false, false, false);
        }
        if (ufoSourceActive) {
            if (mayFly) {
                return new Decision(ownedByUfo, false, false);
            }
            return new Decision(true, true, false);
        }
        return new Decision(false, false, ownedByUfo && mayFly);
    }

    public record Decision(boolean ownedAfter, boolean grantFlight, boolean revokeFlight) {
        public Decision {
            if (grantFlight && revokeFlight) {
                throw new IllegalArgumentException("flight cannot be granted and revoked together");
            }
        }
    }
}

package com.raishxn.ufo.api.multiblock;

public final class Ae2NodeAvailability {
    private Ae2NodeAvailability() {
    }

    public static boolean isUsable(boolean nodePresent, boolean gridPresent, boolean active, boolean powered) {
        return nodePresent && gridPresent && active && powered;
    }
}

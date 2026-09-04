package com.raishxn.ufo.api.multiblock.topology;

import java.util.List;

/** Pure schema-one topology for the Quantum Cryoforge. */
public final class QuantumCryoforgeTopologySchema {
    private static final List<String[]> LAYERS = List.of(
            new String[]{
                    "BBBBAA",
                    "BBBBBA",
                    "BBBBBB",
                    "BBBBBB",
                    "ABBBBB",
                    "AABBBA",
                    "BBAAAA"
            },
            new String[]{
                    "BEBBBA",
                    "BEEEEB",
                    "BEFFEB",
                    "BEEEEC",
                    "BBFFEB",
                    "ABBBBB",
                    "BBBBBA"
            },
            new String[]{
                    "BEBBBA",
                    "BFFFEB",
                    "BEAAAD",
                    "BFAAAD",
                    "BAAAAD",
                    "BBFFEB",
                    "BBBBBA"
            },
            new String[]{
                    "BEBBBA",
                    "BEEEFB",
                    "BFAAAD",
                    "BEAAAD",
                    "BAAAAD",
                    "BBEEFB",
                    "BBBBBA"
            },
            new String[]{
                    "BEBBBA",
                    "BFFFEB",
                    "BEAAAD",
                    "BFAAAD",
                    "BAAAAD",
                    "BBFFEB",
                    "BBBBBA"
            },
            new String[]{
                    "BEBBBA",
                    "BEEEEB",
                    "BEFFEB",
                    "BEEEFB",
                    "BBFFEB",
                    "BBBBBB",
                    "ABBBBA"
            },
            new String[]{
                    "BBBBAA",
                    "BBBBBA",
                    "BBBBBA",
                    "BBBBBA",
                    "BBBBBA",
                    "ABBBBA",
                    "BABBAA"
            });

    private QuantumCryoforgeTopologySchema() {
    }

    public static int schemaVersion() {
        return 1;
    }

    /** Returns a defensive copy so callers cannot mutate the canonical schema. */
    public static List<String[]> layers() {
        return LAYERS.stream().map(String[]::clone).toList();
    }
}

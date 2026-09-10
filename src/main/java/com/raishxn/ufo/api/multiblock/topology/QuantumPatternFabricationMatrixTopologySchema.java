package com.raishxn.ufo.api.multiblock.topology;

import java.util.List;

/** Schema imported from the approved Quantum Pattern Fabrication Matrix layout. */
public final class QuantumPatternFabricationMatrixTopologySchema {
    private static final List<String[]> LAYERS = List.of(
            new String[]{
                    "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "ACCCCCA",
                    "ACXXXCA", "AHXXXLA", "ACXXXCA", "ACCCCCA"
            },
            new String[]{
                    "CCGCGCC", "CCFCFCC", "CCFCFCC", "CFFFFFC", "ACQFQCA",
                    "AGAFAGA", "AGAFAGA", "AGAAAGA", "ACXGXCA"
            },
            new String[]{
                    "GGGGGGG", "GAAAAAG", "GAAAAAG", "CCFFFCC", "ACQQQCA",
                    "AGAAAGA", "AGAFAGA", "AGAAAGA", "ACXGXCA"
            },
            new String[]{
                    "CCGCGCC", "CCACACC", "GAAAAAG", "CCFFFCC", "ACQFQCA",
                    "AGAFAGA", "AGAFAGA", "AGAAAGA", "ACXGXCA"
            },
            new String[]{
                    "CCGCGCC", "CCGCGCC", "CCGCGCC", "CCCCCCC", "ACCCCCA",
                    "ACQQQCA", "ACQQQCA", "ACQQQCA", "ACCCCCA"
            });

    private QuantumPatternFabricationMatrixTopologySchema() {
    }

    public static int schemaVersion() {
        return 2;
    }

    public static List<String[]> layers() {
        return LAYERS.stream().map(String[]::clone).toList();
    }
}

package com.raishxn.ufo.api.multiblock.topology;

import java.util.List;

/**
 * Schema two imported from the approved Slicer layout. The source controller
 * faced east, so its X/Z axes were normalized to the canonical north-facing
 * pattern, with the controller face on the exterior, before being recorded here.
 */
public final class QuantumSlicerTopologySchema {
    private static final List<String[]> LAYERS = List.of(
            new String[]{"ACQQQXXXQQQCA", "ACCCCCCCCCCCA", "CCCCCCCCCCCCC", "ACCCCCCCCCCCA", "ACQQQXXXQQQCA"},
            new String[]{"ACGGGCCCGGGCA", "CCFFFAAAFFFCC", "CCFFFAFAFFFCC", "CCFFFAAAFFFCC", "ACGGGCCCGGGCA"},
            new String[]{"CCCCCCHCCCCCC", "CCFFFAAAFFFCC", "FFFFFAFAFFFFF", "CCFFFAAAFFFCC", "CCCCCCCCCCCCC"},
            new String[]{"ACGGGCCCGGGCA", "CCFFFAAAFFFCC", "CCFFFAFAFFFCC", "CCFFFAAAFFFCC", "ACGGGCCCGGGCA"},
            new String[]{"ACQQQXXXQQQCA", "ACGGGCCCGGGCA", "CCCCCCCCCCCCC", "ACGGGCCCGGGCA", "ACQQQXXXQQQCA"});

    private QuantumSlicerTopologySchema() {
    }

    public static int schemaVersion() {
        return 2;
    }

    /** Returns a defensive copy so callers cannot mutate the canonical schema. */
    public static List<String[]> layers() {
        return LAYERS.stream().map(String[]::clone).toList();
    }
}

package com.raishxn.ufo.api.multiblock.topology;

import java.util.List;

/** Schema imported from the approved Infinity Fabrication Singularity layout. */
public final class InfinityFabricationSingularityTopologySchema {
    private static final List<String[]> LAYERS = List.of(
            new String[]{"AAAAAAA", "AAAQAAA", "AACCCAA", "AXCCCXA", "AACCCAA", "AAAQAAA", "AAAAAAA"},
            new String[]{"AAAQAAA", "AGGVGGA", "AGAFAGA", "XVFFFVX", "AGAFAGA", "AGGVGGA", "AAAQAAA"},
            new String[]{"AACCCAA", "AGAFAGA", "CAAAAAC", "CFAFAFC", "CAAAAAC", "AGAFAGA", "AACCCAA"},
            new String[]{"AQCCCQA", "XGAFAGX", "CAAAAAC", "HFAFAFL", "CAAAAAC", "XGAFAGX", "AQCCCQA"},
            new String[]{"AACCCAA", "AGAFAGA", "CAAAAAC", "CFAFAFC", "CAAAAAC", "AGAFAGA", "AACCCAA"},
            new String[]{"AAAQAAA", "AGGVGGA", "AGAFAGA", "XVFFFVX", "AGAFAGA", "AGGVGGA", "AAAQAAA"},
            new String[]{"AAAAAAA", "AAAQAAA", "AACCCAA", "AXCCCXA", "AACCCAA", "AAAQAAA", "AAAAAAA"});

    private InfinityFabricationSingularityTopologySchema() {
    }

    public static int schemaVersion() {
        return 2;
    }

    public static List<String[]> layers() {
        return LAYERS.stream().map(String[]::clone).toList();
    }
}

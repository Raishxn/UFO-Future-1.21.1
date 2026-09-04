package com.raishxn.ufo.api.multiblock.topology;

import java.util.List;

/** Schema two imported from the approved QPA Copy/Paste Gadget layout. */
public final class QpaTopologySchema {
    private static final List<String[]> LAYERS = List.of(
            new String[]{"AAAAA", "QAXAQ", "ACCCA", "ACCCA", "ACCCA", "QCCCQ", "QCCCQ", "ACCCA", "ACCCA", "ACCCA", "QAXAQ", "AAAAA"},
            new String[]{"AAXAA", "CCFCC", "CGFFC", "FGGFF", "FFGGF", "GFFGG", "GGFFG", "FGGFF", "FFGGF", "CFFGC", "CCFCC", "AAXAA"},
            new String[]{"AGGGA", "CAAAC", "FAAAF", "FAAAF", "GAAAG", "GAAAG", "FAAAF", "FAAAF", "GAAAG", "GAAAG", "CAAAC", "AGGGA"},
            new String[]{"XGHGX", "FAAAF", "FAAAF", "GAAAG", "GAAAG", "FAAAF", "FAAAF", "GAAAG", "GAAAG", "FAAAF", "FAAAF", "XGGGX"},
            new String[]{"AGGGA", "CAAAC", "GAAAG", "GAAAG", "FAAAF", "FAAAF", "GAAAG", "GAAAG", "FAAAF", "FAAAF", "CAAAC", "AGGGA"},
            new String[]{"AAXAA", "CCFCC", "CGFFC", "FGGFF", "FFGGF", "GFFGG", "GGFFG", "FGGFF", "FFGGF", "CFFGC", "CCFCC", "AAXAA"},
            new String[]{"AAAAA", "QAXAQ", "ACCCA", "ACCCA", "ACCCA", "QCCCQ", "QCCCQ", "ACCCA", "ACCCA", "ACCCA", "QAXAQ", "AAAAA"});

    private QpaTopologySchema() {
    }

    public static int schemaVersion() {
        return 2;
    }

    /** Returns a defensive copy so callers cannot mutate the canonical schema. */
    public static List<String[]> layers() {
        return LAYERS.stream().map(String[]::clone).toList();
    }
}

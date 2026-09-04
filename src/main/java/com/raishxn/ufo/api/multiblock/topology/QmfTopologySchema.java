package com.raishxn.ufo.api.multiblock.topology;

import java.util.List;

/** Schema two imported from the approved QMF Copy/Paste Gadget layout. */
public final class QmfTopologySchema {
    private static final List<String[]> LAYERS = List.of(
            new String[]{
                    "AAAAAAAAAAAAAAA", "CACAAAAAAAAACAC", "CACAAAAAAAAACAC", "CACAAAAAAAAACAC",
                    "CACAAAAAAAAACAC", "CACAAAAAAAAACAC", "AAAAAAAAAAAAAAA"
            },
            new String[]{
                    "CACAAAAAAAAACAC", "CACAAACHCAAACAC", "CFCAAACFCAAACFC", "CFCCCCFFFCCCCFC",
                    "CFCAAACFCAAACFC", "CACAAACCCAAACAC", "CACAAAAAAAAACAC"
            },
            new String[]{
                    "CACAAAAAAAAACAC", "CFCAACCGCCAACFC", "CFCCCCAAACCCCFC", "QFCAAAAQAAAACFQ",
                    "CFCCCCAAACCCCFC", "CFCAACCGCCAACFC", "CACAAAAAAAAACAC"
            },
            new String[]{
                    "CACAAAAAAAAACAC", "CFCAAGGGGGAACFC", "QFCGGGAAAGGGCFQ", "XFCAAAAXAAAACFX",
                    "QFCGGGAAAGGGCFQ", "CFCAAGGGGGAACFC", "CACAAAAAAAAACAC"
            },
            new String[]{
                    "CACAAAAAAAAACAC", "CFCAACCGCCAACFC", "CFCCCCAAACCCCFC", "QFCAAAAQAAAACFQ",
                    "CFCCCCAAACCCCFC", "CFCAACCGCCAACFC", "CACAAAAAAAAACAC"
            },
            new String[]{
                    "CACAAAAAAAAACAC", "CACAAACCCAAACAC", "CFCAACCFCCAACFC", "CFCCCCFFFCCCCFC",
                    "CFCAACCFCCAACFC", "CACAAACCCAAACAC", "CACAAAAAAAAACAC"
            },
            new String[]{
                    "AAAAAAAAAAAAAAA", "CACAAAAAAAAACAC", "CACAAAAAAAAACAC", "CACAAAAAAAAACAC",
                    "CACAAAAAAAAACAC", "CACAAAAAAAAACAC", "AAAAAAAAAAAAAAA"
            });

    private QmfTopologySchema() {
    }

    public static int schemaVersion() {
        return 2;
    }

    /** Returns a defensive copy so callers cannot mutate the canonical schema. */
    public static List<String[]> layers() {
        return LAYERS.stream().map(String[]::clone).toList();
    }
}

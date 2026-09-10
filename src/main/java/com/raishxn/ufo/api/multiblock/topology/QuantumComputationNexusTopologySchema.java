package com.raishxn.ufo.api.multiblock.topology;

import java.util.List;

/** Schema imported from the approved Quantum Computation Nexus Copy/Paste Gadget layout. */
public final class QuantumComputationNexusTopologySchema {
    private static final List<String[]> IMPORTED_LAYERS = List.of(
            new String[]{
                    "AAAAAAAAAAAA", "AAAAAAAAAAAA", "ACCCCAAAAAAA", "ACCCCCCAAAAA", "ACCCCCCCAAAA",
                    "AAAAACCCCCAA", "AAAAAACCCCAA", "AAAAAAAACCCA", "AAAAAAAACCCA", "AAAAAAAACCCA",
                    "AAAAAAAACCCA", "AAAAAAAACCCA", "AAAAAACCCCAA", "AAAAACCCCCAA", "ACCCCCCCCAAA",
                    "ACCCCCCAAAAA", "ACCCCAAAAAAA", "AAAAAAAAAAAA"
            },
            new String[]{
                    "AAAAAAAAAAAA", "ACCCCAAAAAAA", "XFFFFCCAAAAA", "XIIIIIICAAAA", "XFFFFFFFCCAA",
                    "ACCCCIIIIICA", "AAAACCIIIICA", "AAAAACCCIIIC", "AAAAAAACIIIC", "AAAAAAACIIIC",
                    "AAAAAAACIIIC", "AAAAACCCIIIC", "AAAACCIIIICA", "ACCCCIIIIICA", "QFFFFFFFFCAA",
                    "QIIIIIICCAAA", "QFFFFCCAAAAA", "ACCCCAAAAAAA"
            },
            new String[]{
                    "AAAAAAAAAAAA", "ACGGGAAAAAAA", "XIIIIGGAAAAA", "QFFFFFFGAAAA", "XIIIIIIIGGAA",
                    "ACGGGIIIIIGA", "AAAAGGIIIIGA", "AAAAAGGGIIIC", "AAAAAAACIIIC", "AAAAAAAHIIIL",
                    "AAAAAAACIIIC", "AAAAAGGGIIIC", "AAAAGGIIIIGA", "ACGGGIIIIIGA", "QIIIIIIIIGAA",
                    "XFFFFFFGGAAA", "QIIIIGGAAAAA", "ACGGGAAAAAAA"
            },
            new String[]{
                    "AAAAAAAAAAAA", "ACCCCAAAAAAA", "XFFFFCCAAAAA", "XIIIIIICAAAA", "XFFFFFFFCCAA",
                    "ACCCCIIIIICA", "AAAACCIIIICA", "AAAAACCCIIIC", "AAAAAAACIIIC", "AAAAAAACIIIC",
                    "AAAAAAACIIIC", "AAAAACCCIIIC", "AAAACCIIIICA", "ACCCCIIIIICA", "QFFFFFFFFCAA",
                    "QIIIIIICCAAA", "QFFFFCCAAAAA", "ACCCCAAAAAAA"
            },
            new String[]{
                    "AAAAAAAAAAAA", "AAAAAAAAAAAA", "ACCCCAAAAAAA", "ACCCCCCAAAAA", "ACCCCCCCAAAA",
                    "AAAAACCCCCAA", "AAAAAACCCCAA", "AAAAAAAACCCA", "AAAAAAAACCCA", "AAAAAAAACCCA",
                    "AAAAAAAACCCA", "AAAAAAAACCCA", "AAAAAACCCCAA", "AAAAACCCCCAA", "ACCCCCCCCAAA",
                    "ACCCCCCAAAAA", "ACCCCAAAAAAA", "AAAAAAAAAAAA"
            });
    // The Copy/Paste Gadget export stores its horizontal axes transposed. First
    // normalize those axes, then turn the complete layout around so the structure
    // and its hologram extend from the back of the controller's visible front.
    private static final List<String[]> LAYERS = rotate180(rotateCounterClockwise(IMPORTED_LAYERS));

    private QuantumComputationNexusTopologySchema() {
    }

    public static int schemaVersion() {
        return 5;
    }

    public static List<String[]> layers() {
        return LAYERS.stream().map(String[]::clone).toList();
    }

    private static List<String[]> rotateCounterClockwise(List<String[]> sourceLayers) {
        return sourceLayers.stream().map(source -> {
            int sourceDepth = source.length;
            int sourceWidth = source[0].length();
            String[] rotated = new String[sourceWidth];
            for (int z = 0; z < sourceWidth; z++) {
                StringBuilder row = new StringBuilder(sourceDepth);
                for (int x = 0; x < sourceDepth; x++) {
                    row.append(source[x].charAt(sourceWidth - 1 - z));
                }
                rotated[z] = row.toString();
            }
            return rotated;
        }).toList();
    }

    private static List<String[]> rotate180(List<String[]> sourceLayers) {
        return sourceLayers.stream().map(source -> {
            String[] rotated = new String[source.length];
            for (int z = 0; z < source.length; z++) {
                rotated[z] = new StringBuilder(source[source.length - 1 - z]).reverse().toString();
            }
            return rotated;
        }).toList();
    }
}

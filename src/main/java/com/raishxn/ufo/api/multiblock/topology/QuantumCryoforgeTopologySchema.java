package com.raishxn.ufo.api.multiblock.topology;

import java.util.List;

/** Canonical 9x14x9 topology exported from the redesigned Quantum Cryoforge. */
public final class QuantumCryoforgeTopologySchema {
    private static final List<String[]> LAYERS = List.of(
            layer("BBBBBBBBB", "BBBBBBBBB", "BBBBBBBBB", "BBBBBBBBB", "BBBBBBBBB", "BBBBBBBBB", "BBBBBBBBB", "BBBBBBBBB", "BBBBBBBBB"),
            layer("AABBCBBAA", "ABDDDDDBA", "BDDDDDDDB", "BDDFDFDDB", "BDDDFDDDB", "BDDFDFDDB", "BDDDDDDDB", "ABDDDDDBA", "AABBBBBAA"),
            layer("AABEBEBAA", "ABAAAAABA", "BAAAAAAAB", "EAAFAFAAE", "BAAAFAAAB", "EAAFAFAAE", "BAAAAAAAB", "ABAAAAABA", "AABEBEBAA"),
            layer("AABEBEBAA", "ABAAAAABA", "BAAAAAAAB", "EAAFAFAAE", "BAAAFAAAB", "EAAFAFAAE", "BAAAAAAAB", "ABAAAAABA", "AABEBEBAA"),
            layer("AABBBBBAA", "ABAAAAABA", "BAAAAAAAB", "BAAFAFAAB", "BAAAFAAAB", "BAAFAFAAB", "BAAAAAAAB", "ABAAAAABA", "AABBBBBAA"),
            layer("AAQQQQQAA", "AQAAAAAQA", "QAAAAAAAQ", "QAAFAFAAQ", "QAAAFAAAQ", "QAAFAFAAQ", "QAAAAAAAQ", "AQAAAAAQA", "AAQQQQQAA"),
            layer("AAQQQQQAA", "AQAAAAAQA", "QAAAAAAAQ", "QAAFAFAAQ", "QAAAFAAAQ", "QAAFAFAAQ", "QAAAAAAAQ", "AQAAAAAQA", "AAQQQQQAA"),
            layer("AAAAQAAAA", "AALLLLLAA", "ALLAAALLA", "ALAFAFALA", "QLAAFAALQ", "ALAFAFALA", "ALLAAALLA", "AALLLLLAA", "AAAAQAAAA"),
            layer("AAAAQAAAA", "AALLLLLAA", "ALLAAALLA", "ALAFAFALA", "QLAAFAALQ", "ALAFAFALA", "ALLAAALLA", "AALLLLLAA", "AAAAQAAAA"),
            layer("AAQQQQQAA", "AQLLLLLQA", "QLLAAALLQ", "QLAFAFALQ", "QLAAFAALQ", "QLAFAFALQ", "QLLAAALLQ", "AQLLLLLQA", "AAQQQQQAA"),
            layer("AAAAAAAAA", "AABBBBBAA", "ABBBBBBBA", "ABBBBBBBA", "ABBBBBBBA", "ABBBBBBBA", "ABBBBBBBA", "AABBBBBAA", "AAAAAAAAA"),
            layer("AAAAAAAAA", "AAAAAAAAA", "AAABBBAAA", "AABBBBBAA", "AABBBBBAA", "AABBBBBAA", "AAABBBAAA", "AAAAAAAAA", "AAAAAAAAA"),
            layer("AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAQAAAA", "AAAQQQAAA", "AAAAQAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA"),
            layer("AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAQAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA")
    );

    private QuantumCryoforgeTopologySchema() {
    }

    private static String[] layer(String... rows) {
        return rows;
    }

    public static int schemaVersion() {
        return 2;
    }

    public static List<String[]> layers() {
        return LAYERS.stream().map(String[]::clone).toList();
    }
}

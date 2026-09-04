package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.api.multiblock.topology.QmfTopologySchema;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QmfTopologySchemaTest {

    @Test
    void schemaTwoMatchesApprovedLayoutWithItsOriginalControllerSurroundings() {
        var layers = QmfTopologySchema.layers();
        assertEquals(2, QmfTopologySchema.schemaVersion());
        var summary = MultiblockTemplateCompiler.validate(
                layers, 'H', Set.of('A', 'C', 'F', 'G', 'H', 'Q', 'X'), true);

        assertEquals(15, summary.width());
        assertEquals(7, summary.height());
        assertEquals(7, summary.depth());
        assertEquals(7, summary.controllerX());
        assertEquals(1, summary.controllerY());
        assertEquals(1, summary.controllerZ());
        assertEquals(Set.of('A', 'C', 'F', 'G', 'H', 'Q', 'X'), summary.symbols());

        Map<Character, Long> counts = layers.stream()
                .flatMap(rows -> java.util.Arrays.stream(rows))
                .flatMapToInt(String::chars)
                .mapToObj(value -> (char) value)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(Map.of('A', 398L, 'C', 245L, 'F', 52L, 'G', 26L, 'H', 1L, 'Q', 10L, 'X', 3L), counts);
        assertEquals('C', layers.get(1)[1].charAt(6));
        assertEquals('C', layers.get(1)[1].charAt(8));
        assertEquals('A', layers.get(0)[1].charAt(7));
        assertEquals('G', layers.get(2)[1].charAt(7));
        assertEquals('A', layers.get(1)[0].charAt(7));
    }

    @Test
    void returnedTemplateCannotMutateTheCanonicalSchema() {
        var first = QmfTopologySchema.layers();
        first.getFirst()[0] = "HHHHH";

        assertEquals("AAAAAAAAAAAAAAA", QmfTopologySchema.layers().getFirst()[0]);
    }
}

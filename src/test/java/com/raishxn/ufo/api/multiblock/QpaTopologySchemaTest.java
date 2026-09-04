package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.api.multiblock.topology.QpaTopologySchema;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QpaTopologySchemaTest {

    @Test
    void schemaTwoMatchesApprovedLayoutAndControllerGlassFrame() {
        var layers = QpaTopologySchema.layers();
        assertEquals(2, QpaTopologySchema.schemaVersion());
        var summary = MultiblockTemplateCompiler.validate(
                layers, 'H', Set.of('A', 'C', 'F', 'G', 'H', 'Q', 'X'), true);

        assertEquals(5, summary.width());
        assertEquals(7, summary.height());
        assertEquals(12, summary.depth());
        assertEquals(2, summary.controllerX());
        assertEquals(3, summary.controllerY());
        assertEquals(0, summary.controllerZ());
        assertEquals(Set.of('A', 'C', 'F', 'G', 'H', 'Q', 'X'), summary.symbols());

        Map<Character, Long> counts = layers.stream()
                .flatMap(java.util.Arrays::stream)
                .flatMapToInt(String::chars)
                .mapToObj(value -> (char) value)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(Map.of('A', 166L, 'C', 80L, 'F', 72L, 'G', 73L, 'H', 1L, 'Q', 16L, 'X', 12L), counts);
        assertEquals('G', layers.get(3)[0].charAt(1));
        assertEquals('G', layers.get(3)[0].charAt(3));
        assertEquals('G', layers.get(2)[0].charAt(2));
        assertEquals('G', layers.get(4)[0].charAt(2));
    }

    @Test
    void returnedTemplateCannotMutateTheCanonicalSchema() {
        var first = QpaTopologySchema.layers();
        first.getFirst()[0] = "HHHHH";

        assertEquals("AAAAA", QpaTopologySchema.layers().getFirst()[0]);
    }
}

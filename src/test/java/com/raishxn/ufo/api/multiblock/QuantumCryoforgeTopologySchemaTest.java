package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.api.multiblock.topology.QuantumCryoforgeTopologySchema;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuantumCryoforgeTopologySchemaTest {

    @Test
    void schemaOnePreservesLegacyDimensionsAnchorSymbolsAndCellCounts() {
        var layers = QuantumCryoforgeTopologySchema.layers();
        assertEquals(1, QuantumCryoforgeTopologySchema.schemaVersion());
        var summary = MultiblockTemplateCompiler.validate(
                layers, 'C', Set.of('A', 'B', 'C', 'D', 'E', 'F'), true);

        assertEquals(6, summary.width());
        assertEquals(7, summary.height());
        assertEquals(7, summary.depth());
        assertEquals(5, summary.controllerX());
        assertEquals(1, summary.controllerY());
        assertEquals(3, summary.controllerZ());
        assertEquals(Set.of('A', 'B', 'C', 'D', 'E', 'F'), summary.symbols());

        Map<Character, Long> counts = layers.stream()
                .flatMap(java.util.Arrays::stream)
                .flatMapToInt(String::chars)
                .mapToObj(value -> (char) value)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(Map.of('A', 64L, 'B', 158L, 'C', 1L, 'D', 9L, 'E', 38L, 'F', 24L), counts);
    }

    @Test
    void returnedTemplateCannotMutateTheCanonicalSchema() {
        var first = QuantumCryoforgeTopologySchema.layers();
        first.getFirst()[0] = "CCCCCC";

        assertEquals("BBBBAA", QuantumCryoforgeTopologySchema.layers().getFirst()[0]);
    }
}

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
    void schemaTwoMatchesTheRedesignedCryoforgeExport() {
        var layers = QuantumCryoforgeTopologySchema.layers();
        assertEquals(2, QuantumCryoforgeTopologySchema.schemaVersion());
        var summary = MultiblockTemplateCompiler.validate(
                layers, 'C', Set.of('A', 'B', 'C', 'D', 'E', 'F', 'Q', 'L'), true);

        assertEquals(9, summary.width());
        assertEquals(14, summary.height());
        assertEquals(9, summary.depth());
        assertEquals(4, summary.controllerX());
        assertEquals(1, summary.controllerY());
        assertEquals(0, summary.controllerZ());
        assertEquals(Set.of('A', 'B', 'C', 'D', 'E', 'F', 'Q', 'L'), summary.symbols());

        Map<Character, Long> counts = layers.stream()
                .flatMap(java.util.Arrays::stream)
                .flatMapToInt(String::chars)
                .mapToObj(value -> (char) value)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(Map.of('A', 648L, 'B', 226L, 'C', 1L, 'D', 40L,
                'E', 16L, 'F', 45L, 'Q', 86L, 'L', 72L), counts);
    }

    @Test
    void returnedTemplateCannotMutateTheCanonicalSchema() {
        var first = QuantumCryoforgeTopologySchema.layers();
        first.getFirst()[0] = "CCCCCC";

        assertEquals("BBBBBBBBB", QuantumCryoforgeTopologySchema.layers().getFirst()[0]);
    }
}

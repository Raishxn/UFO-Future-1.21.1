package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.api.multiblock.topology.QuantumSlicerTopologySchema;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuantumSlicerTopologySchemaTest {

    @Test
    void schemaTwoNormalizesApprovedEastFacingLayoutWithAnExteriorControllerFace() {
        var layers = QuantumSlicerTopologySchema.layers();
        assertEquals(2, QuantumSlicerTopologySchema.schemaVersion());
        var summary = MultiblockTemplateCompiler.validate(
                layers, 'H', Set.of('A', 'C', 'F', 'G', 'H', 'Q', 'X'), true);

        assertEquals(13, summary.width());
        assertEquals(5, summary.height());
        assertEquals(5, summary.depth());
        assertEquals(6, summary.controllerX());
        assertEquals(2, summary.controllerY());
        assertEquals(0, summary.controllerZ());

        Map<Character, Long> counts = layers.stream()
                .flatMap(java.util.Arrays::stream)
                .flatMapToInt(String::chars)
                .mapToObj(value -> (char) value)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(Map.of('A', 48L, 'C', 143L, 'F', 61L, 'G', 36L, 'H', 1L, 'Q', 24L, 'X', 12L), counts);
        assertEquals('C', layers.get(2)[0].charAt(5));
        assertEquals('C', layers.get(2)[0].charAt(7));
        assertEquals('C', layers.get(1)[0].charAt(6));
        assertEquals('C', layers.get(3)[0].charAt(6));
    }
}

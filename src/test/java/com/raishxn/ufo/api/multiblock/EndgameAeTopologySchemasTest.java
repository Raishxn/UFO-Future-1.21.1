package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.api.multiblock.topology.InfinityFabricationSingularityTopologySchema;
import com.raishxn.ufo.api.multiblock.topology.QuantumComputationNexusTopologySchema;
import com.raishxn.ufo.api.multiblock.topology.QuantumPatternFabricationMatrixTopologySchema;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndgameAeTopologySchemasTest {

    @Test
    void computationNexusMatchesTheApprovedExport() {
        assertSchema(
                QuantumComputationNexusTopologySchema.layers(),
                18, 5, 12,
                8, 2, 7,
                Map.of('A', 578L, 'C', 242L, 'F', 58L, 'G', 36L, 'H', 1L, 'I', 146L,
                        'L', 1L, 'Q', 9L, 'X', 9L));
        var controllerLayer = QuantumComputationNexusTopologySchema.layers().get(2);
        assertEquals('H', controllerLayer[7].charAt(8));
        assertEquals('L', controllerLayer[11].charAt(8));
    }

    @Test
    void patternFabricationMatrixMatchesTheApprovedExport() {
        assertSchema(
                QuantumPatternFabricationMatrixTopologySchema.layers(),
                7, 5, 9,
                1, 0, 6,
                Map.of('A', 89L, 'C', 127L, 'F', 22L, 'G', 44L, 'H', 1L, 'L', 1L, 'Q', 16L, 'X', 15L));
    }

    @Test
    void infinityFabricationSingularityMatchesTheApprovedExport() {
        assertSchema(
                InfinityFabricationSingularityTopologySchema.layers(),
                7, 7, 7,
                0, 3, 3,
                Map.of('A', 196L, 'C', 51L, 'F', 25L, 'G', 36L, 'H', 1L, 'K', 1L, 'L', 1L, 'Q', 12L, 'V', 8L, 'X', 12L));
    }

    private static void assertSchema(java.util.List<String[]> layers,
                                     int width, int height, int depth,
                                     int controllerX, int controllerY, int controllerZ,
                                     Map<Character, Long> expectedCounts) {
        Set<Character> symbols = expectedCounts.keySet();
        var summary = MultiblockTemplateCompiler.validate(layers, 'H', symbols, true);
        assertEquals(width, summary.width());
        assertEquals(height, summary.height());
        assertEquals(depth, summary.depth());
        assertEquals(controllerX, summary.controllerX());
        assertEquals(controllerY, summary.controllerY());
        assertEquals(controllerZ, summary.controllerZ());

        Map<Character, Long> counts = layers.stream()
                .flatMap(Arrays::stream)
                .flatMapToInt(String::chars)
                .mapToObj(value -> (char) value)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(expectedCounts, counts);
    }
}

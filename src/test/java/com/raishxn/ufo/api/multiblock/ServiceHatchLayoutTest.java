package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.api.multiblock.topology.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ServiceHatchLayoutTest {
    @Test void preservesAllNonServiceCellsAcrossQuantumStructures() {
        for (var original : List.of(QmfTopologySchema.layers(), QpaTopologySchema.layers(),
                QuantumSlicerTopologySchema.layers(), QuantumCryoforgeTopologySchema.layers())) {
            boolean cryo = original.stream().flatMap(layer -> java.util.Arrays.stream(layer)).noneMatch(row -> row.indexOf('H') >= 0);
            char casing = cryo ? 'B' : 'C';
            var changed = ServiceHatchLayout.apply(original, cryo ? 'C' : 'H', casing);
            int coolant = 0, energy = 0, differences = 0;
            for (int y = 0; y < original.size(); y++) for (int z = 0; z < original.get(y).length; z++) {
                String before = original.get(y)[z], after = changed.get(y)[z];
                assertEquals(before.length(), after.length());
                for (int x = 0; x < before.length(); x++) {
                    char c = after.charAt(x);
                    if (c == 'J') coolant++;
                    if (c == 'K') energy++;
                    if (before.charAt(x) != c) {
                        differences++;
                        assertEquals(casing, before.charAt(x));
                        assertTrue(c == 'J' || c == 'K');
                    }
                }
            }
            assertEquals(2, differences);
            assertEquals(1, coolant);
            assertEquals(1, energy);
        }
    }
}

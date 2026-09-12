package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class SingularityEnergyIsolationContractTest {
    private static final Path JAVA = Path.of("src/main/java/com/raishxn/ufo");

    @Test void automaticCraftingCannotUseGridPower() throws IOException {
        String link = Files.readString(JAVA.resolve("block/entity/QuantumGridLinkBE.java"));
        int start = link.indexOf("public AutoCraftResult autoCraftPatterns(");
        int end = link.indexOf("private static Map<AEKey, Long> aggregateRequirements", start);
        String automatic = link.substring(start, end);
        assertFalse(automatic.contains("getEnergyService()"));
        assertFalse(automatic.contains("extractAEPower("));
        assertTrue(automatic.contains("singularity.extractCraftingEnergy(requestedPower, true)"));
        assertTrue(automatic.contains("singularity.extractCraftingEnergy(cost, false)"));
    }

    @Test void structureAndPreviewRequireAnEnergyHatch() throws IOException {
        var layers = com.raishxn.ufo.api.multiblock.topology.InfinityFabricationSingularityTopologySchema.layers();
        assertEquals(1L, layers.stream().flatMap(java.util.Arrays::stream)
                .flatMapToInt(String::chars).filter(symbol -> symbol == 'K').count());
        String factory = Files.readString(JAVA.resolve("block/entity/pattern/InfinityFabricationSingularityPatternFactory.java"));
        assertTrue(factory.contains(".where('K', MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get())"));
        String controller = Files.readString(JAVA.resolve("block/entity/InfinityFabricationSingularityControllerBE.java"));
        assertTrue(controller.contains("hatch.extractBufferedExternalEnergyAE(requested - extracted, simulate)"));
        assertFalse(controller.contains("getEnergyService()"));
    }
}

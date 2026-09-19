package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the Minecraft integration paths that would otherwise bypass the reservoir. */
class EnergyHatchIsolationContractTest {
    private static final Path ENTITIES = Path.of("src/main/java/com/raishxn/ufo/block/entity");

    @Test
    void hatchHasNoNetworkEnergyFallback() throws IOException {
        String source = Files.readString(ENTITIES.resolve("MassiveOutputHatchBE.java"));
        assertFalse(source.contains("extractAEPower("));
        assertFalse(source.contains("extractNetworkFeAsAe("));
        assertTrue(source.contains("this.externalEnergy.extract(maxAmount,"));
        assertTrue(source.contains("externalEnergy.receiveFe("));
        assertTrue(source.contains("public boolean canExtract() { return false; }"));
        assertTrue(source.contains("tag.putDouble(\"externalEnergyAE\", this.externalEnergy.stored())"));
        assertTrue(source.contains("this.externalEnergy.restore(tag.getDouble(\"externalEnergyAE\"))"));
    }

    @Test
    void patternProxyCannotBypassEnergyHatches() throws IOException {
        String source = Files.readString(ENTITIES.resolve("AbstractParallelMultiblockControllerBE.java"));
        assertFalse(source.contains("extractAEPower("));
        assertFalse(source.contains("allowGridRelay"));
        assertTrue(source.contains("this.energyPorts.extract(needed, false)"));
    }

    @Test
    void aeHatchesExposeEveryFaceToCables() throws IOException {
        String massiveHatch = Files.readString(ENTITIES.resolve("MassiveOutputHatchBE.java"));
        String patternBuffer = Files.readString(ENTITIES.resolve("QuantumPatternHatchBE.java"));
        assertTrue(massiveHatch.contains("return EnumSet.allOf(Direction.class);"));
        assertTrue(patternBuffer.contains("return EnumSet.allOf(Direction.class);"));
        assertFalse(massiveHatch.contains("return EnumSet.of(facing);"));
    }
}

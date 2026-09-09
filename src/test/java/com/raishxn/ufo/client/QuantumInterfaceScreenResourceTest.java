package com.raishxn.ufo.client;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuantumInterfaceScreenResourceTest {
    @Test
    void uniqueAe2StyleResourceIsAJsonObject() throws Exception {
        var stream = getClass().getResourceAsStream(
                "/assets/ae2/screens/ufo_quantum_interface.json");
        assertNotNull(stream, "Quantum Interface screen style must be packaged");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            assertTrue(JsonParser.parseReader(reader).isJsonObject(),
                    "AE2 screen styles must have a JSON object at their root");
        }
    }

    @Test
    void patternBufferUsesItsOwnMenuTitle() throws Exception {
        var stream = getClass().getResourceAsStream(
                "/assets/ae2/screens/quantum_pattern_buffer.json");
        assertNotNull(stream, "Quantum Pattern Buffer screen style must be packaged");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var root = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("block.ufo.quantum_pattern_buffer",
                    root.getAsJsonObject("text")
                            .getAsJsonObject("dialog_title")
                            .getAsJsonObject("text")
                            .get("translate").getAsString());
        }
    }
}

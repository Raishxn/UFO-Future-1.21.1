package com.raishxn.ufo.client;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
}

package com.raishxn.ufo.metadata;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MixinConfigurationContractTest {
    @Test
    void ufoDoesNotInterceptAe2GlobalPostRegistrationLifecycle() throws IOException {
        String mixinConfiguration;
        try (InputStream stream = MixinConfigurationContractTest.class.getResourceAsStream("/ufo.mixins.json")) {
            assertNotNull(stream, "ufo.mixins.json must be present on the test classpath");
            mixinConfiguration = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertFalse(mixinConfiguration.contains("MixinAppEngBase"));
    }
}

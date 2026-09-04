package com.raishxn.ufo.metadata;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModMetadataContractTest {
    private static final Pattern DEPENDENCY_BLOCK = Pattern.compile(
            "\\[\\[dependencies\\.ufo]](.*?)(?=\\[\\[|\\z)", Pattern.DOTALL);
    private static final Pattern FIELD = Pattern.compile(
            "(?m)^\\s*([A-Za-z]+)\\s*=\\s*\"([^\"]*)\"\\s*(?:#.*)?$");

    @Test
    void generatedMetadataDeclaresTheExactRequiredRuntimeContract() throws IOException {
        Map<String, Dependency> dependencies = dependencies();

        assertEquals(Set.of("neoforge", "minecraft", "ae2", "ufocore", "ae2addonlib", "geckolib", "mekanism"),
                dependencies.values().stream()
                        .filter(dependency -> dependency.type().equalsIgnoreCase("required"))
                        .map(Dependency::modId)
                        .collect(java.util.stream.Collectors.toSet()));

        assertDependency(dependencies, "neoforge", "required", "[21.1.216,)", "NONE", "BOTH");
        assertDependency(dependencies, "minecraft", "required", "[1.21.1]", "NONE", "BOTH");
        assertDependency(dependencies, "ae2", "required", "[19.2.17,20)", "AFTER", "BOTH");
        assertDependency(dependencies, "ufocore", "required", "[0.1.0-alpha.1,0.2)", "AFTER", "BOTH");
        assertDependency(dependencies, "ae2addonlib", "required", "[1.0.3-1.21.1,2)", "AFTER", "BOTH");
        assertDependency(dependencies, "geckolib", "required", "[4.8.2,5)", "AFTER", "BOTH");
        assertDependency(dependencies, "mekanism", "required", "[10.7.18,11)", "AFTER", "BOTH");
    }

    @Test
    void optionalIntegrationsRemainOptionalAndOrderedAfterTheirHosts() throws IOException {
        Map<String, Dependency> dependencies = dependencies();

        assertDependency(dependencies, "jei", "optional", "", "AFTER", "CLIENT");
        assertDependency(dependencies, "emi", "optional", "", "AFTER", "CLIENT");
        assertDependency(dependencies, "appmek", "optional", "", "AFTER", "BOTH");
        assertDependency(dependencies, "appflux", "optional", "", "AFTER", "BOTH");
        assertEquals(Set.of("jei", "emi", "appmek", "appflux"), dependencies.values().stream()
                .filter(dependency -> dependency.type().equalsIgnoreCase("optional"))
                .map(Dependency::modId)
                .collect(java.util.stream.Collectors.toSet()));
    }

    private static Map<String, Dependency> dependencies() throws IOException {
        String metadata;
        try (InputStream stream = ModMetadataContractTest.class.getResourceAsStream(
                "/META-INF/neoforge.mods.toml")) {
            assertNotNull(stream, "generated neoforge.mods.toml must be present on the test classpath");
            metadata = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        Map<String, Dependency> result = new LinkedHashMap<>();
        Matcher blocks = DEPENDENCY_BLOCK.matcher(metadata);
        while (blocks.find()) {
            Map<String, String> fields = new LinkedHashMap<>();
            Matcher fieldMatcher = FIELD.matcher(blocks.group(1));
            while (fieldMatcher.find()) {
                fields.put(fieldMatcher.group(1), fieldMatcher.group(2));
            }
            Dependency dependency = new Dependency(
                    fields.getOrDefault("modId", ""),
                    fields.getOrDefault("type", ""),
                    fields.getOrDefault("versionRange", ""),
                    fields.getOrDefault("ordering", ""),
                    fields.getOrDefault("side", ""));
            result.put(dependency.modId(), dependency);
        }
        return result;
    }

    private static void assertDependency(Map<String, Dependency> dependencies, String modId, String type,
                                         String versionRange, String ordering, String side) {
        assertEquals(new Dependency(modId, type, versionRange, ordering, side), dependencies.get(modId));
    }

    private record Dependency(String modId, String type, String versionRange, String ordering, String side) {
    }
}

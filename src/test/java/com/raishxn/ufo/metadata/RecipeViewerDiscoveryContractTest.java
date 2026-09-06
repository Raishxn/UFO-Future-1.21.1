package com.raishxn.ufo.metadata;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeViewerDiscoveryContractTest {
    @Test
    void jeiRecipeProviderIsDiscoverableWithoutAnEmiEntrypointSuppressingIt() throws IOException {
        Path classes = Path.of("build/classes/java/main/com/raishxn/ufo");
        String jeiPlugin = classBytes(classes.resolve("compat/jei/UfoJeiPlugin.class"));
        assertTrue(jeiPlugin.contains("Lmezz/jei/api/JeiPlugin;"));

        // EMI's PluginCallerMixin skips JEI plugins for mods with an EMI entrypoint.
        // Inspect compiled classes so a stale entrypoint in the artifact also fails.
        try (var files = Files.walk(classes)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                assertFalse(classBytes(file).contains("Ldev/emi/emi/api/EmiEntrypoint;"),
                        () -> file + " suppresses UFO's JEI recipes through EMI's handled-mod filter");
            }
        }
    }

    private static String classBytes(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);
    }
}

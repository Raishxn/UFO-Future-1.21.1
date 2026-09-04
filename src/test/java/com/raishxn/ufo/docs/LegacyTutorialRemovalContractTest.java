package com.raishxn.ufo.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyTutorialRemovalContractTest {
    private static final Path MAIN_JAVA = Path.of("src/main/java");
    private static final Path MAIN_RESOURCES = Path.of("src/main/resources");
    private static final Path GUIDE_ROOT = MAIN_RESOURCES.resolve("assets/ufo/ae2guide");

    @Test
    void legacyTutorialPackagesAndPrototypeDocumentStayRemoved() throws IOException {
        assertTrue(hasNoRegularFiles(MAIN_JAVA.resolve("com/raishxn/ufo/api/tutorial")));
        assertTrue(hasNoRegularFiles(MAIN_JAVA.resolve("com/raishxn/ufo/client/tutorial")));
        assertFalse(Files.exists(Path.of("docs/ufo-ponder-plan.md")));
    }

    @Test
    void legacyTextsKeybindsAndRegistrationsStayRemoved() throws IOException {
        List<String> forbidden = List.of(
                "key.ufo.open_tutorial",
                "ufo.tutorial.",
                "open_ufo_tutorial",
                "ufotutorial",
                "open ufo tutorial",
                "press w to open guide",
                "hold [%s] to open guide",
                "segure [%s] para abrir o guia");
        List<String> errors = new ArrayList<>();

        for (Path root : List.of(MAIN_JAVA, MAIN_RESOURCES)) {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.filter(Files::isRegularFile)
                        .filter(LegacyTutorialRemovalContractTest::isTextFile)
                        .toList()) {
                    String content = Files.readString(path).toLowerCase(Locale.ROOT);
                    for (String token : forbidden) {
                        if (content.contains(token)) {
                            errors.add(path + " contains legacy token " + token);
                        }
                    }
                }
            }
        }

        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    @Test
    void noLegacyTutorialOrPonderAssetsRemain() throws IOException {
        List<Path> orphanAssets;
        try (Stream<Path> paths = Files.walk(MAIN_RESOURCES.resolve("assets/ufo"))) {
            orphanAssets = paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.contains("tutorial") || name.contains("ponder");
                    })
                    .toList();
        }
        assertTrue(orphanAssets.isEmpty(), "legacy tutorial assets remain: " + orphanAssets);
    }

    @Test
    void guideMeContentAndGeneratorRemainInstalled() throws IOException {
        assertTrue(Files.isRegularFile(GUIDE_ROOT.resolve("ufo_intro/index.md")));
        assertTrue(Files.isRegularFile(GUIDE_ROOT.resolve("ufo_intro/machines.md")));
        assertTrue(Files.isRegularFile(GUIDE_ROOT.resolve("ufo_intro/troubleshooting.md")));
        assertTrue(Files.isRegularFile(MAIN_JAVA.resolve(
                "com/raishxn/ufo/datagen/GuideStructureProvider.java")));

        try (Stream<Path> pages = Files.walk(GUIDE_ROOT)) {
            assertTrue(pages.filter(path -> path.toString().endsWith(".md")).count() >= 30,
                    "GuideME page tree was unexpectedly removed");
        }
        String build = Files.readString(Path.of("build.gradle"));
        assertTrue(build.contains("org.appliedenergistics:guideme"),
                "GuideME compile/runtime integration must remain installed");
    }

    private static boolean isTextFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".java") || name.endsWith(".json") || name.endsWith(".mcmeta")
                || name.endsWith(".toml") || name.endsWith(".md") || name.endsWith(".txt");
    }

    private static boolean hasNoRegularFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return true;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.noneMatch(Files::isRegularFile);
        }
    }
}

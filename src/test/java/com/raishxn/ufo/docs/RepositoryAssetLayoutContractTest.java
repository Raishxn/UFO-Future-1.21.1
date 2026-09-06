package com.raishxn.ufo.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryAssetLayoutContractTest {
    @Test
    void formedCraftingCubesHaveGeometryForEveryTier() throws IOException {
        Path models = Path.of("src/generated/resources/assets/ufo/models/block");
        for (String name : List.of("1b", "50b", "1t", "250t", "1qd")) {
            assertCraftingGeometry(models.resolve(name + "_mega_crafting_storage_formed.json"),
                    "STORAGE_" + name.toUpperCase(java.util.Locale.ROOT), false);
        }
        for (String name : List.of("50m", "150m", "300m", "750m", "2b")) {
            assertCraftingGeometry(models.resolve(name + "_mega_co_processor_formed.json"),
                    "COPROCESSOR_" + name.toUpperCase(java.util.Locale.ROOT), true);
        }
    }

    private static void assertCraftingGeometry(Path path, String tier, boolean coprocessor) throws IOException {
        var json = com.google.gson.JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        org.junit.jupiter.api.Assertions.assertEquals("ufo:crafting_cube", json.get("loader").getAsString(), path.toString());
        org.junit.jupiter.api.Assertions.assertEquals(tier, json.get("tier").getAsString(), path.toString());
        org.junit.jupiter.api.Assertions.assertEquals(coprocessor, json.get("coprocessor").getAsBoolean(), path.toString());
    }

    private static final Path MAIN_RESOURCES = Path.of("src/main/resources");
    private static final Path GENERATED_RESOURCES = Path.of("src/generated/resources");
    private static final Path UFO_TEXTURES = MAIN_RESOURCES.resolve("assets/ufo/textures");
    private static final Pattern MODEL_REFERENCE = Pattern.compile(
            "\\\"(?:parent|model)\\\"\\s*:\\s*\\\"(ufo:[^\\\"]+)\\\"");
    private static final Pattern TEXTURE_OBJECT = Pattern.compile(
            "\\\"textures\\\"\\s*:\\s*\\{([^}]*)}", Pattern.DOTALL);
    private static final Pattern RESOURCE_VALUE = Pattern.compile(
            "\\\"[^\\\"]+\\\"\\s*:\\s*\\\"(ufo:[^\\\"]+)\\\"");

    @Test
    void generatedAndManualResourcesNeverHaveTwoOwners() throws IOException {
        List<Path> conflicts = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(MAIN_RESOURCES)) {
            for (Path manual : paths.filter(Files::isRegularFile).toList()) {
                Path relative = MAIN_RESOURCES.relativize(manual);
                if (Files.isRegularFile(GENERATED_RESOURCES.resolve(relative))) {
                    conflicts.add(relative);
                }
            }
        }
        assertTrue(conflicts.isEmpty(), "resources with manual and generated owners: " + conflicts);
    }

    @Test
    void legacyTextureTreesStayRemoved() {
        List<Path> forbidden = List.of(
                UFO_TEXTURES.resolve("block/crafting"),
                UFO_TEXTURES.resolve("block/quantum_processor_assembler"),
                UFO_TEXTURES.resolve("block/quantum_slicer"),
                UFO_TEXTURES.resolve("ufoset"),
                UFO_TEXTURES.resolve("item/ufoset/armors"));
        for (Path path : forbidden) {
            assertFalse(Files.exists(path), "legacy texture tree returned: " + path);
        }
    }

    @Test
    void animationMetadataAlwaysHasItsPng() throws IOException {
        List<Path> orphanMetadata = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(MAIN_RESOURCES.resolve("assets"))) {
            for (Path metadata : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".png.mcmeta"))
                    .toList()) {
                Path png = metadata.resolveSibling(
                        metadata.getFileName().toString().substring(0,
                                metadata.getFileName().toString().length() - ".mcmeta".length()));
                if (!Files.isRegularFile(png)) {
                    orphanMetadata.add(metadata);
                }
            }
        }
        assertTrue(orphanMetadata.isEmpty(), "metadata without PNG: " + orphanMetadata);
    }

    @Test
    void stableAndPlannedAssetsRemainInTheirPublishedLocations() {
        List<Path> required = List.of(
                UFO_TEXTURES.resolve("block/multiblock/entropy_singularity_casing.png"),
                UFO_TEXTURES.resolve("block/multiblock/entropy_singularity_casing_ctm.png"),
                UFO_TEXTURES.resolve("block/multiblock/quantum_hyper_mechanical_casing.png"),
                UFO_TEXTURES.resolve("block/multiblock/quantum_hyper_mechanical_casing_ctm.png"),
                UFO_TEXTURES.resolve("block/multiblock/coolant_fluid_hatch_overlay.png"),
                UFO_TEXTURES.resolve("item/ufoset/axe.png"),
                UFO_TEXTURES.resolve("item/ufoset/animations/animations_ufo.png"),
                UFO_TEXTURES.resolve("models/armor/astral_nexus_layer_1.png"),
                UFO_TEXTURES.resolve("gui/universalguipages.png"));
        for (Path path : required) {
            assertTrue(Files.isRegularFile(path), "required asset moved or removed: " + path);
        }
    }

    @Test
    void everyLocalModelReferenceAndTextureSlotResolves() throws IOException {
        Map<String, Path> models = new LinkedHashMap<>();
        collectModels(GENERATED_RESOURCES.resolve("assets/ufo/models"), models);
        collectModels(MAIN_RESOURCES.resolve("assets/ufo/models"), models);

        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Path> entry : models.entrySet()) {
            String json = Files.readString(entry.getValue());
            Matcher modelReferences = MODEL_REFERENCE.matcher(json);
            while (modelReferences.find()) {
                String id = modelReferences.group(1);
                if (id.endsWith(".obj")) {
                    Path obj = MAIN_RESOURCES.resolve("assets/ufo/" + id.substring("ufo:".length()));
                    if (!Files.isRegularFile(obj)) {
                        missing.add(entry.getValue() + " -> " + id);
                    }
                } else if (!models.containsKey(id)) {
                    missing.add(entry.getValue() + " -> " + id);
                }
            }
            Matcher textureObjects = TEXTURE_OBJECT.matcher(json);
            while (textureObjects.find()) {
                Matcher textureReferences = RESOURCE_VALUE.matcher(textureObjects.group(1));
                while (textureReferences.find()) {
                    String value = textureReferences.group(1);
                    Path png = MAIN_RESOURCES.resolve("assets/ufo/textures/"
                            + value.substring("ufo:".length()) + ".png");
                    if (!Files.isRegularFile(png)) {
                        missing.add(entry.getValue() + " -> " + value);
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "broken local model graph: " + missing);
    }

    @Test
    void releaseHistoryLivesOutsideTheRepositoryRoot() throws IOException {
        assertTrue(Files.isRegularFile(Path.of("docs/releases/CHANGELOG.md")));
        try (Stream<Path> rootFiles = Files.list(Path.of("."))) {
            assertTrue(rootFiles.filter(Files::isRegularFile)
                    .noneMatch(path -> path.getFileName().toString().startsWith("UPDATE_LOG")),
                    "fragmented update log returned to repository root");
        }
    }

    private static void collectModels(Path root, Map<String, Path> models) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path model : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .toList()) {
                String id = "ufo:" + root.relativize(model).toString().replace('\\', '/');
                models.putIfAbsent(id.substring(0, id.length() - ".json".length()), model);
            }
        }
    }

}

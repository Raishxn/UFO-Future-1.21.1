package com.raishxn.ufo.docs;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectedTextureContractTest {
    private static final Path GENERATED_MODELS = Path.of("src/generated/resources/assets/ufo/models/block");
    private static final Path TEXTURES = Path.of("src/main/resources/assets/ufo/textures/block/multiblock");
    private static final List<String> CASINGS = List.of(
            "quantum_hyper_mechanical_casing", "entropy_singularity_casing");

    @Test
    void generatedCasingModelsUseOnlyTheUfoLoaderAndTextures() throws IOException {
        for (String casing : CASINGS) {
            String model = Files.readString(GENERATED_MODELS.resolve(casing + ".json"));
            assertTrue(model.contains("\"loader\": \"ufo:connected_texture\""));
            assertTrue(model.contains("\"connection\": \"ufo:same_block\""));
            assertTrue(model.contains("\"render_type\": \"minecraft:solid\""));
            assertTrue(model.contains("\"base\": \"ufo:block/multiblock/" + casing + "\""));
            assertTrue(model.contains("\"ctm\": \"ufo:block/multiblock/" + casing + "_ctm\""));
            assertTrue(model.contains("\"particle\": \"ufo:block/multiblock/" + casing + "\""));

            String itemModel = Files.readString(Path.of(
                    "src/generated/resources/assets/ufo/models/item", casing + ".json"));
            assertTrue(itemModel.contains("\"parent\": \"ufo:block/" + casing + "_inventory\""),
                    casing + " item must use the explicit vanilla fallback model");

            String inventoryModel = Files.readString(GENERATED_MODELS.resolve(casing + "_inventory.json"));
            assertTrue(inventoryModel.contains("\"parent\": \"minecraft:block/cube_all\""));
            assertTrue(inventoryModel.contains("\"all\": \"ufo:block/multiblock/" + casing + "\""));
        }
    }

    @Test
    void compactSheetsAndFallbackTexturesHaveTheRequiredDimensions() throws IOException {
        for (String casing : CASINGS) {
            assertDimensions(TEXTURES.resolve(casing + ".png"), 16, 16);
            assertDimensions(TEXTURES.resolve(casing + "_ctm.png"), 32, 32);
        }
    }

    @Test
    void obsoleteExternalCtmMetadataAndNamespacesStayRemoved() throws IOException {
        assertFalse(Files.exists(TEXTURES.resolve("quantum_hyper_mechanical_casing.png.mcmeta")));
        for (Path root : List.of(Path.of("src/main"), Path.of("src/generated/resources"))) {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.filter(Files::isRegularFile).filter(ConnectedTextureContractTest::isText).toList()) {
                    String text = Files.readString(path).toLowerCase(Locale.ROOT);
                    assertFalse(text.contains("\"ldlib\""), path + " contains LDLib CTM metadata");
                    assertFalse(text.contains("gtocore:"), path + " contains a GTOCore asset reference");
                }
            }
        }
        String build = Files.readString(Path.of("build.gradle")).toLowerCase(Locale.ROOT);
        assertFalse(build.contains("team.chisel") || build.contains("athena") || build.contains("fusion"),
                "Native CTM must not gain an external runtime dependency");
    }

    private static void assertDimensions(Path path, int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        assertTrue(image != null, path + " must be a readable PNG");
        assertEquals(width, image.getWidth(), path + " width");
        assertEquals(height, image.getHeight(), path + " height");
    }

    private static boolean isText(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".java") || name.endsWith(".json") || name.endsWith(".mcmeta")
                || name.endsWith(".toml") || name.endsWith(".md") || name.endsWith(".gradle");
    }
}

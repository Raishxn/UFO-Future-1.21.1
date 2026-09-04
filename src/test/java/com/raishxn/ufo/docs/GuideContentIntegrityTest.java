package com.raishxn.ufo.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideContentIntegrityTest {
    private static final Path GUIDE_ROOT = Path.of("src/main/resources/assets/ufo/ae2guide");
    private static final Pattern PARENT = Pattern.compile("(?m)^\\s*parent:\\s*([^\\s]+)\\s*$");
    private static final Pattern LOCAL_MARKDOWN_LINK = Pattern.compile("\\[[^]]+]\\((?!https?://)([^)#]+\\.md)(?:#[^)]*)?\\)");
    private static final Pattern IMPORTED_STRUCTURE = Pattern.compile("<ImportStructure\\s+src=\\\"([^\\\"]+\\.snbt)\\\"");
    private static final Pattern RECIPE = Pattern.compile("<Recipe\\s+id=\\\"([a-z0-9_.-]+):([a-z0-9_./-]+)\\\"");
    private static final Pattern ITEM_LINK = Pattern.compile("<ItemLink[^>]*\\bid=\\\"(ufo:[a-z0-9_./-]+)\\\"");
    private static final Pattern STRUCTURE_STATE = Pattern.compile("state:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern UFO_COMPONENT_ID = Pattern.compile(
            "<(?:ItemLink|ItemImage|ItemIcon|BlockImage)[^>]*\\bid=\\\"(ufo:[a-z0-9_./-]+)\\\""
                    + "|^\\s*(?:icon:\\s*|-\\s+)(ufo:[a-z0-9_./-]+)\\s*$",
            Pattern.MULTILINE);
    private static final Pattern ITEM_IDS_BLOCK = Pattern.compile(
            "(?m)^item_ids:[ \\t]*\\R((?:[ \\t]+-[ \\t]+ufo:[a-z0-9_./-]+[ \\t]*(?:\\R|$))+)");
    private static final Pattern ITEM_ID_LINE = Pattern.compile(
            "(?m)^[ \\t]+-[ \\t]+(ufo:[a-z0-9_./-]+)[ \\t]*$");

    @Test
    void guideHasNoEmptyPagesOrBrokenParentsAndLinks() throws IOException {
        List<String> errors = new ArrayList<>();
        for (Path page : guidePages()) {
            String content = Files.readString(page);
            if (content.isBlank()) {
                errors.add("empty page: " + page);
            }

            var parents = PARENT.matcher(content);
            while (parents.find()) {
                Path target = GUIDE_ROOT.resolve(parents.group(1)).normalize();
                if (!Files.isRegularFile(target)) {
                    errors.add("missing parent " + parents.group(1) + " in " + page);
                }
            }

            var links = LOCAL_MARKDOWN_LINK.matcher(content);
            while (links.find()) {
                Path target = page.getParent().resolve(links.group(1)).normalize();
                if (!Files.isRegularFile(target)) {
                    errors.add("broken link " + links.group(1) + " in " + page);
                }
            }

            var structures = IMPORTED_STRUCTURE.matcher(content);
            while (structures.find()) {
                Path mainTarget = page.getParent().resolve(structures.group(1)).normalize();
                Path relative = GUIDE_ROOT.relativize(mainTarget);
                Path generatedTarget = Path.of("src/generated/resources/assets/ufo/ae2guide").resolve(relative);
                if (!Files.isRegularFile(mainTarget) && !Files.isRegularFile(generatedTarget)) {
                    errors.add("missing imported structure " + structures.group(1) + " in " + page);
                }
            }
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    @Test
    void ufoGuideComponentIdsHaveGeneratedModels() throws IOException {
        Set<String> knownIds = modeledGuideIds();
        List<String> errors = new ArrayList<>();
        for (Path page : guidePages()) {
            var matcher = UFO_COMPONENT_ID.matcher(Files.readString(page));
            while (matcher.find()) {
                String id = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                if (!knownIds.contains(id)) {
                    errors.add("unknown component id " + id + " in " + page);
                }
            }
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    @Test
    void guideDoesNotContainDraftPlaceholders() throws IOException {
        List<String> forbidden = List.of(
                "Additional Suggested Bonuses",
                "if your mod includes them",
                "Add or remove these effects",
                "usually a mid-to-late game craft");
        List<String> errors = new ArrayList<>();
        for (Path page : guidePages()) {
            String content = Files.readString(page);
            for (String phrase : forbidden) {
                if (content.contains(phrase)) {
                    errors.add("draft phrase '" + phrase + "' in " + page);
                }
            }
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    @Test
    void guideNavigationDoesNotIndexTheSameItemTwice() throws IOException {
        Map<String, Path> ownerByItem = new HashMap<>();
        List<String> errors = new ArrayList<>();
        for (Path page : guidePages()) {
            var blocks = ITEM_IDS_BLOCK.matcher(Files.readString(page));
            while (blocks.find()) {
                var ids = ITEM_ID_LINE.matcher(blocks.group(1));
                while (ids.find()) {
                    String id = ids.group(1);
                    Path previous = ownerByItem.putIfAbsent(id, page);
                    if (previous != null && !previous.equals(page)) {
                        errors.add("duplicate navigation item " + id + " in " + previous + " and " + page);
                    }
                }
            }
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    @Test
    void guideRecipesExistInRuntimeResources() throws IOException {
        List<String> errors = new ArrayList<>();
        for (Path page : guidePages()) {
            var recipes = RECIPE.matcher(Files.readString(page));
            while (recipes.find()) {
                String namespace = recipes.group(1);
                String recipePath = recipes.group(2) + ".json";
                Path main = Path.of("src/main/resources/data", namespace, "recipe", recipePath);
                Path generated = Path.of("src/generated/resources/data", namespace, "recipe", recipePath);
                if (!Files.isRegularFile(main) && !Files.isRegularFile(generated)) {
                    errors.add("missing recipe " + recipes.group() + " in " + page);
                }
            }
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    @Test
    void everyUfoItemLinkHasExactlyOneNavigationTarget() throws IOException {
        Map<String, Path> ownerByItem = navigationOwners();
        List<String> errors = new ArrayList<>();
        for (Path page : guidePages()) {
            var links = ITEM_LINK.matcher(Files.readString(page));
            while (links.find()) {
                if (!ownerByItem.containsKey(links.group(1))) {
                    errors.add("ItemLink without indexed guide page " + links.group(1) + " in " + page);
                }
            }
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    @Test
    void generatedGuideStructuresUseGuideMeTextualListSyntax() throws IOException {
        Path root = Path.of("src/generated/resources/assets/ufo/ae2guide/assets/assemblies");
        List<String> errors = new ArrayList<>();
        try (Stream<Path> files = Files.list(root)) {
            for (Path structure : files.filter(path -> path.toString().endsWith(".snbt")).toList()) {
                String content = Files.readString(structure);
                if (content.contains("[I;")) {
                    errors.add("structure uses an NBT int-array instead of GuideME list syntax: " + structure);
                }
                if (!content.contains("size: [") || !content.contains("pos: [")) {
                    errors.add("structure is missing textual size/position lists: " + structure);
                }
                int paletteStart = content.indexOf("palette: [");
                if (paletteStart < 0) {
                    errors.add("structure has no GuideME palette: " + structure);
                    continue;
                }
                String palette = content.substring(paletteStart);
                var states = STRUCTURE_STATE.matcher(content.substring(0, paletteStart));
                while (states.find()) {
                    if (!palette.contains("\"" + states.group(1) + "\"")) {
                        errors.add("state missing from GuideME palette in " + structure + ": " + states.group(1));
                    }
                }
            }
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    private static List<Path> guidePages() throws IOException {
        try (Stream<Path> files = Files.walk(GUIDE_ROOT)) {
            return files.filter(path -> path.toString().endsWith(".md"))
                    .sorted()
                    .toList();
        }
    }

    private static Set<String> modeledGuideIds() throws IOException {
        Set<String> result = new HashSet<>();
        collectIds(Path.of("src/generated/resources/assets/ufo/models/item"), result);
        collectIds(Path.of("src/generated/resources/assets/ufo/blockstates"), result);
        collectIds(Path.of("src/main/resources/assets/ufo/models/item"), result);
        collectIds(Path.of("src/main/resources/assets/ufo/blockstates"), result);

        // AE2 renders cable-bus parts through its part model system instead of a
        // conventional item-model JSON. The item itself is registered by ModItems.
        result.add("ufo:quantum_pattern_provider_part");
        return result;
    }

    private static Map<String, Path> navigationOwners() throws IOException {
        Map<String, Path> result = new HashMap<>();
        for (Path page : guidePages()) {
            var blocks = ITEM_IDS_BLOCK.matcher(Files.readString(page));
            while (blocks.find()) {
                var ids = ITEM_ID_LINE.matcher(blocks.group(1));
                while (ids.find()) {
                    result.put(ids.group(1), page);
                }
            }
        }
        return result;
    }

    private static void collectIds(Path root, Set<String> target) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String relative = root.relativize(path).toString().replace('\\', '/');
                        target.add("ufo:" + relative.substring(0, relative.length() - ".json".length()));
                    });
        }
    }
}

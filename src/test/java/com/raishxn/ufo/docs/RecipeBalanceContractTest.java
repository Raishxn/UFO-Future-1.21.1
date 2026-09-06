package com.raishxn.ufo.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeBalanceContractTest {
    private static final Path RECIPE_ROOT = Path.of("src/generated/resources/data/ufo/recipe");
    private static final long BULK_SIZE = 64L;

    @Test
    void baseStellarMatterKeepsRestoredEnergyCosts() throws IOException {
        String[] materials = {"white_dwarf", "neutron_star", "pulsar"};
        long[] costs = {5_000_000L, 10_000_000L, 15_000_000L};
        for (int i = 0; i < materials.length; i++) {
            String name = materials[i] + "_matter";
            for (String path : List.of("dma/" + name + ".json",
                    "universal/qmf/" + name + "_batch.json")) {
                JsonObject json = JsonParser.parseString(Files.readString(RECIPE_ROOT.resolve(path))).getAsJsonObject();
                long scale = path.startsWith("dma/") ? 1L : BULK_SIZE;
                assertEquals(costs[i] * scale, longValue(json, "energy"), path);
                assertEquals(600L * (i + 1), longValue(json, "time"), path);
            }
        }
    }

    @Test
    void unambiguousBulkRecipesScaleLinearlyFromTheirUnitRecipe() throws IOException {
        Map<String, List<Recipe>> recipesByOutput = new HashMap<>();
        try (Stream<Path> files = Files.walk(RECIPE_ROOT)) {
            for (Path path : files.filter(file -> file.toString().endsWith(".json")).toList()) {
                JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                for (Output output : outputs(json)) {
                    recipesByOutput.computeIfAbsent(output.id(), ignored -> new ArrayList<>())
                            .add(new Recipe(path, json, output.amount()));
                }
            }
        }

        List<String> errors = new ArrayList<>();
        int comparedPairs = 0;
        for (Map.Entry<String, List<Recipe>> entry : recipesByOutput.entrySet()) {
            List<Recipe> units = entry.getValue().stream().filter(recipe -> recipe.outputAmount() == 1).toList();
            List<Recipe> bulks = entry.getValue().stream().filter(recipe -> recipe.outputAmount() == BULK_SIZE).toList();
            if (units.size() != 1 || bulks.size() != 1) {
                continue;
            }
            if (!isProcessingRecipe(units.getFirst().json()) || !isProcessingRecipe(bulks.getFirst().json())) {
                continue;
            }

            comparedPairs++;
            compareScaledMap(entry.getKey(), "item", itemInputs(units.getFirst().json()),
                    itemInputs(bulks.getFirst().json()), errors);
            compareScaledMap(entry.getKey(), "fluid", fluidInputs(units.getFirst().json()),
                    fluidInputs(bulks.getFirst().json()), errors);
            compareScaledValue(entry.getKey(), "energy", longValue(units.getFirst().json(), "energy"),
                    longValue(bulks.getFirst().json(), "energy"), errors);

            long unitTime = longValue(units.getFirst().json(), "time");
            long bulkTime = longValue(bulks.getFirst().json(), "time");
            if (unitTime != bulkTime) {
                errors.add(entry.getKey() + " time must stay constant for 64x parallel processing: "
                        + unitTime + " -> " + bulkTime);
            }
        }

        assertTrue(comparedPairs >= 30, "Expected broad unit/bulk coverage, found " + comparedPairs + " pairs");
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    @Test
    void componentMatrixProgressionKeepsTheApprovedEightfoldChain() throws IOException {
        assertProcess("dma/component/phase_shift.json", "ae2:cell_component_256k", 2,
                2_000_000L, 400);
        assertProcess("dma/component/hyper_dense_component_matrix.json", "ufo:phase_shift_component_matrix", 8,
                8_000_000L, 800);
        assertProcess("universal/qmf/tesseract_component_matrix_batch.json",
                "ufo:hyper_dense_component_matrix", 8, 400_000_000L, 1_600);
        assertProcess("universal/qmf/event_horizon_component_matrix_batch.json",
                "ufo:tesseract_component_matrix", 8, 1_600_000_000L, 4_800);
        assertProcess("universal/qmf/cosmic_string_component_matrix_batch.json",
                "ufo:event_horizon_component_matrix", 8, 6_400_000_000L, 19_200);
    }

    @Test
    void stellarFragmentProgressionPreservesMassAndEscalatesEnergy() throws IOException {
        JsonObject whiteDwarf = recipe("dma/ingot/white_dwarf_fragment.json");
        JsonObject neutronStar = recipe("dma/ingot/neutron_star_fragment.json");
        JsonObject pulsar = recipe("dma/ingot/pulsar_fragment.json");

        assertEquals(2, outputAmount(whiteDwarf, "ufo:white_dwarf_fragment_ingot"));
        assertEquals(2, outputAmount(neutronStar, "ufo:neutron_star_fragment_ingot"));
        assertEquals(2, outputAmount(pulsar, "ufo:pulsar_fragment_ingot"));
        assertEquals(2, itemInputs(neutronStar).get("ufo:white_dwarf_fragment_ingot"));
        assertEquals(2, itemInputs(pulsar).get("ufo:neutron_star_fragment_ingot"));
        assertEquals(List.of(100_000L, 500_000L, 1_000_000L),
                List.of(longValue(whiteDwarf, "energy"), longValue(neutronStar, "energy"),
                        longValue(pulsar, "energy")));
        assertEquals(List.of(400L, 600L, 800L),
                List.of(longValue(whiteDwarf, "time"), longValue(neutronStar, "time"),
                        longValue(pulsar, "time")));
    }

    @Test
    void infinityCellEnergyClassesMatchTheirProgressionTier() throws IOException {
        assertInfinityCell("infinity_cobblestone_cell", 1, 250_000_000L, 2_500);
        assertInfinityCell("infinity_obsidian_cell", 2, 1_000_000_000L, 10_000);
        assertInfinityCell("infinity_polonium_pellet_cell", 3, 2_000_000_000L, 20_000);
        assertInfinityCell("infinity_antimatter_pellet_cell", 3, 4_000_000_000L, 40_000);
    }

    private static void assertProcess(String relativePath, String gateItem, long gateAmount,
                                      long energy, long time) throws IOException {
        JsonObject json = recipe(relativePath);
        assertEquals(gateAmount, itemInputs(json).get(gateItem), relativePath + " gate amount");
        assertEquals(energy, longValue(json, "energy"), relativePath + " energy");
        assertEquals(time, longValue(json, "time"), relativePath + " time");
    }

    private static void assertInfinityCell(String name, long tier, long energy, long time) throws IOException {
        String relativePath = "universal/qmf/infinity_cell/" + name + ".json";
        JsonObject json = recipe(relativePath);
        assertEquals(tier, optionalLong(json, "required_tier", 1), relativePath + " tier");
        assertEquals(energy, longValue(json, "energy"), relativePath + " energy");
        assertEquals(time, longValue(json, "time"), relativePath + " time");
    }

    private static JsonObject recipe(String relativePath) throws IOException {
        return JsonParser.parseString(Files.readString(RECIPE_ROOT.resolve(relativePath))).getAsJsonObject();
    }

    private static long outputAmount(JsonObject json, String id) {
        return outputs(json).stream()
                .filter(output -> output.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing output " + id))
                .amount();
    }

    private static boolean isProcessingRecipe(JsonObject json) {
        return json.has("energy") && json.has("time")
                && (json.has("item_inputs") || json.has("fluid_inputs"));
    }

    private static void compareScaledMap(String recipe, String kind, Map<String, Long> unit,
                                         Map<String, Long> bulk, List<String> errors) {
        if (!unit.keySet().equals(bulk.keySet())) {
            errors.add(recipe + " " + kind + " inputs differ: " + unit.keySet() + " vs " + bulk.keySet());
            return;
        }
        for (Map.Entry<String, Long> entry : unit.entrySet()) {
            long expected = Math.multiplyExact(entry.getValue(), BULK_SIZE);
            long actual = bulk.get(entry.getKey());
            if (actual != expected) {
                errors.add(recipe + " " + kind + " " + entry.getKey() + " must scale 64x: expected "
                        + expected + ", found " + actual);
            }
        }
    }

    private static void compareScaledValue(String recipe, String field, long unit, long bulk,
                                           List<String> errors) {
        long expected = Math.multiplyExact(unit, BULK_SIZE);
        if (bulk != expected) {
            errors.add(recipe + " " + field + " must scale 64x: expected " + expected + ", found " + bulk);
        }
    }

    private static Map<String, Long> itemInputs(JsonObject json) {
        Map<String, Long> result = new HashMap<>();
        for (JsonElement element : array(json, "item_inputs")) {
            JsonObject input = element.getAsJsonObject();
            JsonObject ingredient = input.getAsJsonObject("ingredient");
            String id = ingredient.has("item") ? ingredient.get("item").getAsString()
                    : "#" + ingredient.get("tag").getAsString();
            result.merge(id, optionalLong(input, "amount", 1), Long::sum);
        }
        return result;
    }

    private static Map<String, Long> fluidInputs(JsonObject json) {
        Map<String, Long> result = new HashMap<>();
        for (JsonElement element : array(json, "fluid_inputs")) {
            JsonObject input = element.getAsJsonObject();
            JsonObject ingredient = input.has("ingredient") ? input.getAsJsonObject("ingredient")
                    : input.getAsJsonObject("fluid");
            String id = ingredient.has("fluid") ? ingredient.get("fluid").getAsString()
                    : ingredient.has("id") ? ingredient.get("id").getAsString()
                    : "#" + ingredient.get("tag").getAsString();
            result.merge(id, optionalLong(input, "amount", 1), Long::sum);
        }
        return result;
    }

    private static List<Output> outputs(JsonObject json) {
        List<Output> result = new ArrayList<>();
        if (json.has("item_output")) {
            JsonObject output = json.getAsJsonObject("item_output");
            result.add(new Output(output.get("id").getAsString(), optionalLong(output, "count", 1)));
        }
        for (JsonElement element : array(json, "item_outputs")) {
            JsonObject output = element.getAsJsonObject();
            if (output.has("id")) {
                result.add(new Output(output.get("id").getAsString(),
                        output.has("count") ? output.get("count").getAsLong() : optionalLong(output, "#", 1)));
            }
        }
        return result;
    }

    private static JsonArray array(JsonObject json, String field) {
        return json.has(field) ? json.getAsJsonArray(field) : new JsonArray();
    }

    private static long longValue(JsonObject json, String field) {
        return json.get(field).getAsLong();
    }

    private static long optionalLong(JsonObject json, String field, long fallback) {
        return json.has(field) ? json.get(field).getAsLong() : fallback;
    }

    private record Output(String id, long amount) {
    }

    private record Recipe(Path path, JsonObject json, long outputAmount) {
    }
}

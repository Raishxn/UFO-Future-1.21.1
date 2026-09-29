package com.raishxn.ufo.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StellarSimulationCatalogContractTest {
    private static final Path RECIPES = Path.of("src/generated/resources/data/ufo/recipe/stellar_simulation");

    @Test
    void focusedProgramsReturnWithLargerYieldsAndOmnibusCoversTheirOutputs() throws IOException {
        Set<String> allItems = new HashSet<>();
        Set<String> allFluids = new HashSet<>();
        try (Stream<Path> files = Files.list(RECIPES)) {
            var recipes = files.filter(path -> path.toString().endsWith(".json")).toList();
            assertEquals(14, recipes.size());
            for (Path path : recipes) {
                JsonObject recipe = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                JsonArray items = recipe.getAsJsonArray("item_outputs");
                JsonArray fluids = recipe.getAsJsonArray("fluid_outputs");
                assertTrue(items.size() <= 81, path.toString());
                assertTrue(fluids.size() <= 18, path.toString());
                assertTrue(amount(items) >= 200_000_000L, path.toString());
                assertTrue(amount(fluids) >= 100_000_000L, path.toString());
                assertUniqueAndProcessed(items, path);
                assertUniqueAndProcessed(fluids, path);
                if (!path.getFileName().toString().equals("stellar_omnibus.json")) {
                    for (var output : items) allItems.add(output.getAsJsonObject().get("id").getAsString());
                    for (var output : fluids) allFluids.add(output.getAsJsonObject().get("id").getAsString());
                }
            }
        }
        JsonObject omnibus = JsonParser.parseString(Files.readString(
                RECIPES.resolve("stellar_omnibus.json"))).getAsJsonObject();
        assertEquals(3, omnibus.get("field_tier").getAsInt());
        allItems.remove("minecraft:cobblestone");
        allItems.remove("minecraft:obsidian");
        allItems.addAll(Set.of(
                "ufo:white_dwarf_fragment_ingot", "ufo:neutron_star_fragment_ingot",
                "ufo:pulsar_fragment_ingot", "ufo:obsidian_matrix",
                "ufo:charged_enriched_neutronium_sphere", "ufo:uu_matter_crystal",
                "appflux:harden_insulating_resin", "appflux:charged_redstone",
                "advanced_ae:quantum_infused_dust", "advanced_ae:shattered_singularity"));
        allFluids.addAll(Set.of(
                "ufo:transcending_matter", "ufo:source_bose_einstein_condensate",
                "mekanismgenerators:deuterium", "mekanismgenerators:tritium",
                "mekanismgenerators:fusion_fuel"));
        assertEquals(allItems, ids(omnibus.getAsJsonArray("item_outputs")));
        assertEquals(allFluids, ids(omnibus.getAsJsonArray("fluid_outputs")));
        assertEquals(64, omnibus.getAsJsonArray("item_outputs").size());
        assertEquals(17, omnibus.getAsJsonArray("fluid_outputs").size());
    }

    private static long amount(JsonArray outputs) {
        long total = 0;
        for (var element : outputs) total += element.getAsJsonObject().get("#").getAsLong();
        return total;
    }

    private static Set<String> ids(JsonArray outputs) {
        Set<String> ids = new HashSet<>();
        for (var element : outputs) ids.add(element.getAsJsonObject().get("id").getAsString());
        return ids;
    }

    private static void assertUniqueAndProcessed(JsonArray outputs, Path path) {
        Set<String> ids = new HashSet<>();
        for (var element : outputs) {
            JsonObject output = element.getAsJsonObject();
            String id = output.get("id").getAsString();
            assertFalse(id.equals("minecraft:air") || id.equals("minecraft:ancient_debris")
                    || id.endsWith("_ore"), path + ": " + id);
            assertTrue(output.get("#").getAsLong() > 0, path.toString());
            assertTrue(ids.add(id), "Duplicate output " + id + " in " + path);
        }
    }
}

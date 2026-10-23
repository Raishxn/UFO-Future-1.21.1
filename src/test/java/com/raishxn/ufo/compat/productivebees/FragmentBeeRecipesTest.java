package com.raishxn.ufo.compat.productivebees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FragmentBeeRecipesTest {
    private static final Map<String, String> RECIPES = FragmentBeeRecipes.generate(List.of(
            new FragmentBeeRecipes.Family("white_dwarf_fragment", 0.08, 0.15, 1),
            new FragmentBeeRecipes.Family("neutron_star_fragment", 0.05, 0.10, 1),
            new FragmentBeeRecipes.Family("pulsar_fragment", 0.5, 0.75, 4)));

    private static JsonObject recipe(String path) {
        String json = RECIPES.get(path);
        assertNotNull(json, path);
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void conversionsChainFromNetheriteWithConfiguredChances() {
        var whiteDwarf = recipe("recipe/bee_conversion/ufo/white_dwarf_fragment_bee.json");
        assertEquals("productivebees:netherite", whiteDwarf.get("source").getAsString());
        assertEquals(0.08, whiteDwarf.get("chance").getAsDouble(), 1e-9);
        assertEquals("ufo:white_dwarf_fragment_ingot", whiteDwarf.getAsJsonObject("item").get("item").getAsString());

        var neutron = recipe("recipe/bee_conversion/ufo/neutron_star_fragment_bee.json");
        assertEquals("productivebees:white_dwarf_fragment", neutron.get("source").getAsString());

        var pulsar = recipe("recipe/bee_conversion/ufo/pulsar_fragment_bee.json");
        assertEquals("productivebees:neutron_star_fragment", pulsar.get("source").getAsString());
        assertEquals(0.5, pulsar.get("chance").getAsDouble(), 1e-9);
    }

    @Test
    void centrifugeYieldsConfiguredNuggetsAndWax() {
        var comb = recipe("recipe/centrifuge/ufo/honeycomb_pulsar_fragment.json");
        var outputs = comb.getAsJsonArray("outputs");
        var nugget = outputs.get(0).getAsJsonObject();
        assertEquals("ufo:pulsar_fragment_nugget", nugget.getAsJsonObject("item").get("item").getAsString());
        assertEquals(0.75, nugget.get("chance").getAsDouble(), 1e-9);
        assertEquals(4, nugget.get("max").getAsInt());
        assertEquals("productivebees:wax", outputs.get(1).getAsJsonObject().getAsJsonObject("item").get("item").getAsString());
        assertEquals("productivebees:bee_exists",
                comb.getAsJsonArray("neoforge:conditions").get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    void servesOneConversionAndOneCentrifugePerFamily() {
        assertEquals(6, RECIPES.size());
    }
}

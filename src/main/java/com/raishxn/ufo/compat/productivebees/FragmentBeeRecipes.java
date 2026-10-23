package com.raishxn.ufo.compat.productivebees;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure JSON generation for the stellar fragment bee recipes, kept free of Minecraft types so it
 * can be unit tested. Families are chained in order: the first converts from the Netherite Bee,
 * each later one from the previous family's bee.
 */
public final class FragmentBeeRecipes {
    public static final String NAMESPACE = "productivebees";
    private static final String FIRST_PARENT = "netherite";

    public record Family(String id, double conversionChance, double nuggetChance, int nuggetMax) {
    }

    private FragmentBeeRecipes() {
    }

    /** Recipe JSON keyed by path inside the {@value #NAMESPACE} namespace. */
    public static Map<String, String> generate(List<Family> families) {
        Map<String, String> out = new LinkedHashMap<>();
        String parent = FIRST_PARENT;
        for (Family family : families) {
            out.put("recipe/bee_conversion/ufo/" + family.id() + "_bee.json", conversion(parent, family));
            out.put("recipe/centrifuge/ufo/honeycomb_" + family.id() + ".json", centrifuge(family));
            parent = family.id();
        }
        return out;
    }

    private static String conversion(String parent, Family family) {
        var json = new JsonObject();
        json.addProperty("type", "productivebees:bee_conversion");
        json.addProperty("source", "productivebees:" + parent);
        json.addProperty("result", "productivebees:" + family.id());
        json.addProperty("chance", family.conversionChance());
        json.add("item", item("ufo:" + family.id() + "_ingot"));
        json.add("neoforge:conditions", beeExists(parent, family.id()));
        return json.toString();
    }

    private static String centrifuge(Family family) {
        var json = new JsonObject();
        json.addProperty("type", "productivebees:centrifuge");
        var ingredient = new JsonObject();
        ingredient.addProperty("type", "productivebees:component");
        var components = new JsonObject();
        components.addProperty("productivebees:bee_type", "productivebees:" + family.id());
        ingredient.add("components", components);
        ingredient.addProperty("items", "productivebees:configurable_honeycomb");
        json.add("ingredient", ingredient);

        var outputs = new JsonArray();
        var nugget = new JsonObject();
        nugget.add("item", item("ufo:" + family.id() + "_nugget"));
        nugget.addProperty("min", 1);
        nugget.addProperty("max", Math.max(1, family.nuggetMax()));
        nugget.addProperty("chance", family.nuggetChance());
        outputs.add(nugget);
        var wax = new JsonObject();
        wax.add("item", item("productivebees:wax"));
        outputs.add(wax);
        json.add("outputs", outputs);
        json.add("neoforge:conditions", beeExists(family.id()));
        return json.toString();
    }

    private static JsonObject item(String id) {
        var item = new JsonObject();
        item.addProperty("item", id);
        return item;
    }

    private static JsonArray beeExists(String... bees) {
        var conditions = new JsonArray();
        for (String bee : bees) {
            var condition = new JsonObject();
            condition.addProperty("type", "productivebees:bee_exists");
            condition.addProperty("bee", "productivebees:" + bee);
            conditions.add(condition);
        }
        return conditions;
    }
}

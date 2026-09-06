package com.raishxn.ufo.datagen;

import com.google.gson.JsonObject;
import com.raishxn.ufo.UfoMod;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class CraftingModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    private String tier;
    private boolean coprocessor;

    CraftingModelBuilder(T parent, ExistingFileHelper helper) {
        super(UfoMod.id("crafting_cube"), parent, helper, false);
    }

    CraftingModelBuilder<T> tier(Enum<?> tier, boolean coprocessor) {
        this.tier = tier.name();
        this.coprocessor = coprocessor;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        var result = super.toJson(json);
        result.addProperty("tier", tier);
        result.addProperty("coprocessor", coprocessor);
        return result;
    }
}

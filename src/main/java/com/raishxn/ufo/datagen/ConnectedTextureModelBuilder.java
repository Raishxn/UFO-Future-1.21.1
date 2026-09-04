package com.raishxn.ufo.datagen;

import com.google.gson.JsonObject;
import com.raishxn.ufo.UfoMod;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class ConnectedTextureModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    private ResourceLocation connection = UfoMod.id("same_block");

    ConnectedTextureModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(UfoMod.id("connected_texture"), parent, existingFileHelper, false);
    }

    ConnectedTextureModelBuilder<T> connection(ResourceLocation connection) {
        this.connection = connection;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        JsonObject result = super.toJson(json);
        result.addProperty("connection", connection.toString());
        return result;
    }
}

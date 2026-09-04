package com.raishxn.ufo.client.ctm;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

final class ConnectedTextureLoader implements IGeometryLoader<ConnectedTextureGeometry> {
    @Override
    public ConnectedTextureGeometry read(JsonObject json, JsonDeserializationContext context) {
        ResourceLocation connection = ResourceLocation.parse(GsonHelper.getAsString(
                json, "connection", ConnectionPredicates.SAME_BLOCK_ID.toString()));
        RenderType renderType = parseRenderType(GsonHelper.getAsString(json, "render_type", "minecraft:solid"));
        return new ConnectedTextureGeometry(connection, ChunkRenderTypeSet.of(renderType),
                GsonHelper.getAsBoolean(json, "ambientocclusion", true),
                GsonHelper.getAsBoolean(json, "gui3d", true),
                GsonHelper.getAsBoolean(json, "uses_block_light", true));
    }

    private static RenderType parseRenderType(String name) {
        return switch (name) {
            case "solid", "minecraft:solid" -> RenderType.solid();
            case "cutout", "minecraft:cutout" -> RenderType.cutout();
            case "cutout_mipped", "minecraft:cutout_mipped" -> RenderType.cutoutMipped();
            case "translucent", "minecraft:translucent" -> RenderType.translucent();
            default -> throw new JsonParseException("Unsupported UFO connected-texture render type: " + name);
        };
    }
}

package com.raishxn.ufo.client.ctm;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

final class ConnectedTextureGeometry implements IUnbakedGeometry<ConnectedTextureGeometry> {
    private final ResourceLocation connectionId;
    private final ChunkRenderTypeSet renderTypes;
    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final boolean usesBlockLight;

    ConnectedTextureGeometry(ResourceLocation connectionId, ChunkRenderTypeSet renderTypes,
            boolean ambientOcclusion, boolean gui3d, boolean usesBlockLight) {
        this.connectionId = connectionId;
        this.renderTypes = renderTypes;
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
        this.usesBlockLight = usesBlockLight;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
            ItemOverrides overrides) {
        TextureAtlasSprite base = spriteGetter.apply(context.getMaterial("base"));
        TextureAtlasSprite ctm = spriteGetter.apply(context.getMaterial("ctm"));
        if (ctm.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            ctm = base;
        }
        return new ConnectedTextureBakedModel(base, ctm, ConnectionPredicates.get(connectionId),
                renderTypes, ambientOcclusion, gui3d, usesBlockLight);
    }
}

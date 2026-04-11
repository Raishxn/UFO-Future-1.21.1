package com.raishxn.ufo.client.renderer;

import com.raishxn.ufo.client.model.ApocalypseTypeAModel;
import com.raishxn.ufo.entity.custom.ApocalypseTypeAEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ApocalypseTypeARenderer extends GeoEntityRenderer<ApocalypseTypeAEntity> {
    public ApocalypseTypeARenderer(EntityRendererProvider.Context context) {
        super(context, new ApocalypseTypeAModel());
        this.shadowRadius = 1.2F;
    }

    @Override
    public @Nullable RenderType getRenderType(ApocalypseTypeAEntity animatable, ResourceLocation texture,
                                              @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}

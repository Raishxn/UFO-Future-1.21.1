package com.raishxn.ufo.client.model;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.entity.custom.ApocalypseTypeAEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ApocalypseTypeAModel extends DefaultedEntityGeoModel<ApocalypseTypeAEntity> {
    public ApocalypseTypeAModel() {
        super(UfoMod.id("apocalypse_type_a"), "h_head");
    }

    @Override
    public ResourceLocation getTextureResource(ApocalypseTypeAEntity animatable) {
        return UfoMod.id("textures/entity/apocalypse_type_a.png");
    }
}

package com.raishxn.ufo.client.render;

import com.raishxn.ufo.UfoMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.HashMap;
import java.util.Map;

public class StellarModelRegistry {

    public static final ResourceLocation STAR = UfoMod.id("obj/star");
    public static final ResourceLocation BLUE_STAR = UfoMod.id("obj/blue_star");
    public static final ResourceLocation NEUTRON_STAR = UfoMod.id("obj/neutron_star");
    public static final ResourceLocation SPACE = UfoMod.id("obj/space");
    public static final ResourceLocation CLIMBER = UfoMod.id("obj/climber");
    public static final ResourceLocation OVERWORLD = UfoMod.id("obj/overworld");
    public static final ResourceLocation THE_NETHER = UfoMod.id("obj/the_nether");
    public static final ResourceLocation THE_END = UfoMod.id("obj/the_end");

    private static final Map<String, ResourceLocation> RECIPE_MODEL_MAPPING = new HashMap<>();

    // Cache for baked models - populated after model loading
    private static final Map<ResourceLocation, BakedModel> MODEL_CACHE = new HashMap<>();

    static {
        RECIPE_MODEL_MAPPING.put("Neutron Bombardment", NEUTRON_STAR);
        RECIPE_MODEL_MAPPING.put("Stellar Synthesis", BLUE_STAR);
        RECIPE_MODEL_MAPPING.put("Supernova Harvest", STAR);
        RECIPE_MODEL_MAPPING.put("Diamond Pressure", STAR);
        RECIPE_MODEL_MAPPING.put("Iron Core Fusion", STAR);
        RECIPE_MODEL_MAPPING.put("Red Giant Collapse", STAR);
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(STAR, "standalone"));
        event.register(new ModelResourceLocation(BLUE_STAR, "standalone"));
        event.register(new ModelResourceLocation(NEUTRON_STAR, "standalone"));
        event.register(new ModelResourceLocation(SPACE, "standalone"));
        event.register(new ModelResourceLocation(CLIMBER, "standalone"));
        event.register(new ModelResourceLocation(OVERWORLD, "standalone"));
        event.register(new ModelResourceLocation(THE_NETHER, "standalone"));
        event.register(new ModelResourceLocation(THE_END, "standalone"));
    }

    public static ResourceLocation getModelForSimulation(String simulationName) {
        return RECIPE_MODEL_MAPPING.getOrDefault(simulationName, STAR);
    }

    /**
     * Retrieves the BakedModel for the given ResourceLocation.
     * Uses ModelResourceLocation with "standalone" variant, matching how we register them.
     */
    public static BakedModel getBakedModel(ResourceLocation loc) {
        BakedModel cached = MODEL_CACHE.get(loc);
        if (cached != null) {
            return cached;
        }

        ModelResourceLocation mrl = new ModelResourceLocation(loc, "standalone");
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(mrl);
        BakedModel missingModel = Minecraft.getInstance().getModelManager().getMissingModel();

        if (model != null && model != missingModel) {
            MODEL_CACHE.put(loc, model);
            return model;
        }
        return null;
    }

    /**
     * Clear the model cache - should be called on resource reload
     */
    public static void clearCache() {
        MODEL_CACHE.clear();
    }
}

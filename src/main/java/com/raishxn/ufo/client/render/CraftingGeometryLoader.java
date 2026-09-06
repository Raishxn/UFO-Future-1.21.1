package com.raishxn.ufo.client.render;

import appeng.client.render.crafting.AbstractCraftingUnitModelProvider;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.geometry.*;
import java.util.function.Function;

/** Loads UFO crafting cubes without AE2's namespace-restricted built-in hook. */
@EventBusSubscriber(modid = UfoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CraftingGeometryLoader implements IGeometryLoader<CraftingGeometryLoader.Geometry> {
    @SubscribeEvent
    public static void register(ModelEvent.RegisterGeometryLoaders event) {
        event.register(UfoMod.id("crafting_cube"), new CraftingGeometryLoader());
    }

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) {
        String tier = GsonHelper.getAsString(json, "tier");
        return new Geometry(GsonHelper.getAsBoolean(json, "coprocessor", false)
                ? new ModCoProcessorModelProvider(MegaCoProcessorTier.valueOf(tier))
                : new ModCraftingStorageModelProvider(MegaCraftingStorageTier.valueOf(tier)));
    }

    public record Geometry(AbstractCraftingUnitModelProvider<?> provider) implements IUnbakedGeometry<Geometry> {
        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> sprites, ModelState state, ItemOverrides overrides) {
            return provider.getBakedModel(sprites);
        }
    }
}

package com.raishxn.ufo.compat.productivebees;

import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.UfoMod;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Built-in data pack that serves the Productive Bees conversion and centrifuge recipes of the
 * stellar fragment bees from {@link UFOConfig#FRAGMENT_BEES}. The JSON is generated each time the
 * pack is read, so editing {@code config/ufo/common.toml} and running /reload applies new chances.
 * Every recipe keeps the {@code productivebees:bee_exists} conditions, so nothing loads without
 * Productive Bees. A modpack can still override these recipe ids with a higher data pack or KubeJS.
 */
public final class FragmentBeeRecipePack implements PackResources {
    private static final String NAMESPACE = FragmentBeeRecipes.NAMESPACE;
    private static final PackLocationInfo LOCATION = new PackLocationInfo(
            UfoMod.MOD_ID + ":fragment_bee_recipes",
            Component.literal("UFO fragment bee recipes (config/ufo/common.toml)"),
            PackSource.BUILT_IN, Optional.empty());

    private final PackLocationInfo location;

    private FragmentBeeRecipePack(PackLocationInfo location) {
        this.location = location;
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        Pack pack = Pack.readMetaAndCreate(LOCATION, new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo info) {
                return new FragmentBeeRecipePack(info);
            }

            @Override
            public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                return new FragmentBeeRecipePack(info);
            }
        }, PackType.SERVER_DATA, new PackSelectionConfig(true, Pack.Position.TOP, false));
        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        }
    }

    /** Recipe JSON by resource location, rebuilt from the current config values. */
    static Map<ResourceLocation, String> recipes() {
        var families = new java.util.ArrayList<FragmentBeeRecipes.Family>();
        UFOConfig.FRAGMENT_BEES.forEach((family, settings) -> families.add(new FragmentBeeRecipes.Family(
                family, value(settings.conversionChance()), value(settings.nuggetChance()), value(settings.nuggetMax()))));
        Map<ResourceLocation, String> out = new LinkedHashMap<>();
        FragmentBeeRecipes.generate(families).forEach((path, json) ->
                out.put(ResourceLocation.fromNamespaceAndPath(FragmentBeeRecipes.NAMESPACE, path), json));
        return out;
    }

    private static double value(ModConfigSpec.DoubleValue value) {
        return UFOConfig.SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private static int value(ModConfigSpec.IntValue value) {
        return UFOConfig.SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private static ResourceLocation path(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static IoSupplier<InputStream> stream(String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return () -> new ByteArrayInputStream(bytes);
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.SERVER_DATA) {
            return null;
        }
        String content = recipes().get(location);
        return content == null ? null : stream(content);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.SERVER_DATA || !NAMESPACE.equals(namespace)) {
            return;
        }
        String prefix = path.endsWith("/") ? path : path + "/";
        recipes().forEach((location, content) -> {
            if (location.getPath().startsWith(prefix)) {
                output.accept(location, stream(content));
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? Set.of(NAMESPACE) : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(location.title(),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA), Optional.empty());
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
    }
}

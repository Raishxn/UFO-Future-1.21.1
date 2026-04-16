package com.raishxn.ufo.compat.mekanism;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public final class UfoMekCapabilities {
    private UfoMekCapabilities() {
    }

    public static final CapSet<IChemicalHandler> CHEMICAL = new CapSet<>(rl("chemical_handler"), IChemicalHandler.class);

    public record CapSet<T>(BlockCapability<T, @Nullable Direction> block, ItemCapability<T, Void> item) {
        public CapSet(ResourceLocation name, Class<T> handlerClass) {
            this(BlockCapability.createSided(name, handlerClass), ItemCapability.createVoid(name, handlerClass));
        }
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, path);
    }
}

package com.raishxn.ufo.compat.mekanism;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.storage.MEStorage;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

public class UfoMekanismExternalStorageStrategy implements ExternalStorageStrategy {
    private final BlockCapabilityCache<IChemicalHandler, Direction> cache;

    public UfoMekanismExternalStorageStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.cache = BlockCapabilityCache.create(UfoMekCapabilities.CHEMICAL.block(), level, fromPos, fromSide);
    }

    @Override
    public @Nullable MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        var storage = cache.getCapability();
        return storage == null ? null : new ChemicalHandlerFacade(storage, extractableOnly, injectOrExtractCallback);
    }
}

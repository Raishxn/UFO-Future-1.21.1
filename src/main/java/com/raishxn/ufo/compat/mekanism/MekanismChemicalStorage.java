package com.raishxn.ufo.compat.mekanism;

import com.raishxn.ufocore.api.port.ChemicalPort;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface MekanismChemicalStorage extends ChemicalPort<ResourceLocation> {
    boolean supportsChemicalIO();

    long getChemicalCapacity();

    @Nullable
    ResourceLocation getStoredChemicalId();

    long getStoredChemicalAmount();

    void setStoredChemical(@Nullable ResourceLocation chemicalId, long amount);
}

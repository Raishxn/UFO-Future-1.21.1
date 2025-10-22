package com.raishxn.ufo.core;

import appeng.block.crafting.ICraftingUnitType; // <<-- 1. IMPORT CORRETO
import com.raishxn.ufo.block.ModBlocks;
import net.minecraft.world.item.Item;

// 2. FAÇA O ENUM IMPLEMENTAR A INTERFACE
public enum MegaCoProcessorTier implements ICraftingUnitType {
    COPROCESSOR_50M("50m", "50M", 50_000_000),
    COPROCESSOR_150M("150m", "150M", 150_000_000),
    COPROCESSOR_300M("300m", "300M", 300_000_000),
    COPROCESSOR_750M("750m", "750M", 750_000_000),
    COPROCESSOR_2B("2b", "2B", Integer.MAX_VALUE);

    private final String registryId;
    private final String displayName;
    private final int acceleratorThreads;

    MegaCoProcessorTier(String registryId, String displayName, int acceleratorThreads) {
        this.registryId = registryId;
        this.displayName = displayName;
        this.acceleratorThreads = acceleratorThreads;
    }

    public String getRegistryId() {
        return registryId;
    }

    public String getDisplayName() {
        return displayName;
    }

    // --- MÉTODOS DA INTERFACE ICraftingUnitType ---

    @Override
    public int getAcceleratorThreads() {
        return this.acceleratorThreads;
    }

    @Override
    public long getStorageBytes() {
        // 4. Como é um co-processador, ele não tem armazenamento. Retornamos 0.
        return 0;
    }

    @Override
    public Item getItemFromType() {
        // 5. Este método ajuda o AE2 a saber qual item corresponde a este tier.
        return ModBlocks.CO_PROCESSOR_BLOCKS.get(this).get().asItem();
    }
}
package com.raishxn.ufo.core;

import appeng.block.crafting.ICraftingUnitType;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufocore.api.crafting.CraftingComputeCapacity;
import net.minecraft.world.item.Item;

/** Defines the endgame crafting-storage tiers while preserving their legacy registry IDs. */
public enum MegaCraftingStorageTier implements ICraftingUnitType {
    STORAGE_1B("1b", "4G", 4L * 1024L * 1024L * 1024L),
    STORAGE_50B("50b", "16G", 16L * 1024L * 1024L * 1024L),
    STORAGE_1T("1t", "64G", 64L * 1024L * 1024L * 1024L),
    STORAGE_250T("250t", "256G", 256L * 1024L * 1024L * 1024L),
    STORAGE_1QD("1qd", "1T", 1024L * 1024L * 1024L * 1024L);

    private final String registryId;
    private final String displayName;
    private final long bytes;

    MegaCraftingStorageTier(String registryId, String displayName, long bytes) {
        this.registryId = registryId;
        this.displayName = displayName;
        this.bytes = bytes;
    }

    public String getRegistryId() {
        return registryId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getBytes() {
        return bytes;
    }

    /** Capacity contribution when this block is installed in a Quantum Computation Nexus. */
    public CraftingComputeCapacity quantumComputeContribution() {
        return CraftingComputeCapacity.storage(bytes);
    }

    @Override
    public long getStorageBytes() {
        return this.bytes;
    }

    @Override
    public int getAcceleratorThreads() {
        return 0;
    }

    @Override
    public Item getItemFromType() {
        return ModBlocks.CRAFTING_STORAGE_BLOCKS.get(this).get().asItem();
    }
}

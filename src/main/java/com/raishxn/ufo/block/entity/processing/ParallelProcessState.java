package com.raishxn.ufo.block.entity.processing;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ParallelProcessState {
    private ResourceLocation recipeId;
    private long energyBuffer;
    private long[] itemBuffers = new long[0];
    private long[] fluidBuffers = new long[0];
    private int progress;

    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public boolean isActive() {
        return this.recipeId != null;
    }

    public void clear() {
        this.recipeId = null;
        this.energyBuffer = 0L;
        this.itemBuffers = new long[0];
        this.fluidBuffers = new long[0];
        this.progress = 0;
    }

    public void resizeBuffers(int itemSize, int fluidSize) {
        if (this.itemBuffers.length != itemSize) {
            this.itemBuffers = new long[itemSize];
        }
        if (this.fluidBuffers.length != fluidSize) {
            this.fluidBuffers = new long[fluidSize];
        }
    }

    public void clearBuffers() {
        java.util.Arrays.fill(this.itemBuffers, 0L);
        java.util.Arrays.fill(this.fluidBuffers, 0L);
    }

    public long getEnergyBuffer() {
        return energyBuffer;
    }

    public void setEnergyBuffer(long energyBuffer) {
        this.energyBuffer = energyBuffer;
    }

    public long[] getItemBuffers() {
        return itemBuffers;
    }

    public long[] getFluidBuffers() {
        return fluidBuffers;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (this.recipeId != null) {
            tag.putString("recipeId", this.recipeId.toString());
        }
        tag.putLong("energyBuffer", this.energyBuffer);
        tag.putLongArray("itemBuffers", this.itemBuffers);
        tag.putLongArray("fluidBuffers", this.fluidBuffers);
        tag.putInt("progress", this.progress);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        this.recipeId = tag.contains("recipeId") ? ResourceLocation.parse(tag.getString("recipeId")) : null;
        this.energyBuffer = tag.getLong("energyBuffer");
        this.itemBuffers = tag.getLongArray("itemBuffers");
        this.fluidBuffers = tag.getLongArray("fluidBuffers");
        this.progress = tag.getInt("progress");
    }
}

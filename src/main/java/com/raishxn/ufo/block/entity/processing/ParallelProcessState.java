package com.raishxn.ufo.block.entity.processing;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ParallelProcessState {
    private ResourceLocation recipeId;
    public com.raishxn.ufo.wireless.WirelessBonus wirelessBonus = com.raishxn.ufo.wireless.WirelessBonus.NONE;
    private long energyBuffer;
    private long[] itemBuffers = new long[0];
    private long[] fluidBuffers = new long[0];
    private long[] chemicalBuffers = new long[0];
    private int progress;
    private boolean patternPushed;
    private final ProcessPauseState pauseState = new ProcessPauseState();
    private final TransactionalAmountLedger<AEKey> bufferedInputs = new TransactionalAmountLedger<>();
    private final TransactionalAmountLedger<AEKey> pendingOutputs = new TransactionalAmountLedger<>();
    private final TransactionalAmountLedger<AEKey> pendingByproducts = new TransactionalAmountLedger<>();
    private boolean outputsPrepared;
    private int outputPolicyVersion;

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
        wirelessBonus = com.raishxn.ufo.wireless.WirelessBonus.NONE;
        this.recipeId = null;
        this.energyBuffer = 0L;
        this.itemBuffers = new long[0];
        this.fluidBuffers = new long[0];
        this.chemicalBuffers = new long[0];
        this.progress = 0;
        this.patternPushed = false;
        this.pauseState.clear();
        this.bufferedInputs.clear();
        this.pendingOutputs.clear();
        this.pendingByproducts.clear();
        this.outputsPrepared = false;
        this.outputPolicyVersion = AutocraftingOutputPolicy.LEGACY_UNVERSIONED;
    }

    public void resizeBuffers(int itemSize, int fluidSize, int chemicalSize) {
        if (this.itemBuffers.length != itemSize) {
            this.itemBuffers = java.util.Arrays.copyOf(this.itemBuffers, itemSize);
        }
        if (this.fluidBuffers.length != fluidSize) {
            this.fluidBuffers = java.util.Arrays.copyOf(this.fluidBuffers, fluidSize);
        }
        if (this.chemicalBuffers.length != chemicalSize) {
            this.chemicalBuffers = java.util.Arrays.copyOf(this.chemicalBuffers, chemicalSize);
        }
    }

    public void clearBuffers() {
        java.util.Arrays.fill(this.itemBuffers, 0L);
        java.util.Arrays.fill(this.fluidBuffers, 0L);
        java.util.Arrays.fill(this.chemicalBuffers, 0L);
        this.bufferedInputs.clear();
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

    public long[] getChemicalBuffers() {
        return chemicalBuffers;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isPatternPushed() {
        return patternPushed;
    }

    public void setPatternPushed(boolean patternPushed) {
        this.patternPushed = patternPushed;
    }

    public int getOutputPolicyVersion() {
        return this.outputPolicyVersion;
    }

    public void setOutputPolicyVersion(int outputPolicyVersion) {
        this.outputPolicyVersion = Math.max(AutocraftingOutputPolicy.LEGACY_UNVERSIONED, outputPolicyVersion);
    }

    public boolean isPaused() {
        return this.pauseState.isPaused();
    }

    public void togglePaused() {
        this.pauseState.toggle(isActive());
    }

    public List<GenericStack> getBufferedInputs() {
        return toGenericStacks(this.bufferedInputs);
    }

    public void recordBufferedInput(AEKey key, long amount) {
        this.bufferedInputs.add(key, amount);
    }

    public void consumeBufferedInput(AEKey key, long amount) {
        this.bufferedInputs.consume(key, amount);
    }

    public boolean hasTrackedInputs() {
        return !this.bufferedInputs.isEmpty();
    }

    public List<GenericStack> getPendingOutputs() {
        return toGenericStacks(this.pendingOutputs);
    }

    public void prepareOutputs(List<GenericStack> outputs) {
        prepareOutputs(outputs, List.of());
    }

    public void prepareOutputs(List<GenericStack> outputs, List<GenericStack> byproducts) {
        if (this.outputsPrepared) {
            return;
        }
        for (GenericStack output : outputs) {
            if (output != null) {
                this.pendingOutputs.add(output.what(), output.amount());
            }
        }
        for (GenericStack byproduct : byproducts) {
            if (byproduct != null) {
                this.pendingByproducts.add(byproduct.what(), byproduct.amount());
            }
        }
        this.outputsPrepared = true;
    }

    public void consumePendingOutput(AEKey key, long amount) {
        this.pendingOutputs.consume(key, amount);
    }

    public List<GenericStack> getPendingByproducts() {
        return toGenericStacks(this.pendingByproducts);
    }

    public void consumePendingByproduct(AEKey key, long amount) {
        this.pendingByproducts.consume(key, amount);
    }

    public boolean hasPendingPromisedOutputs() {
        return !this.pendingOutputs.isEmpty();
    }

    public boolean hasPendingByproducts() {
        return !this.pendingByproducts.isEmpty();
    }

    public boolean hasPendingOutputs() {
        return hasPendingPromisedOutputs() || hasPendingByproducts();
    }

    public boolean isOutputsPrepared() {
        return this.outputsPrepared;
    }

    public boolean hasBufferedWork() {
        if (this.energyBuffer > 0L || this.progress > 0 || this.outputsPrepared
                || !this.bufferedInputs.isEmpty() || !this.pendingOutputs.isEmpty()
                || !this.pendingByproducts.isEmpty()) {
            return true;
        }

        for (long amount : this.itemBuffers) {
            if (amount > 0L) {
                return true;
            }
        }

        for (long amount : this.fluidBuffers) {
            if (amount > 0L) {
                return true;
            }
        }

        for (long amount : this.chemicalBuffers) {
            if (amount > 0L) {
                return true;
            }
        }

        return false;
    }

    public boolean hasLegacyMaterialBuffers() {
        return containsPositive(this.itemBuffers)
                || containsPositive(this.fluidBuffers)
                || containsPositive(this.chemicalBuffers);
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("wirelessBonus", com.raishxn.ufo.wireless.WirelessBonusNbt.save(wirelessBonus));
        if (this.recipeId != null) {
            tag.putString("recipeId", this.recipeId.toString());
        }
        tag.putLong("energyBuffer", this.energyBuffer);
        tag.putLongArray("itemBuffers", this.itemBuffers);
        tag.putLongArray("fluidBuffers", this.fluidBuffers);
        tag.putLongArray("chemicalBuffers", this.chemicalBuffers);
        tag.putInt("progress", this.progress);
        tag.putBoolean("patternPushed", this.patternPushed);
        tag.putBoolean("paused", this.pauseState.isPaused());
        tag.put("bufferedInputs", saveStacks(getBufferedInputs(), registries));
        tag.put("pendingOutputs", saveStacks(getPendingOutputs(), registries));
        tag.put("pendingByproducts", saveStacks(getPendingByproducts(), registries));
        tag.putBoolean("outputsPrepared", this.outputsPrepared);
        tag.putInt("outputPolicyVersion", this.outputPolicyVersion);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        wirelessBonus = com.raishxn.ufo.wireless.WirelessBonusNbt.load(tag.getCompound("wirelessBonus"));
        this.recipeId = tag.contains("recipeId") ? ResourceLocation.parse(tag.getString("recipeId")) : null;
        this.energyBuffer = tag.getLong("energyBuffer");
        this.itemBuffers = tag.getLongArray("itemBuffers");
        this.fluidBuffers = tag.getLongArray("fluidBuffers");
        this.chemicalBuffers = tag.getLongArray("chemicalBuffers");
        this.progress = tag.getInt("progress");
        this.patternPushed = tag.getBoolean("patternPushed");
        this.pauseState.setPaused(tag.getBoolean("paused"));
        this.bufferedInputs.clear();
        this.pendingOutputs.clear();
        this.pendingByproducts.clear();
        loadStacks(tag, "bufferedInputs", this.bufferedInputs, registries);
        loadStacks(tag, "pendingOutputs", this.pendingOutputs, registries);
        loadStacks(tag, "pendingByproducts", this.pendingByproducts, registries);
        this.outputsPrepared = tag.getBoolean("outputsPrepared");
        this.outputPolicyVersion = tag.contains("outputPolicyVersion", Tag.TAG_INT)
                ? Math.max(AutocraftingOutputPolicy.LEGACY_UNVERSIONED, tag.getInt("outputPolicyVersion"))
                : AutocraftingOutputPolicy.LEGACY_UNVERSIONED;
    }

    private static ListTag saveStacks(List<GenericStack> stacks, HolderLookup.Provider registries) {
        ListTag tags = new ListTag();
        for (GenericStack stack : stacks) {
            if (stack.amount() > 0L) {
                tags.add(GenericStack.writeTag(registries, stack));
            }
        }
        return tags;
    }

    private static void loadStacks(CompoundTag parent, String name, TransactionalAmountLedger<AEKey> target, HolderLookup.Provider registries) {
        if (!parent.contains(name, Tag.TAG_LIST)) {
            return;
        }
        ListTag tags = parent.getList(name, Tag.TAG_COMPOUND);
        for (int i = 0; i < tags.size(); i++) {
            GenericStack stack = GenericStack.readTag(registries, tags.getCompound(i));
            if (stack != null && stack.amount() > 0L) {
                target.add(stack.what(), stack.amount());
            }
        }
    }

    private static List<GenericStack> toGenericStacks(TransactionalAmountLedger<AEKey> ledger) {
        return ledger.snapshot().stream()
                .map(entry -> new GenericStack(entry.key(), entry.amount()))
                .toList();
    }

    private static boolean containsPositive(long[] amounts) {
        for (long amount : amounts) {
            if (amount > 0L) {
                return true;
            }
        }
        return false;
    }
}

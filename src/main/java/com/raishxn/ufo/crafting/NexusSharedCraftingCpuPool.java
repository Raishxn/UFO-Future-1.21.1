package com.raishxn.ufo.crafting;

import appeng.api.config.Actionable;
import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.hooks.ticking.TickHandler;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.raishxn.ufo.api.ae.NexusVirtualCpuHost;
import com.raishxn.ufo.api.ae.NexusVirtualCraftingClusterBridge;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPool;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** Shared-capacity Nexus pool: idle capacity is one CPU; every active job gets one temporary CPU. */
public final class NexusSharedCraftingCpuPool implements SharedCraftingCpuPool {
    /**
     * AE2 executes one Java call per dispatched pattern. UFO crafting units can advertise billions of lanes, but
     * passing that raw number to AE2 would make a single server tick attempt billions of calls. Time-slice the
     * physical execution while retaining the full lane count for capacity, selection and sharing.
     */
    private static final int MAX_PATTERN_DISPATCH_SLOTS = 2_048;

    private static final String TAG_CPUS = "cpus";
    private static final String TAG_ID = "id";
    private static final String TAG_RESERVED = "reserved";
    private static final String TAG_STATE = "state";
    private static final String TAG_TOTAL = "total";
    private static final String TAG_COPROCESSORS = "coprocessors";
    private static final String TAG_INFINITE = "infinite";

    private final NexusVirtualCpuHost host;
    private final Map<UUID, Entry> active = new LinkedHashMap<>();
    private long totalStorage;
    private long remainingStorage;
    private int sharedCoProcessors;
    private boolean infiniteMode;
    private boolean listChanged;

    public NexusSharedCraftingCpuPool(NexusVirtualCpuHost host) {
        this.host = host;
    }

    public void reconfigure(long storage, int coProcessors, boolean infinite) {
        storage = Math.max(0L, storage);
        coProcessors = Math.max(0, coProcessors);
        long advertisedStorage = infinite ? Long.MAX_VALUE : storage;
        int advertisedCoProcessors = infinite ? Integer.MAX_VALUE - 1 : coProcessors;
        if (advertisedStorage == totalStorage && advertisedCoProcessors == sharedCoProcessors
                && infinite == infiniteMode) return;
        totalStorage = advertisedStorage;
        sharedCoProcessors = advertisedCoProcessors;
        infiniteMode = infinite;
        // Running jobs own their already-reserved bytes and remain valid. New capacity changes
        // only the unreserved pool and the execution lanes assigned on the next tick.
        recalculateRemaining();
        listChanged = true;
        host.ufo$markCpuDirty();
    }

    @Override
    public Collection<? extends ICraftingCPU> getActiveCpus() {
        return active.values().stream().map(Entry::cpu).toList();
    }

    @Override
    public long tickCraftingLogic(IEnergyService energyService, ICraftingService craftingService) {
        if (!(craftingService instanceof CraftingService concrete)) return Long.MIN_VALUE;
        List<Entry> scheduled = new ArrayList<>(active.values());
        if (scheduled.isEmpty()) return Long.MIN_VALUE;
        int count = scheduled.size();
        int advertisedSlots = sharedCoProcessors >= Integer.MAX_VALUE - 1
                ? Integer.MAX_VALUE : sharedCoProcessors + 1;
        int dispatchSlots = Math.min(advertisedSlots, MAX_PATTERN_DISPATCH_SLOTS);
        dispatchSlots = throttleToNetworkPower(energyService, dispatchSlots);
        int scheduledCount = Math.min(count, dispatchSlots);
        long latest = Long.MIN_VALUE;
        for (int index = 0; index < scheduledCount; index++) {
            Entry entry = scheduled.get(index);
            int allocation = dispatchSlots / scheduledCount + (index < dispatchSlots % scheduledCount ? 1 : 0);
            ((NexusVirtualCraftingClusterBridge) (Object) entry.cpu())
                    .ufo$setVirtualCoProcessors(Math.max(0, allocation - 1));
            entry.cpu().craftingLogic.tickCraftingLogic(energyService, concrete);
            latest = Math.max(latest, entry.cpu().craftingLogic.getLastModifiedOnTick());
        }
        rotateOrder();
        removeDrained();
        return latest;
    }

    /**
     * A persisted job resumes at full lane count on every world load and can out-draw the
     * network's generation, brown-out every node and leave the whole grid flickering.
     * Scale dispatch lanes with the network's stored power: healthy buffers run full
     * speed, draining buffers sip until generation catches back up.
     */
    private static int throttleToNetworkPower(IEnergyService energyService, int dispatchSlots) {
        double maxStored = energyService.getMaxStoredPower();
        if (maxStored <= 0.0D) return dispatchSlots;
        double stored = energyService.getStoredPower();
        double ratio = stored / maxStored;
        if (ratio < 0.10D) return 1;
        if (ratio < 0.25D) return Math.max(1, dispatchSlots / 8);
        if (ratio < 0.50D) return Math.max(1, dispatchSlots / 2);
        return dispatchSlots;
    }

    @Override
    public void addWaitingKeys(Set<AEKey> waitingKeys) {
        for (Entry entry : active.values()) entry.cpu().craftingLogic.getAllWaitingFor(waitingKeys);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode) {
        long inserted = 0L;
        for (Entry entry : active.values()) {
            if (inserted >= amount) break;
            inserted += entry.cpu().craftingLogic.insert(what, amount - inserted, mode);
        }
        return inserted;
    }

    @Override
    public long getRequestedAmount(AEKey what) {
        long result = 0L;
        for (Entry entry : active.values()) {
            long addition = entry.cpu().craftingLogic.getWaitingFor(what);
            result = result >= Long.MAX_VALUE - addition ? Long.MAX_VALUE : result + addition;
        }
        return result;
    }

    @Override
    public ICraftingSubmitResult submitJob(IGrid grid, ICraftingPlan plan, IActionSource source,
                                            @Nullable ICraftingRequester requester) {
        if (!isActive()) return CraftingSubmitResult.CPU_OFFLINE;
        long reserved = Math.max(0L, plan.bytes());
        if (!infiniteMode && reserved > remainingStorage) return CraftingSubmitResult.CPU_TOO_SMALL;

        UUID id = UUID.randomUUID();
        CraftingCPUCluster cpu = new CraftingCPUCluster(BlockPos.ZERO, BlockPos.ZERO);
        ((NexusVirtualCraftingClusterBridge) (Object) cpu)
                .ufo$configureVirtualCpu(host, reserved, sharedCoProcessors);
        active.put(id, new Entry(id, reserved, cpu));
        if (!infiniteMode) remainingStorage -= reserved;
        ICraftingSubmitResult result = cpu.submitJob(grid, plan, source, requester);
        if (!result.successful()) {
            active.remove(id);
            recalculateRemaining();
        } else {
            listChanged = true;
            host.ufo$markCpuDirty();
        }
        return result;
    }

    @Override public boolean isBusy() { return false; }
    @Override public @Nullable CraftingJobStatus getJobStatus() { return null; }
    @Override public void cancelJob() { }
    @Override public long getAvailableStorage() { return remainingStorage; }
    @Override public int getCoProcessors() { return sharedCoProcessors; }
    @Override public int getCpuPriority() { return host.ufo$getCpuPriority(); }
    @Override public Component getName() { return host.ufo$getCpuName(); }
    @Override public CpuSelectionMode getSelectionMode() { return host.ufo$getCpuSelectionMode(); }
    @Override public boolean isActive() { return host.ufo$isCpuActive() && totalStorage > 0L; }

    @Override
    public boolean consumeCpuListChanged() {
        boolean changed = listChanged;
        listChanged = false;
        return changed;
    }

    @Override
    public void restoreCraftingLinks(Consumer<ICraftingLink> consumer) {
        for (Entry entry : active.values()) {
            ICraftingLink link = entry.cpu().craftingLogic.getLastLink();
            if (link instanceof CraftingLink) consumer.accept(link);
        }
    }

    public int getActiveJobCount() {
        return active.size();
    }

    public boolean hasPersistentState() {
        return !active.isEmpty();
    }

    /** Cancels every temporary job while the Grid Link is still attached, returning held ingredients to ME storage. */
    public void cancelAllJobs() {
        if (active.isEmpty()) return;
        for (Entry entry : active.values()) entry.cpu().cancelJob();
        removeDrained();
        host.ufo$markCpuDirty();
    }

    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong(TAG_TOTAL, totalStorage);
        tag.putInt(TAG_COPROCESSORS, sharedCoProcessors);
        tag.putBoolean(TAG_INFINITE, infiniteMode);
        ListTag list = new ListTag();
        for (Entry entry : active.values()) {
            CompoundTag state = new CompoundTag();
            entry.cpu().writeToNBT(state, registries);
            CompoundTag encoded = new CompoundTag();
            encoded.putUUID(TAG_ID, entry.id());
            encoded.putLong(TAG_RESERVED, entry.reserved());
            encoded.put(TAG_STATE, state);
            list.add(encoded);
        }
        if (!list.isEmpty()) tag.put(TAG_CPUS, list);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        totalStorage = Math.max(0L, tag.getLong(TAG_TOTAL));
        sharedCoProcessors = Math.max(0, tag.getInt(TAG_COPROCESSORS));
        infiniteMode = tag.getBoolean(TAG_INFINITE);
        active.clear();
        for (Tag raw : tag.getList(TAG_CPUS, Tag.TAG_COMPOUND)) {
            CompoundTag encoded = (CompoundTag) raw;
            if (!encoded.hasUUID(TAG_ID) || !encoded.contains(TAG_STATE, Tag.TAG_COMPOUND)) continue;
            UUID id = encoded.getUUID(TAG_ID);
            long reserved = Math.max(0L, encoded.getLong(TAG_RESERVED));
            CraftingCPUCluster cpu = new CraftingCPUCluster(BlockPos.ZERO, BlockPos.ZERO);
            ((NexusVirtualCraftingClusterBridge) (Object) cpu)
                    .ufo$configureVirtualCpu(host, reserved, sharedCoProcessors);
            cpu.readFromNBT(encoded.getCompound(TAG_STATE), registries);
            active.put(id, new Entry(id, reserved, cpu));
        }
        recalculateRemaining();
        listChanged = !active.isEmpty();
    }

    private void removeDrained() {
        boolean removed = active.values().removeIf(entry -> !entry.cpu().craftingLogic.hasJob()
                && entry.cpu().craftingLogic.getInventory().list.isEmpty());
        if (removed) {
            recalculateRemaining();
            listChanged = true;
            host.ufo$markCpuDirty();
        }
    }

    private void rotateOrder() {
        if (active.size() < 2) return;
        var iterator = active.entrySet().iterator();
        var first = iterator.next();
        iterator.remove();
        active.put(first.getKey(), first.getValue());
    }

    private void recalculateRemaining() {
        if (infiniteMode) {
            remainingStorage = Long.MAX_VALUE;
            return;
        }
        long used = 0L;
        for (Entry entry : active.values()) {
            used = used >= Long.MAX_VALUE - entry.reserved() ? Long.MAX_VALUE : used + entry.reserved();
        }
        remainingStorage = Math.max(0L, totalStorage - used);
    }

    private record Entry(UUID id, long reserved, CraftingCPUCluster cpu) {
    }
}

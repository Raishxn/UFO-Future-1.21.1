package com.raishxn.ufo.mixin;

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = CraftingCPUCluster.class, priority = 3000, remap = false)
public abstract class MixinCraftingCPUCluster {
    @Shadow private MachineSource machineSrc;
    @Shadow private List<CraftingBlockEntity> blockEntities;
    @Shadow private List<CraftingMonitorBlockEntity> status;
    @Shadow private long storage;
    @Shadow private int accelerator;

    /**
     * @author Codex
     * @reason Remove AE2's hard 16-threads-per-block restriction while preserving
     * the real co-processor values from UFO tiers and saturating the total at
     * Integer.MAX_VALUE to avoid overflow when CPUs become extremely large.
     */
    @Overwrite
    void addBlockEntity(CraftingBlockEntity te) {
        if (this.machineSrc == null || te.isCoreBlock()) {
            this.machineSrc = new MachineSource(te);
        }

        te.setCoreBlock(false);
        te.saveChanges();
        this.blockEntities.add(0, te);

        if (te instanceof CraftingMonitorBlockEntity monitor) {
            this.status.add(monitor);
        }

        if (te.getStorageBytes() > 0) {
            this.storage += te.getStorageBytes();
        }

        int threads = te.getAcceleratorThreads();
        if (threads > 0) {
            long combined = (long) this.accelerator + threads;
            this.accelerator = (int) Math.min(Integer.MAX_VALUE, combined);
        }
    }
}

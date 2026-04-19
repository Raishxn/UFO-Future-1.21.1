package com.raishxn.ufo.mixin;

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = CraftingCPUCluster.class, priority = 3000, remap = false)
public abstract class MixinCraftingCPUCluster {
    @Shadow private List<CraftingBlockEntity> blockEntities;
    @Shadow private int accelerator;

    /**
     * Preserve AE2's original method shape so other add-ons can still inject
     * into addBlockEntity, but bypass the hard per-block 16 thread exception
     * for UFO's larger crafting units.
     */
    @Inject(
            method = "addBlockEntity",
            at = @At(value = "NEW", target = "java/lang/IllegalArgumentException"),
            cancellable = true
    )
    private void ufo$allowLargeCoProcessors(CraftingBlockEntity te, CallbackInfo ci) {
        if (te.getAcceleratorThreads() > 16) {
            this.accelerator = ufo$getSaturatedThreadSum();
            ci.cancel();
        }
    }

    @Inject(method = "addBlockEntity", at = @At("TAIL"))
    private void ufo$clampThreadTotal(CraftingBlockEntity te, CallbackInfo ci) {
        this.accelerator = ufo$getSaturatedThreadSum();
    }

    private int ufo$getSaturatedThreadSum() {
        long total = 0L;
        for (CraftingBlockEntity blockEntity : this.blockEntities) {
            int threads = blockEntity.getAcceleratorThreads();
            if (threads > 0) {
                total += threads;
                if (total >= Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        return (int) total;
    }
}

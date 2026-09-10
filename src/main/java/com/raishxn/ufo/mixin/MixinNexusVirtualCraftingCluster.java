package com.raishxn.ufo.mixin;

import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.raishxn.ufo.api.ae.NexusVirtualCpuHost;
import com.raishxn.ufo.api.ae.NexusVirtualCraftingClusterBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Supplies a real AE2 CraftingCpuLogic with a host that is not a physical CPU cube. */
@Mixin(value = CraftingCPUCluster.class, priority = 3000, remap = false)
public abstract class MixinNexusVirtualCraftingCluster implements NexusVirtualCraftingClusterBridge {
    @Shadow private long storage;
    @Shadow private int accelerator;
    @Unique @Nullable private NexusVirtualCpuHost ufo$virtualHost;

    @Override
    public void ufo$configureVirtualCpu(NexusVirtualCpuHost host, long storage, int coProcessors) {
        this.ufo$virtualHost = host;
        this.storage = Math.max(0L, storage);
        this.accelerator = Math.max(0, coProcessors);
    }

    @Override
    public void ufo$setVirtualCoProcessors(int coProcessors) {
        if (ufo$virtualHost != null) this.accelerator = Math.max(0, coProcessors);
    }

    @Inject(method = "markDirty", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualMarkDirty(CallbackInfo ci) {
        if (ufo$virtualHost != null) {
            ufo$virtualHost.ufo$markCpuDirty();
            ci.cancel();
        }
    }

    @Inject(method = "updateOutput", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualUpdateOutput(@Nullable GenericStack output, CallbackInfo ci) {
        if (ufo$virtualHost != null) {
            ufo$virtualHost.ufo$markCpuDirty();
            ci.cancel();
        }
    }

    @Inject(method = "getSrc", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualSource(CallbackInfoReturnable<IActionSource> cir) {
        if (ufo$virtualHost != null) cir.setReturnValue(ufo$virtualHost.ufo$getCpuActionSource());
    }

    @Inject(method = "getGrid", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualGrid(CallbackInfoReturnable<IGrid> cir) {
        if (ufo$virtualHost != null) cir.setReturnValue(ufo$virtualHost.ufo$getCpuGrid());
    }

    @Inject(method = "getNode", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualNode(CallbackInfoReturnable<IGridNode> cir) {
        if (ufo$virtualHost != null) cir.setReturnValue(ufo$virtualHost.ufo$getCpuNode());
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualActive(CallbackInfoReturnable<Boolean> cir) {
        if (ufo$virtualHost != null) cir.setReturnValue(ufo$virtualHost.ufo$isCpuActive());
    }

    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualLevel(CallbackInfoReturnable<Level> cir) {
        if (ufo$virtualHost != null) cir.setReturnValue(ufo$virtualHost.ufo$getCpuLevel());
    }

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualName(CallbackInfoReturnable<Component> cir) {
        if (ufo$virtualHost != null) cir.setReturnValue(ufo$virtualHost.ufo$getCpuName());
    }

    @Inject(method = "getSelectionMode", at = @At("HEAD"), cancellable = true)
    private void ufo$virtualSelection(CallbackInfoReturnable<CpuSelectionMode> cir) {
        if (ufo$virtualHost != null) cir.setReturnValue(ufo$virtualHost.ufo$getCpuSelectionMode());
    }
}

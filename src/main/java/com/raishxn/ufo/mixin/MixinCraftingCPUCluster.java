package com.raishxn.ufo.mixin;

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingCPUCluster.class, priority = 3000, remap = false)
public abstract class MixinCraftingCPUCluster {
    private static final int UFO_MAX_SAFE_COPROCESSORS = Integer.MAX_VALUE - 1;

    @Shadow private int accelerator;

    /**
     * The per-unit thread cap check must never reject UFO's crafting units
     * (50M-2B threads). Rather than rewriting the cap constant itself — a
     * {@code @ModifyConstant} monopolizes the constant and crashes any other
     * add-on that also rewrites it (e.g. BiggerAE2, whose require=1 injector
     * then fails) — we wrap only the {@code getAcceleratorThreads()} call that
     * feeds the {@code threads <= cap} comparison and return 0, so the check
     * passes against whatever cap value any add-on installs. The sum of
     * threads (a separate call site) keeps using the real value.
     */
    @ModifyExpressionValue(
            method = "addBlockEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/blockentity/crafting/CraftingBlockEntity;getAcceleratorThreads()I",
                    ordinal = 1))
    private int ufo$passThreadCapCheck(int original) {
        return 0;
    }

    @Inject(method = "addBlockEntity", at = @At("TAIL"))
    private void ufo$clampThreadTotal(CraftingBlockEntity te, CallbackInfo ci) {
        if (this.accelerator < 0 || this.accelerator == Integer.MAX_VALUE) {
            this.accelerator = UFO_MAX_SAFE_COPROCESSORS;
        }
    }
}

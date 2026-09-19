package com.raishxn.ufo.mixin;

import com.raishxn.ufo.compat.mmr.MmrShutdownCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stops an already-running MMR timer iteration from scheduling work after server teardown begins. */
@Pseudo
@Mixin(targets = "es.degrassi.mmreborn.api.controller.MMRWorldSavedData", remap = false)
public abstract class MixinMmrAsyncShutdownGuard {
    @Inject(method = "searchingTask", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void ufo$stopAsyncChecksDuringShutdown(CallbackInfo ci) {
        if (!MmrShutdownCompat.acceptingAsyncChecks()) ci.cancel();
    }
}

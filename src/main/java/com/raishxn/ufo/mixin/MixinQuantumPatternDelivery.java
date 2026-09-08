package com.raishxn.ufo.mixin;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderTarget;
import com.raishxn.ufo.block.entity.QuantumPatternProviderLogic;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep AE2's persisted send queue, but resolve its pinned remote destination. */
@Mixin(PatternProviderLogic.class)
public abstract class MixinQuantumPatternDelivery {
    @Inject(method = "findAdapter", at = @At("HEAD"), cancellable = true)
    private void ufo$remoteAdapter(Direction side, CallbackInfoReturnable<PatternProviderTarget> cir) {
        if ((Object) this instanceof QuantumPatternProviderLogic logic && logic.hasRemoteDelivery()) {
            cir.setReturnValue(logic.remoteDeliveryAdapter());
        }
    }
}

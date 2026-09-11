package com.raishxn.ufo.mixin;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.raishxn.ufo.api.ae.PatternMatrixAutoUpload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sends a successfully encoded pattern to the highest-priority Matrix on the same ME grid. */
@Mixin(PatternEncodingTermMenu.class)
public abstract class MixinPatternEncodingAutoUpload {
    @Inject(method = "encode", at = @At("RETURN"))
    private void ufo$uploadEncodedPattern(CallbackInfo ci) {
        PatternEncodingTermMenu menu = (PatternEncodingTermMenu) (Object) this;
        if (menu.getPlayer().level().isClientSide()) return;

        // AE2 Lightning performs its own target-selection flow in its terminal subclass.
        if (menu.getClass().getName().startsWith("com.moakiee.ae2lt.")) return;
        PatternMatrixAutoUpload.upload(
                menu, ((AccessorPatternEncodingTermMenu) menu).ufo$getEncodingLogic());
    }
}

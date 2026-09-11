package com.raishxn.ufo.mixin;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.raishxn.ufo.api.ae.PatternMatrixAutoUpload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Runs after AE2 Lightning has completed its derived-pattern conversion. */
@Pseudo
@Mixin(targets = "com.moakiee.ae2lt.menu.TianshuPatternEncodingTermMenu", remap = false)
public abstract class MixinTianshuPatternEncodingAutoUpload {
    @Inject(method = "encodeServerWithOptions", at = @At("RETURN"), require = 0, remap = false)
    private void ufo$uploadTianshuPattern(Boolean interceptDuplicateUpload, CallbackInfo ci) {
        PatternEncodingTermMenu menu = (PatternEncodingTermMenu) (Object) this;
        PatternMatrixAutoUpload.upload(
                menu, ((AccessorPatternEncodingTermMenu) menu).ufo$getEncodingLogic());
    }
}

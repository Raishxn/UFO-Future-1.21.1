package com.raishxn.ufo.mixin;

import com.raishxn.ufo.compat.emi.EmiStructurePreviewWidget;
import dev.emi.emi.screen.WidgetGroup;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** EMI 1.1 doesn't expose drag/scroll callbacks on Widget. */
@Pseudo
@Mixin(targets = "dev.emi.emi.screen.RecipeScreen", remap = false)
public abstract class MixinEmiStructureInput {
    @Shadow private List<WidgetGroup> currentPage;

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void ufo$drag(double x, double y, int button, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        for (var group : currentPage) for (var widget : group.widgets) {
            if (widget instanceof EmiStructurePreviewWidget preview
                    && preview.drag(x - group.x(), y - group.y(), button, dx, dy)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void ufo$scroll(double x, double y, double sx, double sy, CallbackInfoReturnable<Boolean> cir) {
        for (var group : currentPage) for (var widget : group.widgets) {
            if (widget instanceof EmiStructurePreviewWidget preview
                    && preview.scroll(x - group.x(), y - group.y(), sx, sy)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}

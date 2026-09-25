package com.raishxn.ufo.mixin;

import com.raishxn.ufo.compat.jei.StellarSimulationRecipeCategory;
import dev.emi.emi.jemi.JemiRecipe;
import dev.emi.emi.screen.RecipeDisplay;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep the inner EMI recipe frame as tall as the Stellar Nexus atlas. */
@Pseudo
@Mixin(targets = "dev.emi.emi.screen.RecipeTab", remap = false)
public abstract class MixinEmiStellarRecipeTab {
    @Shadow @Final private List<RecipeDisplay> displays;

    @Inject(method = "getVerticalRecipeSpace", at = @At("RETURN"), cancellable = true)
    private void ufo$useFullStellarHeight(int backgroundHeight, CallbackInfoReturnable<Integer> result) {
        if (result.getReturnValue() < backgroundHeight - 46 && displays.stream().anyMatch(
                display -> display.recipe instanceof JemiRecipe<?> jemi
                        && jemi.category instanceof StellarSimulationRecipeCategory)) {
            result.setReturnValue(backgroundHeight - 46);
        }
    }
}

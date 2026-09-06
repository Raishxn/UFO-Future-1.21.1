package com.raishxn.ufo.mixin;

import com.raishxn.ufo.client.preview.StructurePreviewModel;
import com.raishxn.ufo.compat.emi.EmiStructurePreviewWidget;
import com.raishxn.ufo.compat.jei.MultiblockInfoCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep JEI recipe discovery, replacing only UFO's structure page adapter. */
@Pseudo
@Mixin(targets = "dev.emi.emi.jemi.JemiRecipe", remap = false)
public abstract class MixinJemiStructureRecipe {
    @Shadow public IRecipeCategory<?> category;
    @Shadow public Object recipe;

    @Inject(method = "addWidgets", at = @At("HEAD"), cancellable = true)
    private void ufo$interactiveStructure(WidgetHolder widgets, CallbackInfo ci) {
        if (category instanceof MultiblockInfoCategory && recipe instanceof StructurePreviewModel model) {
            widgets.add(new EmiStructurePreviewWidget(model, MultiblockInfoCategory.WIDTH, MultiblockInfoCategory.HEIGHT));
            ci.cancel();
        }
    }
}

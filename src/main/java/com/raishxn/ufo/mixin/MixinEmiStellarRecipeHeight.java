package com.raishxn.ufo.mixin;

import com.raishxn.ufo.util.UfoText;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.raishxn.ufo.compat.jei.StellarSimulationRecipeCategory;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.jemi.JemiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.screen.RecipeDisplay;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Give the 256px Stellar Nexus JEI atlas room below EMI's 37px recipe header. */
@Pseudo
@Mixin(targets = "dev.emi.emi.screen.RecipeScreen", remap = false)
public abstract class MixinEmiStellarRecipeHeight {
    private static final int STELLAR_RECIPE_SCREEN_HEIGHT = 256 + 46;

    @Shadow private Map<EmiRecipeCategory, List<EmiRecipe>> recipes;
    @Shadow private List<RecipeTab> tabs;
    @Shadow private int tab;
    @Shadow private int page;
    @Shadow private int x;
    @Shadow private int y;
    @Shadow private int backgroundWidth;

    @ModifyExpressionValue(method = "init", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
    private int ufo$fitStellarTitle(int original) {
        return hasStellarRecipe() ? Math.max(original, 272) : original;
    }

    @ModifyExpressionValue(method = "init", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int ufo$fitStellarRecipe(int original) {
        if (!hasStellarRecipe()) {
            return original;
        }
        int availableHeight = ((RecipeScreen) (Object) this).height - 52 - EmiConfig.verticalMargin;
        return Math.min(Math.max(original, STELLAR_RECIPE_SCREEN_HEIGHT), availableHeight);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Ldev/emi/emi/api/recipe/EmiRecipeCategory;getName()Lnet/minecraft/network/chat/Component;"))
    private Component ufo$showSimulationTitle(Component original) {
        StellarSimulationRecipe recipe = currentStellarRecipe();
        if (recipe == null || recipe.getSimulationName().isBlank()) {
            return original;
        }
        return Component.literal(recipe.getSimulationName());
    }

    @Inject(method = "getWorkstationBounds", at = @At("RETURN"), cancellable = true)
    private void ufo$moveStellarWorkstation(int index, CallbackInfoReturnable<Bounds> result) {
        if (EmiConfig.workstationLocation != SidebarSide.BOTTOM || tabs == null
                || currentStellarRecipe() == null) {
            return;
        }
        int resolveOffset = ((RecipeScreen) (Object) this).getResolveOffset();
        int row = index < 0 ? 0 : index * 18 + resolveOffset;
        result.setReturnValue(new Bounds(x + backgroundWidth + 4, y + 40 + row, 18, 18));
    }

    private boolean hasStellarRecipe() {
        return recipes != null && recipes.values().stream().flatMap(List::stream).anyMatch(
                recipe -> recipe instanceof JemiRecipe<?> jemi
                        && jemi.category instanceof StellarSimulationRecipeCategory);
    }

    private StellarSimulationRecipe currentStellarRecipe() {
        if (tabs == null || tab < 0 || tab >= tabs.size()) {
            return null;
        }
        for (RecipeDisplay display : tabs.get(tab).getPage(page)) {
            if (display.recipe instanceof JemiRecipe<?> jemi
                    && jemi.category instanceof StellarSimulationRecipeCategory
                    && jemi.recipe instanceof StellarSimulationRecipe recipe) {
                return recipe;
            }
        }
        return null;
    }
}

package com.raishxn.ufo.mixin;

import com.raishxn.ufo.item.ModCellItems;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Apply EMI's existing item visibility filter without replacing the JEMI plugin. */
@Pseudo
@Mixin(targets = "dev.emi.emi.registry.EmiStackList", remap = false)
public abstract class MixinEmiOptionalChemicalItems {
    @Inject(method = "isHiddenFromRecipeViewers", at = @At("HEAD"), cancellable = true)
    private static void ufo$hideUnavailableChemicals(Object value, CallbackInfoReturnable<Boolean> cir) {
        if (value instanceof Item item && ModCellItems.hideWithoutMekanism(item)) {
            cir.setReturnValue(true);
        }
    }
}

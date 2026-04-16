package com.raishxn.ufo.mixin;

import com.raishxn.ufo.compat.mekanism.UfoMekanismKeyType;
import appeng.api.util.KeyTypeSelection;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyTypeSelection.class)
public class MixinKeyTypeSelection {
    @Inject(method = "readFromNBT", at = @At("HEAD"), cancellable = true)
    private void ufo$keepDefaultSelectionWhenTagMissing(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (!tag.contains("enabledKeyTypes")) {
            ci.cancel();
        }
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void ufo$enableChemicalKeyTypeForLegacySelections(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (!tag.contains("enabledKeyTypes")) {
            return;
        }

        ListTag enabledKeyTypes = tag.getList("enabledKeyTypes", 8);
        String chemicalId = UfoMekanismKeyType.TYPE.getId().toString();
        for (int i = 0; i < enabledKeyTypes.size(); i++) {
            if (chemicalId.equals(enabledKeyTypes.getString(i))) {
                return;
            }
        }

        KeyTypeSelection self = (KeyTypeSelection) (Object) this;
        try {
            self.setEnabled(UfoMekanismKeyType.TYPE, true);
        } catch (IllegalArgumentException ignored) {
            // The key type is not part of this host's supported set.
        }
    }
}

package com.raishxn.ufo.compat.mekanism;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import mekanism.api.chemical.ChemicalStack;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import me.ramidzkh.mekae2.ae2.MekanismKeyType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Isolates optional Applied Mekanistics linkage from the native fallback. */
final class AppliedMekanisticsCompat {
    private AppliedMekanisticsCompat() {
    }

    static AEKeyType keyType() {
        return MekanismKeyType.TYPE;
    }

    static @Nullable ResourceLocation chemicalId(AEKey key) {
        return key instanceof MekanismKey mekanismKey ? mekanismKey.getId() : null;
    }

    static @Nullable AEKey keyOf(ChemicalStack stack) {
        return stack.isEmpty() ? null : MekanismKey.of(stack);
    }
}

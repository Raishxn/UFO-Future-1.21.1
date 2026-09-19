package com.raishxn.ufo.compat.mekanism;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UfoMekanismStorageCompatTest {
    @Test
    void nativeChemicalAdaptersAreExclusiveWithAppliedMekanistics() {
        assertTrue(UfoMekanismStorageCompat.shouldRegisterNativeAdapters(true, false));
        assertFalse(UfoMekanismStorageCompat.shouldRegisterNativeAdapters(true, true),
                "AppMek already owns Mekanism capability bridging; a second adapter duplicates chemicals");
        assertFalse(UfoMekanismStorageCompat.shouldRegisterNativeAdapters(false, false));
    }
}

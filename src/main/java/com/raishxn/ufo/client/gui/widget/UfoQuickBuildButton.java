package com.raishxn.ufo.client.gui.widget;

/*
 * Adapted from AE2 Lightning Tech's TextureToggleButton.
 * Source code: LGPL-3.0. quick_build.png: original UFO Future asset.
 * Copyright AE2 Lightning Tech contributors.
 */

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufocore.client.gui.widget.UfoToolbarTextureButton;
import net.minecraft.network.chat.Component;

/** AE2-style toolbar action using UFO's original quick-build icon. */
public final class UfoQuickBuildButton extends UfoToolbarTextureButton {
    public UfoQuickBuildButton(OnPress onPress) {
        super(UfoMod.id("textures/gui/buttons/quick_build.png"),
                Component.literal("Auto-build structure"), onPress);
    }
}

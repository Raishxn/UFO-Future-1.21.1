package com.raishxn.ufo.client.gui.widget;

/*
 * Adapted from AE2 Lightning Tech TextureToggleButton, revision 7e2e2726401e4ff4720e5f7efa9f4c0031b57c22.
 * Copyright AE2 Lightning Tech contributors. Source licensed under LGPL-3.0.
 * Associated AE2LT icons are CC BY-NC-SA 3.0; see assets/ufo/AE2LT-WIRELESS-NOTICE.md.
 * UFO adaptation: two-state controls, translated tooltips and UFO resource namespace.
 */

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import com.raishxn.ufo.UfoMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class QuantumWirelessToggleButton extends Button {
    private final ResourceLocation off, on;
    private final String translation;
    private boolean state;

    public QuantumWirelessToggleButton(String off, String on, String translation, OnPress action) {
        super(0, 0, 16, 16, Component.translatable(translation), action, DEFAULT_NARRATION);
        this.off = UfoMod.id("textures/gui/buttons/" + off + ".png");
        this.on = UfoMod.id("textures/gui/buttons/" + on + ".png");
        this.translation = translation;
        setState(false);
    }

    public void setState(boolean state) {
        this.state = state;
        var label = Component.translatable(translation + (state ? ".on" : ".off"));
        setMessage(label); setTooltip(Tooltip.create(label));
    }

    @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int offset = isHovered() ? 1 : 0;
        var background = isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                : isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND;
        background.getBlitter().dest(getX() - 1, getY() + offset, 18, 20).zOffset(2).blit(graphics);
        Blitter.texture(state ? on : off, 16, 16).src(0, 0, 16, 16)
                .dest(getX(), getY() + 1 + offset).zOffset(3).blit(graphics);
    }
}

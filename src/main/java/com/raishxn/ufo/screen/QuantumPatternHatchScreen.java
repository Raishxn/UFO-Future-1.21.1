package com.raishxn.ufo.screen;

import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class QuantumPatternHatchScreen extends PatternProviderScreen<QuantumPatternHatchMenu> {
    public QuantumPatternHatchScreen(QuantumPatternHatchMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
}

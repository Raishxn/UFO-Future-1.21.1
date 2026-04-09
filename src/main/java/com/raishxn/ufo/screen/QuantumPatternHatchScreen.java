package com.raishxn.ufo.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ToolboxPanel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class QuantumPatternHatchScreen extends AEBaseScreen<QuantumPatternHatchMenu> {
    public QuantumPatternHatchScreen(QuantumPatternHatchMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.addOpenPriorityButton();

        if (menu.getToolbox().isPresent()) {
            this.widgets.add("toolbox", new ToolboxPanel(style, menu.getToolbox().getName()));
        }
    }
}

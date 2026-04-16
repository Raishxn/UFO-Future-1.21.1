package com.raishxn.ufo.screen;

import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EntropicConvergenceEngineScreen extends AbstractUniversalMultiblockControllerScreen<EntropicConvergenceEngineMenu> {
    public EntropicConvergenceEngineScreen(EntropicConvergenceEngineMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
}

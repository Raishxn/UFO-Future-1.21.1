package com.raishxn.ufo.screen;

import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EntropicAssemblerMatrixScreen extends AbstractUniversalMultiblockControllerScreen<EntropicAssemblerMatrixMenu> {
    public EntropicAssemblerMatrixScreen(EntropicAssemblerMatrixMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
}

package com.raishxn.ufo.screen;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class QuantumPatternHatchMenu extends PatternProviderMenu {
    public QuantumPatternHatchMenu(MenuType<? extends PatternProviderMenu> menuType, int id, Inventory playerInventory,
            PatternProviderLogicHost host) {
        super(menuType, id, playerInventory, host);
    }

    public QuantumPatternHatchMenu(int id, Inventory playerInventory, PatternProviderLogicHost host) {
        this(com.raishxn.ufo.init.ModMenus.QUANTUM_PATTERN_HATCH_MENU.get(), id, playerInventory, host);
    }
}

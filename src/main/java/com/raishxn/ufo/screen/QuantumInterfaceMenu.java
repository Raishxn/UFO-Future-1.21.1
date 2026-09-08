package com.raishxn.ufo.screen;

import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.SlotSemantics;
import com.raishxn.ufo.block.entity.QuantumInterfaceBlockEntity;
import com.raishxn.ufo.init.ModMenus;
import net.minecraft.world.entity.player.Inventory;

public class QuantumInterfaceMenu extends InterfaceMenu {
    @GuiSync(20) public boolean wireless;
    @GuiSync(21) public boolean autoExport;
    @GuiSync(22) public boolean autoImport;
    @GuiSync(23) public boolean fast;
    @GuiSync(24) public int page;
    @GuiSync(25) public int links;
    @GuiSync(26) public long unlimitedSlots;
    @GuiSync(27) public int linkRange = 32;

    public QuantumInterfaceMenu(int id, Inventory inventory, QuantumInterfaceBlockEntity host) {
        super(ModMenus.QUANTUM_INTERFACE_MENU.get(), id, inventory, host);
        registerClientAction("quantumMode", this::toggleWireless);
        registerClientAction("quantumRange", Integer.class, this::adjustRange);
        registerClientAction("quantumExport", this::toggleExport);
        registerClientAction("quantumImport", this::toggleImport);
        registerClientAction("quantumSpeed", this::toggleFast);
        registerClientAction("quantumPage", Integer.class, this::changePage);
        registerClientAction("quantumUnlimited", Integer.class, this::toggleUnlimited);
        arrangeSlots();
    }

    private QuantumInterfaceBlockEntity quantum() { return (QuantumInterfaceBlockEntity) getHost(); }
    public void toggleUnlimited(int slot) {
        if (slot < 0 || slot >= 36 || slot / 18 != page) return;
        if (isClientSide()) { sendClientAction("quantumUnlimited", slot); return; }
        quantum().toggleUnlimited(slot);
    }
    public void toggleWireless() {
        if (isClientSide()) { sendClientAction("quantumMode"); return; }
        quantum().wirelessLinks().toggleMode(); quantum().setChanged();
    }
    public void adjustRange(int delta) {
        if (delta != 1 && delta != -1) return;
        if (isClientSide()) { sendClientAction("quantumRange", delta); return; }
        quantum().wirelessLinks().setRange(quantum().wirelessLinks().range() + delta);
        quantum().setChanged();
    }
    public void toggleExport() {
        if (isClientSide()) { sendClientAction("quantumExport"); return; }
        quantum().toggleExport();
    }
    public void toggleImport() {
        if (isClientSide()) { sendClientAction("quantumImport"); return; }
        quantum().toggleImport();
    }
    public void toggleFast() {
        if (isClientSide()) { sendClientAction("quantumSpeed"); return; }
        quantum().toggleFast();
    }
    public void changePage(int direction) {
        if (isClientSide()) { sendClientAction("quantumPage", direction); return; }
        if (direction != -1 && direction != 1) return;
        page = Math.floorMod(page + direction, 2); arrangeSlots();
    }

    public void arrangeSlots() {
        for (var semantic : java.util.List.of(SlotSemantics.CONFIG, SlotSemantics.STORAGE)) {
            var slots = getSlots(semantic);
            for (int i = 0; i < slots.size(); i++) {
                var slot = slots.get(i);
                if (slot instanceof AppEngSlot aeSlot) aeSlot.setActive(i / 18 == page);
                slot.x = 8 + (i % 9) * 18;
                slot.y = (semantic == SlotSemantics.CONFIG ? 53 : 71) + ((i % 18) / 9) * 60;
            }
        }
    }

    @Override public void broadcastChanges() {
        if (isServerSide()) {
            wireless = quantum().wirelessLinks().enabled(); links = quantum().wirelessLinks().size();
            linkRange = quantum().wirelessLinks().range();
            unlimitedSlots = quantum().unlimitedSlots();
            autoExport = quantum().autoExport(); autoImport = quantum().autoImport(); fast = quantum().fast();
        }
        super.broadcastChanges();
    }
}

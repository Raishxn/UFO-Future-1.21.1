package com.raishxn.ufo.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import com.raishxn.ufo.client.gui.widget.QuantumWirelessToggleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class QuantumInterfaceScreen extends AEBaseScreen<QuantumInterfaceMenu> {
    private final QuantumWirelessToggleButton mode, export, imports, speed;
    private final java.util.List<IconButton> amountButtons = new java.util.ArrayList<>();

    public QuantumInterfaceScreen(QuantumInterfaceMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
        mode = button("wired_mode", "wireless_mode", "gui.ufo.wireless.mode", menu::toggleWireless);
        export = button("auto_export_off", "auto_export_on", "gui.ufo.wireless.export", menu::toggleExport);
        imports = button("auto_input_off", "auto_input_on", "gui.ufo.wireless.import", menu::toggleImport);
        speed = button("speed_normal", "speed_fast", "gui.ufo.wireless.speed", menu::toggleFast);
        addToLeftToolbar(new IconButton(b -> menu.changePage(-1)) { @Override protected Icon getIcon() { return Icon.ARROW_LEFT; } });
        addToLeftToolbar(new IconButton(b -> menu.changePage(1)) { @Override protected Icon getIcon() { return Icon.ARROW_RIGHT; } });
        widgets.addOpenPriorityButton();
        addToLeftToolbar(new IconButton(b -> menu.adjustRange(hasShiftDown() ? -1 : 1)) {
            @Override protected Icon getIcon() { return Icon.COG; }
            @Override public java.util.List<Component> getTooltipMessage() {
                return java.util.List.of(Component.translatable("gui.ufo.wireless.range", menu.linkRange),
                        Component.translatable("gui.ufo.wireless.range_hint"));
            }
        });
        widgets.add("upgrades", new appeng.client.gui.widgets.UpgradesPanel(menu.getSlots(appeng.menu.SlotSemantics.UPGRADE), java.util.List::of));
        for (int i = 0; i < 18; i++) {
            int column = i;
            var amountButton = new IconButton(b -> {
                int slot = menu.page * 18 + column;
                if (hasShiftDown()) menu.toggleUnlimited(slot);
                else menu.openSetAmountMenu(slot);
            }) {
                @Override protected Icon getIcon() { return isHoveredOrFocused() ? Icon.COG : Icon.COG_DISABLED; }
                @Override public java.util.List<Component> getTooltipMessage() {
                    return java.util.List.of(getMessage(), Component.translatable("gui.ufo.wireless.unlimited.hint"));
                }
            };
            amountButton.setDisableBackground(true);
            amountButtons.add(amountButton);
            widgets.add("amtButton" + (i + 1), amountButton);
        }
    }

    private QuantumWirelessToggleButton button(String off, String on, String key, Runnable action) {
        var button = new QuantumWirelessToggleButton(off, on, key, b -> action.run());
        addToLeftToolbar(button); return button;
    }

    @Override protected void updateBeforeRender() {
        super.updateBeforeRender(); menu.arrangeSlots();
        mode.setState(menu.wireless); export.setState(menu.autoExport); imports.setState(menu.autoImport); speed.setState(menu.fast);
        setTextContent("page", Component.literal((menu.page + 1) + "/2"));
        for (int i = 0; i < amountButtons.size(); i++) {
            boolean unlimited = (menu.unlimitedSlots & (1L << (menu.page * 18 + i))) != 0;
            var text = Component.translatable(unlimited ? "gui.ufo.wireless.unlimited.on" : "gui.ufo.wireless.unlimited.off");
            amountButtons.get(i).setMessage(text);
            amountButtons.get(i).visible = !menu.getSlots(appeng.menu.SlotSemantics.CONFIG)
                    .get(menu.page * 18 + i).getItem().isEmpty();
        }
    }
}

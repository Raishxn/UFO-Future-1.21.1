package com.raishxn.ufo.screen;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.api.upgrades.Upgrades;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.ToggleButton;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.ConfigButtonPacket;
import appeng.menu.SlotSemantics;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class QuantumPatternHatchScreen extends AEBaseScreen<QuantumPatternHatchMenu> {
    private final SettingToggleButton<YesNo> blockingModeButton;
    private final SettingToggleButton<LockCraftingMode> lockCraftingModeButton;
    private final ToggleButton showInPatternAccessTerminalButton;
    private final QuantumPatternHatchLockReason lockReason;
    private final com.raishxn.ufo.client.gui.widget.QuantumWirelessToggleButton wirelessButton;
    public QuantumPatternHatchScreen(QuantumPatternHatchMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.wirelessButton = new com.raishxn.ufo.client.gui.widget.QuantumWirelessToggleButton(
                "wired_mode", "wireless_mode", "gui.ufo.wireless.mode", b -> menu.toggleQuantumWireless());
        this.addToLeftToolbar(wirelessButton);
        this.addToLeftToolbar(new appeng.client.gui.widgets.IconButton(b -> menu.adjustRange(hasShiftDown() ? -1 : 1)) {
            @Override protected Icon getIcon() { return Icon.COG; }
            @Override public List<Component> getTooltipMessage() {
                return List.of(Component.translatable("gui.ufo.wireless.range", menu.linkRange),
                        Component.translatable("gui.ufo.wireless.range_hint"));
            }
        });

        this.blockingModeButton = new ServerSettingToggleButton<>(Settings.BLOCKING_MODE, YesNo.NO);
        this.addToLeftToolbar(this.blockingModeButton);

        this.lockCraftingModeButton = new ServerSettingToggleButton<>(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);
        this.addToLeftToolbar(this.lockCraftingModeButton);

        widgets.addOpenPriorityButton();

        this.showInPatternAccessTerminalButton = new ToggleButton(Icon.PATTERN_ACCESS_SHOW,
                Icon.PATTERN_ACCESS_HIDE,
                GuiText.PatternAccessTerminal.text(), GuiText.PatternAccessTerminalHint.text(),
                btn -> selectNextPatternProviderMode());
        this.addToLeftToolbar(this.showInPatternAccessTerminalButton);

        this.widgets.add("upgrades", new UpgradesPanel(
                menu.getSlots(SlotSemantics.UPGRADE), this::getCompatibleUpgrades));

        this.lockReason = new QuantumPatternHatchLockReason(this);
        widgets.add("lockReason", this.lockReason);

        if (menu.getToolbox().isPresent()) {
            this.widgets.add("toolbox", new ToolboxPanel(style, menu.getToolbox().getName()));
        }
    }

    @Override
    public List<net.minecraft.client.renderer.Rect2i> getExclusionZones() {
        var zones = new ArrayList<>(super.getExclusionZones());
        if (menu.hasQuantumWireless) zones.add(new net.minecraft.client.renderer.Rect2i(
                leftPos + imageWidth, topPos + 80, 63, 110));
        return zones;
    }

    @Override
    public void drawBG(net.minecraft.client.gui.GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(graphics, x, y, mouseX, mouseY, partialTicks);
        if (menu.hasQuantumWireless) {
            com.raishxn.ufo.client.gui.widget.MultiblockSupplyWidget.renderWireless(
                    graphics, font, x + imageWidth, y + 80, menu.patternBuffer,
                    menu.connectedMachines, menu.bonusMachines,
                    menu.bonusSpeed, menu.bonusEnergy, menu.bonusHeat,
                    menu.activeMultiblocks, menu.multiSpeed, menu.multiEnergy, menu.multiHeat);
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.wirelessButton.visible = menu.hasQuantumWireless;
        this.wirelessButton.active = menu.hasQuantumWireless;
        this.wirelessButton.setState(menu.quantumWireless);

        this.lockReason.setVisible(menu.getLockCraftingMode() != LockCraftingMode.NONE);
        this.blockingModeButton.set(this.menu.getBlockingMode());
        this.lockCraftingModeButton.set(this.menu.getLockCraftingMode());
        this.showInPatternAccessTerminalButton.setState(this.menu.getShowInAccessTerminal() == YesNo.YES);
    }

    private void selectNextPatternProviderMode() {
        final boolean backwards = isHandlingRightClick();
        ServerboundPacket message = new ConfigButtonPacket(Settings.PATTERN_ACCESS_TERMINAL, backwards);
        PacketDistributor.sendToServer(message);
    }

    private List<Component> getCompatibleUpgrades() {
        var lines = new ArrayList<Component>();
        lines.add(GuiText.CompatibleUpgrades.text());
        lines.addAll(Upgrades.getTooltipLinesForMachine(menu.getUpgrades().getUpgradableItem()));
        return lines;
    }
}

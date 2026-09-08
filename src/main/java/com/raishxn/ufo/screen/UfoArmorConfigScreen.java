package com.raishxn.ufo.screen;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.armor.UfoArmorSetting;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.SetUfoArmorModuleSettingPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UfoArmorConfigScreen extends AbstractContainerScreen<UfoArmorConfigMenu> {
    private static final ResourceLocation TEXTURE = UfoMod.id("textures/gui/ufo_armor_config.png");
    private static final ResourceLocation AE2_STATES = ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/states.png");
    private static final ResourceLocation AE2_CHECKBOX = ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/checkbox.png");
    private static final ResourceLocation MODULE_BUTTONS = UfoMod.id("textures/gui/advanced_ae_states.png");
    private static final ResourceLocation AE2_BUTTON = ResourceLocation.fromNamespaceAndPath("ae2", "button");
    private static final int ROW_X = 9;
    private static final int ROW_Y = 32;
    private static final int ROW_WIDTH = 128;
    private static final int ROW_HEIGHT = 16;
    private static final int VISIBLE_ROWS = 4;
    private int scrollOffset;
    private UfoArmorModule configuringModule;
    private UfoArmorSetting draggingSetting;

    public UfoArmorConfigScreen(UfoArmorConfigMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 195;
        inventoryLabelX = 8;
        inventoryLabelY = 102;
        titleLabelX = 10;
        titleLabelY = 8;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderModuleTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (configuringModule == null) renderModuleRows(graphics, mouseX, mouseY);
        else renderSettingsPanel(graphics, mouseX, mouseY);

        int selected = menu.selectedPiece();
        if (selected >= 0 && selected < 4) {
            int x = leftPos + 151;
            int y = topPos + 26 + selected * 18;
            graphics.fill(x, y, x + 18, y + 1, 0xFF7FCBD5);
            graphics.fill(x, y + 17, x + 18, y + 18, 0xFF7FCBD5);
            graphics.fill(x, y, x + 1, y + 18, 0xFF7FCBD5);
            graphics.fill(x + 17, y, x + 18, y + 18, 0xFF7FCBD5);
        }

        int total = installedModules().size();
        if (configuringModule == null && total > VISIBLE_ROWS) {
            int trackX = leftPos + 139;
            int trackY = topPos + 33;
            graphics.fill(trackX, trackY, trackX + 5, trackY + 60, 0xFF666875);
            int travel = 50;
            int thumbY = trackY + (scrollOffset * travel / Math.max(1, total - VISIBLE_ROWS));
            graphics.fill(trackX + 1, thumbY, trackX + 4, thumbY + 10, 0xFFB9BAC5);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF404050, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404050, false);

        if (configuringModule == null) {
            List<UfoArmorModule> modules = visibleModules();
            for (int row = 0; row < modules.size(); row++) {
                UfoArmorModule module = modules.get(row);
                graphics.pose().pushPose();
                graphics.pose().scale(0.75F, 0.75F, 1.0F);
                graphics.drawString(font, Component.translatable(module.translationKey()),
                        Math.round((ROW_X + 2) / 0.75F), Math.round((ROW_Y + row * ROW_HEIGHT + 4) / 0.75F),
                        0xFF404050, false);
                graphics.pose().popPose();
            }
        } else {
            graphics.drawString(font, Component.translatable(configuringModule.translationKey()),
                    ROW_X + 3, ROW_Y + 3, 0xFF404050, false);
            List<UfoArmorSetting> settings = UfoArmorSetting.forModule(configuringModule);
            for (int row = 0; row < settings.size(); row++) {
                UfoArmorSetting setting = settings.get(row);
                int y = ROW_Y + 18 + row * 27;
                graphics.pose().pushPose();
                graphics.pose().scale(0.7F, 0.7F, 1.0F);
                graphics.drawString(font, Component.translatable(setting.translationKey()),
                        Math.round((ROW_X + 4) / 0.7F), Math.round(y / 0.7F), 0xFF404050, false);
                graphics.pose().popPose();
                String value = setting.formatted(UfoArmorItem.moduleSetting(menu.selectedArmor(), setting));
                graphics.drawString(font, value, ROW_X + 124 - font.width(value), y, 0xFF404050, false);
            }
        }

        ItemStack selected = menu.selectedArmor();
        if (selected.getItem() instanceof UfoArmorItem armor) {
            int used = UfoArmorItem.installedModules(selected).size();
            graphics.drawString(font, used + "/" + UfoArmorModule.capacity(armor.getType()), 126, 96, 0xFF616161, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && minecraft != null && minecraft.gameMode != null) {
            if (configuringModule != null) {
                if (inside(mouseX, mouseY, leftPos + 119, topPos + ROW_Y + 1, 14, 14)) {
                    configuringModule = null;
                    return true;
                }
                List<UfoArmorSetting> settings = UfoArmorSetting.forModule(configuringModule);
                for (int row = 0; row < settings.size(); row++) {
                    int y = topPos + ROW_Y + 18 + row * 27;
                    if (inside(mouseX, mouseY, leftPos + ROW_X + 4, y + 8, 116, 12)) {
                        draggingSetting = settings.get(row);
                        setSettingFromMouse(draggingSetting, mouseX);
                        return true;
                    }
                }
                return true;
            }
            for (int piece = 0; piece < 4; piece++) {
                if (inside(mouseX, mouseY, leftPos + 152, topPos + 27 + piece * 18, 16, 16)) {
                    configuringModule = null;
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, piece);
                    return true;
                }
            }
            List<UfoArmorModule> modules = visibleModules();
            for (int row = 0; row < modules.size(); row++) {
                int y = topPos + ROW_Y + row * ROW_HEIGHT;
                UfoArmorModule module = modules.get(row);
                if (!UfoArmorSetting.forModule(module).isEmpty()
                        && inside(mouseX, mouseY, leftPos + 77, y, 16, 16)) {
                    configuringModule = module;
                    return true;
                }
                if (inside(mouseX, mouseY, leftPos + 94, y, 22, 16)) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 100 + module.ordinal());
                    return true;
                }
                if (inside(mouseX, mouseY, leftPos + 116, y, 16, 16)) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 200 + module.ordinal());
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<UfoArmorModule> modules = installedModules();
        int max = Math.max(0, modules.size() - VISIBLE_ROWS);
        if (scrollY < 0) scrollOffset = Math.min(max, scrollOffset + 1);
        if (scrollY > 0) scrollOffset = Math.max(0, scrollOffset - 1);
        return max > 0 || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingSetting != null) {
            setSettingFromMouse(draggingSetting, mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSetting = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private List<UfoArmorModule> installedModules() {
        return new ArrayList<>(UfoArmorItem.installedModules(menu.selectedArmor()));
    }

    private List<UfoArmorModule> visibleModules() {
        List<UfoArmorModule> modules = installedModules();
        int maxOffset = Math.max(0, modules.size() - VISIBLE_ROWS);
        scrollOffset = Math.min(scrollOffset, maxOffset);
        return modules.subList(scrollOffset, Math.min(modules.size(), scrollOffset + VISIBLE_ROWS));
    }

    private void renderModuleTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (configuringModule != null) return;
        List<UfoArmorModule> modules = visibleModules();
        for (int row = 0; row < modules.size(); row++) {
            int y = topPos + ROW_Y + row * ROW_HEIGHT;
            if (!inside(mouseX, mouseY, leftPos + ROW_X, y, ROW_WIDTH, ROW_HEIGHT)) continue;
            UfoArmorModule module = modules.get(row);
            boolean enabled = UfoArmorItem.isModuleEnabled(menu.selectedArmor(), module);
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(module.translationKey()));
            tooltip.add(Component.translatable("tooltip.ufo.module." + module.id())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
            tooltip.add(Component.translatable(enabled ? "gui.ufo.module.enabled" : "gui.ufo.module.disabled")
                    .withStyle(enabled ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED));
            if (!UfoArmorSetting.forModule(module).isEmpty()) {
                tooltip.add(Component.translatable("gui.ufo.module.configure")
                        .withStyle(net.minecraft.ChatFormatting.AQUA));
            }
            if (module.energyCost() > 0) {
                tooltip.add(Component.translatable("gui.ufo.module.energy", module.energyCost())
                        .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            }
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            return;
        }
    }

    private void renderModuleRows(GuiGraphics graphics, int mouseX, int mouseY) {
        List<UfoArmorModule> modules = visibleModules();
        for (int row = 0; row < modules.size(); row++) {
            int y = topPos + ROW_Y + row * ROW_HEIGHT;
            graphics.blit(TEXTURE, leftPos + ROW_X, y - 1, 0, 195, ROW_WIDTH, ROW_HEIGHT);
            UfoArmorModule module = modules.get(row);
            if (!UfoArmorSetting.forModule(module).isEmpty()) {
                boolean hovered = inside(mouseX, mouseY, leftPos + 77, y, 16, 16);
                graphics.blit(AE2_STATES, leftPos + 77, y, hovered ? 32 : 48, 64,
                        16, 16, 256, 256);
            }
            boolean enabled = UfoArmorItem.isModuleEnabled(menu.selectedArmor(), module);
            boolean toggleHover = inside(mouseX, mouseY, leftPos + 96, y, 16, 16);
            int checkboxU = toggleHover ? 22 : 0;
            int checkboxV = enabled ? 40 : 28;
            graphics.blit(AE2_CHECKBOX, leftPos + 94, y + 2, checkboxU, checkboxV,
                    22, 12, 64, 64);
            boolean removeHover = inside(mouseX, mouseY, leftPos + 116, y, 16, 16);
            graphics.blit(MODULE_BUTTONS, leftPos + 116, y, removeHover ? 80 : 96, 0,
                    16, 16, 256, 256);
        }
    }

    private void renderSettingsPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos + ROW_X, topPos + ROW_Y - 1, 0, 195, ROW_WIDTH, ROW_HEIGHT);
        boolean backHover = inside(mouseX, mouseY, leftPos + 119, topPos + ROW_Y + 1, 14, 14);
        graphics.blit(AE2_STATES, leftPos + 119, topPos + ROW_Y,
                96, 16, 16, 16, 256, 256);
        if (backHover) graphics.fill(leftPos + 119, topPos + ROW_Y, leftPos + 135,
                topPos + ROW_Y + 16, 0x225ACBD8);
        List<UfoArmorSetting> settings = UfoArmorSetting.forModule(configuringModule);
        for (int row = 0; row < settings.size(); row++) {
            UfoArmorSetting setting = settings.get(row);
            int y = topPos + ROW_Y + 18 + row * 27;
            int value = UfoArmorItem.moduleSetting(menu.selectedArmor(), setting);
            int minX = leftPos + ROW_X + 4;
            int maxX = minX + 116;
            int lineY = y + 14;
            graphics.fill(minX - 1, lineY - 2, maxX + 1, lineY + 3, 0xFFF2F2F2);
            graphics.hLine(minX, maxX, lineY, 0xFF696D88);
            graphics.vLine(minX, lineY - 2, lineY + 2, 0xFF696D88);
            graphics.vLine(maxX, lineY - 2, lineY + 2, 0xFF696D88);
            float fraction = (value - setting.min()) / (float) (setting.max() - setting.min());
            int handleX = minX + Math.round(fraction * (116 - 8));
            graphics.blitSprite(AE2_BUTTON, handleX, lineY - 5, 8, 10);
        }
    }

    private void setSettingFromMouse(UfoArmorSetting setting, double mouseX) {
        ItemStack armor = menu.selectedArmor();
        double fraction = Math.max(0.0, Math.min(1.0,
                (mouseX - (leftPos + ROW_X + 4)) / (116.0 - 8.0)));
        int value = setting.clamp(setting.min() + (int) Math.round(fraction * (setting.max() - setting.min())));
        UfoArmorItem.setModuleSetting(armor, setting, value);
        ModPackets.sendToServer(new SetUfoArmorModuleSettingPacket(
                setting.module().ordinal(), setting.ordinal(), value));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}

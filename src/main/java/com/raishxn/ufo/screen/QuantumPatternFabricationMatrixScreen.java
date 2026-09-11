package com.raishxn.ufo.screen;

import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.OpenGuideButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.core.network.serverbound.SwitchGuisPacket;
import appeng.items.tools.GuideItem;
import appeng.menu.implementations.PriorityMenu;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.client.gui.widget.UfoQuickBuildButton;
import com.raishxn.ufo.client.render.StructureHighlightRenderer;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketAutoBuildMultiblock;
import com.raishxn.ufo.network.packet.PacketScanUniversalStructure;
import com.raishxn.ufocore.client.gui.widget.UfoAe2IconButton;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** AE-style status dashboard for the Quantum Pattern Fabrication Matrix. */
public final class QuantumPatternFabricationMatrixScreen
        extends AbstractContainerScreen<QuantumPatternFabricationMatrixMenu> {
    private static final int PANEL_WIDTH = 252;
    private static final int PANEL_HEIGHT = 180;
    private static final int BACKGROUND = 0xF20B1020;
    private static final int PANEL = 0xFF151D32;
    private static final int PANEL_ALT = 0xFF11182A;
    private static final int CYAN = 0xFF58E6FF;
    private static final int PURPLE = 0xFFB268FF;
    private static final int TEXT = 0xFFE8F6FF;
    private static final int MUTED = 0xFF8DA4B8;

    private VerticalButtonBar leftToolbar;
    private Button autoBuildButton;
    private Button patternManagementButton;

    public QuantumPatternFabricationMatrixScreen(QuantumPatternFabricationMatrixMenu menu,
                                                  Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        titleLabelY = 9;
        inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        leftToolbar = new VerticalButtonBar();
        leftToolbar.add(new OpenGuideButton(button -> {
            if (minecraft != null && minecraft.player != null) {
                GuidesCommon.openGuide(minecraft.player, GuideItem.GUIDE_ID,
                        new PageAnchor(UfoMod.id("ufo_intro/quantum_pattern_fabrication_matrix.md"), null));
            }
        }));
        leftToolbar.add(new UfoAe2IconButton(Icon.SCHEDULING_DEFAULT,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.scan"),
                button -> scanStructure()));
        UfoQuickBuildButton quickBuild = new UfoQuickBuildButton(button ->
                ModPackets.sendToServer(new PacketAutoBuildMultiblock(menu.getBlockEntity().getBlockPos())));
        quickBuild.setTooltip(Tooltip.create(
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.auto_build")));
        autoBuildButton = quickBuild;
        leftToolbar.add(quickBuild);
        leftToolbar.setPosition(new Point(3, 1));
        leftToolbar.populateScreen(this::addRenderableWidget,
                new Rect2i(leftPos, topPos, imageWidth, imageHeight), null);

        Component priorityLabel = Component.translatable(
                "gui.ufo.quantum_pattern_fabrication_matrix.priority");
        TabButton priorityButton = new TabButton(Icon.PRIORITY, priorityLabel,
                button -> PacketDistributor.sendToServer(SwitchGuisPacket.openSubMenu(PriorityMenu.TYPE)));
        priorityButton.setTooltip(Tooltip.create(priorityLabel));
        priorityButton.setSize(20, 20);
        priorityButton.setPosition(leftPos + imageWidth - 24, topPos - 5);
        addRenderableWidget(priorityButton);

        Component patternsLabel = Component.translatable(
                "gui.ufo.quantum_pattern_fabrication_matrix.open_pattern_management");
        TabButton patternsButton = new TabButton(Icon.PATTERN_ACCESS_SHOW, patternsLabel,
                button -> menu.openPatternManagement());
        patternsButton.setTooltip(Tooltip.create(patternsLabel));
        patternsButton.setSize(20, 20);
        patternsButton.setPosition(leftPos + imageWidth - 24, topPos + 17);
        patternManagementButton = addRenderableWidget(patternsButton);
        updateToolbarState();
        leftToolbar.updateBeforeRender();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateToolbarState();
        leftToolbar.updateBeforeRender();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (inside(mouseX, mouseY, leftPos + 10, topPos + 70, 232, 38)) {
            graphics.renderTooltip(font,
                    Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.pattern_usage_detail",
                            menu.getStoredPatternCount(), menu.getPatternCapacity())
                            .withStyle(ChatFormatting.AQUA), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, BACKGROUND);
        outline(graphics, x, y, imageWidth, imageHeight, 0xFF314660);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 3, CYAN);
        graphics.fill(x + imageWidth / 2, y + 1, x + imageWidth - 1, y + 3, PURPLE);

        panel(graphics, x + 10, y + 27, 232, 33, PANEL_ALT);
        panel(graphics, x + 10, y + 70, 232, 38, PANEL);
        panel(graphics, x + 10, y + 118, 111, 51, PANEL);
        panel(graphics, x + 131, y + 118, 111, 51, PANEL);

        int capacity = menu.getPatternCapacity();
        int used = menu.getStoredPatternCount();
        int barWidth = capacity <= 0 ? 0 : Math.min(224, (int) ((long) used * 224L / capacity));
        graphics.fill(x + 14, y + 94, x + 238, y + 101, 0xFF263249);
        if (barWidth > 0) {
            graphics.fill(x + 14, y + 94, x + 14 + barWidth, y + 101, CYAN);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.title"),
                imageWidth / 2, titleLabelY, TEXT);

        Component status;
        int statusColor;
        if (!menu.isFormed()) {
            status = Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.incomplete");
            statusColor = 0xFFFF667A;
        } else if (!menu.isGridActive()) {
            status = Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.formed_grid_offline");
            statusColor = 0xFFFFB85C;
        } else {
            status = Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.online");
            statusColor = 0xFF5DFFA2;
        }
        graphics.drawString(font,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.status"),
                18, 34, MUTED, false);
        graphics.drawString(font, status, 18, 46, statusColor, false);
        String fields = Integer.toString(menu.getFieldCount());
        graphics.drawString(font, fields, imageWidth - 18 - font.width(fields), 41, TEXT, false);

        graphics.drawString(font,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.pattern_library"),
                18, 77, MUTED, false);
        String usage = menu.getStoredPatternCount() + " / " + menu.getPatternCapacity();
        graphics.drawString(font, usage, imageWidth - 18 - font.width(usage), 77, TEXT, false);

        graphics.drawString(font,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.field_generators"),
                18, 127, MUTED, false);
        Component fieldBreakdown = Component.translatable(
                "gui.ufo.quantum_pattern_fabrication_matrix.field_breakdown",
                menu.getTier1Fields(), menu.getTier2Fields(), menu.getTier3Fields());
        graphics.drawCenteredString(font, fieldBreakdown, 65, 146, CYAN);

        graphics.drawString(font,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.auto_upload"),
                139, 127, MUTED, false);
        Component upload = Component.translatable(
                menu.isGridActive()
                        ? "gui.ufo.quantum_pattern_fabrication_matrix.auto_upload_ready"
                        : "gui.ufo.quantum_pattern_fabrication_matrix.auto_upload_offline");
        graphics.drawCenteredString(font, upload, 186, 146, PURPLE);
    }

    private void updateToolbarState() {
        if (autoBuildButton != null) {
            autoBuildButton.visible = !menu.isFormed();
            autoBuildButton.active = !menu.isFormed();
        }
        if (patternManagementButton != null) patternManagementButton.active = true;
    }

    private void scanStructure() {
        BlockPos controllerPos = menu.getBlockEntity().getBlockPos();
        ModPackets.sendToServer(new PacketScanUniversalStructure(controllerPos));
        if (minecraft == null || minecraft.level == null || minecraft.player == null) return;

        var blockEntity = minecraft.level.getBlockEntity(controllerPos);
        var definition = MultiblockControllerDefinitions.getDefinition(blockEntity);
        if (definition.isEmpty()) return;
        var state = minecraft.level.getBlockState(controllerPos);
        var facing = MultiblockControllerDefinitions.getPatternFacing(blockEntity, state);
        var result = definition.get().pattern().match(minecraft.level, controllerPos, facing);
        if (result.isValid()) {
            minecraft.player.displayClientMessage(
                    Component.translatable("message.ufo.structure_formed").withStyle(ChatFormatting.GREEN), true);
            return;
        }
        minecraft.player.displayClientMessage(
                Component.literal(result.allErrors().size() + " block(s) missing or misplaced.")
                        .withStyle(ChatFormatting.RED), false);
        for (int index = 0; index < Math.min(50, result.allErrors().size()); index++) {
            StructureHighlightRenderer.highlight(result.allErrors().get(index).pos(), 5000);
        }
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static void panel(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
        outline(graphics, x, y, width, height, 0xFF263A54);
    }

    private static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}

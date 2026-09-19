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
import com.raishxn.ufo.block.MultiblockBlocks;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** AE-style status dashboard for the Quantum Pattern Fabrication Matrix. */
public final class QuantumPatternFabricationMatrixScreen
        extends AbstractContainerScreen<QuantumPatternFabricationMatrixMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "ae2", "textures/guis/quantumpatternfabricationmatrix.png");
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 127;
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
        titleLabelY = 10_000;
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
        priorityButton.setPosition(leftPos + imageWidth - 3, topPos + 5);
        addRenderableWidget(priorityButton);

        Component patternsLabel = Component.translatable(
                "gui.ufo.quantum_pattern_fabrication_matrix.open_pattern_management");
        TabButton patternsButton = new TabButton(Icon.PATTERN_ACCESS_SHOW, patternsLabel,
                button -> menu.openPatternManagement());
        patternsButton.setTooltip(Tooltip.create(patternsLabel));
        patternsButton.setSize(20, 20);
        patternsButton.setPosition(leftPos + imageWidth - 3, topPos + 27);
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
        if (inside(mouseX, mouseY, leftPos + 16, topPos + 47, 143, 15)) {
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
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        leftToolbar.drawBackgroundLayer(graphics,
                new Rect2i(leftPos, topPos, imageWidth, imageHeight), Point.ZERO);

        int capacity = menu.getPatternCapacity();
        int used = menu.getStoredPatternCount();
        int barWidth = capacity <= 0 ? 0 : Math.min(141, (int) ((long) used * 141L / capacity));
        graphics.fill(x + 17, y + 58, x + 158, y + 62, 0xFF202633);
        if (barWidth > 0) {
            float fullness = Math.min(1.0F, used / (float) capacity);
            int red = Math.round(255.0F * fullness);
            int green = Math.round(255.0F * (1.0F - fullness));
            graphics.fill(x + 17, y + 58, x + 17 + barWidth, y + 62,
                    0xFF000000 | red << 16 | green << 8);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
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
        drawCenteredFitted(graphics,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.status"),
                14, 14, 160, 0.75F, MUTED);
        drawCenteredFitted(graphics, status, 14, 23, 160, 0.75F, statusColor);

        drawLeftFitted(graphics,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.pattern_library"),
                16, 47, 115, 0.72F, MUTED);
        String usage = menu.getStoredPatternCount() + " / " + menu.getPatternCapacity();
        drawRightFitted(graphics, Component.literal(usage), 116, 47, 158, 0.8F, TEXT);

        drawCenteredFitted(graphics,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.field_generators"),
                17, 82, 80, 0.68F, MUTED);
        renderField(graphics, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().asItem().getDefaultInstance(),
                menu.getTier1Fields(), 18, 94);
        renderField(graphics, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get().asItem().getDefaultInstance(),
                menu.getTier2Fields(), 40, 94);
        renderField(graphics, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get().asItem().getDefaultInstance(),
                menu.getTier3Fields(), 62, 94);

        drawCenteredFitted(graphics,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.auto_upload"),
                94, 82, 157, 0.72F, MUTED);
        Component upload = Component.translatable(
                menu.isGridActive()
                        ? "gui.ufo.quantum_pattern_fabrication_matrix.auto_upload_ready"
                        : "gui.ufo.quantum_pattern_fabrication_matrix.auto_upload_offline");
        drawCenteredFitted(graphics, upload, 94, 99, 157, 0.85F, PURPLE);
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

    private void renderField(GuiGraphics graphics, ItemStack field, int count, int x, int y) {
        graphics.renderItem(field, x, y);
        graphics.renderItemDecorations(font, field, x, y, Integer.toString(count));
    }

    private void drawCenteredFitted(GuiGraphics graphics, Component text, int minX, int y, int maxX,
                                    float maxScale, int color) {
        drawFitted(graphics, text, minX, y, maxX, maxScale, color, 0.5F);
    }

    private void drawLeftFitted(GuiGraphics graphics, Component text, int minX, int y, int maxX,
                                float maxScale, int color) {
        drawFitted(graphics, text, minX, y, maxX, maxScale, color, 0.0F);
    }

    private void drawRightFitted(GuiGraphics graphics, Component text, int minX, int y, int maxX,
                                 float maxScale, int color) {
        drawFitted(graphics, text, minX, y, maxX, maxScale, color, 1.0F);
    }

    private void drawFitted(GuiGraphics graphics, Component text, int minX, int y, int maxX,
                            float maxScale, int color, float alignment) {
        int width = Math.max(1, font.width(text));
        float scale = Math.min(maxScale, (maxX - minX + 1) / (float) width);
        float anchor = minX + (maxX - minX + 1) * alignment;
        graphics.pose().pushPose();
        graphics.pose().translate(anchor, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        int drawX = Math.round(-width * alignment);
        graphics.drawString(font, text, drawX, 0, color, false);
        graphics.pose().popPose();
    }
}

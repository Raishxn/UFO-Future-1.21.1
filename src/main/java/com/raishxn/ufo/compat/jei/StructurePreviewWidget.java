package com.raishxn.ufo.compat.jei;

/*
 * Ported and adapted from AE2 Lightning Tech's InteractiveMultiblockPreview.
 * Copyright AE2 Lightning Tech contributors. Licensed under LGPL-3.0.
 * No AE2 Lightning Tech visual assets are included in this file.
 */

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.raishxn.ufo.client.preview.StructurePreviewLayout;
import com.raishxn.ufo.client.preview.StructurePreviewModel;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import appeng.client.render.overlay.OverlayRenderType;

import java.util.List;

/** JEI adapter for UFO's LGPL-compatible AE2LT-style interactive structure viewer. */
final class StructurePreviewWidget implements IRecipeWidget, IJeiInputHandler {
    private static final int NAME_Y = 1, TOOLBAR_Y = 14, TOOLBAR_H = 16;
    private static final int VIEW_X = 2, VIEW_Y = 31, PANEL_W = 105, FOOTER_H = 17, TAB_H = 15, ROW_H = 26;
    private static final float DEFAULT_YAW = 225, DEFAULT_PITCH = 30, DEPTH_SCALE = .1F;
    private static final int TEXT = 0xFFFFFFFF, MUTED = 0xFFD0D0D0, BORDER = 0xFF626262;
    private static final int SELECTED_GLOW_ALPHA = 132, HOVERED_GLOW_ALPHA = 190;
    private static final ScreenPosition POSITION = new ScreenPosition(0, 0);

    private final StructurePreviewModel model;
    private final int width, height, panelX, panelHeight, viewWidth, viewHeight;
    private final float baseScale;
    private final ScreenRectangle area;
    private float yaw = DEFAULT_YAW, pitch = DEFAULT_PITCH, zoom = 1, panX, panY;
    private boolean autoRotate = true, hideShell;
    private StructurePreviewLayout.LayerMode layerMode = StructurePreviewLayout.LayerMode.FULL;
    private PanelTab panelTab = PanelTab.BLOCKS;
    private int layer, materialScroll;
    private StructurePreviewModel.Cell selectedCell, hoveredCell;

    StructurePreviewWidget(StructurePreviewModel model, int width, int height) {
        this.model = model;
        this.width = width;
        this.height = height;
        this.panelX = width - PANEL_W - 2;
        this.panelHeight = height - VIEW_Y - FOOTER_H;
        this.viewWidth = panelX - VIEW_X - 4;
        this.viewHeight = panelHeight;
        this.area = new ScreenRectangle(0, 0, width, height);
        this.layer = Math.max(0, (model.sizeY() - 1) / 2);
        float horizontal = (float) Math.hypot(model.sizeX(), model.sizeZ());
        float projectedHeight = model.sizeY() * .866F + horizontal * .5F;
        this.baseScale = Math.min(viewWidth * .9F / Math.max(1, horizontal),
                viewHeight * .9F / Math.max(1, projectedHeight));
    }

    @Override public ScreenPosition getPosition() { return POSITION; }
    @Override public ScreenRectangle getArea() { return area; }
    @Override public void tick() { if (autoRotate) yaw = wrap(yaw + .35F); }

    @Override
    public void drawWidget(GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        hoveredCell = inside(VIEW_X, VIEW_Y, viewWidth, viewHeight, mouseX, mouseY) ? pickCell(mouseX, mouseY) : null;
        drawHeader(g, font);
        drawToolbar(g, font, mouseX, mouseY);
        drawViewport(g);
        drawPanel(g, font, mouseX, mouseY);
        drawCentered(g, font, "Drag: rotate | Shift/middle: pan | Wheel: zoom | Click: inspect",
                2, height - FOOTER_H + 3, width - 4, 0xFF000000);
    }

    private void drawHeader(GuiGraphics g, Font f) {
        drawTrimmed(g, f, model.title().getString(), 2, NAME_Y, width - 70, TEXT);
        String dimensions = model.sizeX() + "x" + model.sizeY() + "x" + model.sizeZ();
        g.drawString(f, dimensions, width - f.width(dimensions) - 2, NAME_Y, MUTED, false);
    }

    private void drawToolbar(GuiGraphics g, Font f, double mx, double my) {
        iconButton(g, 2, TOOLBAR_Y, Icon.RESET, false, true, mx, my);
        iconButton(g, 20, TOOLBAR_Y, autoRotate ? Icon.PAUSE : Icon.PLAY, autoRotate, true, mx, my);
        iconButton(g, 38, TOOLBAR_Y, hideShell ? Icon.SHELL_HIDDEN : Icon.SHELL, hideShell, true, mx, my);
        iconButton(g, 56, TOOLBAR_Y, layerIcon(), layerMode != StructurePreviewLayout.LayerMode.FULL, true, mx, my);
        iconButton(g, 76, TOOLBAR_Y, Icon.MINUS, false, layer > 0, mx, my);
        insetLabel(g, f, 94, TOOLBAR_Y, 49, TOOLBAR_H, "Y " + (layer + 1) + "/" + model.sizeY());
        iconButton(g, 145, TOOLBAR_Y, Icon.PLUS, false, layer < model.sizeY() - 1, mx, my);
    }

    private void drawViewport(GuiGraphics g) {
        g.fill(VIEW_X, VIEW_Y, VIEW_X + viewWidth, VIEW_Y + viewHeight, 0xFF101010);
        g.renderOutline(VIEW_X, VIEW_Y, viewWidth, viewHeight, BORDER);
        enableLocalScissor(g, VIEW_X + 1, VIEW_Y + 1, VIEW_X + viewWidth - 1, VIEW_Y + viewHeight - 1);
        Minecraft mc = Minecraft.getInstance();
        var buffers = mc.renderBuffers().bufferSource();
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(viewCenterX() + panX, viewCenterY() + panY, 80);
        pose.scale(renderScale(), -renderScale(), renderScale() * DEPTH_SCALE);
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.translate(-model.sizeX() / 2F, -model.sizeY() / 2F, -model.sizeZ() / 2F);
        for (var cell : model.cells()) {
            if (!visible(cell) || cell.state().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) continue;
            pose.pushPose();
            pose.translate(cell.localPos().getX(), cell.localPos().getY(), cell.localPos().getZ());
            mc.getBlockRenderer().renderSingleBlock(cell.state(), pose, buffers, LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            pose.popPose();
        }
        buffers.endBatch();
        if (selectedCell != null && selectedCell != hoveredCell && visible(selectedCell)) {
            renderGlowCube(pose, buffers, selectedCell, SELECTED_GLOW_ALPHA);
        }
        if (hoveredCell != null && visible(hoveredCell)) {
            renderGlowCube(pose, buffers, hoveredCell, HOVERED_GLOW_ALPHA);
        }
        pose.popPose();
        buffers.endBatch(OverlayRenderType.getBlockHilightFace());
        Lighting.setupFor3DItems();
        g.disableScissor();
    }

    private static void renderGlowCube(PoseStack pose, MultiBufferSource buffers,
            StructurePreviewModel.Cell cell, int alpha) {
        VertexConsumer consumer = buffers.getBuffer(OverlayRenderType.getBlockHilightFace());
        pose.pushPose();
        pose.translate(cell.localPos().getX(), cell.localPos().getY(), cell.localPos().getZ());
        Matrix4f matrix = pose.last().pose();
        float low = -.025F, high = 1.025F;
        glowQuad(consumer, matrix, alpha,
                low, low, low, high, low, low, high, low, high, low, low, high, 0, -1, 0);
        glowQuad(consumer, matrix, alpha,
                low, high, high, high, high, high, high, high, low, low, high, low, 0, 1, 0);
        glowQuad(consumer, matrix, alpha,
                low, low, low, low, high, low, high, high, low, high, low, low, 0, 0, -1);
        glowQuad(consumer, matrix, alpha,
                high, low, high, high, high, high, low, high, high, low, low, high, 0, 0, 1);
        glowQuad(consumer, matrix, alpha,
                low, low, high, low, high, high, low, high, low, low, low, low, -1, 0, 0);
        glowQuad(consumer, matrix, alpha,
                high, low, low, high, high, low, high, high, high, high, low, high, 1, 0, 0);
        pose.popPose();
    }

    private static void glowQuad(VertexConsumer consumer, Matrix4f matrix, int alpha,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            float nx, float ny, float nz) {
        glowVertex(consumer, matrix, x1, y1, z1, nx, ny, nz, alpha);
        glowVertex(consumer, matrix, x2, y2, z2, nx, ny, nz, alpha);
        glowVertex(consumer, matrix, x3, y3, z3, nx, ny, nz, alpha);
        glowVertex(consumer, matrix, x4, y4, z4, nx, ny, nz, alpha);
    }

    private static void glowVertex(VertexConsumer consumer, Matrix4f matrix,
            float x, float y, float z, float nx, float ny, float nz, int alpha) {
        consumer.addVertex(matrix, x, y, z).setColor(90, 190, 255, alpha).setNormal(nx, ny, nz);
    }

    private void drawPanel(GuiGraphics g, Font f, double mx, double my) {
        g.fill(panelX, VIEW_Y, panelX + PANEL_W, VIEW_Y + panelHeight, 0xFF181818);
        g.renderOutline(panelX, VIEW_Y, PANEL_W, panelHeight, BORDER);
        textButton(g, f, panelX + 1, VIEW_Y + 1, 51, TAB_H, "Blocks", panelTab == PanelTab.BLOCKS, mx, my);
        textButton(g, f, panelX + 52, VIEW_Y + 1, 51, TAB_H, "Details", panelTab == PanelTab.DETAILS, mx, my);
        if (panelTab == PanelTab.BLOCKS) drawMaterials(g, f, mx, my); else drawDetails(g, f);
    }

    private void drawMaterials(GuiGraphics g, Font f, double mx, double my) {
        int top = contentTop(), bottom = contentBottom();
        enableLocalScissor(g, panelX + 1, top, panelX + PANEL_W - 1, bottom);
        for (int i = 0; i < model.materials().size(); i++) {
            int y = top + i * ROW_H - materialScroll;
            if (y + ROW_H <= top || y >= bottom) continue;
            boolean hover = inside(panelX + 1, y, PANEL_W - 2, ROW_H, mx, my);
            g.fill(panelX + 1, y, panelX + PANEL_W - 1, y + ROW_H,
                    hover ? 0xFF353535 : (i % 2 == 0 ? 0xFF202020 : 0xFF1C1C1C));
            var material = model.materials().get(i);
            g.renderItem(material.stack(), panelX + 4, y + 4);
            drawTrimmed(g, f, material.stack().getHoverName().getString(), panelX + 27, y + 3, PANEL_W - 34, TEXT);
            drawTrimmed(g, f, "x" + material.count(), panelX + 27, y + 14, PANEL_W - 34, MUTED);
        }
        g.disableScissor();
        drawScrollbar(g);
    }

    private void drawDetails(GuiGraphics g, Font f) {
        int top = contentTop();
        if (selectedCell == null) {
            int y = top + 5;
            for (var line : f.split(Component.literal("Click a block in the preview to inspect its role and valid alternatives."), PANEL_W - 10)) {
                g.drawString(f, line, panelX + 5, y, MUTED, false);
                y += f.lineHeight + 2;
            }
            return;
        }
        drawItemSlot(g, selectedCell.state().getBlock().asItem().getDefaultInstance(), panelX + 4, top + 2);
        drawTrimmed(g, f, selectedCell.state().getBlock().getName().getString(), panelX + 25, top + 3, PANEL_W - 27, TEXT);
        drawTrimmed(g, f, "Role: " + selectedCell.role().getString(), panelX + 4, top + 24, PANEL_W - 8, MUTED);
        var p = selectedCell.localPos();
        drawTrimmed(g, f, "Position: " + p.getX() + ", " + p.getY() + ", " + p.getZ(), panelX + 4, top + 35, PANEL_W - 8, MUTED);
        g.drawString(f, "Alternatives", panelX + 4, top + 48, TEXT, false);
        List<BlockState> alternatives = visibleAlternatives();
        boolean allowsAir = selectedCell.alternatives().stream().anyMatch(BlockState::isAir);
        if (alternatives.size() == 1 && !allowsAir) {
            g.drawString(f, "Fixed block", panelX + 4, top + 62, MUTED, false);
        } else {
            for (int i = 0; i < Math.min(8, alternatives.size()); i++) {
                drawItemSlot(g, alternatives.get(i).getBlock().asItem().getDefaultInstance(),
                        panelX + 4 + i % 4 * 23, top + 63 + i / 4 * 21);
            }
            if (allowsAir) {
                int index = Math.min(8, alternatives.size());
                int x = panelX + 4 + index % 4 * 23, y = top + 63 + index / 4 * 21;
                g.fill(x, y, x + 18, y + 18, 0xFF262626);
                g.renderOutline(x, y, 18, 18, BORDER);
                drawCentered(g, f, "X", x, y + (18 - f.lineHeight) / 2 + 1, 18, MUTED);
            }
        }
    }

    private static void drawItemSlot(GuiGraphics g, net.minecraft.world.item.ItemStack stack, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF303030);
        g.renderOutline(x, y, 18, 18, BORDER);
        if (!stack.isEmpty()) g.renderItem(stack, x + 1, y + 1);
    }

    private void drawScrollbar(GuiGraphics g) {
        int max = maxMaterialScroll();
        if (max <= 0) return;
        int trackTop = contentTop() + 1, trackHeight = contentBottom() - contentTop() - 2;
        int thumbHeight = Math.max(12, trackHeight * (contentBottom() - contentTop()) / (model.materials().size() * ROW_H));
        int thumbY = trackTop + Math.round((float) materialScroll / max * (trackHeight - thumbHeight));
        int x = panelX + PANEL_W - 4;
        g.fill(x, trackTop, x + 2, trackTop + trackHeight, 0xFF303030);
        g.fill(x, thumbY, x + 2, thumbY + thumbHeight, 0xFFB0B0B0);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mx, double my) {
        String control = controlTooltip(mx, my);
        if (control != null) { tooltip.add(Component.literal(control)); return; }
        if (panelTab == PanelTab.DETAILS && selectedCell != null) {
            int top = contentTop();
            if (inside(panelX + 4, top + 2, 18, 18, mx, my)) {
                tooltip.add(selectedCell.state().getBlock().getName());
                return;
            }
            List<BlockState> alternatives = visibleAlternatives();
            boolean allowsAir = selectedCell.alternatives().stream().anyMatch(BlockState::isAir);
            if (alternatives.size() != 1 || allowsAir) {
                for (int i = 0; i < Math.min(8, alternatives.size()); i++) {
                    if (inside(panelX + 4 + i % 4 * 23, top + 63 + i / 4 * 21, 18, 18, mx, my)) {
                        tooltip.add(alternatives.get(i).getBlock().getName());
                        return;
                    }
                }
            }
        }
        if (inside(VIEW_X, VIEW_Y, viewWidth, viewHeight, mx, my)) {
            var cell = pickCell(mx, my);
            if (cell != null) {
                tooltip.add(cell.state().getBlock().getName());
                tooltip.add(Component.literal("Role: " + cell.role().getString()));
                tooltip.add(Component.literal("Click for details"));
            }
        }
    }

    @Override
    public boolean handleInput(double mx, double my, IJeiUserInput input) {
        InputConstants.Key key = input.getKey();
        if (key.getType() != InputConstants.Type.MOUSE || (key.getValue() != 0 && key.getValue() != 2)) return false;
        boolean handled = clickable(mx, my, key.getValue());
        if (!handled || input.isSimulate()) return handled;
        if (key.getValue() == 0 && handleToolbar(mx, my)) return true;
        if (key.getValue() == 0 && inside(panelX + 1, VIEW_Y + 1, 51, TAB_H, mx, my)) {
            panelTab = PanelTab.BLOCKS; clickSound(); return true;
        }
        if (key.getValue() == 0 && inside(panelX + 52, VIEW_Y + 1, 51, TAB_H, mx, my)) {
            panelTab = PanelTab.DETAILS; clickSound(); return true;
        }
        if (inside(VIEW_X, VIEW_Y, viewWidth, viewHeight, mx, my)) {
            autoRotate = false;
            if (key.getValue() == 0) {
                selectedCell = pickCell(mx, my);
                if (selectedCell != null) panelTab = PanelTab.DETAILS;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleMouseDragged(double mx, double my, InputConstants.Key key, double dx, double dy) {
        if (key.getType() != InputConstants.Type.MOUSE || !inside(VIEW_X, VIEW_Y, viewWidth, viewHeight, mx, my)) return false;
        if (key.getValue() == 2 || (key.getValue() == 0 && Screen.hasShiftDown())) {
            panX = Mth.clamp(panX + (float) dx, -viewWidth * .65F, viewWidth * .65F);
            panY = Mth.clamp(panY + (float) dy, -viewHeight * .65F, viewHeight * .65F);
        } else if (key.getValue() == 0) {
            yaw = wrap(yaw + (float) dx * .8F);
            pitch = Mth.clamp(pitch + (float) dy * .8F, -85, 85);
        } else return false;
        autoRotate = false;
        return true;
    }

    @Override
    public boolean handleMouseScrolled(double mx, double my, double sx, double sy) {
        if (inside(VIEW_X, VIEW_Y, viewWidth, viewHeight, mx, my)) {
            float old = zoom;
            zoom = Mth.clamp((float) (zoom * Math.exp(sy * .12D)), .35F, 3.5F);
            float ratio = zoom / old;
            panX = (float) mx - viewCenterX() - ratio * ((float) mx - viewCenterX() - panX);
            panY = (float) my - viewCenterY() - ratio * ((float) my - viewCenterY() - panY);
            autoRotate = false;
            return true;
        }
        if (panelTab == PanelTab.BLOCKS && inside(panelX, contentTop(), PANEL_W, contentBottom() - contentTop(), mx, my)) {
            materialScroll = Mth.clamp(materialScroll - (int) Math.round(sy * 14), 0, maxMaterialScroll());
            return true;
        }
        return false;
    }

    private boolean handleToolbar(double mx, double my) {
        if (inside(2, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) { reset(); clickSound(); return true; }
        if (inside(20, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) { autoRotate = !autoRotate; clickSound(); return true; }
        if (inside(38, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) { hideShell = !hideShell; clickSound(); return true; }
        if (inside(56, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) { layerMode = nextLayerMode(); clickSound(); return true; }
        if (inside(76, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) { layer = Math.max(0, layer - 1); clickSound(); return true; }
        if (inside(145, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) { layer = Math.min(model.sizeY() - 1, layer + 1); clickSound(); return true; }
        return false;
    }

    private boolean clickable(double mx, double my, int button) {
        if (inside(VIEW_X, VIEW_Y, viewWidth, viewHeight, mx, my)) return true;
        if (button != 0) return false;
        return inside(2, TOOLBAR_Y, 159, TOOLBAR_H, mx, my)
                || inside(panelX + 1, VIEW_Y + 1, PANEL_W - 2, TAB_H, mx, my);
    }

    private StructurePreviewModel.Cell pickCell(double mx, double my) {
        StructurePreviewModel.Cell closest = null;
        float depth = Float.NEGATIVE_INFINITY;
        Matrix4f transform = modelTransform();
        for (var cell : model.cells()) {
            if (!visible(cell)) continue;
            float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
            var p = cell.localPos();
            for (int dx = 0; dx <= 1; dx++) for (int dy = 0; dy <= 1; dy++) for (int dz = 0; dz <= 1; dz++) {
                Vector3f point = transform.transformPosition(new Vector3f(p.getX() + dx, p.getY() + dy, p.getZ() + dz));
                minX = Math.min(minX, point.x); minY = Math.min(minY, point.y);
                maxX = Math.max(maxX, point.x); maxY = Math.max(maxY, point.y);
            }
            Vector3f center = transform.transformPosition(new Vector3f(p.getX() + .5F, p.getY() + .5F, p.getZ() + .5F));
            float padding = Math.max(1, renderScale() * .08F);
            if (mx >= minX - padding && mx <= maxX + padding && my >= minY - padding && my <= maxY + padding && center.z > depth) {
                closest = cell; depth = center.z;
            }
        }
        return closest;
    }

    private Matrix4f modelTransform() {
        return new Matrix4f().translation(viewCenterX() + panX, viewCenterY() + panY, 80)
                .scale(renderScale(), -renderScale(), renderScale() * DEPTH_SCALE)
                .rotateX((float) Math.toRadians(pitch)).rotateY((float) Math.toRadians(yaw))
                .translate(-model.sizeX() / 2F, -model.sizeY() / 2F, -model.sizeZ() / 2F);
    }

    private boolean visible(StructurePreviewModel.Cell cell) {
        return !cell.state().isAir() && !(hideShell && cell.shell())
                && StructurePreviewLayout.isVisible(cell.localPos().getY(), layer, layerMode);
    }

    private List<BlockState> visibleAlternatives() {
        if (selectedCell == null) return List.of();
        return selectedCell.alternatives().stream().filter(state -> !state.isAir()).toList();
    }

    private StructurePreviewLayout.LayerMode nextLayerMode() {
        var values = StructurePreviewLayout.LayerMode.values();
        return values[(layerMode.ordinal() + 1) % values.length];
    }

    private Icon layerIcon() {
        return switch (layerMode) {
            case FULL -> Icon.LAYERS_FULL;
            case UP_TO -> Icon.LAYERS_UP_TO;
            case SINGLE -> Icon.LAYERS_SINGLE;
        };
    }

    private String controlTooltip(double mx, double my) {
        if (inside(2, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) return "Reset view";
        if (inside(20, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) return "Toggle automatic rotation";
        if (inside(38, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) return "Show or hide the structural shell";
        if (inside(56, TOOLBAR_Y, 16, TOOLBAR_H, mx, my)) return "Cycle layer display mode";
        if (inside(76, TOOLBAR_Y, 85, TOOLBAR_H, mx, my)) return "Select vertical layer";
        return null;
    }

    private int contentTop() { return VIEW_Y + TAB_H + 2; }
    private int contentBottom() { return VIEW_Y + panelHeight - 1; }
    private int maxMaterialScroll() { return Math.max(0, model.materials().size() * ROW_H - (contentBottom() - contentTop())); }
    private float renderScale() { return baseScale * zoom; }
    private float viewCenterX() { return VIEW_X + viewWidth / 2F; }
    private float viewCenterY() { return VIEW_Y + viewHeight / 2F; }
    private void reset() { yaw = DEFAULT_YAW; pitch = DEFAULT_PITCH; zoom = 1; panX = panY = 0; }
    private static float wrap(float value) { value %= 360; return value < 0 ? value + 360 : value; }
    private static void clickSound() { Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1)); }

    private static void iconButton(GuiGraphics g, int x, int y, Icon icon, boolean pressed, boolean enabled, double mx, double my) {
        boolean hovered = inside(x, y, 16, 16, mx, my);
        buttonFrame(g, x, y, 16, 16, pressed, enabled, hovered);
        int color = !enabled ? 0xFFA0A0A0 : hovered ? 0xFFFFFFFF : 0xFFE0E0E0;
        double pressOffset = pressed ? .5D : 0D;
        double startX = x + (16 - icon.inkWidth) / 2D - icon.minX + pressOffset;
        double startY = y + (16 - icon.inkHeight) / 2D - icon.minY + pressOffset;
        var pose = g.pose();
        pose.pushPose();
        pose.translate(startX, startY, 0);
        for (int row = 0; row < icon.pixels.length; row++) {
            String pixels = icon.pixels[row];
            int col = 0;
            while (col < pixels.length()) {
                int runStart = pixels.indexOf('#', col);
                if (runStart < 0) break;
                int runEnd = runStart + 1;
                while (runEnd < pixels.length() && pixels.charAt(runEnd) == '#') runEnd++;
                g.fill(runStart, row, runEnd, row + 1, color);
                col = runEnd;
            }
        }
        pose.popPose();
    }

    private static void textButton(GuiGraphics g, Font f, int x, int y, int w, int h, String label, boolean pressed, double mx, double my) {
        buttonFrame(g, x, y, w, h, pressed, true, inside(x, y, w, h, mx, my));
        drawCentered(g, f, label, x, y + (h - f.lineHeight) / 2 + 1, w, TEXT);
    }

    private static void insetLabel(GuiGraphics g, Font f, int x, int y, int w, int h, String label) {
        g.fill(x, y, x + w, y + h, 0xFF000000);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF303030);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFF555555);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, 0xFF555555);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFFAAAAAA);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0xFFAAAAAA);
        drawCentered(g, f, label, x, y + (h - f.lineHeight) / 2 + 1, w, 0xFFE0E0E0);
    }

    private static void buttonFrame(GuiGraphics g, int x, int y, int w, int h, boolean pressed, boolean enabled, boolean hover) {
        int face = enabled ? (pressed ? 0xFF6D6D6D : 0xFF6E6E6E) : 0xFF2B2B2B;
        g.fill(x, y, x + w, y + h, hover ? 0xFFFFFFFF : 0xFF000000);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, face);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, pressed ? 0xFF555555 : 0xFFAAAAAA);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, pressed ? 0xFF555555 : 0xFFAAAAAA);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFF555555);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0xFF555555);
    }

    private static void drawCentered(GuiGraphics g, Font f, String text, int x, int y, int w, int color) {
        String value = f.plainSubstrByWidth(text, Math.max(0, w - 4));
        g.drawString(f, value, x + (w - f.width(value)) / 2, y, color, false);
    }

    private static void drawTrimmed(GuiGraphics g, Font f, String text, int x, int y, int max, int color) {
        String value = f.width(text) > max ? f.plainSubstrByWidth(text, Math.max(0, max - f.width("..."))) + "..." : text;
        g.drawString(f, value, x, y, color, false);
    }

    private static void enableLocalScissor(GuiGraphics g, int left, int top, int right, int bottom) {
        Matrix4f pose = g.pose().last().pose();
        Vector3f first = pose.transformPosition(new Vector3f(left, top, 0));
        Vector3f second = pose.transformPosition(new Vector3f(right, bottom, 0));
        g.enableScissor((int) Math.floor(Math.min(first.x, second.x)), (int) Math.floor(Math.min(first.y, second.y)),
                (int) Math.ceil(Math.max(first.x, second.x)), (int) Math.ceil(Math.max(first.y, second.y)));
    }

    private static boolean inside(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private enum PanelTab { BLOCKS, DETAILS }
    private enum Icon {
        RESET("...###...", "...#.#...", "...#.#...", "###...###", "#...#...#", "###...###", "...#.#...", "...#.#...", "...###..."),
        PLAY("..#......", "..##.....", "..###....", "..####...", "..#####..", "..####...", "..###....", "..##.....", "..#......"),
        PAUSE(".........", "..##.##..", "..##.##..", "..##.##..", "..##.##..", "..##.##..", "..##.##..", "..##.##..", "........."),
        SHELL("...###...", ".##...##.", "#.#....#.", "#..#...#.", "#...#..#.", "#....#.#.", ".##...##.", "...###...", "........."),
        SHELL_HIDDEN("...###..#", ".##...###", "#.#...##.", "#..#.##..", "#...##.#.", "#..##..#.", ".###..##.", ".####....", "#........"),
        LAYERS_FULL(".#######.", "..#####..", ".........", ".#######.", "..#####..", ".........", ".#######.", "..#####..", "........."),
        LAYERS_UP_TO(".........", ".........", ".........", ".#######.", "..#####..", ".........", ".#######.", "..#####..", "........."),
        LAYERS_SINGLE(".........", ".........", ".........", ".#######.", "..#####..", ".........", ".........", ".........", "........."),
        MINUS(".........", ".........", ".........", ".........", "..#####..", ".........", ".........", ".........", "........."),
        PLUS(".........", "....#....", "....#....", "....#....", "..#####..", "....#....", "....#....", "....#....", ".........");
        final String[] pixels;
        final int minX, minY, inkWidth, inkHeight;

        Icon(String... pixels) {
            this.pixels = pixels;
            int left = Integer.MAX_VALUE, right = Integer.MIN_VALUE;
            int top = Integer.MAX_VALUE, bottom = Integer.MIN_VALUE;
            for (int row = 0; row < pixels.length; row++) {
                for (int col = 0; col < pixels[row].length(); col++) {
                    if (pixels[row].charAt(col) != '#') continue;
                    left = Math.min(left, col);
                    right = Math.max(right, col);
                    top = Math.min(top, row);
                    bottom = Math.max(bottom, row);
                }
            }
            this.minX = left;
            this.minY = top;
            this.inkWidth = right - left + 1;
            this.inkHeight = bottom - top + 1;
        }
    }
}

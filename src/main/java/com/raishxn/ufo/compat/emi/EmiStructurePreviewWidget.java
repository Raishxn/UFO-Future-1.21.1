package com.raishxn.ufo.compat.emi;

import com.mojang.blaze3d.platform.InputConstants;
import com.raishxn.ufo.client.preview.StructurePreviewModel;
import com.raishxn.ufo.compat.jei.StructurePreviewWidget;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import java.util.List;

/** Input-capable adapter for JEMI, whose extras bridge only draws JEI widgets. */
public final class EmiStructurePreviewWidget extends Widget {
    private final Bounds bounds;
    private final StructurePreviewWidget preview;
    private long lastTick = Long.MIN_VALUE;

    public EmiStructurePreviewWidget(StructurePreviewModel model, int width, int height) {
        bounds = new Bounds(0, 0, width, height);
        preview = new StructurePreviewWidget(model, width, height);
    }

    @Override public Bounds getBounds() { return bounds; }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        long tick = net.minecraft.Util.getMillis() / 50;
        if (tick != lastTick) { preview.tick(); lastTick = tick; }
        preview.drawWidget(graphics, mouseX, mouseY);
    }
    @Override public boolean mouseClicked(int x, int y, int button) {
        return preview.handleClick(x, y, InputConstants.Type.MOUSE.getOrCreate(button), false);
    }
    public boolean drag(double x, double y, int button, double dx, double dy) {
        return preview.handleMouseDragged(x, y, InputConstants.Type.MOUSE.getOrCreate(button), dx, dy);
    }
    public boolean scroll(double x, double y, double sx, double sy) {
        return preview.handleMouseScrolled(x, y, sx, sy);
    }
    @Override public List<ClientTooltipComponent> getTooltip(int x, int y) {
        return preview.getTooltipLines(x, y).stream()
                .map(line -> ClientTooltipComponent.create(line.getVisualOrderText())).toList();
    }
}

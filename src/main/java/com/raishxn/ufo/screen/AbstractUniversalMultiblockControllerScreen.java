package com.raishxn.ufo.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.block.entity.UniversalDisplayedRecipe;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketToggleUniversalOverclock;
import com.raishxn.ufo.network.packet.PacketToggleUniversalSafeMode;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public abstract class AbstractUniversalMultiblockControllerScreen<M extends AbstractUniversalMultiblockControllerMenu<?>>
        extends UpgradeableScreen<M> {

    private Button safeModeButton;
    private Button overclockButton;

    protected AbstractUniversalMultiblockControllerScreen(M menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.safeModeButton = this.addRenderableWidget(Button.builder(Component.literal("⚗"), btn ->
                        ModPackets.sendToServer(new PacketToggleUniversalSafeMode(this.menu.getBlockEntity().getBlockPos())))
                .bounds(this.leftPos + this.imageWidth + 4, this.topPos + 24, 20, 20)
                .tooltip(Tooltip.create(Component.literal("Toggle Safe Mode")))
                .build());

        this.overclockButton = this.addRenderableWidget(Button.builder(Component.literal("⚡"), btn ->
                        ModPackets.sendToServer(new PacketToggleUniversalOverclock(this.menu.getBlockEntity().getBlockPos())))
                .bounds(this.leftPos + this.imageWidth + 4, this.topPos + 48, 20, 20)
                .tooltip(Tooltip.create(Component.literal("Toggle Overclock")))
                .build());
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTick) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTick);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        renderTemperatureBar(guiGraphics);
        renderRecipeList(guiGraphics);
    }

    private void renderTemperatureBar(GuiGraphics guiGraphics) {
        int barX = this.leftPos + 14;
        int barY = this.topPos + 9;
        int barWidth = 146;
        int barHeight = 10;
        int filled = (int) (barWidth * Math.min(1.0F, this.menu.getTemperature() / (float) this.menu.getMaxTemperature()));

        if (filled > 0) {
            guiGraphics.fill(barX, barY, barX + filled, barY + barHeight, 0xCCB32020);
        }
    }

    private void renderRecipeList(GuiGraphics guiGraphics) {
        int listX = this.leftPos + 7;
        int listY = this.topPos + 30;
        int lineHeight = 10;
        int maxTextWidth = 156;

        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(getScreenTitle().getString(), maxTextWidth), listX, listY, 0xF0F0F0, false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(buildStatusLine(), maxTextWidth), listX, listY + 10, 0xD0D7E6, false);

        List<UniversalDisplayedRecipe> recipes = this.menu.getDisplayedRecipes();
        for (int i = 0; i < 4; i++) {
            int rowY = listY + 22 + i * lineHeight;
            if (i < recipes.size()) {
                renderRecipeRow(guiGraphics, recipes.get(i), listX, rowY);
            } else if (i == 0 && recipes.isEmpty()) {
                guiGraphics.drawString(this.font, this.menu.isAssembled() ? "No active recipes." : "Structure incomplete.", listX, rowY, 0x8A91A6, false);
            }
        }
    }

    private void renderRecipeRow(GuiGraphics guiGraphics, UniversalDisplayedRecipe recipe, int x, int y) {
        ItemStack iconStack = recipe.itemIcon();
        if (iconStack.isEmpty()) {
            FluidStack fluid = recipe.fluidIcon();
            if (!fluid.isEmpty()) {
                iconStack = new ItemStack(fluid.getFluid().getBucket());
            }
        }

        int textX = x;
        if (!iconStack.isEmpty()) {
            guiGraphics.renderItem(iconStack, x, y - 3);
            textX += 18;
        }

        String amount = formatAmount(recipe.outputAmount());
        String time = recipe.maxProgress() > 0 ? recipe.progress() + "/" + recipe.maxProgress() : "-/-";
        String line = recipe.label().getString() + " x" + amount + " " + time;
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(line, 142), textX, y, 0xE6EBF5, false);
    }

    private String buildStatusLine() {
        if (!this.menu.isAssembled()) {
            return "Status: Incomplete";
        }
        StringBuilder builder = new StringBuilder(this.menu.isRunning() ? "Status: Running" : "Status: Idle");
        builder.append(" | ");
        builder.append(this.menu.isSafeMode() ? "Safe ON" : "Safe OFF");
        builder.append(" | ");
        builder.append(this.menu.isOverclocked() ? "OC ON" : "OC OFF");
        return builder.toString();
    }

    private Component getScreenTitle() {
        String raw = this.title.getString();
        if (raw.endsWith(" Controller")) {
            raw = raw.substring(0, raw.length() - " Controller".length());
        }
        return Component.literal(raw);
    }

    private static String formatAmount(long amount) {
        if (amount >= 1_000_000_000L) {
            return String.format("%.1fB", amount / 1_000_000_000.0);
        }
        if (amount >= 1_000_000L) {
            return String.format("%.1fM", amount / 1_000_000.0);
        }
        if (amount >= 1_000L) {
            return String.format("%.1fK", amount / 1_000.0);
        }
        return Long.toString(amount);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.safeModeButton != null) {
            boolean safe = this.menu.isSafeMode();
            this.safeModeButton.setMessage(Component.literal(safe ? "⚗" : "☣"));
            this.safeModeButton.setTooltip(Tooltip.create(Component.literal(safe ? "Safe Mode enabled" : "Safe Mode disabled")));
        }
        if (this.overclockButton != null) {
            boolean oc = this.menu.isOverclocked();
            this.overclockButton.setMessage(Component.literal(oc ? "⚡" : "⏩"));
            this.overclockButton.setTooltip(Tooltip.create(Component.literal(oc ? "Overclock enabled" : "Overclock disabled")));
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(14, 9, 146, 10, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal("Temperature: " + this.menu.getTemperature() + " / " + this.menu.getMaxTemperature()),
                    mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

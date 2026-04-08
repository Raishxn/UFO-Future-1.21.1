package com.raishxn.ufo.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class QuantumProcessorAssemblerControllerScreen extends AbstractContainerScreen<QuantumProcessorAssemblerControllerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/quantum_processor_assembler_gui.png");

    public QuantumProcessorAssemblerControllerScreen(QuantumProcessorAssemblerControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.isAssembled()) {
            guiGraphics.drawString(this.font, menu.isRunning() ? "Status: Running" : "Status: Idle", x + 10, y + 25,
                    menu.isRunning() ? 0x00FF00 : 0x555555);
            guiGraphics.drawString(this.font,
                    "Progress: " + menu.getProgress() + " / " + menu.getMaxProgress(),
                    x + 10, y + 40, 0x404040);
        } else {
            guiGraphics.drawString(this.font, "Structure: Incomplete", x + 10, y + 25, 0xFF0000);
            guiGraphics.drawString(this.font, "Build exact 5x5x5 structure", x + 10, y + 35, 0x404040);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

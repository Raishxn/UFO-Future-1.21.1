package com.raishxn.ufo.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class QmfControllerScreen extends AbstractContainerScreen<QmfControllerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/quantum_matter_fabricator_gui.png");

    public QmfControllerScreen(QmfControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.isAssembled()) {
            if (menu.isRunning()) {
                guiGraphics.drawString(this.font, "Status: Running", x + 10, y + 25, 0x00FF00);
            } else {
                guiGraphics.drawString(this.font, "Status: Idle", x + 10, y + 25, 0x555555);
            }
            int progress = menu.getProgress();
            int maxProgress = menu.getMaxProgress();
            
            guiGraphics.drawString(this.font, "Progress: " + progress + " / " + maxProgress, x + 10, y + 40, 0x404040);
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

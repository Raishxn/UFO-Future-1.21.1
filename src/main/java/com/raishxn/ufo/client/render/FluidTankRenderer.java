package com.raishxn.ufo.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Matrix4f;

// Utilitário para desenhar Fluidos em GUIs
public class FluidTankRenderer {
    private final long capacity;
    private final int width;
    private final int height;

    public FluidTankRenderer(long capacity, int width, int height) {
        this.capacity = capacity;
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphics guiGraphics, int x, int y, FluidStack fluidStack) {
        if (fluidStack.isEmpty()) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        int fluidHeight = (int) (height * ((float) fluidStack.getAmount() / capacity));
        renderTiledFluidTexture(guiGraphics, x, y + height - fluidHeight, width, fluidHeight, fluidStack);
    }

    private void renderTiledFluidTexture(GuiGraphics guiGraphics, int x, int y, int width, int height, FluidStack fluidStack) {
        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation fluidStill = fluidExtensions.getStillTexture(fluidStack);
        if (fluidStill == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        int color = fluidExtensions.getTintColor(fluidStack);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        RenderSystem.setShaderColor(r, g, b, a);

        int xTileCount = width / 16;
        int xRemainder = width % 16;
        int yTileCount = height / 16;
        int yRemainder = height % 16;

        int yStart = y + height;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width_t = (xTile == xTileCount) ? xRemainder : 16;
                int height_t = (yTile == yTileCount) ? yRemainder : 16;
                int x_t = x + (xTile * 16);
                int y_t = yStart - ((yTile + 1) * 16);

                if (width_t > 0 && height_t > 0) {
                    int maskTop = 16 - height_t;
                    int maskRight = 16 - width_t;

                    drawTexture(guiGraphics, x_t, y_t + maskTop, sprite, maskRight, maskTop, 100);
                }
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawTexture(GuiGraphics guiGraphics, int x, int y, TextureAtlasSprite sprite, int maskRight, int maskTop, int z) {
        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        uMax = uMax - (maskRight / 16f * (uMax - uMin));
        vMax = vMax - (maskTop / 16f * (vMax - vMin));

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f model = guiGraphics.pose().last().pose();

        buffer.addVertex(model, x, y + 16 - maskTop, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(uMin, vMax);
        buffer.addVertex(model, x + 16 - maskRight, y + 16 - maskTop, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(uMax, vMax);
        buffer.addVertex(model, x + 16 - maskRight, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(uMax, vMin);
        buffer.addVertex(model, x, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(uMin, vMin);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
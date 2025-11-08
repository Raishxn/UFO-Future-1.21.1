package com.raishxn.ufo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.client.render.FluidTankRenderer;
import com.raishxn.ufo.menu.DimensionalMatterAssemblerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class DimensionalMatterAssemblerScreen extends AbstractContainerScreen<DimensionalMatterAssemblerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/dimensional_matter_assembler.png");
    // Textura para os botões laterais (abas)
    private static final ResourceLocation CONFIG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/dma_faceconfig_gui.png");

    private FluidTankRenderer coolantRenderer;
    private FluidTankRenderer inputFluidRenderer;
    private FluidTankRenderer outputFluidRenderer1;
    private FluidTankRenderer outputFluidRenderer2;

    public DimensionalMatterAssemblerScreen(DimensionalMatterAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 183;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 7;
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();
        coolantRenderer = new FluidTankRenderer(16000, 13, 57);
        inputFluidRenderer = new FluidTankRenderer(16000, 15, 57);
        outputFluidRenderer1 = new FluidTankRenderer(16000, 18, 20);
        outputFluidRenderer2 = new FluidTankRenderer(16000, 18, 20);

        // === Botão para abrir a Configuração de Faces ===
        // Posicionado à esquerda da GUI principal (leftPos - 17)
        this.addRenderableWidget(new SideTabButton(this.leftPos - 17, this.topPos + 5, 16, 19,
                Component.literal("Side Config"),
                b -> Minecraft.getInstance().setScreen(new DimensionalMatterAssemblerConfigScreen(this.menu.blockEntity, this)),
                1, 1)); // Coordenadas U,V na textura dma_faceconfig_gui.png

        // === Botão de Upgrades (Exemplo futuro) ===
        // Posicionado à direita (leftPos + imageWidth)
        // this.addRenderableWidget(new SideTabButton(this.leftPos + this.imageWidth, this.topPos + 5, 16, 19, ...));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        coolantRenderer.render(guiGraphics, x + 4, y + 19, menu.getCoolantStack());
        inputFluidRenderer.render(guiGraphics, x + 23, y + 19, menu.getInputFluidStack());
        outputFluidRenderer1.render(guiGraphics, x + 114, y + 74, menu.getOutputFluid1Stack());
        outputFluidRenderer2.render(guiGraphics, x + 143, y + 74, menu.getOutputFluid2Stack());

        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        int energyBarH = 17;
        int scaledEnergyHeight = (maxEnergy > 0 && energy > 0) ? (int)((long)energy * energyBarH / maxEnergy) : 0;
        if (scaledEnergyHeight > 0) {
            guiGraphics.blit(TEXTURE, x + 152, y + 34 + (energyBarH - scaledEnergyHeight), 176, 0 + (energyBarH - scaledEnergyHeight), 5, scaledEnergyHeight);
        }

        int progress = menu.getProgressPercent();
        int progressBarH = 11;
        int scaledProgressWidth = (progress * 20) / 100;
        if (scaledProgressWidth > 0) {
            guiGraphics.blit(TEXTURE, x + 102, y + 42, 176, 19, scaledProgressWidth, progressBarH);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(152, 34, 5, 17, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE").withStyle(ChatFormatting.AQUA), mouseX, mouseY);
        }
        if (isHovering(102, 42, 20, 11, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal(menu.getProgressPercent() + "%"), mouseX, mouseY);
        }

        if (isHovering(4, 19, 13, 57, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getCoolantStack(), 16000, "Coolant");
        if (isHovering(23, 19, 15, 57, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getInputFluidStack(), 16000, "Input Fluid");
        if (isHovering(114, 74, 18, 20, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getOutputFluid1Stack(), 16000, "Output Fluid 1");
        if (isHovering(143, 74, 18, 20, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getOutputFluid2Stack(), 16000, "Output Fluid 2");
    }

    private void renderFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, FluidStack fluid, int capacity, String defaultName) {
        List<Component> tooltip = new ArrayList<>();
        if (!fluid.isEmpty()) {
            tooltip.add(fluid.getHoverName());
            tooltip.add(Component.literal(String.format("%,d / %,d mB", fluid.getAmount(), capacity)).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal(defaultName + " (Empty)").withStyle(ChatFormatting.GRAY));
        }
        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    // === Classe interna para botões laterais (Abas) ===
    private static class SideTabButton extends Button {
        private final int textureU, textureV;

        public SideTabButton(int x, int y, int width, int height, Component message, OnPress onPress, int u, int v) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.textureU = u;
            this.textureV = v;
            this.setTooltip(Tooltip.create(message));
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, CONFIG_TEXTURE);
            // Renderiza a parte da textura especificada
            guiGraphics.blit(CONFIG_TEXTURE, getX(), getY(), textureU, textureV, width, height);

            // Se estiver com o mouse por cima, pode desenhar um highlight opcional
            if (isHovered) {
                guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0x55FFFFFF);
            }
        }
    }
}
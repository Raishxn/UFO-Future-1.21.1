package com.raishxn.ufo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.client.render.FluidTankRenderer;
import com.raishxn.ufo.menu.DimensionalMatterAssemblerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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

    private FluidTankRenderer coolantRenderer;
    private FluidTankRenderer inputFluidRenderer;
    private FluidTankRenderer outputFluidRenderer1;
    private FluidTankRenderer outputFluidRenderer2;

    public DimensionalMatterAssemblerScreen(DimensionalMatterAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 183; // <--- CORREÇÃO: Altura real da sua textura (0 a 182 = 183 pixels)

        // Ajuste opcional: o rótulo do inventário geralmente fica 94 pixels acima da base.
        // Se sua GUI é mais alta, talvez precise recalcular isso.
        // Tente manter assim por enquanto, ou ajuste se o texto "Inventory" ficar na posição errada.
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
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 1. Fundo Principal
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 2. Renderiza os fluidos (atrás de elementos da GUI se houver transparência)
        coolantRenderer.render(guiGraphics, x + 4, y + 19, menu.getCoolantStack());
        inputFluidRenderer.render(guiGraphics, x + 23, y + 19, menu.getInputFluidStack());
        outputFluidRenderer1.render(guiGraphics, x + 114, y + 74, menu.getOutputFluid1Stack());
        outputFluidRenderer2.render(guiGraphics, x + 143, y + 74, menu.getOutputFluid2Stack());

        // Opcional: Se seus tanques na textura principal forem transparentes,
        // descomente a linha abaixo para desenhar o vidro por cima dos fluidos:
        // guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 3. Barra de Energia (enche de baixo para cima)
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        int energyBarH = 17;
        int scaledEnergyHeight = (maxEnergy > 0 && energy > 0) ? (int)((long)energy * energyBarH / maxEnergy) : 0;
        if (scaledEnergyHeight > 0) {
            guiGraphics.blit(TEXTURE,
                    x + 152, y + 34 + (energyBarH - scaledEnergyHeight),
                    176, 0 + (energyBarH - scaledEnergyHeight),
                    5, scaledEnergyHeight);
        }

        // 4. Barra de Progresso (enche da esquerda para a direita)
        int progress = menu.getProgressPercent();
        int progressBarW = 20;
        int progressBarH = 11;
        int scaledProgressWidth = (progress * progressBarW) / 100;
        if (scaledProgressWidth > 0) {
            // CORREÇÃO APLICADA AQUI: UV começa em 176, 19
            guiGraphics.blit(TEXTURE,
                    x + 102, y + 42,   // Posição na tela
                    176, 19,           // Posição na textura (UV da barra cheia)
                    scaledProgressWidth, progressBarH);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // Tooltips Manuais
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
}
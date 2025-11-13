package com.raishxn.ufo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.client.gui.widget.DMAConfigWidget;
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

import java.text.DecimalFormat; // <-- IMPORTAÇÃO ADICIONADA
import java.util.ArrayList;
import java.util.List;

public class DimensionalMatterAssemblerScreen extends AbstractContainerScreen<DimensionalMatterAssemblerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/dimensional_matter_assembler.png");

    private FluidTankRenderer coolantRenderer;
    private FluidTankRenderer inputFluidRenderer;
    private FluidTankRenderer outputFluidRenderer1;
    private FluidTankRenderer outputFluidRenderer2;

    // O widget de configuração
    private DMAConfigWidget configWidget;

    public DimensionalMatterAssemblerScreen(DimensionalMatterAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // ATUALIZADO: Novas dimensões da UI (0,0 até 193,182)
        this.imageWidth = 194;
        this.imageHeight = 183;

        this.inventoryLabelY = Integer.MAX_VALUE;

        this.titleLabelX = 7;
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // ATUALIZADO: Novas dimensões dos tanques
        // Coolant: 9,21 a 19,73 -> (11 largura, 53 altura)
        coolantRenderer = new FluidTankRenderer(16000, 11, 53);
        // Fluid Input: 28,21 a 38,73 -> (11 largura, 53 altura)
        inputFluidRenderer = new FluidTankRenderer(16000, 11, 53);
        // Fluid Output 1: 119,76 a 132,91 -> (14 largura, 16 altura)
        outputFluidRenderer1 = new FluidTankRenderer(16000, 14, 16);
        // Fluid Output 2: 148,76 a 161,91 -> (14 largura, 16 altura)
        outputFluidRenderer2 = new FluidTankRenderer(16000, 14, 16);

        // O widget de config permanece o mesmo, `leftPos` será atualizado
        this.configWidget = new DMAConfigWidget(this.leftPos - 22, this.topPos + 5, this.menu.blockEntity);
        this.addRenderableWidget(this.configWidget);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // ATUALIZADO: Novas posições de renderização dos tanques
        // Coolant: 9,21
        coolantRenderer.render(guiGraphics, x + 10, y + 22, menu.getCoolantStack());
        // Fluid Input: 28,21
        inputFluidRenderer.render(guiGraphics, x + 29, y + 22, menu.getInputFluidStack());
        // Fluid Output 1: 119,76
        outputFluidRenderer1.render(guiGraphics, x + 120, y + 77, menu.getOutputFluid1Stack());
        // Fluid Output 2: 148,76
        outputFluidRenderer2.render(guiGraphics, x + 149, y + 77, menu.getOutputFluid2Stack());

        // ATUALIZADO: Barra de Energia
        // Posição: 155,34 (Largura 6, Altura 18)
        // UV: 225,0 (Largura 6, Altura 18)
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        int energyBarH = 18; // Nova altura
        int scaledEnergyHeight = (maxEnergy > 0 && energy > 0) ? (int)((long)energy * energyBarH / maxEnergy) : 0;
        if (scaledEnergyHeight > 0) {
            guiGraphics.blit(TEXTURE,
                    x + 155, // Nova Posição X
                    y + 34 + (energyBarH - scaledEnergyHeight), // Nova Posição Y (render de baixo p/ cima)
                    225, // Nova UV X
                    0 + (energyBarH - scaledEnergyHeight), // Nova UV Y (render de baixo p/ cima)
                    6, // Nova Largura
                    scaledEnergyHeight);
        }

        // ATUALIZADO: Barra de Progresso
        // Posição: 105,42 (Largura 21, Altura 12)
        // UV: 234,0 (Largura 21, Altura 12)
        int progress = menu.getProgressPercent();
        int progressBarW = 21; // Nova largura
        int progressBarH = 12; // Nova altura
        int scaledProgressWidth = (progress * progressBarW) / 100; // Usa a nova largura
        if (scaledProgressWidth > 0) {
            guiGraphics.blit(TEXTURE,
                    x + 105, // Nova Posição X
                    y + 42, // Nova Posição Y
                    234, // Nova UV X
                    0, // Nova UV Y
                    scaledProgressWidth,
                    progressBarH); // Nova Altura
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // <-- INÍCIO: RENDERIZAÇÃO DO TEXTO DA TEMPERATURA (MODIFICADO)
        int tempKelvin = menu.getTemperature();

        // 1. Converte de Kelvin para Celsius (K - 273.15)
        double tempCelsius = tempKelvin - 273.15;

        // 2. Formata o valor de Celsius (ex: "1.2 k°C" ou "-253 °C")
        String tempText = formatTemperature(tempCelsius);

        // Coordenadas da "tela": 9,84 (largura 31, altura 9)
        int textX = this.leftPos + 9 + (31 / 2); // Centraliza (9 + 15 = 24)
        int textY = this.topPos + 85;             // Posição Y (84 + 1 pixel padding)
        int color = 0xFFFFFF; // Cor branca

        // Desenha o texto centralizado
        guiGraphics.drawCenteredString(font, tempText, textX, textY, color);
        // <-- FIM: RENDERIZAÇÃO DO TEXTO DA TEMPERATURA

        renderTooltip(guiGraphics, mouseX, mouseY);

        // ATUALIZADO: Tooltips (áreas de hover)
        // Energia: 155,34 (Largura 6, Altura 18)
        if (isHovering(155, 34, 6, 18, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE").withStyle(ChatFormatting.AQUA), mouseX, mouseY);
        }
        // Progresso: 105,42 (Largura 21, Altura 12)
        if (isHovering(105, 42, 21, 12, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.literal(menu.getProgressPercent() + "%"), mouseX, mouseY);
        }

        // ATUALIZADO: Tooltips dos tanques (áreas de hover)
        // Coolant: 9,21 (Largura 11, Altura 53)
        if (isHovering(9, 21, 11, 53, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getCoolantStack(), 16000, "Coolant");
        // Input Fluid: 28,21 (Largura 11, Altura 53)
        if (isHovering(28, 21, 11, 53, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getInputFluidStack(), 16000, "Input Fluid");
        // Output Fluid 1: 119,76 (Largura 14, Altura 16)
        if (isHovering(119, 76, 14, 16, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getOutputFluid1Stack(), 16000, "Output Fluid 1");
        // Output Fluid 2: 148,76 (Largura 14, Altura 16)
        if (isHovering(148, 76, 14, 16, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getOutputFluid2Stack(), 16000, "Output Fluid 2");

        // <-- MODIFICADO: Tooltip para a 'tela de temperatura' (9, 84, 31, 9)
        if (isHovering(9, 84, 31, 9, mouseX, mouseY)) {
            // Mostra o valor preciso em Celsius e o valor original em Kelvin
            String celsiusStr = String.format("%.2f °C", tempCelsius);
            String kelvinStr = tempKelvin + " K";

            guiGraphics.renderTooltip(font,
                    Component.literal("Temperatura: " + celsiusStr + " (" + kelvinStr + ")").withStyle(ChatFormatting.RED),
                    mouseX, mouseY);
        }
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

    // =================================================================================
    // --- ADICIONADO: MÉTODO PARA FORMATAR TEMPERATURA ---
    // =================================================================================
    /**
     * Formata um valor double em uma string compacta com sufixos (k, M, G, T)
     * e adiciona "°C" no final.
     * Ex: 1227 -> "1.2 k°C"
     * Ex: -253 -> "-253 °C"
     */
    private String formatTemperature(double value) {
        // Se o valor for "pequeno" (entre -1000 e 1000), mostre o número inteiro.
        if (value < 1000 && value > -1000) {
            // Arredonda para o inteiro mais próximo
            return Math.round(value) + " °C";
        }

        // Define os sufixos (k = Kilo/mil, M = Mega/milhão, G = Giga/bilhão, T = Tera/trilhão)
        String[] suffixes = {"", "k", "M", "G", "T"};
        int index = 0;
        double absValue = Math.abs(value);

        // Divide por 1000 até estar abaixo de 1000 ou esgotar os sufixos
        while (absValue >= 1000 && index < suffixes.length - 1) {
            absValue /= 1000.0;
            index++;
        }

        // Restaura o sinal original (negativo ou positivo)
        double formattedValue = (value < 0) ? -absValue : absValue;

        // Formata para ter uma casa decimal (ex: "1.2")
        DecimalFormat df = new DecimalFormat("0.0");

        // Retorna a string final, ex: "-2.5 k°C" ou "1.2 M°C"
        return df.format(formattedValue) + " " + suffixes[index] + "°C";
    }
}
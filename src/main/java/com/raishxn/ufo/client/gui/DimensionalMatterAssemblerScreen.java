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
        this.imageWidth = 176;
        this.imageHeight = 183;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 7;
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // ATUALIZADO: Novas dimensões (largura, altura) corrigidas
        coolantRenderer = new FluidTankRenderer(16000, 10, 52);
        inputFluidRenderer = new FluidTankRenderer(16000, 10, 52);
        outputFluidRenderer1 = new FluidTankRenderer(16000, 13, 15);
        outputFluidRenderer2 = new FluidTankRenderer(16000, 12, 15);

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

        // Posições de início (X, Y) mantiveram-se as mesmas da sua última mensagem,
        // mas garantindo que estão certas aqui:
        coolantRenderer.render(guiGraphics, x + 7, y + 22, menu.getCoolantStack());
        inputFluidRenderer.render(guiGraphics, x + 26, y + 22, menu.getInputFluidStack());
        outputFluidRenderer1.render(guiGraphics, x + 117, y + 77, menu.getOutputFluid1Stack());
        outputFluidRenderer2.render(guiGraphics, x + 146, y + 77, menu.getOutputFluid2Stack());

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

        // ATUALIZADO: Tooltips com as novas dimensões corrigidas
        if (isHovering(7, 22, 10, 52, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getCoolantStack(), 16000, "Coolant");
        if (isHovering(26, 22, 10, 52, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getInputFluidStack(), 16000, "Input Fluid");
        if (isHovering(117, 77, 13, 15, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getOutputFluid1Stack(), 16000, "Output Fluid 1");
        if (isHovering(146, 77, 12, 15, mouseX, mouseY)) renderFluidTooltip(guiGraphics, mouseX, mouseY, menu.getOutputFluid2Stack(), 16000, "Output Fluid 2");
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
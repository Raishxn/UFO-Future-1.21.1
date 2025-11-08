package com.raishxn.ufo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketChangeSideConfig;
import com.raishxn.ufo.network.packet.PacketToggleAutoEject;
import com.raishxn.ufo.util.ConfigType;
import com.raishxn.ufo.util.IOMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DimensionalMatterAssemblerConfigScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/dma_faceconfig_gui.png");

    private final DimensionalMatterAssemblerBlockEntity be;
    private final Screen parentScreen;
    private ConfigType activeType = ConfigType.ITEM;

    private int guiLeft;
    private int guiTop;
    private final int imageWidth = 96;
    private final int imageHeight = 91;

    // Variáveis para arrastar
    private boolean isDragging = false;
    private double dragStartX, dragStartY;
    private int initialGuiLeft, initialGuiTop;

    public DimensionalMatterAssemblerConfigScreen(DimensionalMatterAssemblerBlockEntity be, Screen parentScreen) {
        super(Component.literal("DMA Configuration"));
        this.be = be;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        if (this.guiLeft == 0 && this.guiTop == 0) {
            this.guiLeft = (this.width - imageWidth) / 2;
            this.guiTop = (this.height - imageHeight) / 2;
        }

        // Botão Fechar
        this.addRenderableWidget(Button.builder(Component.literal("x"), button -> Minecraft.getInstance().setScreen(parentScreen))
                .bounds(guiLeft + 5, guiTop + 6, 6, 6)
                .tooltip(Tooltip.create(Component.literal("Close")))
                .build());

        // Botão Auto-Eject
        this.addRenderableWidget(Button.builder(Component.empty(), button -> {
                    ModPackets.sendToServer(new PacketToggleAutoEject(be.getBlockPos(), activeType.ordinal()));
                }).bounds(guiLeft + 82, guiTop + 7, 7, 7)
                .tooltip(Tooltip.create(Component.literal("Toggle Auto-Eject")))
                .build());

        // Botões das Faces
        addFaceButton(Direction.UP, 45, 33);
        addFaceButton(Direction.WEST, 31, 47);
        addFaceButton(Direction.NORTH, 45, 47);
        addFaceButton(Direction.EAST, 59, 47);
        addFaceButton(Direction.SOUTH, 31, 61);
        addFaceButton(Direction.DOWN, 45, 61);

        // Botões de Abas
        int tabX = guiLeft - 25;
        int startY = guiTop + 5;
        addTabButton(tabX, startY, ConfigType.ITEM, "Items", ChatFormatting.BLUE);
        addTabButton(tabX, startY + 22, ConfigType.FLUID, "Fluids", ChatFormatting.AQUA);
        addTabButton(tabX, startY + 44, ConfigType.COOLANT, "Coolant", ChatFormatting.GREEN);
        addTabButton(tabX, startY + 66, ConfigType.ENERGY, "Energy", ChatFormatting.RED);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Renderiza a tela PAI no fundo
        if (parentScreen != null) {
            // Usamos -1, -1 para o mouse para que a tela de trás não reaja (não acenda slots, etc)
            parentScreen.render(guiGraphics, -1, -1, partialTick);
        }

        // 2. Desenha um fundo preto semi-transparente para destacar o popup
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x80000000, 0x90000000);

        // 3. Renderiza a GUI de configuração por cima
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 23, imageWidth, imageHeight);

        // Textos e Títulos
        boolean isEjecting = be.isAutoEject(activeType);
        Component ejectText = Component.literal("Eject: ").append(Component.literal(isEjecting ? "ON" : "OFF")
                .withStyle(isEjecting ? ChatFormatting.GREEN : ChatFormatting.RED));

        float textScale = 0.8f;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(guiLeft + 31, guiTop + 13, 0);
        guiGraphics.pose().scale(textScale, textScale, textScale);
        guiGraphics.drawString(this.font, ejectText, 0, 0, 0xFFFFFFFF, false);
        guiGraphics.pose().popPose();

        guiGraphics.drawCenteredString(this.font, activeType.name(), this.guiLeft + imageWidth / 2, this.guiTop - 10, 0xFFFFFFFF);

        // Renderiza os widgets (botões) desta tela
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // === Lógica de Arrastar (Mantida igual) ===
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(0, 0, imageWidth, 20, mouseX, mouseY)) {
            this.isDragging = true;
            this.dragStartX = mouseX;
            this.dragStartY = mouseY;
            this.initialGuiLeft = this.guiLeft;
            this.initialGuiTop = this.guiTop;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isDragging) {
            this.isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDragging) {
            this.guiLeft = this.initialGuiLeft + (int)(mouseX - this.dragStartX);
            this.guiTop = this.initialGuiTop + (int)(mouseY - this.dragStartY);
            this.rebuildWidgets();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= this.guiLeft + x && mouseX < this.guiLeft + x + width &&
                mouseY >= this.guiTop + y && mouseY < this.guiTop + y + height;
    }

    // === Helpers de Botões (Mantidos iguais) ===
    private void addFaceButton(Direction dir, int x, int y) {
        this.addRenderableWidget(new FaceButton(guiLeft + x, guiTop + y, dir));
    }

    private void addTabButton(int x, int y, ConfigType type, String label, ChatFormatting color) {
        Button btn = Button.builder(Component.literal(label.substring(0, 1)).withStyle(color), b -> {
            this.activeType = type;
            this.rebuildWidgets();
        }).bounds(x, y, 20, 20).tooltip(Tooltip.create(Component.literal(label).withStyle(color))).build();
        btn.active = (this.activeType != type);
        this.addRenderableWidget(btn);
    }

    @Override public boolean isPauseScreen() { return false; }

    private class FaceButton extends Button {
        private final Direction direction;
        public FaceButton(int x, int y, Direction direction) {
            super(x, y, 10, 10, Component.empty(), b -> {
                ModPackets.sendToServer(new PacketChangeSideConfig(be.getBlockPos(), direction, activeType.ordinal()));
            }, DEFAULT_NARRATION);
            this.direction = direction;
        }
        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            IOMode mode = be.getSideConfig(activeType, direction);
            int color = 0x00000000;
            if (mode == IOMode.ITEM_IN || mode == IOMode.FLUID_IN || mode == IOMode.COOLANT_IN || mode == IOMode.ENERGY) {
                color = 0xAAFF0000;
            } else if (mode == IOMode.ITEM_OUT || mode == IOMode.FLUID_OUT_1 || mode == IOMode.FLUID_OUT_2) {
                color = 0xAA0000FF;
            }
            if (color != 0x00000000) {
                guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, color);
            }
            if (isHovered) {
                guiGraphics.renderOutline(getX() - 1, getY() - 1, width + 2, height + 2, 0xFFFFFFFF);
                guiGraphics.renderTooltip(font, Component.literal(direction.getName().toUpperCase() + ": ").append(mode.getTooltip()), mouseX, mouseY);
            }
        }
    }
}
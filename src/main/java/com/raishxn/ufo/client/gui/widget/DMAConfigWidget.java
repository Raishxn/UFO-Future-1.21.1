package com.raishxn.ufo.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketChangeSideConfig;
import com.raishxn.ufo.network.packet.PacketToggleAutoEject;
import com.raishxn.ufo.network.packet.PacketToggleAutoInput;
import com.raishxn.ufo.util.ConfigType;
import com.raishxn.ufo.util.IOMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class DMAConfigWidget extends AbstractWidget {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "textures/gui/dma_config_widget.png");

    private final DimensionalMatterAssemblerBlockEntity blockEntity;
    private boolean expanded = false;
    private ConfigType activeType = ConfigType.FLUID;

    // === COORDENADAS DA TEXTURA ===
    // Botão expandir: 0,28 -> 22,53 (L=22, A=25)
    private static final int EXPAND_U = 0, EXPAND_V = 28, EXPAND_W = 22, EXPAND_H = 25;
    // Widget expandido: 0,53 -> 58,160 (L=58, A=107)
    private static final int PANEL_U = 0, PANEL_V = 54, PANEL_W = 58, PANEL_H = 107;

    // === OFFSETS RELATIVOS (Calculados: Coord Textura - Coord Origem Painel [0, 53]) ===
    // MODO (Fluids/Itens/Coolant) - ATUALIZADO
    private static final int OFF_MODE_Y = 87, MODE_BTN_SIZE = 13;
    private static final int OFF_FLUID_X = 6;
    private static final int OFF_ITEM_X = 23;
    private static final int OFF_COOLANT_X = 40;

    // Minimizar: 43,61 -> 54,73 (L=11, A=12) | Relativo: X=43-0=43, Y=61-53=8
    private static final int OFF_MINIMIZE_X = 43, OFF_MINIMIZE_Y = 8, MINIMIZE_W = 11, MINIMIZE_H = 12;

    // Auto Input: 9,62 -> 22,75 (L=13, A=13) | Relativo: X=9, Y=62-53=9
    private static final int OFF_AUTO_IN_X = 9, OFF_AUTO_IN_Y = 9, AUTO_W = 13, AUTO_H = 13;
    // Auto Output: 27,62 -> 40,75 (L=13, A=13) | Relativo: X=27, Y=62-53=9
    private static final int OFF_AUTO_OUT_X = 27, OFF_AUTO_OUT_Y = 9;

    private static final int FACE_SIZE = 9;
    // Cima: 24,97 | Relativo Y: 97-53 = 44
    private static final int OFF_UP_X = 24, OFF_UP_Y = 44;
    // Esquerda: 12,109 | Relativo Y: 109-53 = 56
    private static final int OFF_LEFT_X = 12, OFF_LEFT_Y = 56;
    // Meio (Front): 24,109 | Relativo Y: 109-53 = 56
    private static final int OFF_FRONT_X = 24, OFF_FRONT_Y = 56;
    // Direito: 36,109 | Relativo Y: 109-53 = 56
    private static final int OFF_RIGHT_X = 36, OFF_RIGHT_Y = 56;
    // Baixo: 24,121 | Relativo Y: 121-53 = 68
    private static final int OFF_DOWN_X = 24, OFF_DOWN_Y = 68;
    // Atrás: 36,121 | Relativo Y: 121-53 = 68
    private static final int OFF_BACK_X = 36, OFF_BACK_Y = 68;

    // Overlays começam em V=161
    private static final int OVERLAY_V = 161;
    // AUTO_OVERLAY: Valores originais mantidos (se precisar ajustar, me avise)
    private static final int AUTO_OVERLAY_V = 189;
    private static final int AUTO_OVERLAY_IN_U = 0;
    private static final int AUTO_OVERLAY_OUT_U = 14;

    public DMAConfigWidget(int x, int y, DimensionalMatterAssemblerBlockEntity blockEntity) {
        super(x, y, EXPAND_W, EXPAND_H, Component.empty());
        this.blockEntity = blockEntity;
    }

    public void setActiveType(ConfigType type) {
        this.activeType = type;
    }

    @Override
    public void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        if (!expanded) {
            this.width = EXPAND_W;
            this.height = EXPAND_H;
            gg.blit(TEXTURE, getX(), getY(), EXPAND_U, EXPAND_V, EXPAND_W, EXPAND_H);
            if (isHoveredOrFocused()) {
                gg.renderTooltip(Minecraft.getInstance().font, Component.literal("Configurar I/O"), mouseX, mouseY);
            }
        } else {
            this.width = PANEL_W;
            this.height = PANEL_H;
            int startX = getX();
            int startY = getY();

            gg.blit(TEXTURE, startX, startY, PANEL_U, PANEL_V, PANEL_W, PANEL_H);

            renderModeButton(gg, mouseX, mouseY, startX + OFF_FLUID_X, startY + OFF_MODE_Y, ConfigType.FLUID, "Mode: Fluids");
            renderModeButton(gg, mouseX, mouseY, startX + OFF_ITEM_X, startY + OFF_MODE_Y, ConfigType.ITEM, "Mode: Itens");
            renderModeButton(gg, mouseX, mouseY, startX + OFF_COOLANT_X, startY + OFF_MODE_Y, ConfigType.COOLANT, "Mode: Coolant");

            Direction facing = getFacing();
            renderFace(gg, mouseX, mouseY, startX + OFF_UP_X, startY + OFF_UP_Y, Direction.UP, "Top");
            renderFace(gg, mouseX, mouseY, startX + OFF_DOWN_X, startY + OFF_DOWN_Y, Direction.DOWN, "Bottom");
            renderFace(gg, mouseX, mouseY, startX + OFF_FRONT_X, startY + OFF_FRONT_Y, facing, "Front");
            renderFace(gg, mouseX, mouseY, startX + OFF_BACK_X, startY + OFF_BACK_Y, facing.getOpposite(), "Back");
            renderFace(gg, mouseX, mouseY, startX + OFF_LEFT_X, startY + OFF_LEFT_Y, facing.getCounterClockWise(), "Left");
            renderFace(gg, mouseX, mouseY, startX + OFF_RIGHT_X, startY + OFF_RIGHT_Y, facing.getClockWise(), "Right");

            boolean autoIn = blockEntity.isAutoInput(activeType);
            if (autoIn) gg.blit(TEXTURE, startX + OFF_AUTO_IN_X, startY + OFF_AUTO_IN_Y, AUTO_OVERLAY_IN_U, AUTO_OVERLAY_V, AUTO_W, AUTO_H);
            renderControl(gg, mouseX, mouseY, startX + OFF_AUTO_IN_X, startY + OFF_AUTO_IN_Y, AUTO_W, AUTO_H, "Auto-Input (" + activeType.name() + "): " + (autoIn ? "§aON" : "§cOFF"));

            boolean autoOut = blockEntity.isAutoEject(activeType);
            if (autoOut) gg.blit(TEXTURE, startX + OFF_AUTO_OUT_X, startY + OFF_AUTO_OUT_Y, AUTO_OVERLAY_OUT_U, AUTO_OVERLAY_V, AUTO_W, AUTO_H);
            renderControl(gg, mouseX, mouseY, startX + OFF_AUTO_OUT_X, startY + OFF_AUTO_OUT_Y, AUTO_W, AUTO_H, "Auto-Output (" + activeType.name() + "): " + (autoOut ? "§aON" : "§cOFF"));

            renderControl(gg, mouseX, mouseY, startX + OFF_MINIMIZE_X, startY + OFF_MINIMIZE_Y, MINIMIZE_W, MINIMIZE_H, "Minimize");
        }
        RenderSystem.disableDepthTest();
    }

    private void renderModeButton(GuiGraphics gg, int mx, int my, int x, int y, ConfigType type, String tooltip) {
        if (this.activeType == type) {
            gg.fill(x, y, x + MODE_BTN_SIZE, y + MODE_BTN_SIZE, 0x5500FF00);
        } else if (isClick(mx, my, x, y, MODE_BTN_SIZE, MODE_BTN_SIZE)) {
            gg.fill(x, y, x + MODE_BTN_SIZE, y + MODE_BTN_SIZE, 0x33FFFFFF);
            gg.renderTooltip(Minecraft.getInstance().font, Component.literal(tooltip), mx, my);
        } else if (this.activeType == type && isClick(mx, my, x, y, MODE_BTN_SIZE, MODE_BTN_SIZE)) {
            gg.renderTooltip(Minecraft.getInstance().font, Component.literal(tooltip + " (Active)"), mx, my);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) return false;
        if (!isValidClickButton(button) || !isHovered) return false;

        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (!expanded) {
            playClickSound();
            this.expanded = true;
            this.setX(this.getX() - (PANEL_W - EXPAND_W));
            this.width = PANEL_W;
            this.height = PANEL_H;
            return true;
        } else {
            int startX = getX();
            int startY = getY();

            if (isClick(mx, my, startX + OFF_MINIMIZE_X, startY + OFF_MINIMIZE_Y, MINIMIZE_W, MINIMIZE_H)) {
                playClickSound();
                this.expanded = false;
                this.setX(this.getX() + (PANEL_W - EXPAND_W));
                this.width = EXPAND_W;
                this.height = EXPAND_H;
                return true;
            }

            if (isClick(mx, my, startX + OFF_FLUID_X, startY + OFF_MODE_Y, MODE_BTN_SIZE, MODE_BTN_SIZE)) {
                playClickSound();
                this.activeType = ConfigType.FLUID;
                return true;
            }
            if (isClick(mx, my, startX + OFF_ITEM_X, startY + OFF_MODE_Y, MODE_BTN_SIZE, MODE_BTN_SIZE)) {
                playClickSound();
                this.activeType = ConfigType.ITEM;
                return true;
            }
            if (isClick(mx, my, startX + OFF_COOLANT_X, startY + OFF_MODE_Y, MODE_BTN_SIZE, MODE_BTN_SIZE)) {
                playClickSound();
                this.activeType = ConfigType.COOLANT;
                return true;
            }

            if (isClick(mx, my, startX + OFF_AUTO_OUT_X, startY + OFF_AUTO_OUT_Y, AUTO_W, AUTO_H)) {
                playClickSound();
                ModPackets.sendToServer(new PacketToggleAutoEject(blockEntity.getBlockPos(), activeType.ordinal()));
                return true;
            }
            if (isClick(mx, my, startX + OFF_AUTO_IN_X, startY + OFF_AUTO_IN_Y, AUTO_W, AUTO_H)) {
                playClickSound();
                ModPackets.sendToServer(new PacketToggleAutoInput(blockEntity.getBlockPos(), activeType.ordinal()));
                return true;
            }

            Direction facing = getFacing();
            if (clickFace(mx, my, startX + OFF_UP_X, startY + OFF_UP_Y, Direction.UP)) return true;
            if (clickFace(mx, my, startX + OFF_DOWN_X, startY + OFF_DOWN_Y, Direction.DOWN)) return true;
            if (clickFace(mx, my, startX + OFF_FRONT_X, startY + OFF_FRONT_Y, facing)) return true;
            if (clickFace(mx, my, startX + OFF_BACK_X, startY + OFF_BACK_Y, facing.getOpposite())) return true;
            if (clickFace(mx, my, startX + OFF_LEFT_X, startY + OFF_LEFT_Y, facing.getCounterClockWise())) return true;
            if (clickFace(mx, my, startX + OFF_RIGHT_X, startY + OFF_RIGHT_Y, facing.getClockWise())) return true;

            return true;
        }
    }

    private void renderFace(GuiGraphics gg, int mx, int my, int x, int y, Direction dir, String name) {
        IOMode mode = blockEntity.getSideConfig(activeType, dir);
        int overlayU = getOverlayU(mode);

        if (overlayU >= 0 && mode != IOMode.NONE) {
            gg.blit(TEXTURE, x, y, overlayU, OVERLAY_V, FACE_SIZE, FACE_SIZE);
        }

        if (mx >= x && mx < x + FACE_SIZE && my >= y && my < y + FACE_SIZE) {
            gg.fill(x, y, x + FACE_SIZE, y + FACE_SIZE, 0x5FFFFFFF);
            gg.renderTooltip(Minecraft.getInstance().font, Component.literal(name + ": ").append(mode.getTooltip()), mx, my);
        }
    }

    private void renderControl(GuiGraphics gg, int mx, int my, int x, int y, int w, int h, String tooltip) {
        if (mx >= x && mx < x + w && my >= y && my < y + h) {
            gg.fill(x, y, x + w, y + h, 0x3FFFFFFF);
            gg.renderTooltip(Minecraft.getInstance().font, Component.literal(tooltip), mx, my);
        }
    }

    private boolean clickFace(int mx, int my, int x, int y, Direction dir) {
        if (isClick(mx, my, x, y, FACE_SIZE, FACE_SIZE)) {
            playClickSound();
            IOMode currentMode = blockEntity.getSideConfig(activeType, dir);
            IOMode nextMode = getNextMode(activeType, currentMode);
            ModPackets.sendToServer(new PacketChangeSideConfig(blockEntity.getBlockPos(), dir, activeType.ordinal(), nextMode.ordinal()));
            return true;
        }
        return false;
    }

    private IOMode getNextMode(ConfigType type, IOMode current) {
        switch (type) {
            case ITEM:
                return switch (current) {
                    case NONE -> IOMode.ITEM_IN;
                    case ITEM_IN -> IOMode.ITEM_OUT;
                    case ITEM_OUT -> IOMode.ITEM_IO;
                    case ITEM_IO -> IOMode.ITEM_OUT2;
                    case ITEM_OUT2 -> IOMode.ITEM_IO2;
                    default -> IOMode.NONE;
                };
            case FLUID:
                return switch (current) {
                    case NONE -> IOMode.FLUID_IN;
                    case FLUID_IN -> IOMode.FLUID_OUT_1;
                    case FLUID_OUT_1 -> IOMode.FLUID_IO_1;
                    case FLUID_IO_1 -> IOMode.FLUID_OUT_2;
                    case FLUID_OUT_2 -> IOMode.FLUID_IO_2;
                    default -> IOMode.NONE;
                };
            case COOLANT:
                return (current == IOMode.NONE) ? IOMode.COOLANT_IN : IOMode.NONE;
            default:
                return IOMode.NONE;
        }
    }

    private int getOverlayU(IOMode mode) {
        return switch (mode) {
            case ITEM_IN -> 0;
            case ITEM_OUT -> 10;
            case ITEM_IO -> 20;
            case ITEM_OUT2 -> 30;
            case ITEM_IO2 -> 40;
            case FLUID_IN -> 50;
            case FLUID_OUT_1 -> 60;
            case FLUID_IO_1 -> 70;
            case FLUID_OUT_2 -> 80;
            case FLUID_IO_2 -> 90;
            case COOLANT_IN -> 100;
            default -> -1;
        };
    }

    private boolean isClick(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private Direction getFacing() {
        if (blockEntity.getBlockState().hasProperty(HorizontalDirectionalBlock.FACING)) {
            return blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        }
        return Direction.NORTH;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
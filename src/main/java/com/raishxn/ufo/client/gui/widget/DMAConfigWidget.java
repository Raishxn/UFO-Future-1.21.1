package com.raishxn.ufo.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketChangeSideConfig;
import com.raishxn.ufo.network.packet.PacketToggleAutoEject;
// Certifique-se de ter criado o PacketToggleAutoInput conforme instrução anterior, ou comente as linhas que o usam.
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
    private ConfigType activeType = ConfigType.ITEM;

    // Coordenadas da Textura
    private static final int EXPAND_U = 0, EXPAND_V = 28, EXPAND_W = 22, EXPAND_H = 25;
    private static final int PANEL_U = 0, PANEL_V = 53, PANEL_W = 58, PANEL_H = 107;

    // Offsets
    private static final int OFF_MINIMIZE_X = 43, OFF_MINIMIZE_Y = 8, MINIMIZE_W = 11, MINIMIZE_H = 12;
    private static final int OFF_AUTO_IN_X = 9, OFF_AUTO_IN_Y = 9, AUTO_W = 13, AUTO_H = 13;
    private static final int OFF_AUTO_OUT_X = 27, OFF_AUTO_OUT_Y = 9;

    // Botões de Face
    private static final int FACE_SIZE = 9;
    private static final int OFF_UP_X = 24, OFF_UP_Y = 44;
    private static final int OFF_LEFT_X = 12, OFF_LEFT_Y = 56;
    private static final int OFF_FRONT_X = 24, OFF_FRONT_Y = 56;
    private static final int OFF_RIGHT_X = 36, OFF_RIGHT_Y = 56;
    private static final int OFF_DOWN_X = 24, OFF_DOWN_Y = 68;
    private static final int OFF_BACK_X = 36, OFF_BACK_Y = 68;

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
            int startX = getX() - (PANEL_W - EXPAND_W);
            int startY = getY();

            gg.blit(TEXTURE, startX, startY, PANEL_U, PANEL_V, PANEL_W, PANEL_H);

            Direction facing = getFacing();
            renderFace(gg, mouseX, mouseY, startX + OFF_UP_X, startY + OFF_UP_Y, Direction.UP, "Cima");
            renderFace(gg, mouseX, mouseY, startX + OFF_DOWN_X, startY + OFF_DOWN_Y, Direction.DOWN, "Baixo");
            renderFace(gg, mouseX, mouseY, startX + OFF_FRONT_X, startY + OFF_FRONT_Y, facing, "Frente");
            renderFace(gg, mouseX, mouseY, startX + OFF_BACK_X, startY + OFF_BACK_Y, facing.getOpposite(), "Trás");
            renderFace(gg, mouseX, mouseY, startX + OFF_LEFT_X, startY + OFF_LEFT_Y, facing.getCounterClockWise(), "Esquerda");
            renderFace(gg, mouseX, mouseY, startX + OFF_RIGHT_X, startY + OFF_RIGHT_Y, facing.getClockWise(), "Direita");

            boolean autoIn = blockEntity.isAutoInput(activeType);
            renderControl(gg, mouseX, mouseY, startX + OFF_AUTO_IN_X, startY + OFF_AUTO_IN_Y, AUTO_W, AUTO_H, "Auto-Input: " + (autoIn ? "§aON" : "§cOFF"));

            boolean autoOut = blockEntity.isAutoEject(activeType);
            renderControl(gg, mouseX, mouseY, startX + OFF_AUTO_OUT_X, startY + OFF_AUTO_OUT_Y, AUTO_W, AUTO_H, "Auto-Output: " + (autoOut ? "§aON" : "§cOFF"));

            renderControl(gg, mouseX, mouseY, startX + OFF_MINIMIZE_X, startY + OFF_MINIMIZE_Y, MINIMIZE_W, MINIMIZE_H, "Minimizar");
        }
        RenderSystem.disableDepthTest();
    }

    private void renderFace(GuiGraphics gg, int mx, int my, int x, int y, Direction dir, String name) {
        IOMode mode = blockEntity.getSideConfig(activeType, dir);
        int overlayU = getOverlayU(mode);

        // Só desenha se não for NONE (assumindo que NONE retorna -1 ou um valor alto)
        if (overlayU >= 0 && mode != IOMode.NONE) {
            gg.blit(TEXTURE, x, y, overlayU, 161, FACE_SIZE, FACE_SIZE);
        }

        if (mx >= x && mx < x + FACE_SIZE && my >= y && my < y + FACE_SIZE) {
            gg.fill(x, y, x + FACE_SIZE, y + FACE_SIZE, 0x5FFFFFFF);
            // Usa o tooltip definido no próprio enum IOMode
            gg.renderTooltip(Minecraft.getInstance().font, Component.literal(name + ": ").append(mode.getTooltip()), mx, my);
        }
    }

    private void renderControl(GuiGraphics gg, int mx, int my, int x, int y, int w, int h, String tooltip) {
        if (mx >= x && mx < x + w && my >= y && my < y + h) {
            gg.fill(x, y, x + w, y + h, 0x3FFFFFFF);
            gg.renderTooltip(Minecraft.getInstance().font, Component.literal(tooltip), mx, my);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) return false;
        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (!expanded) {
            if (isHoveredOrFocused()) {
                playClickSound();
                this.expanded = true;
                this.setX(this.getX() - (PANEL_W - EXPAND_W));
                return true;
            }
        } else {
            int startX = getX();
            int startY = getY();

            if (isClick(mx, my, startX + OFF_MINIMIZE_X, startY + OFF_MINIMIZE_Y, MINIMIZE_W, MINIMIZE_H)) {
                playClickSound();
                this.expanded = false;
                this.setX(this.getX() + (PANEL_W - EXPAND_W));
                return true;
            }
            if (isClick(mx, my, startX + OFF_AUTO_OUT_X, startY + OFF_AUTO_OUT_Y, AUTO_W, AUTO_H)) {
                playClickSound();
                ModPackets.sendToServer(new PacketToggleAutoEject(blockEntity.getBlockPos(), activeType.ordinal()));
                return true;
            }
            if (isClick(mx, my, startX + OFF_AUTO_IN_X, startY + OFF_AUTO_IN_Y, AUTO_W, AUTO_H)) {
                playClickSound();
                // Requer PacketToggleAutoInput criado
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
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickFace(int mx, int my, int x, int y, Direction dir) {
        if (isClick(mx, my, x, y, FACE_SIZE, FACE_SIZE)) {
            playClickSound();
            // Calcula o próximo modo válido para este Tipo de Configuração
            IOMode currentMode = blockEntity.getSideConfig(activeType, dir);
            IOMode nextMode = getNextMode(activeType, currentMode);

            ModPackets.sendToServer(new PacketChangeSideConfig(blockEntity.getBlockPos(), dir, activeType.ordinal(), nextMode.ordinal()));
            return true;
        }
        return false;
    }

    /**
     * Lógica central para ciclar modos dependendo da aba que o jogador está.
     */
    private IOMode getNextMode(ConfigType type, IOMode current) {
        switch (type) {
            case ITEM:
                return switch (current) {
                    case NONE -> IOMode.ITEM_IN;
                    case ITEM_IN -> IOMode.ITEM_OUT;
                    default -> IOMode.NONE;
                };
            case FLUID:
                return switch (current) {
                    case NONE -> IOMode.FLUID_IN;
                    case FLUID_IN -> IOMode.FLUID_OUT_1;
                    case FLUID_OUT_1 -> IOMode.FLUID_OUT_2;
                    default -> IOMode.NONE;
                };
            case ENERGY:
                return (current == IOMode.NONE) ? IOMode.ENERGY : IOMode.NONE;
            // Adicione caso para COOLANT se houver ConfigType.COOLANT
            // case COOLANT: return (current == IOMode.NONE) ? IOMode.COOLANT_IN : IOMode.NONE;
            default:
                return IOMode.NONE;
        }
    }

    private int getOverlayU(IOMode mode) {
        return switch (mode) {
            case ITEM_IN -> 0;
            case ITEM_OUT -> 10;
            case ITEM_OUT2 -> 20;
            // case ITEM_IO -> 20; // Se adicionar depois
            // case ITEM_OUT_2 -> 30;
            case FLUID_IN -> 50;
            case FLUID_OUT_1 -> 60;
            // case FLUID_IO -> 70;
            case FLUID_OUT_2 -> 80;
            case COOLANT_IN -> 100;
            // case ENERGY -> ???; // Adicione coordenada se tiver overlay de energia
            default -> -1; // Não desenha nada para NONE ou não mapeado
        };
    }

    private boolean isClick(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    private void playClickSound() { Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)); }
    private Direction getFacing() {
        if (blockEntity.getBlockState().hasProperty(HorizontalDirectionalBlock.FACING)) {
            return blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        }
        return Direction.NORTH;
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { defaultButtonNarrationText(narrationElementOutput); }
}
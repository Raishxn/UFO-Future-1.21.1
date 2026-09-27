package com.raishxn.ufo.screen;

import com.raishxn.ufo.item.StructureTerminalSettings;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketTerminalSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Held-item Structure Terminal: scan/build/replace/dismantle modes, AE mode, field tier and AE binding. */
public final class StructureScannerScreen extends Screen {

    private static final int PANEL_WIDTH = 200;
    private static final int ROW_HEIGHT = 24;

    private final InteractionHand hand;
    private final ItemStack stack;
    private boolean build;
    private boolean replace;
    private boolean dismantle;
    private boolean ae;
    private int tier;
    private Button buildButton;
    private Button replaceButton;
    private Button dismantleButton;
    private Button aeButton;
    private Button tierButton;

    public StructureScannerScreen(InteractionHand hand, ItemStack stack) {
        super(Component.translatable("gui.ufo.terminal.title"));
        this.hand = hand;
        this.stack = stack;
        this.build = StructureTerminalSettings.getBuildMode(stack);
        this.replace = StructureTerminalSettings.getReplaceMode(stack);
        this.dismantle = StructureTerminalSettings.getDismantleMode(stack);
        this.ae = StructureTerminalSettings.getAeMode(stack);
        this.tier = StructureTerminalSettings.getFieldTier(stack);
    }

    @Override
    protected void init() {
        int x = this.width / 2 - PANEL_WIDTH / 2;
        int y = this.height / 2 - 88;

        buildButton = addRenderableWidget(Button.builder(toggleLabel("gui.ufo.terminal.build", build), ignored -> {
            build = !build;
            if (!build) {
                replace = false;
                dismantle = false;
            }
            StructureTerminalSettings.setBuildMode(stack, build);
            StructureTerminalSettings.setReplaceMode(stack, replace);
            StructureTerminalSettings.setDismantleMode(stack, dismantle);
            refreshLabels();
            sync();
        }).bounds(x, y, PANEL_WIDTH, 20).build());

        replaceButton = addRenderableWidget(Button.builder(toggleLabel("gui.ufo.terminal.replace", replace), ignored -> {
            replace = !replace;
            if (replace) {
                build = true;
                dismantle = false;
            }
            StructureTerminalSettings.setReplaceMode(stack, replace);
            StructureTerminalSettings.setBuildMode(stack, build);
            StructureTerminalSettings.setDismantleMode(stack, dismantle);
            refreshLabels();
            sync();
        }).bounds(x, y + ROW_HEIGHT, PANEL_WIDTH, 20).build());

        dismantleButton = addRenderableWidget(Button.builder(toggleLabel("gui.ufo.terminal.dismantle", dismantle), ignored -> {
            dismantle = !dismantle;
            if (dismantle) {
                build = true;
                replace = false;
            }
            StructureTerminalSettings.setDismantleMode(stack, dismantle);
            StructureTerminalSettings.setBuildMode(stack, build);
            StructureTerminalSettings.setReplaceMode(stack, replace);
            refreshLabels();
            sync();
        }).bounds(x, y + ROW_HEIGHT * 2, PANEL_WIDTH, 20).build());

        aeButton = addRenderableWidget(Button.builder(toggleLabel("gui.ufo.terminal.ae_mode", ae), ignored -> {
            ae = !ae;
            StructureTerminalSettings.setAeMode(stack, ae);
            aeButton.setMessage(toggleLabel("gui.ufo.terminal.ae_mode", ae));
            sync();
        }).bounds(x, y + ROW_HEIGHT * 3, PANEL_WIDTH, 20).build());

        tierButton = addRenderableWidget(Button.builder(tierLabel(), ignored -> {
            tier = tier % 3 + 1;
            StructureTerminalSettings.setFieldTier(stack, tier);
            tierButton.setMessage(tierLabel());
            sync();
        }).bounds(x, y + ROW_HEIGHT * 4, PANEL_WIDTH, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> onClose())
                .bounds(x, y + ROW_HEIGHT * 6 + 8, PANEL_WIDTH, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int y = this.height / 2 - 88;
        graphics.drawCenteredString(this.font, boundLabel(), centerX, y + ROW_HEIGHT * 5 + 4, 0xFFAAAAAA);
    }

    private void refreshLabels() {
        buildButton.setMessage(toggleLabel("gui.ufo.terminal.build", build));
        replaceButton.setMessage(toggleLabel("gui.ufo.terminal.replace", replace));
        dismantleButton.setMessage(toggleLabel("gui.ufo.terminal.dismantle", dismantle));
    }

    private Component toggleLabel(String key, boolean value) {
        return Component.translatable(key,
                Component.translatable(value ? "gui.ufo.terminal.on" : "gui.ufo.terminal.off"));
    }

    private Component tierLabel() {
        return Component.translatable("gui.ufo.terminal.tier",
                Component.translatable("gui.ufo.terminal.tier_" + tier));
    }

    private Component boundLabel() {
        GlobalPos bound = StructureTerminalSettings.getBoundPos(stack);
        if (bound == null) {
            return Component.translatable("gui.ufo.terminal.unbound");
        }
        return Component.translatable("gui.ufo.terminal.bound",
                bound.pos().toShortString() + " (" + bound.dimension().location() + ")");
    }

    private void sync() {
        ModPackets.sendToServer(new PacketTerminalSettings(hand.ordinal(), build, replace, dismantle, ae, tier));
    }
}

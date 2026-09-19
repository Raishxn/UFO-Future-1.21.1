package com.raishxn.ufo.screen;

import com.raishxn.ufo.item.StructureScannerSettings;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketSetStructureScannerSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Lightweight held-item terminal for structure construction settings. */
public final class StructureScannerScreen extends Screen {
    private final InteractionHand hand;
    private StructureScannerSettings.Mode mode;
    private boolean hatchMode;
    private Button modeButton;
    private Button hatchButton;

    public StructureScannerScreen(InteractionHand hand, ItemStack stack) {
        super(Component.translatable("gui.ufo.structure_scanner.title"));
        this.hand = hand;
        StructureScannerSettings settings = StructureScannerSettings.read(stack);
        this.mode = settings.mode();
        this.hatchMode = settings.hatchMode();
    }

    @Override
    protected void init() {
        int x = width / 2 - 90;
        int y = height / 2 - 38;
        modeButton = addRenderableWidget(Button.builder(modeLabel(), ignored -> {
            mode = StructureScannerSettings.Mode.byOrdinal(mode.ordinal() + 1);
            modeButton.setMessage(modeLabel());
            sync();
        }).bounds(x, y, 180, 20).build());
        hatchButton = addRenderableWidget(Button.builder(hatchLabel(), ignored -> {
            hatchMode = !hatchMode;
            hatchButton.setMessage(hatchLabel());
            sync();
        }).bounds(x, y + 26, 180, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> onClose())
                .bounds(x, y + 60, 180, 20).build());
    }

    private Component modeLabel() {
        return Component.translatable("gui.ufo.structure_scanner.mode",
                Component.translatable("gui.ufo.structure_scanner.mode." + mode.name().toLowerCase(java.util.Locale.ROOT)));
    }

    private Component hatchLabel() {
        return Component.translatable("gui.ufo.structure_scanner.hatch_mode",
                Component.translatable(hatchMode ? "options.on" : "options.off"));
    }

    private void sync() {
        ModPackets.sendToServer(new PacketSetStructureScannerSettings(hand.ordinal(), mode.ordinal(), hatchMode));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 64, 0xFFE7D9FF);
        graphics.drawCenteredString(font,
                Component.translatable("gui.ufo.structure_scanner.hint"), width / 2, height / 2 + 16, 0xFFAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

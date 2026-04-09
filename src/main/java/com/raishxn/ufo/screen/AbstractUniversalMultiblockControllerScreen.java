package com.raishxn.ufo.screen;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.UniversalDisplayedRecipe;
import com.raishxn.ufo.client.render.StructureHighlightRenderer;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketScanUniversalStructure;
import com.raishxn.ufo.network.packet.PacketToggleUniversalOverclock;
import com.raishxn.ufo.network.packet.PacketToggleUniversalSafeMode;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DirectionalBlock;
import net.neoforged.neoforge.fluids.FluidStack;

public abstract class AbstractUniversalMultiblockControllerScreen<M extends AbstractUniversalMultiblockControllerMenu<?>>
        extends UpgradeableScreen<M> {

    private Button safeModeButton;
    private Button overclockButton;
    private Button scanButton;

    protected AbstractUniversalMultiblockControllerScreen(M menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        setSlotsHidden(SlotSemantics.PLAYER_INVENTORY, true);
        setSlotsHidden(SlotSemantics.PLAYER_HOTBAR, true);

        this.scanButton = this.addRenderableWidget(Button.builder(Component.literal("Scan"), btn -> {
                    BlockPos pos = this.menu.getBlockEntity().getBlockPos();
                    ModPackets.sendToServer(new PacketScanUniversalStructure(pos));
                    runLocalStructureScan(pos);
                })
                .bounds(this.leftPos + this.imageWidth - 88, this.topPos + this.imageHeight - 24, 42, 20)
                .tooltip(Tooltip.create(Component.literal("Force a multiblock scan and report missing blocks")))
                .build());

        this.safeModeButton = this.addRenderableWidget(Button.builder(Component.literal("Safe"), btn ->
                        ModPackets.sendToServer(new PacketToggleUniversalSafeMode(this.menu.getBlockEntity().getBlockPos())))
                .bounds(this.leftPos + this.imageWidth - 44, this.topPos + this.imageHeight - 24, 20, 20)
                .tooltip(Tooltip.create(Component.literal("Toggle Safe Mode")))
                .build());

        this.overclockButton = this.addRenderableWidget(Button.builder(Component.literal("OC"), btn ->
                        ModPackets.sendToServer(new PacketToggleUniversalOverclock(this.menu.getBlockEntity().getBlockPos())))
                .bounds(this.leftPos + this.imageWidth - 22, this.topPos + this.imageHeight - 24, 20, 20)
                .tooltip(Tooltip.create(Component.literal("Toggle Overclock")))
                .build());
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTick) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTick);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        renderTemperatureBar(guiGraphics);
        renderRecipeList(guiGraphics);
    }

    private void renderTemperatureBar(GuiGraphics guiGraphics) {
        int barX = this.leftPos + 14;
        int barY = this.topPos + 9;
        int barWidth = 146;
        int barHeight = 10;
        int filled = (int) (barWidth * Math.min(1.0F, this.menu.getTemperature() / (float) this.menu.getMaxTemperature()));

        if (filled > 0) {
            guiGraphics.fill(barX, barY, barX + filled, barY + barHeight, 0xCCB32020);
        }
    }

    private void renderRecipeList(GuiGraphics guiGraphics) {
        int listX = this.leftPos + 7;
        int listY = this.topPos + 30;
        int lineHeight = 10;
        int maxTextWidth = 156;

        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(getScreenTitle().getString(), maxTextWidth), listX, listY, 0xF0F0F0, false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(buildStatusLine(), maxTextWidth), listX, listY + 10, 0xD0D7E6, false);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(buildEnergyLine(), maxTextWidth), listX, listY + 20, 0xB9D8FF, false);

        List<UniversalDisplayedRecipe> recipes = this.menu.getDisplayedRecipes();
        for (int i = 0; i < 8; i++) {
            int rowY = listY + 32 + i * lineHeight;
            if (i < recipes.size()) {
                renderRecipeRow(guiGraphics, recipes.get(i), listX, rowY);
            } else if (i == 0 && recipes.isEmpty()) {
                guiGraphics.drawString(this.font, this.menu.isAssembled() ? "No active recipes." : "Structure incomplete.", listX, rowY, 0x8A91A6, false);
            }
        }
    }

    private void renderRecipeRow(GuiGraphics guiGraphics, UniversalDisplayedRecipe recipe, int x, int y) {
        ItemStack iconStack = recipe.itemIcon();
        boolean fluidRecipe = false;
        if (iconStack.isEmpty()) {
            FluidStack fluid = recipe.fluidIcon();
            if (!fluid.isEmpty()) {
                iconStack = new ItemStack(fluid.getFluid().getBucket());
                fluidRecipe = true;
            }
        }

        int textX = x;
        if (!iconStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y - 1, 0.0F);
            guiGraphics.pose().scale(0.5F, 0.5F, 1.0F);
            guiGraphics.renderItem(iconStack, 0, 0);
            guiGraphics.pose().popPose();
            textX += 10;
        }

        String amount = fluidRecipe ? formatAmount(recipe.outputAmount()) + " mB" : formatAmount(recipe.outputAmount()) + "x";
        String time = recipe.maxProgress() > 0 ? recipe.progress() + "/" + recipe.maxProgress() : "-/-";
        String line = amount + " " + recipe.label().getString() + " " + time;
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(line, 146), textX, y, 0xE6EBF5, false);
    }

    private String buildStatusLine() {
        if (!this.menu.isAssembled()) {
            return "Status: Incomplete";
        }
        StringBuilder builder = new StringBuilder(this.menu.isRunning() ? "Status: Running" : "Status: Idle");
        builder.append(" | ");
        builder.append("MK").append(this.menu.getMachineTier());
        builder.append(" | ");
        builder.append(this.menu.isSafeMode() ? "Safe ON" : "Safe OFF");
        builder.append(" | ");
        builder.append(this.menu.isOverclocked() ? "OC ON" : "OC OFF");
        return builder.toString();
    }

    private String buildEnergyLine() {
        return "Energy: " + formatAmount(this.menu.getStoredEnergy()) + " AE / " + formatAmount(this.menu.getMaxEnergy()) + " AE";
    }

    private Component getScreenTitle() {
        String raw = this.title.getString();
        if (raw.endsWith(" Controller")) {
            raw = raw.substring(0, raw.length() - " Controller".length());
        }
        return Component.literal(raw);
    }

    private static String formatAmount(long amount) {
        if (amount >= 1_000_000_000L) {
            return String.format("%.1fB", amount / 1_000_000_000.0);
        }
        if (amount >= 1_000_000L) {
            return String.format("%.1fM", amount / 1_000_000.0);
        }
        if (amount >= 1_000L) {
            return String.format("%.1fK", amount / 1_000.0);
        }
        return Long.toString(amount);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.safeModeButton != null) {
            boolean safe = this.menu.isSafeMode();
            this.safeModeButton.setMessage(Component.literal(safe ? "S" : "!"));
            this.safeModeButton.setTooltip(Tooltip.create(Component.literal(safe ? "Safe Mode enabled" : "Safe Mode disabled")));
        }
        if (this.overclockButton != null) {
            boolean oc = this.menu.isOverclocked();
            this.overclockButton.setMessage(Component.literal(oc ? "OC" : ">"));
            this.overclockButton.setTooltip(Tooltip.create(Component.literal(oc ? "Overclock enabled" : "Overclock disabled")));
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovering(14, 9, 146, 10, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal("Temperature: " + this.menu.getTemperature() + " / " + this.menu.getMaxTemperature()),
                    mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void runLocalStructureScan(BlockPos pos) {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) {
            return;
        }

        var blockEntity = this.minecraft.level.getBlockEntity(pos);
        if (!(blockEntity instanceof IMultiblockController controller)) {
            return;
        }

        var definition = MultiblockControllerDefinitions.getDefinition(blockEntity);
        if (definition.isEmpty()) {
            return;
        }

        Direction facing = Direction.NORTH;
        var state = this.minecraft.level.getBlockState(pos);
        if (state.hasProperty(DirectionalBlock.FACING)) {
            facing = state.getValue(DirectionalBlock.FACING);
        }

        MultiblockPattern.MatchResult result = definition.get().pattern().match(this.minecraft.level, pos, facing);
        if (result.isValid()) {
            if (controller.isAssembled()) {
                this.minecraft.player.displayClientMessage(
                        Component.translatable("message.ufo.structure_formed").withStyle(ChatFormatting.GREEN), true);
            } else {
                this.minecraft.player.displayClientMessage(
                        definition.get().name().copy().append(Component.literal(": structure shape is valid, but extra controller validation failed.")
                                .withStyle(ChatFormatting.RED)),
                        false);
            }
            return;
        }

        reportStructureErrors(definition.get(), result.allErrors());
    }

    private void reportStructureErrors(MultiblockControllerDefinition definition, List<MultiblockPattern.PatternError> errors) {
        if (this.minecraft == null || this.minecraft.player == null || errors == null || errors.isEmpty()) {
            return;
        }

        int shown = Math.min(errors.size(), 10);
        this.minecraft.player.displayClientMessage(
                definition.name().copy()
                        .append(Component.literal(": " + errors.size() + " block(s) missing or misplaced.").withStyle(ChatFormatting.RED)),
                false);

        for (int i = 0; i < shown; i++) {
            var error = errors.get(i);
            BlockPos errorPos = error.pos();
            Component message = Component.literal("  [" + errorPos.getX() + ", " + errorPos.getY() + ", " + errorPos.getZ() + "] Expected: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(error.expected().copy().withStyle(ChatFormatting.YELLOW));
            this.minecraft.player.displayClientMessage(message, false);
        }

        if (errors.size() > shown) {
            this.minecraft.player.displayClientMessage(
                    Component.literal("  ... and " + (errors.size() - shown) + " more.").withStyle(ChatFormatting.GRAY),
                    false);
        }

        int maxHighlight = Math.min(errors.size(), 50);
        for (int i = 0; i < maxHighlight; i++) {
            StructureHighlightRenderer.highlight(errors.get(i).pos(), 5000);
        }
    }
}

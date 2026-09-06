package com.raishxn.ufo.screen;

import appeng.client.gui.Icon;
import com.raishxn.ufo.client.gui.widget.MultiblockSupplyWidget;
import net.minecraft.client.renderer.Rect2i;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.UniversalDisplayedRecipe;
import com.raishxn.ufocore.client.gui.widget.UfoAe2IconButton;
import com.raishxn.ufocore.client.gui.widget.UfoAtlasButton;
import com.raishxn.ufo.client.gui.widget.UfoQuickBuildButton;
import com.raishxn.ufocore.client.gui.widget.UfoStateIconButton;
import com.raishxn.ufo.client.render.StructureHighlightRenderer;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketScanUniversalStructure;
import com.raishxn.ufo.network.packet.PacketAutoBuildMultiblock;
import com.raishxn.ufo.network.packet.PacketToggleUniversalOverclock;
import com.raishxn.ufo.network.packet.PacketToggleUniversalProcessPaused;
import com.raishxn.ufo.network.packet.PacketToggleUniversalSafeMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public abstract class AbstractUniversalMultiblockControllerScreen<M extends AbstractUniversalMultiblockControllerMenu<?>>
        extends UpgradeableScreen<M> {

    private static final ResourceLocation MAIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/universalgui2.png");
    private static final ResourceLocation PAGE_TEXTURE = UfoMod.id("textures/gui/universalguipages.png");
    private static final int RECIPES_PER_PAGE = 9;
    private static final int[] SLOT_X = {0, 54, 108, 0, 54, 108, 0, 54, 108};
    private static final int[] SLOT_Y = {0, 0, 0, 36, 36, 36, 73, 73, 73};

    private UfoStateIconButton safeModeButton;
    private UfoStateIconButton overclockButton;
    private Button autoBuildButton;
    private UfoAtlasButton previousPageButton;
    private UfoAtlasButton nextPageButton;
    private final List<UfoAtlasButton> processActionButtons = new ArrayList<>();
    private int currentPage;

    protected AbstractUniversalMultiblockControllerScreen(
            M menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;

        this.addToLeftToolbar(new UfoAe2IconButton(
                Icon.SCHEDULING_DEFAULT,
                Component.literal("Scan multiblock structure"),
                button -> {
                    BlockPos pos = this.menu.getBlockEntity().getBlockPos();
                    ModPackets.sendToServer(new PacketScanUniversalStructure(pos));
                    runLocalStructureScan(pos);
                }));

        var quickBuild = new UfoQuickBuildButton(button -> ModPackets.sendToServer(
                new PacketAutoBuildMultiblock(this.menu.getBlockEntity().getBlockPos())));
        quickBuild.setTooltip(Tooltip.create(Component.literal("Auto-build structure")));
        this.autoBuildButton = this.addToLeftToolbar(quickBuild);

        this.safeModeButton = this.addToLeftToolbar(new UfoStateIconButton(
                Component.literal("Safe Mode"), button -> ModPackets.sendToServer(
                        new PacketToggleUniversalSafeMode(this.menu.getBlockEntity().getBlockPos()))));

        this.overclockButton = this.addToLeftToolbar(new UfoStateIconButton(
                Component.literal("Overclock"), button -> ModPackets.sendToServer(
                        new PacketToggleUniversalOverclock(this.menu.getBlockEntity().getBlockPos()))));
    }

    @Override
    protected void init() {
        super.init();
        setSlotsHidden(SlotSemantics.PLAYER_INVENTORY, false);
        setSlotsHidden(SlotSemantics.PLAYER_HOTBAR, false);

        this.previousPageButton = new UfoAtlasButton(
                this.leftPos + 57, this.topPos + 151, 16, 16,
                MAIN_TEXTURE, 256, 256, 0, 0,
                Component.literal("Previous process page"),
                button -> setCurrentPage(this.currentPage - 1));
        this.previousPageButton.setTooltip(Tooltip.create(Component.literal("Previous process page")));
        this.addRenderableWidget(this.previousPageButton);

        this.nextPageButton = new UfoAtlasButton(
                this.leftPos + 105, this.topPos + 151, 16, 16,
                MAIN_TEXTURE, 256, 256, 0, 17,
                Component.literal("Next process page"),
                button -> setCurrentPage(this.currentPage + 1));
        this.nextPageButton.setTooltip(Tooltip.create(Component.literal("Next process page")));
        this.addRenderableWidget(this.nextPageButton);

        this.processActionButtons.clear();
        for (int slot = 0; slot < RECIPES_PER_PAGE; slot++) {
            final int visibleSlot = slot;
            UfoAtlasButton actionButton = new UfoAtlasButton(
                    this.leftPos + 8 + SLOT_X[slot] + 38,
                    this.topPos + 36 + SLOT_Y[slot] + 22,
                    11, 10, MAIN_TEXTURE, 256, 256, 239, 0,
                    Component.literal("Pause process"),
                    button -> toggleVisibleProcess(visibleSlot));
            this.processActionButtons.add(actionButton);
            this.addRenderableWidget(actionButton);
        }
        updatePageButtons();
        updateProcessActionButtons();
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY,
            int mouseX, int mouseY, float partialTick) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTick);
        guiGraphics.blit(PAGE_TEXTURE, this.leftPos + 8, this.topPos + 36,
                0, 0, 160, 107, 256, 256);
        renderInformationBar(guiGraphics);
        renderTemperatureBar(guiGraphics);
        renderProcessPage(guiGraphics);
        renderPageCounter(guiGraphics);
        var supplyArea = getSupplyExclusionArea();
        if (this.menu.hasSupplyWidget()) {
            MultiblockSupplyWidget.render(guiGraphics, this.font, supplyArea.getX(), supplyArea.getY(),
                    this.menu.getSupplyStatus());
        }
    }

    /** Dock below the four upgrade slots to keep both panels accessible. */
    public Rect2i getSupplyExclusionArea() {
        return new Rect2i(this.leftPos + this.imageWidth - 1, this.topPos + 86,
                MultiblockSupplyWidget.WIDTH, MultiblockSupplyWidget.HEIGHT);
    }

    private void renderInformationBar(GuiGraphics guiGraphics) {
        String status = this.menu.isAssembled()
                ? (this.menu.isRunning() ? "RUNNING" : "IDLE")
                : "INCOMPLETE";
        String[] fields = {
                status,
                "MK" + this.menu.getMachineTier(),
                this.menu.isSafeMode() ? "SAFE" : "RISK",
                this.menu.isOverclocked() ? "OC" : "STD"
        };
        for (int i = 0; i < fields.length; i++) {
            int fieldX = this.leftPos + 23 + i * 32;
            int fieldWidth = i == fields.length - 1 ? 33 : 32;
            drawCenteredHalfScaleString(guiGraphics, fields[i], fieldX, this.topPos + 9,
                    fieldWidth, 0xFFE5F4FF);
        }
    }

    private void renderTemperatureBar(GuiGraphics guiGraphics) {
        int availableWidth = 145;
        int filled = Math.round(availableWidth * Math.min(1.0F,
                this.menu.getTemperature() / (float) this.menu.getMaxTemperature()));
        if (filled > 0) {
            guiGraphics.fill(this.leftPos + 16, this.topPos + 20,
                    this.leftPos + 16 + filled, this.topPos + 29, 0xDDB32020);
        }
    }

    private void renderProcessPage(GuiGraphics guiGraphics) {
        List<UniversalDisplayedRecipe> recipes = this.menu.getDisplayedRecipes();
        int firstRecipe = this.currentPage * RECIPES_PER_PAGE;
        for (int slot = 0; slot < RECIPES_PER_PAGE; slot++) {
            int recipeIndex = firstRecipe + slot;
            if (recipeIndex >= recipes.size()) {
                break;
            }
            renderProcessSlot(guiGraphics, recipes.get(recipeIndex), slot);
        }
    }

    private void renderProcessSlot(GuiGraphics guiGraphics, UniversalDisplayedRecipe recipe, int slot) {
        int x = this.leftPos + 8 + SLOT_X[slot];
        int y = this.topPos + 36 + SLOT_Y[slot];
        ItemStack icon = recipe.itemIcon();
        if (icon.isEmpty()) {
            FluidStack fluid = recipe.fluidIcon();
            if (!fluid.isEmpty()) {
                icon = new ItemStack(fluid.getFluid().getBucket());
            }
        }
        if (!icon.isEmpty()) {
            guiGraphics.renderItem(icon, x + 3, y + 3);
        }

        int progressWidth = 0;
        if (recipe.maxProgress() > 0) {
            progressWidth = Math.round(22.0F * Math.min(1.0F,
                    recipe.progress() / (float) recipe.maxProgress()));
        }
        if (progressWidth > 0) {
            guiGraphics.fill(x + 3, y + 23, x + 3 + progressWidth, y + 25, 0xFF4ED5E7);
        }

        String state = recipe.paused() ? "PAUSED" : "RUNNING";
        drawCenteredHalfScaleString(guiGraphics, state, x + 2, y + 27, 24, 5,
                recipe.paused() ? 0xFFFFC36B : 0xFF8DFFB3);
    }

    private void renderPageCounter(GuiGraphics guiGraphics) {
        String pageText = (this.currentPage + 1) + "/" + getPageCount();
        int textX = this.leftPos + 73 + (32 - this.font.width(pageText)) / 2;
        guiGraphics.drawString(this.font, pageText, textX, this.topPos + 155, 0xFFE5F4FF, false);
    }

    private void drawCenteredHalfScaleString(GuiGraphics guiGraphics, String text,
            int x, int y, int availableWidth, int color) {
        drawCenteredHalfScaleString(guiGraphics, text, x, y, availableWidth, 0, color);
    }

    private void drawCenteredHalfScaleString(GuiGraphics guiGraphics, String text,
            int x, int y, int availableWidth, int availableHeight, int color) {
        String clipped = this.font.plainSubstrByWidth(text, availableWidth * 2);
        float renderedWidth = this.font.width(clipped) * 0.5F;
        float renderedHeight = this.font.lineHeight * 0.5F;
        float centeredY = availableHeight > 0
                ? y + (availableHeight - renderedHeight) / 2.0F + 0.5F
                : y;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + (availableWidth - renderedWidth) / 2.0F, centeredY, 0.0F);
        guiGraphics.pose().scale(0.5F, 0.5F, 1.0F);
        guiGraphics.drawString(this.font, clipped, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.autoBuildButton != null) {
            this.autoBuildButton.visible = !this.menu.isAssembled();
            this.autoBuildButton.active = !this.menu.isAssembled();
        }
        setCurrentPage(this.currentPage);
        updateProcessActionButtons();
        boolean controlsAvailable = !this.menu.isRunning() && this.menu.getDisplayedRecipes().isEmpty();
        if (this.safeModeButton != null) {
            boolean safe = this.menu.isSafeMode();
            Component tooltip = Component.literal(safe ? "Safe Mode enabled" : "Safe Mode disabled");
            this.safeModeButton.setMessage(tooltip);
            if (safe) {
                this.safeModeButton.setAtlasSprite(MAIN_TEXTURE, 256, 256, 0, 47, 14, 12);
            } else {
                this.safeModeButton.setAe2Icon(Icon.INVALID);
            }
            this.safeModeButton.active = controlsAvailable;
        }
        if (this.overclockButton != null) {
            boolean overclocked = this.menu.isOverclocked();
            Component tooltip = Component.literal(overclocked ? "Overclock enabled" : "Overclock disabled");
            this.overclockButton.setMessage(tooltip);
            this.overclockButton.setAtlasSprite(MAIN_TEXTURE, 256, 256, 0, 33, 14, 14);
            this.overclockButton.active = controlsAvailable;
        }
    }

    private void setCurrentPage(int requestedPage) {
        this.currentPage = Math.max(0, Math.min(requestedPage, getPageCount() - 1));
        updatePageButtons();
        updateProcessActionButtons();
    }

    private int getPageCount() {
        int capacity = Math.max(this.menu.getMaxParallels(), this.menu.getDisplayedRecipes().size());
        return Math.max(1, (capacity + RECIPES_PER_PAGE - 1) / RECIPES_PER_PAGE);
    }

    private void updatePageButtons() {
        if (this.previousPageButton != null) {
            this.previousPageButton.active = this.currentPage > 0;
        }
        if (this.nextPageButton != null) {
            this.nextPageButton.active = this.currentPage + 1 < getPageCount();
        }
    }

    private void updateProcessActionButtons() {
        List<UniversalDisplayedRecipe> recipes = this.menu.getDisplayedRecipes();
        int firstRecipe = this.currentPage * RECIPES_PER_PAGE;
        for (int slot = 0; slot < this.processActionButtons.size(); slot++) {
            UfoAtlasButton button = this.processActionButtons.get(slot);
            int recipeIndex = firstRecipe + slot;
            if (recipeIndex >= recipes.size() || recipes.get(recipeIndex).processIndex() < 0) {
                button.visible = false;
                button.active = false;
                continue;
            }
            UniversalDisplayedRecipe recipe = recipes.get(recipeIndex);
            button.visible = true;
            button.active = true;
            button.setSource(recipe.paused() ? 227 : 239, 0);
            button.setMessage(Component.literal(recipe.paused() ? "Resume process" : "Pause process"));
            button.setTooltip(Tooltip.create(button.getMessage()));
        }
    }

    private void toggleVisibleProcess(int visibleSlot) {
        int recipeIndex = this.currentPage * RECIPES_PER_PAGE + visibleSlot;
        List<UniversalDisplayedRecipe> recipes = this.menu.getDisplayedRecipes();
        if (recipeIndex < 0 || recipeIndex >= recipes.size()) {
            return;
        }
        int processIndex = recipes.get(recipeIndex).processIndex();
        if (processIndex < 0) {
            return;
        }
        ModPackets.sendToServer(new PacketToggleUniversalProcessPaused(
                this.menu.getBlockEntity().getBlockPos(), processIndex));
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var supplyArea = getSupplyExclusionArea();
        if (this.menu.hasSupplyWidget() && supplyArea.contains(mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, MultiblockSupplyWidget.tooltip(
                    this.menu.getSupplyStatus(), mouseY - supplyArea.getY(), this.menu.isSafeMode(),
                    this.menu.isOverclocked(), this.menu.getMaxParallels()).stream()
                    .map(Component::getVisualOrderText).toList(), mouseX, mouseY);
            return;
        }
        if (isHovering(15, 19, 147, 11, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal("Temperature: " + this.menu.getTemperature()
                            + " / " + this.menu.getMaxTemperature()),
                    mouseX, mouseY);
            return;
        }
        UniversalDisplayedRecipe recipe = getHoveredRecipe(mouseX, mouseY);
        if (recipe != null) {
            guiGraphics.renderTooltip(this.font,
                    buildRecipeTooltip(recipe).stream().map(Component::getVisualOrderText).toList(),
                    mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private UniversalDisplayedRecipe getHoveredRecipe(int mouseX, int mouseY) {
        List<UniversalDisplayedRecipe> recipes = this.menu.getDisplayedRecipes();
        int firstRecipe = this.currentPage * RECIPES_PER_PAGE;
        for (int slot = 0; slot < RECIPES_PER_PAGE; slot++) {
            int recipeIndex = firstRecipe + slot;
            if (recipeIndex >= recipes.size()) {
                return null;
            }
            int x = this.leftPos + 8 + SLOT_X[slot];
            int y = this.topPos + 36 + SLOT_Y[slot];
            if (mouseX >= x && mouseX < x + 52 && mouseY >= y && mouseY < y + 34) {
                return recipes.get(recipeIndex);
            }
        }
        return null;
    }

    private List<Component> buildRecipeTooltip(UniversalDisplayedRecipe recipe) {
        List<Component> lines = new ArrayList<>();
        lines.add(recipe.label());
        lines.add(Component.literal("Output: " + formatAmount(recipe.outputAmount())
                + (recipe.fluidIcon().isEmpty() ? "x" : " mB")));
        lines.add(Component.literal("Progress: " + formatSeconds(recipe.progress())
                + " / " + formatSeconds(recipe.maxProgress()) + " s"));
        return lines;
    }

    private static String formatAmount(long amount) {
        if (amount >= 1_000_000_000L) {
            return String.format(Locale.ROOT, "%.1fB", amount / 1_000_000_000.0D);
        }
        if (amount >= 1_000_000L) {
            return String.format(Locale.ROOT, "%.1fM", amount / 1_000_000.0D);
        }
        if (amount >= 1_000L) {
            return String.format(Locale.ROOT, "%.1fK", amount / 1_000.0D);
        }
        return Long.toString(amount);
    }

    private static String formatSeconds(int ticks) {
        double seconds = ticks / 20.0D;
        return seconds >= 100.0D
                ? String.format(Locale.ROOT, "%.0f", seconds)
                : String.format(Locale.ROOT, "%.1f", seconds);
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

        var state = this.minecraft.level.getBlockState(pos);
        Direction facing = MultiblockControllerDefinitions.getPatternFacing(blockEntity, state);

        MultiblockPattern.MatchResult result = definition.get().pattern().match(this.minecraft.level, pos, facing);
        if (result.isValid()) {
            if (controller.isAssembled()) {
                this.minecraft.player.displayClientMessage(
                        Component.translatable("message.ufo.structure_formed").withStyle(ChatFormatting.GREEN), true);
            } else {
                this.minecraft.player.displayClientMessage(
                        definition.get().name().copy().append(Component.literal(
                                ": structure shape is valid, but extra controller validation failed.")
                                .withStyle(ChatFormatting.RED)),
                        false);
            }
            return;
        }

        reportStructureErrors(definition.get(), result.allErrors());
    }

    private void reportStructureErrors(
            MultiblockControllerDefinition definition, List<MultiblockPattern.PatternError> errors) {
        if (this.minecraft == null || this.minecraft.player == null || errors == null || errors.isEmpty()) {
            return;
        }

        int shown = Math.min(errors.size(), 10);
        this.minecraft.player.displayClientMessage(
                definition.name().copy()
                        .append(Component.literal(": " + errors.size() + " block(s) missing or misplaced.")
                                .withStyle(ChatFormatting.RED)),
                false);

        for (int i = 0; i < shown; i++) {
            var error = errors.get(i);
            BlockPos errorPos = error.pos();
            Component message = Component.literal("  [" + errorPos.getX() + ", "
                            + errorPos.getY() + ", " + errorPos.getZ() + "] Expected: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(error.expected().copy().withStyle(ChatFormatting.YELLOW));
            this.minecraft.player.displayClientMessage(message, false);
        }

        if (errors.size() > shown) {
            this.minecraft.player.displayClientMessage(
                    Component.literal("  ... and " + (errors.size() - shown) + " more.")
                            .withStyle(ChatFormatting.GRAY),
                    false);
        }

        int maxHighlight = Math.min(errors.size(), 50);
        for (int i = 0; i < maxHighlight; i++) {
            StructureHighlightRenderer.highlight(errors.get(i).pos(), 5000);
        }
    }
}

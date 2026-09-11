package com.raishxn.ufo.screen;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Lightweight 54-slot page over the Matrix's large server-side pattern library. */
public final class QuantumPatternMatrixPatternScreen
        extends AbstractContainerScreen<QuantumPatternMatrixPatternMenu> {
    private static final int GUI_WIDTH = 190;
    private static final int GUI_HEIGHT = 224;
    private static final int SLOT_SPACING = 18;
    private static final int SEARCH_X = 78;
    private static final int SEARCH_Y = 7;
    private static final int SEARCH_WIDTH = 93;
    private static final int SCROLLBAR_X = 175;
    private static final int SCROLLBAR_Y = 22;
    private static final int SCROLLBAR_HEIGHT = 104;
    private static final int HANDLE_HEIGHT = 15;
    private static final int PATTERN_AREA_X = 12;
    private static final int PATTERN_AREA_Y = 21;
    private static final int PATTERN_AREA_WIDTH = 162;
    private static final int PATTERN_AREA_HEIGHT = 108;

    private final Set<Integer> matchingSlots = new HashSet<>();
    private final Map<String, Boolean> nameMatchCache = new HashMap<>();
    private EditBox searchField;
    private boolean draggingScrollbar;
    private long lastPatternRevision = -1;
    private int lastPage = -1;
    private int requestedPage = -1;

    public QuantumPatternMatrixPatternScreen(QuantumPatternMatrixPatternMenu menu,
                                              Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        titleLabelY = 10_000;
        inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        searchField = new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y,
                SEARCH_WIDTH, 11,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.search"));
        searchField.setBordered(false);
        searchField.setMaxLength(64);
        searchField.setTextColor(0xFFE8F6FF);
        searchField.setHint(Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.search_page")
                .copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x71879A))));
        searchField.setResponder(ignored -> refreshMatches());
        addRenderableWidget(searchField);
        setInitialFocus(searchField);
        refreshMatches();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        int page = menu.getCurrentPage();
        if (page != lastPage || menu.getPatternContentRevision() != lastPatternRevision) {
            lastPage = page;
            requestedPage = page;
            refreshMatches();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF20B1020);
        outline(graphics, x, y, imageWidth, imageHeight, 0xFF314660);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 3, 0xFF58E6FF);
        graphics.fill(x + 95, y + 1, x + imageWidth - 1, y + 3, 0xFFB268FF);
        graphics.fill(x + 75, y + 5, x + 174, y + 19, 0xFF10192A);
        outline(graphics, x + 75, y + 5, 99, 14, 0xFF30475F);

        graphics.fill(x + 10, y + 20, x + 174, y + 130, 0xFF11182A);
        outline(graphics, x + 10, y + 20, 164, 110, 0xFF263A54);
        for (int row = 0; row < QuantumPatternMatrixPatternMenu.VISIBLE_ROWS; row++) {
            for (int column = 0; column < QuantumPatternMatrixPatternMenu.COLUMNS; column++) {
                int slot = row * QuantumPatternMatrixPatternMenu.COLUMNS + column;
                int slotX = x + QuantumPatternMatrixPatternMenu.PATTERN_X + column * SLOT_SPACING;
                int slotY = y + QuantumPatternMatrixPatternMenu.PATTERN_Y + row * SLOT_SPACING;
                slotBackground(graphics, slotX, slotY);
                if (searchField != null && !searchField.getValue().isBlank()) {
                    int color = matchingSlots.contains(slot) ? 0x7000FFB0 : 0x70000000;
                    graphics.fill(slotX, slotY, slotX + 16, slotY + 16, color);
                }
            }
        }
        graphics.fill(x + SCROLLBAR_X, y + SCROLLBAR_Y,
                x + SCROLLBAR_X + 11, y + SCROLLBAR_Y + SCROLLBAR_HEIGHT, 0xFF101827);
        int handleY = handleY();
        int handleColor = draggingScrollbar || isMouseOverHandle(mouseX, mouseY, handleY)
                ? 0xFFB268FF : 0xFF58E6FF;
        graphics.fill(x + SCROLLBAR_X + 2, y + handleY,
                x + SCROLLBAR_X + 9, y + handleY + HANDLE_HEIGHT, handleColor);

        graphics.fill(x + 10, y + 136, x + 174, y + 221, 0xFF11182A);
        outline(graphics, x + 10, y + 136, 164, 85, 0xFF263A54);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slotBackground(graphics, x + QuantumPatternMatrixPatternMenu.PLAYER_INVENTORY_X + column * 18,
                        y + QuantumPatternMatrixPatternMenu.PLAYER_INVENTORY_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            slotBackground(graphics, x + QuantumPatternMatrixPatternMenu.PLAYER_INVENTORY_X + column * 18,
                    y + QuantumPatternMatrixPatternMenu.PLAYER_HOTBAR_Y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font,
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.patterns_short"),
                8, 8, 0xFFE8F6FF, false);
        Component page = Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.page",
                menu.getCurrentPage() + 1, menu.getPageCount(), menu.getTotalPatternSlots());
        graphics.drawCenteredString(font, page, 92, 129, 0xFF8DA4B8);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && searchField != null && searchField.isMouseOver(mouseX, mouseY)) {
            searchField.setValue("");
            return true;
        }
        if (button == 0 && isWithinScrollbar(mouseX, mouseY) && menu.getPageCount() > 1) {
            draggingScrollbar = true;
            updatePageFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            updatePageFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isWithinPatternArea(mouseX, mouseY) && scrollY != 0.0D && menu.getPageCount() > 1) {
            requestPage(menu.getCurrentPage() - (int) Math.signum(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void refreshMatches() {
        matchingSlots.clear();
        nameMatchCache.clear();
        List<String> tokens = tokenize(searchField == null ? "" : searchField.getValue());
        if (!tokens.isEmpty()) {
            for (int slot = 0; slot < menu.getPatternSlots().size(); slot++) {
                if (patternMatches(menu.getPatternSlots().get(slot).getItem(), tokens)) matchingSlots.add(slot);
            }
        }
        lastPatternRevision = menu.getPatternContentRevision();
    }

    private boolean patternMatches(ItemStack stack, List<String> tokens) {
        if (stack.isEmpty() || minecraft == null || minecraft.level == null) return false;
        IPatternDetails details;
        try {
            details = PatternDetailsHelper.decodePattern(stack, minecraft.level);
        } catch (RuntimeException ignored) {
            return false;
        }
        if (details == null) return false;
        for (var output : details.getOutputs()) {
            if (output != null && nameMatches(output.what().getDisplayName().getString(), tokens)) return true;
        }
        for (var input : details.getInputs()) {
            if (input == null) continue;
            for (var possible : input.getPossibleInputs()) {
                if (possible != null && nameMatches(possible.what().getDisplayName().getString(), tokens)) return true;
            }
        }
        return false;
    }

    private boolean nameMatches(String name, List<String> tokens) {
        return nameMatchCache.computeIfAbsent(name, ignored -> {
            String lower = name.toLowerCase(Locale.ROOT);
            for (String token : tokens) if (!lower.contains(token)) return false;
            return true;
        });
    }

    private static List<String> tokenize(String text) {
        return text == null || text.isBlank()
                ? List.of()
                : java.util.Arrays.stream(text.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                        .filter(token -> !token.isBlank()).toList();
    }

    private int handleY() {
        int maxPage = menu.getPageCount() - 1;
        if (maxPage <= 0) return SCROLLBAR_Y;
        return SCROLLBAR_Y + Math.round((float) menu.getCurrentPage() / maxPage
                * (SCROLLBAR_HEIGHT - HANDLE_HEIGHT));
    }

    private void updatePageFromMouse(double mouseY) {
        double relative = mouseY - (topPos + SCROLLBAR_Y) - HANDLE_HEIGHT / 2.0D;
        double fraction = Mth.clamp(relative / (SCROLLBAR_HEIGHT - HANDLE_HEIGHT), 0.0D, 1.0D);
        requestPage((int) Math.round(fraction * (menu.getPageCount() - 1)));
    }

    private void requestPage(int page) {
        int clamped = Mth.clamp(page, 0, menu.getPageCount() - 1);
        if (clamped == requestedPage || minecraft == null || minecraft.gameMode == null) return;
        requestedPage = clamped;
        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, QuantumPatternMatrixPatternMenu.pageButton(clamped));
    }

    private boolean isWithinPatternArea(double mouseX, double mouseY) {
        return mouseX >= leftPos + PATTERN_AREA_X && mouseX < leftPos + PATTERN_AREA_X + PATTERN_AREA_WIDTH
                && mouseY >= topPos + PATTERN_AREA_Y && mouseY < topPos + PATTERN_AREA_Y + PATTERN_AREA_HEIGHT;
    }

    private boolean isWithinScrollbar(double mouseX, double mouseY) {
        return mouseX >= leftPos + SCROLLBAR_X && mouseX < leftPos + SCROLLBAR_X + 11
                && mouseY >= topPos + SCROLLBAR_Y && mouseY < topPos + SCROLLBAR_Y + SCROLLBAR_HEIGHT;
    }

    private boolean isMouseOverHandle(double mouseX, double mouseY, int handleY) {
        return mouseX >= leftPos + SCROLLBAR_X && mouseX < leftPos + SCROLLBAR_X + 11
                && mouseY >= topPos + handleY && mouseY < topPos + handleY + HANDLE_HEIGHT;
    }

    private static void slotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF263A54);
        graphics.fill(x, y, x + 16, y + 16, 0xFF0C1321);
    }

    private static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}

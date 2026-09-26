package com.raishxn.ufo.screen;

import com.raishxn.ufo.util.UfoText;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.AECheckbox;
import appeng.menu.SlotSemantics;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.client.gui.widget.UfoQuickBuildButton;
import com.raishxn.ufo.client.render.StructureHighlightRenderer;
import com.raishxn.ufo.crafting.SingularityCraftingMode;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketAutoBuildMultiblock;
import com.raishxn.ufo.network.packet.PacketScanUniversalStructure;
import com.raishxn.ufocore.client.gui.widget.UfoAe2IconButton;
import guideme.PageAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

/**
 * Paged adaptation of AdvancedAE's Quantum Crafter interface.
 * Original UI design: AdvancedAE by PedroKS (GNU LGPL-3.0).
 */
public final class InfinityFabricationSingularityScreen
        extends AEBaseScreen<InfinityFabricationSingularityMenu> {
    private final PageButton previousPage;
    private final PageButton nextPage;
    private final ModeButton modeButton;
    private final UfoQuickBuildButton autoBuildButton;
    private final List<ConfigButton> configButtons = new ArrayList<>();
    private final List<AECheckbox> enableButtons = new ArrayList<>();
    private int requestedPage = -1;

    public InfinityFabricationSingularityScreen(InfinityFabricationSingularityMenu menu,
                                                 Inventory inventory, Component title,
                                                 ScreenStyle style) {
        super(menu, inventory, title, style);

        previousPage = new PageButton(false, ignored -> requestPage(menu.getCurrentPage() - 1));
        previousPage.setMessage(Component.translatable(
                "gui.ufo.infinity_fabrication_singularity.previous_page"));
        addToLeftToolbar(previousPage);

        nextPage = new PageButton(true, ignored -> requestPage(menu.getCurrentPage() + 1));
        nextPage.setMessage(Component.translatable(
                "gui.ufo.infinity_fabrication_singularity.next_page"));
        addToLeftToolbar(nextPage);

        for (int slot = 0; slot < InfinityFabricationSingularityMenu.PATTERN_SLOTS; slot++) {
            int visibleSlot = slot;
            ConfigButton configButton = new ConfigButton(ignored -> showPatternConfigInfo(visibleSlot));
            widgets.add("cfgButton" + (slot + 1), configButton);
            configButtons.add(configButton);

            AECheckbox enableButton = widgets.addCheckbox(
                    "enableButton" + (slot + 1), Component.empty(),
                    () -> menu.toggleVisiblePattern(visibleSlot));
            enableButton.setRadio(true);
            enableButton.setTooltip(Tooltip.create(Component.translatable(
                    "gui.ufo.infinity_fabrication_singularity.enable_pattern")));
            enableButtons.add(enableButton);
        }

        modeButton = addToLeftToolbar(new ModeButton(ignored -> { }));
        modeButton.active = false;
        addToLeftToolbar(new UfoAe2IconButton(Icon.SCHEDULING_DEFAULT,
                Component.translatable("gui.ufo.infinity_fabrication_singularity.scan"),
                ignored -> scanStructure()));
        autoBuildButton = addToLeftToolbar(new UfoQuickBuildButton(ignored ->
                ModPackets.sendToServer(new PacketAutoBuildMultiblock(
                        menu.getBlockEntity().getBlockPos()))));
        autoBuildButton.setTooltip(Tooltip.create(Component.translatable(
                "gui.ufo.infinity_fabrication_singularity.auto_build")));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (requestedPage < 0 || requestedPage == menu.getCurrentPage()) {
            requestedPage = menu.getCurrentPage();
        }
        previousPage.active = menu.getCurrentPage() > 0;
        nextPage.active = menu.getCurrentPage() + 1 < menu.getPageCount();
        autoBuildButton.visible = !menu.isFormed();
        autoBuildButton.active = !menu.isFormed();

        setTextContent("dialog_title", Component.translatable(
                "gui.ufo.infinity_fabrication_singularity.title_page",
                menu.getCurrentPage() + 1, menu.getPageCount()));
        var patternSlots = menu.getSlots(SlotSemantics.MACHINE_INPUT);
        for (int slot = 0; slot < enableButtons.size(); slot++) {
            boolean occupied = slot < patternSlots.size() && patternSlots.get(slot).hasItem();
            configButtons.get(slot).active = occupied;
            enableButtons.get(slot).active = occupied;
            enableButtons.get(slot).setSelected(menu.isVisiblePatternEnabled(slot));
        }
    }

    private Component currentStatus() {
        if (!menu.isFormed()) {
            return Component.translatable("gui.ufo.infinity_fabrication_singularity.incomplete")
                    .withStyle(ChatFormatting.RED);
        }
        if (!menu.isGridActive()) {
            return Component.translatable("gui.ufo.infinity_fabrication_singularity.grid_offline")
                    .withStyle(ChatFormatting.GOLD);
        }
        if (menu.getPatternCount() <= 0) {
            return Component.translatable("gui.ufo.infinity_fabrication_singularity.no_patterns")
                    .withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable("gui.ufo.infinity_fabrication_singularity.online")
                .withStyle(ChatFormatting.GREEN);
    }

    private void showPatternConfigInfo(int slot) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable(
                    "gui.ufo.infinity_fabrication_singularity.aggregate_pattern_config"), true);
        }
    }

    private void requestPage(int page) {
        int clamped = Math.max(0, Math.min(page, menu.getPageCount() - 1));
        if (clamped == requestedPage || minecraft == null || minecraft.gameMode == null) return;
        requestedPage = clamped;
        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, InfinityFabricationSingularityMenu.pageButton(clamped));
    }

    private void scanStructure() {
        BlockPos controllerPos = menu.getBlockEntity().getBlockPos();
        ModPackets.sendToServer(new PacketScanUniversalStructure(controllerPos));
        if (minecraft == null || minecraft.level == null || minecraft.player == null) return;
        var blockEntity = minecraft.level.getBlockEntity(controllerPos);
        var definition = MultiblockControllerDefinitions.getDefinition(blockEntity);
        if (definition.isEmpty()) return;
        var state = minecraft.level.getBlockState(controllerPos);
        var facing = MultiblockControllerDefinitions.getPatternFacing(blockEntity, state);
        var result = definition.get().pattern().match(minecraft.level, controllerPos, facing);
        if (result.isValid()) {
            minecraft.player.displayClientMessage(
                    Component.translatable("message.ufo.structure_formed").withStyle(ChatFormatting.GREEN), true);
            return;
        }
        minecraft.player.displayClientMessage(
                UfoText.literal("gui.ufo.text.s_block_s_missing_or_misplaced", result.allErrors().size())
                        .withStyle(ChatFormatting.RED), false);
        for (int index = 0; index < Math.min(50, result.allErrors().size()); index++) {
            StructureHighlightRenderer.highlight(result.allErrors().get(index).pos(), 5000);
        }
    }

    @Override
    protected @Nullable PageAnchor getHelpTopic() {
        return new PageAnchor(UfoMod.id("ufo_intro/infinity_fabrication_singularity.md"), null);
    }

    private static final class PageButton extends IconButton {
        private final boolean forward;

        private PageButton(boolean forward, OnPress onPress) {
            super(onPress);
            this.forward = forward;
        }

        @Override
        protected Icon getIcon() {
            return forward ? Icon.ARROW_RIGHT : Icon.ARROW_LEFT;
        }
    }

    private static final class ConfigButton extends IconButton {
        private ConfigButton(OnPress onPress) {
            super(onPress);
            setDisableBackground(true);
            setMessage(Component.translatable(
                    "gui.ufo.infinity_fabrication_singularity.configure_pattern"));
        }

        @Override
        protected Icon getIcon() {
            return isHoveredOrFocused() ? Icon.COG : Icon.COG_DISABLED;
        }
    }

    private final class ModeButton extends IconButton {
        private ModeButton(OnPress onPress) {
            super(onPress);
        }

        @Override
        protected Icon getIcon() {
            return switch (menu.getCraftingMode()) {
                case BALANCED -> Icon.SCHEDULING_ROUND_ROBIN;
                case SPEED -> Icon.SCHEDULING_DEFAULT;
                case EFFICIENCY -> Icon.SCHEDULING_RANDOM;
            };
        }

        @Override
        public List<Component> getTooltipMessage() {
            String suffix = menu.getCraftingMode().name().toLowerCase(java.util.Locale.ROOT);
            return List.of(
                    Component.translatable("gui.ufo.infinity_fabrication_singularity.mode." + suffix),
                    Component.translatable("gui.ufo.infinity_fabrication_singularity.mode." + suffix + ".detail"),
                    Component.translatable("gui.ufo.infinity_fabrication_singularity.mode.cycle"));
        }
    }
}

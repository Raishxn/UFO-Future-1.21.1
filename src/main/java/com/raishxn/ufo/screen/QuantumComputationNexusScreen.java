package com.raishxn.ufo.screen;

import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.OpenGuideButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.core.network.serverbound.SwitchGuisPacket;
import appeng.items.tools.GuideItem;
import appeng.menu.implementations.PriorityMenu;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.client.gui.widget.UfoQuickBuildButton;
import com.raishxn.ufo.client.render.StructureHighlightRenderer;
import com.raishxn.ufo.network.ModPackets;
import com.raishxn.ufo.network.packet.PacketAutoBuildMultiblock;
import com.raishxn.ufo.network.packet.PacketScanUniversalStructure;
import com.raishxn.ufocore.client.gui.widget.UfoAe2IconButton;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigInteger;

/** Compact AE-style status dashboard for the Quantum Computation Nexus. */
public final class QuantumComputationNexusScreen extends AbstractContainerScreen<QuantumComputationNexusMenu> {
    private static final int PANEL_WIDTH = 252;
    private static final int PANEL_HEIGHT = 168;
    private static final int BACKGROUND = 0xF20B1020;
    private static final int PANEL = 0xFF151D32;
    private static final int PANEL_ALT = 0xFF11182A;
    private static final int CYAN = 0xFF58E6FF;
    private static final int PURPLE = 0xFFB268FF;
    private static final int TEXT = 0xFFE8F6FF;
    private static final int MUTED = 0xFF8DA4B8;
    private VerticalButtonBar leftToolbar;
    private Button autoBuildButton;

    public QuantumComputationNexusScreen(QuantumComputationNexusMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        titleLabelY = 9;
        inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        this.leftToolbar = new VerticalButtonBar();

        this.leftToolbar.add(new OpenGuideButton(button -> {
            if (minecraft != null && minecraft.player != null) {
                GuidesCommon.openGuide(minecraft.player, GuideItem.GUIDE_ID,
                        new PageAnchor(UfoMod.id("ufo_intro/quantum_computation_nexus.md"), null));
            }
        }));

        this.leftToolbar.add(new UfoAe2IconButton(
                Icon.SCHEDULING_DEFAULT,
                Component.literal("Scan multiblock structure"),
                button -> scanStructure()));

        UfoQuickBuildButton quickBuild = new UfoQuickBuildButton(button ->
                ModPackets.sendToServer(new PacketAutoBuildMultiblock(menu.getBlockEntity().getBlockPos())));
        quickBuild.setTooltip(Tooltip.create(Component.literal("Auto-build structure")));
        this.autoBuildButton = quickBuild;
        this.leftToolbar.add(quickBuild);

        this.leftToolbar.setPosition(new Point(3, 1));
        this.leftToolbar.populateScreen(this::addRenderableWidget,
                new Rect2i(leftPos, topPos, imageWidth, imageHeight), null);

        Component priorityLabel = Component.translatable("gui.ufo.quantum_computation_nexus.cpu_priority");
        TabButton priorityButton = new TabButton(
                Icon.PRIORITY,
                priorityLabel,
                button -> PacketDistributor.sendToServer(SwitchGuisPacket.openSubMenu(PriorityMenu.TYPE)));
        priorityButton.setTooltip(Tooltip.create(priorityLabel));
        priorityButton.setSize(20, 20);
        priorityButton.setPosition(leftPos + imageWidth - 24, topPos - 5);
        addRenderableWidget(priorityButton);

        updateToolbarState();
        this.leftToolbar.updateBeforeRender();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateToolbarState();
        this.leftToolbar.updateBeforeRender();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderMetricTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, BACKGROUND);
        outline(graphics, x, y, imageWidth, imageHeight, 0xFF314660);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 3, CYAN);
        graphics.fill(x + imageWidth / 2, y + 1, x + imageWidth - 1, y + 3, PURPLE);

        panel(graphics, x + 10, y + 27, 232, 29, PANEL_ALT);
        panel(graphics, x + 10, y + 64, 111, 57, PANEL);
        panel(graphics, x + 131, y + 64, 111, 57, PANEL);
        panel(graphics, x + 10, y + 129, 232, 29, PANEL_ALT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font,
                Component.translatable("gui.ufo.quantum_computation_nexus.title"),
                imageWidth / 2, titleLabelY, TEXT);

        Component status;
        int statusColor;
        if (!menu.isFormed()) {
            status = Component.translatable("gui.ufo.quantum_computation_nexus.incomplete");
            statusColor = 0xFFFF667A;
        } else if (!menu.isGridActive()) {
            status = Component.translatable("gui.ufo.quantum_computation_nexus.grid_offline");
            statusColor = 0xFFFFB85C;
        } else if (!menu.isCpuOnline()) {
            status = Component.translatable("gui.ufo.quantum_computation_nexus.no_storage");
            statusColor = 0xFFFFD166;
        } else if (menu.isInfiniteMode()) {
            status = Component.translatable("gui.ufo.quantum_computation_nexus.online_infinite");
            statusColor = 0xFFFFD86B;
        } else {
            status = Component.translatable("gui.ufo.quantum_computation_nexus.online");
            statusColor = 0xFF5DFFA2;
        }
        graphics.drawString(font, Component.translatable("gui.ufo.quantum_computation_nexus.status"), 18, 34, MUTED, false);
        graphics.drawString(font, status, 18, 45, statusColor, false);
        String nodes = menu.getModuleCount() + "  •  " + menu.getStorageModuleCount() + " S  •  "
                + menu.getCoProcessorModuleCount() + " C";
        graphics.drawString(font, nodes, imageWidth - 18 - font.width(nodes), 39, TEXT, false);

        graphics.drawString(font, Component.translatable("gui.ufo.quantum_computation_nexus.storage"), 18, 73, MUTED, false);
        graphics.drawCenteredString(font, menu.isInfiniteMode() ? "∞" : formatBinary(menu.getStorageBytes()),
                65, 91, CYAN);
        graphics.drawString(font, Component.translatable("gui.ufo.quantum_computation_nexus.bytes"), 18, 108, MUTED, false);

        graphics.drawString(font, Component.translatable("gui.ufo.quantum_computation_nexus.parallelism"), 139, 73, MUTED, false);
        graphics.drawCenteredString(font, menu.isInfiniteMode() ? "∞" : formatDecimal(menu.getParallelLanes()),
                186, 91, PURPLE);
        graphics.drawString(font, Component.translatable("gui.ufo.quantum_computation_nexus.lanes"), 139, 108, MUTED, false);

        graphics.drawString(font, Component.translatable("gui.ufo.quantum_computation_nexus.modules"), 18, 138, MUTED, false);
        Component breakdown = Component.translatable("gui.ufo.quantum_computation_nexus.module_breakdown",
                menu.getStorageModuleCount(), menu.getCoProcessorModuleCount(), menu.getCpuPartitionCount());
        graphics.drawString(font, breakdown, 18, 149, TEXT, false);
    }

    private void renderMetricTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (inside(mouseX, mouseY, leftPos + 10, topPos + 64, 111, 57)) {
            var tooltip = Component.literal(menu.getStorageBytes() + " ")
                    .append(Component.translatable("gui.ufo.quantum_computation_nexus.bytes"));
            if (menu.isInfiniteMode()) {
                tooltip.append(" • ")
                        .append(Component.translatable("gui.ufo.quantum_computation_nexus.infinite_active"));
            }
            graphics.renderTooltip(font, tooltip.withStyle(ChatFormatting.AQUA), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + 131, topPos + 64, 111, 57)) {
            var tooltip = Component.literal(menu.getParallelLanes() + " ")
                    .append(Component.translatable("gui.ufo.quantum_computation_nexus.lanes"));
            if (menu.isInfiniteMode()) {
                tooltip.append(" • ")
                        .append(Component.translatable("gui.ufo.quantum_computation_nexus.infinite_active"));
            }
            graphics.renderTooltip(font, tooltip.withStyle(ChatFormatting.LIGHT_PURPLE), mouseX, mouseY);
        }
    }

    private void updateToolbarState() {
        if (autoBuildButton != null) {
            autoBuildButton.visible = !menu.isFormed();
            autoBuildButton.active = !menu.isFormed();
        }
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

        int shown = Math.min(10, result.allErrors().size());
        minecraft.player.displayClientMessage(
                Component.literal(result.allErrors().size() + " block(s) missing or misplaced.")
                        .withStyle(ChatFormatting.RED), false);
        for (int index = 0; index < shown; index++) {
            var error = result.allErrors().get(index);
            minecraft.player.displayClientMessage(
                    Component.literal("  [" + error.pos().getX() + ", " + error.pos().getY() + ", "
                                    + error.pos().getZ() + "] Expected: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(error.expected().copy().withStyle(ChatFormatting.YELLOW)), false);
        }
        for (int index = 0; index < Math.min(50, result.allErrors().size()); index++) {
            StructureHighlightRenderer.highlight(result.allErrors().get(index).pos(), 5000);
        }
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static void panel(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
        outline(graphics, x, y, width, height, 0xFF263A54);
    }

    private static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static String formatBinary(BigInteger amount) {
        String[] units = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};
        BigInteger divisor = BigInteger.ONE;
        int unit = 0;
        while (unit < units.length - 1 && amount.compareTo(divisor.shiftLeft(10)) >= 0) {
            divisor = divisor.shiftLeft(10);
            unit++;
        }
        return compact(amount, divisor) + " " + units[unit];
    }

    private static String formatDecimal(BigInteger amount) {
        String[] units = {"", "K", "M", "B", "T", "Q"};
        BigInteger divisor = BigInteger.ONE;
        int unit = 0;
        while (unit < units.length - 1 && amount.compareTo(divisor.multiply(BigInteger.valueOf(1000))) >= 0) {
            divisor = divisor.multiply(BigInteger.valueOf(1000));
            unit++;
        }
        return compact(amount, divisor) + units[unit];
    }

    private static String compact(BigInteger amount, BigInteger divisor) {
        BigInteger[] parts = amount.divideAndRemainder(divisor);
        if (divisor.equals(BigInteger.ONE) || parts[1].signum() == 0) return parts[0].toString();
        int decimal = parts[1].multiply(BigInteger.TEN).divide(divisor).intValue();
        return decimal == 0 ? parts[0].toString() : parts[0] + "." + decimal;
    }
}

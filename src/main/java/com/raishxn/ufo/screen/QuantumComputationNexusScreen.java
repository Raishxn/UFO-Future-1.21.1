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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigInteger;

/** Compact AE-style status dashboard for the Quantum Computation Nexus. */
public final class QuantumComputationNexusScreen extends AbstractContainerScreen<QuantumComputationNexusMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/quantumcomputation.png");
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 127;
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
        titleLabelY = 10_000;
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
        priorityButton.setPosition(leftPos + imageWidth - 3, topPos + 5);
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
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        leftToolbar.drawBackgroundLayer(graphics,
                new Rect2i(leftPos, topPos, imageWidth, imageHeight), Point.ZERO);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        drawCenteredFitted(graphics,
                Component.translatable("gui.ufo.quantum_computation_nexus.title"),
                14, 9, 160, 1.0F, TEXT);

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
        drawCenteredFitted(graphics, Component.translatable("gui.ufo.quantum_computation_nexus.status"),
                13, 34, 159, 0.75F, MUTED);
        drawCenteredFitted(graphics, status, 13, 44, 159, 0.8F, statusColor);

        drawCenteredFitted(graphics, Component.translatable("gui.ufo.quantum_computation_nexus.storage"),
                13, 61, 78, 0.72F, MUTED);
        drawCenteredFitted(graphics, Component.literal(menu.isInfiniteMode() ? "∞" : formatBinary(menu.getStorageBytes())),
                13, 73, 78, 1.0F, CYAN);
        drawCenteredFitted(graphics, Component.translatable("gui.ufo.quantum_computation_nexus.bytes"),
                13, 85, 78, 0.75F, MUTED);

        drawCenteredFitted(graphics, Component.translatable("gui.ufo.quantum_computation_nexus.parallelism"),
                94, 61, 159, 0.72F, MUTED);
        drawCenteredFitted(graphics, Component.literal(menu.isInfiniteMode() ? "∞" : formatDecimal(menu.getParallelLanes())),
                94, 73, 159, 1.0F, PURPLE);
        drawCenteredFitted(graphics, Component.translatable("gui.ufo.quantum_computation_nexus.lanes"),
                94, 85, 159, 0.75F, MUTED);

        Component breakdown = Component.translatable("gui.ufo.quantum_computation_nexus.module_breakdown",
                menu.getStorageModuleCount(), menu.getCoProcessorModuleCount(), menu.getCpuPartitionCount());
        Component summary = Component.empty()
                .append(Component.translatable("gui.ufo.quantum_computation_nexus.modules"))
                .append("  •  ").append(breakdown);
        drawCenteredFitted(graphics, summary, 13, 103, 159, 0.72F, TEXT);
    }

    private void renderMetricTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (inside(mouseX, mouseY, leftPos + 13, topPos + 60, 66, 36)) {
            var tooltip = Component.literal(menu.getStorageBytes() + " ")
                    .append(Component.translatable("gui.ufo.quantum_computation_nexus.bytes"));
            if (menu.isInfiniteMode()) {
                tooltip.append(" • ")
                        .append(Component.translatable("gui.ufo.quantum_computation_nexus.infinite_active"));
            }
            graphics.renderTooltip(font, tooltip.withStyle(ChatFormatting.AQUA), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + 94, topPos + 60, 66, 36)) {
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

    private void drawCenteredFitted(GuiGraphics graphics, Component text, int minX, int y, int maxX,
                                    float maxScale, int color) {
        int width = Math.max(1, font.width(text));
        float scale = Math.min(maxScale, (maxX - minX + 1) / (float) width);
        graphics.pose().pushPose();
        graphics.pose().translate((minX + maxX + 1) / 2.0F, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawCenteredString(font, text, 0, 0, color);
        graphics.pose().popPose();
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

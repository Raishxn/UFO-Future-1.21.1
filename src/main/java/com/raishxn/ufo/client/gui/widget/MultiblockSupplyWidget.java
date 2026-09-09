package com.raishxn.ufo.client.gui.widget;

import com.raishxn.ufo.screen.MultiblockSupplyStatus;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Reuses the Stellar requirements panel at its original size. */
public final class MultiblockSupplyWidget {
    public static final int WIDTH = 63;
    public static final int HEIGHT = 110;
    private static final int ROW_HEIGHT = 14;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "ae2", "textures/guis/resourcesrequirementswidget.png");

    private MultiblockSupplyWidget() {}

    /** Same panel and typography used by controllers, without coolant rows for a provider. */
    public static void renderWireless(GuiGraphics graphics, Font font, int x, int y,
            boolean patternBuffer, int links, int activeMachines, double speed, double energy, double heat,
            int activeMultiblocks, double multiSpeed, double multiEnergy, double multiHeat) {
        graphics.blit(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT, 256, 256);
        String[] labels = {
                patternBuffer ? "wireless.proxies" : "wireless.connected",
                patternBuffer ? "wireless.multiblocks" : "wireless.eligible",
                "supply.speed", "supply.energy", "supply.heat"
        };
        int eligible = patternBuffer ? activeMultiblocks : activeMachines;
        double effectiveSpeed = patternBuffer ? multiSpeed : speed;
        double effectiveEnergy = patternBuffer ? multiEnergy : energy;
        double effectiveHeat = patternBuffer ? multiHeat : heat;
        String[] values = {Integer.toString(links), Integer.toString(eligible),
                number(effectiveSpeed), number(effectiveEnergy), number(effectiveHeat)};
        for (int i = 0; i < labels.length; i++) {
            smallText(graphics, font, Component.translatable("gui.ufo." + labels[i]).getString(),
                    x + 4, y + 4 + i * 20, 55, 0xFFAAAAAA);
            smallText(graphics, font, values[i], x + 4, y + 11 + i * 20, 55, 0xFFE5F4FF);
        }
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y, MultiblockSupplyStatus status) {
        graphics.blit(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT, 256, 256);
        int coolantRows = coolantRows(status);
        for (int row = 0; row < coolantRows + 4; row++) {
            int rowY = y + 3 + row * ROW_HEIGHT;
            String label;
            String value;
            int textX = x + 4;
            int width = 55;
            int color = 0xFFE5F4FF;
            if (row < coolantRows) {
                if (status.coolants().isEmpty()) {
                    label = Component.translatable("gui.ufo.supply.coolant").getString();
                    value = Component.translatable("gui.ufo.supply.no_coolant").getString();
                    color = 0xFFAAAAAA;
                } else {
                    var coolant = status.coolants().get(row);
                    var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(coolant.fluidId()));
                    var stack = new FluidStack(fluid, 1);
                    var icon = new ItemStack(fluid.getBucket());
                    if (!icon.isEmpty()) {
                        graphics.pose().pushPose();
                        graphics.pose().translate(x + 3, rowY + 1, 0);
                        graphics.pose().scale(0.75F, 0.75F, 1);
                        graphics.renderItem(icon, 0, 0);
                        graphics.pose().popPose();
                    }
                    textX = x + 17;
                    width = 42;
                    label = stack.getHoverName().getString();
                    value = compact(coolant.storedMillibuckets()) + " mB";
                    color = 0xFF8DFFB3;
                }
            } else {
                int metric = row - coolantRows;
                label = Component.translatable("gui.ufo.supply." + switch (metric) {
                    case 0 -> "speed";
                    case 1 -> "energy";
                    case 2 -> "heat";
                    default -> "bonus";
                }).getString();
                value = switch (metric) {
                    case 0 -> number(status.speed()) + "x";
                    case 1 -> number(status.energy()) + "x";
                    case 2 -> number(status.heat()) + "x";
                    default -> "+" + number(status.bonus() * 100) + "%";
                };
            }
            smallText(graphics, font, label, textX, rowY + 1, width, 0xFFAAAAAA);
            smallText(graphics, font, value, textX, rowY + 7, width, color);
        }
    }

    /** Only stored coolants get a row; an empty machine keeps a single placeholder row. */
    private static int coolantRows(MultiblockSupplyStatus status) {
        return Math.max(1, status.coolants().size());
    }

    public static List<Component> tooltip(MultiblockSupplyStatus status, int localY,
                                           boolean safe, boolean overclock, int parallels) {
        int row = (localY - 3) / ROW_HEIGHT;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.ufo.supply.title"));
        if (localY >= 3 && row >= 0 && row < coolantRows(status)) {
            if (status.coolants().isEmpty()) {
                lines.add(Component.translatable("gui.ufo.supply.no_coolant_detail"));
                lines.add(Component.translatable("gui.ufo.supply.capacity", status.capacity(), status.hatches()));
                lines.add(Component.translatable("gui.ufo.supply.external"));
            } else {
                var coolant = status.coolants().get(row);
                var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(coolant.fluidId()));
                lines.add(new FluidStack(fluid, 1).getHoverName());
                lines.add(Component.translatable("gui.ufo.supply.stored", coolant.storedMillibuckets()));
                double efficiency = coolant.millibucketDenominator() > 0
                        ? (double) coolant.heatNumerator() / coolant.millibucketDenominator() : 0;
                lines.add(Component.translatable("gui.ufo.supply.efficiency", number(efficiency)));
                lines.add(Component.translatable("gui.ufo.supply.flow", coolant.maxMillibucketsPerTick()));
                lines.add(Component.translatable("gui.ufo.supply.capacity", status.capacity(), status.hatches()));
                lines.add(Component.translatable("gui.ufo.supply.external"));
                lines.add(Component.translatable("gui.ufo.supply.priority"));
            }
        } else {
            lines.add(Component.translatable("gui.ufo.supply.effective"));
            lines.add(Component.translatable(status.recipeFactorsLocked()
                    ? "gui.ufo.supply.factors_current" : "gui.ufo.supply.factors_next"));
            lines.add(Component.translatable("gui.ufo.supply.speed_detail", number(status.speed())));
            lines.add(Component.translatable("gui.ufo.supply.energy_detail", number(status.energy())));
            lines.add(Component.translatable("gui.ufo.supply.heat_detail", number(status.heat())));
            lines.add(Component.translatable("gui.ufo.supply.bonus_detail", number(status.bonus() * 100)));
            lines.add(Component.translatable("gui.ufo.supply.controls", safe ? "ON" : "OFF", overclock ? "ON" : "OFF"));
            lines.add(Component.translatable("gui.ufo.supply.parallel", parallels, overclock ? 5 : 1));
            if (status.creative()) lines.add(Component.translatable("gui.ufo.supply.creative"));
        }
        return lines;
    }

    private static void smallText(GuiGraphics graphics, Font font, String text, int x, int y, int width, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(0.5F, 0.5F, 1);
        graphics.drawString(font, font.plainSubstrByWidth(text, width * 2), 0, 0, color, false);
        graphics.pose().popPose();
    }

    /** DMA has an internal tank and no multiblock operating modes. */
    public static List<Component> dmaTooltip(MultiblockSupplyStatus status, int localY) {
        var lines = new ArrayList<Component>();
        lines.add(Component.translatable("gui.ufo.supply.title"));
        int row = (localY - 3) / ROW_HEIGHT;
        if (localY >= 3 && row < coolantRows(status)) {
            if (status.coolants().isEmpty()) {
                lines.add(Component.translatable("gui.ufo.supply.no_coolant_detail"));
            } else {
                var coolant = status.coolants().get(row);
                var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(coolant.fluidId()));
                lines.add(new FluidStack(fluid, 1).getHoverName());
                lines.add(Component.translatable("gui.ufo.supply.stored", coolant.storedMillibuckets()));
                lines.add(Component.translatable("gui.ufo.supply.efficiency",
                        number((double) coolant.heatNumerator() / coolant.millibucketDenominator())));
                lines.add(Component.translatable("gui.ufo.supply.flow", coolant.maxMillibucketsPerTick()));
            }
            lines.add(Component.translatable("gui.ufo.supply.dma_tank", status.capacity()));
        } else {
            lines.add(Component.translatable("gui.ufo.supply.effective"));
            lines.add(Component.translatable(status.recipeFactorsLocked()
                    ? "gui.ufo.supply.factors_current" : "gui.ufo.supply.factors_next"));
            lines.add(Component.translatable("gui.ufo.supply.speed_detail", number(status.speed())));
            lines.add(Component.translatable("gui.ufo.supply.energy_detail", number(status.energy())));
            lines.add(Component.translatable("gui.ufo.supply.heat_detail", number(status.heat())));
            lines.add(Component.translatable("gui.ufo.supply.bonus_detail", number(status.bonus() * 100)));
            if (status.creative()) lines.add(Component.translatable("gui.ufo.supply.creative"));
        }
        return lines;
    }

    private static String number(double value) { return String.format(Locale.ROOT, "%.2f", value); }
    private static String compact(long value) {
        if (value >= 1_000_000_000L) return String.format(Locale.ROOT, "%.1fB", value / 1_000_000_000D);
        if (value >= 1_000_000L) return String.format(Locale.ROOT, "%.1fM", value / 1_000_000D);
        if (value >= 1_000L) return String.format(Locale.ROOT, "%.1fK", value / 1_000D);
        return Long.toString(value);
    }
}

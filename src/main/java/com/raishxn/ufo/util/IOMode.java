package com.raishxn.ufo.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum IOMode implements StringRepresentable {
    NONE("none", Component.literal("None").withStyle(ChatFormatting.GRAY)),
    ITEM_IN("item_in", Component.literal("Item Input").withStyle(ChatFormatting.BLUE)),
    ITEM_OUT("item_out", Component.literal("Item Output").withStyle(ChatFormatting.GOLD)),
    FLUID_IN("fluid_in", Component.literal("Fluid Input").withStyle(ChatFormatting.AQUA)),
    FLUID_OUT_1("fluid_out_1", Component.literal("Fluid Output 1").withStyle(ChatFormatting.DARK_AQUA)),
    FLUID_OUT_2("fluid_out_2", Component.literal("Fluid Output 2").withStyle(ChatFormatting.DARK_BLUE)),
    COOLANT_IN("coolant_in", Component.literal("Coolant Input").withStyle(ChatFormatting.GREEN)),
    ENERGY("energy", Component.literal("Energy").withStyle(ChatFormatting.RED));

    private final String name;
    private final Component tooltip;

    IOMode(String name, Component tooltip) {
        this.name = name;
        this.tooltip = tooltip;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public Component getTooltip() {
        return tooltip;
    }

    // Método útil para o botão da GUI depois: cicla para o próximo modo
    public IOMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
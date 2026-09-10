package com.raishxn.ufo.mixin;

import appeng.core.localization.Tooltips;
import appeng.menu.me.crafting.CraftingStatusMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.text.DecimalFormat;

@Mixin(value = appeng.client.gui.widgets.CPUSelectionList.class, priority = 5000, remap = false)
public class MixinCPUSelectionList {
    @Shadow @Final
    private appeng.client.gui.style.Blitter buttonBg;

    @Unique
    private static final DecimalFormat ufo$DF = new DecimalFormat("#.##");
    @Unique
    private static final String[] ufo$UNITS = {"", "K", "M", "G", "T", "P", "E", "Y", "Z", "R", "Q"};
    @Unique
    private static final long ufo$INFINITE_STORAGE_THRESHOLD = Long.MAX_VALUE - 16;
    @Unique
    private static final int ufo$INFINITE_THREADS_THRESHOLD = Integer.MAX_VALUE - 1;

    @ModifyArg(
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"
            ),
            index = 1
    )
    private Component ufo$clipVisibleCpuName(Component name) {
        var font = Minecraft.getInstance().font;
        // AE2 draws CPU names at 2/3 scale. Convert the button's visible width
        // back to unscaled font pixels before shortening the text.
        int maxWidth = Math.max(0, (int) ((buttonBg.getSrcWidth() - 6) / 0.666f));
        if (font.width(name) <= maxWidth) {
            return name;
        }

        String suffix = "...";
        String clipped = font.plainSubstrByWidth(name.getString(), Math.max(0, maxWidth - font.width(suffix)));
        return Component.literal(clipped + suffix).withStyle(name.getStyle());
    }

    @Redirect(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/localization/Tooltips;ofBytes(J)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private MutableComponent storageTooltip(long bytes) {
        return Component.literal(ufo$formatStorage(bytes)).withStyle(Tooltips.NUMBER_TEXT);
    }

    @Redirect(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/localization/Tooltips;ofNumber(J)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private MutableComponent ufo$formatCoProcessorsTooltip(long value) {
        if (value >= ufo$INFINITE_THREADS_THRESHOLD) {
            return Component.literal("\u221E").withStyle(Tooltips.NUMBER_TEXT);
        }
        return Component.literal(ufo$formatDecimal(value)).withStyle(Tooltips.NUMBER_TEXT);
    }

    @Unique
    private static String ufo$formatStorage(long bytes) {
        if (bytes >= ufo$INFINITE_STORAGE_THRESHOLD) {
            return "\u221E";
        }
        if (bytes < 1024) return ufo$DF.format(bytes);
        int unit = Math.min((int) (Math.log(bytes) / Math.log(1024)), ufo$UNITS.length - 1);
        return ufo$DF.format(bytes / Math.pow(1024, unit)) + ufo$UNITS[unit];
    }

    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/gui/widgets/CPUSelectionList;formatStorage(Lappeng/menu/me/crafting/CraftingStatusMenu$CraftingCpuListEntry;)Ljava/lang/String;"
            )
    )
    private String ufo$formatVisibleStorage(appeng.client.gui.widgets.CPUSelectionList instance,
                                            CraftingStatusMenu.CraftingCpuListEntry cpu) {
        return ufo$formatStorage(cpu.storage());
    }

    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"
            )
    )
    private String ufo$formatVisibleCoProcessors(int coProcessors) {
        if (coProcessors >= ufo$INFINITE_THREADS_THRESHOLD) {
            return "\u221E";
        }
        return ufo$formatDecimal(coProcessors);
    }

    @Unique
    private static String ufo$formatDecimal(long amount) {
        if (amount < 1000) return ufo$DF.format(amount);
        int unit = Math.min((int) (Math.log10(amount) / 3), ufo$UNITS.length - 1);
        return ufo$DF.format(amount / Math.pow(1000, unit)) + ufo$UNITS[unit];
    }
}

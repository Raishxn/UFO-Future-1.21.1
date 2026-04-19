package com.raishxn.ufo.mixin;

import appeng.core.localization.Tooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.text.DecimalFormat;

@Mixin(value = appeng.client.gui.widgets.CPUSelectionList.class, priority = 5000, remap = false)
public class MixinCPUSelectionList {
    @Unique
    private static final DecimalFormat ufo$DF = new DecimalFormat("#.##");
    @Unique
    private static final String[] ufo$UNITS = {"", "K", "M", "G", "T", "P", "E", "Y", "Z", "R", "Q"};
    @Unique
    private static final long ufo$INFINITE_STORAGE_THRESHOLD = Long.MAX_VALUE - 16;
    @Unique
    private static final int ufo$INFINITE_THREADS_THRESHOLD = Integer.MAX_VALUE;

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
        return Component.literal(ufo$formatStorage(value)).withStyle(Tooltips.NUMBER_TEXT);
    }

    @Unique
    private String ufo$formatStorage(long bytes) {
        if (bytes >= ufo$INFINITE_STORAGE_THRESHOLD) {
            return "\u221E";
        }
        if (bytes < 1000) return ufo$DF.format(bytes);
        int unit = Math.min((int) (Math.log10(bytes) / 3), ufo$UNITS.length - 1);
        return ufo$DF.format(bytes / Math.pow(1000, unit)) + ufo$UNITS[unit];
    }
}

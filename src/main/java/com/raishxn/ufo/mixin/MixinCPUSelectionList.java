package com.raishxn.ufo.mixin;

import appeng.client.gui.widgets.CPUSelectionList;
import appeng.core.localization.Tooltips;
import appeng.menu.me.crafting.CraftingStatusMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.text.DecimalFormat;

@Mixin(value = CPUSelectionList.class,priority = 5000, remap = false)
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
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/gui/widgets/CPUSelectionList;formatStorage(Lappeng/menu/me/crafting/CraftingStatusMenu$CraftingCpuListEntry;)Ljava/lang/String;"
            )
    )
    private String formatStorage(CPUSelectionList instance, CraftingStatusMenu.CraftingCpuListEntry cpu) {
        return ufo$formatStorage(cpu.storage());
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
            return Component.literal("∞").withStyle(Tooltips.NUMBER_TEXT);
        }
        return Component.literal(ufo$formatStorage(value)).withStyle(Tooltips.NUMBER_TEXT);
    }

    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"
            )
    )
    private String ufo$formatCoProcessorsLabel(int value) {
        if (value >= ufo$INFINITE_THREADS_THRESHOLD) {
            return "∞";
        }
        return ufo$formatStorage(value);
    }

    @Unique
    private String ufo$formatStorage(long bytes) {
        if (bytes >= ufo$INFINITE_STORAGE_THRESHOLD) {
            return "∞";
        }
        if (bytes < 1000) return ufo$DF.format(bytes);
        int unit = Math.min((int) (Math.log10(bytes) / 3), ufo$UNITS.length - 1);
        return ufo$DF.format(bytes / Math.pow(1000, unit)) + ufo$UNITS[unit];
    }
}

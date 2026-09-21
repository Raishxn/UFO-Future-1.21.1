package com.raishxn.ufo.mixin;

import appeng.core.localization.Tooltips;
import appeng.menu.me.crafting.CraftingStatusMenu;
import com.raishxn.ufo.client.CpuAmountFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = appeng.client.gui.widgets.CPUSelectionList.class, priority = 5000, remap = false)
public class MixinCPUSelectionList {
    @Shadow @Final
    private appeng.client.gui.style.Blitter buttonBg;

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
        return Component.literal(CpuAmountFormatter.storage(bytes)).withStyle(Tooltips.NUMBER_TEXT);
    }

    @Redirect(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/localization/Tooltips;ofNumber(J)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private MutableComponent ufo$formatCoProcessorsTooltip(long value) {
        return Component.literal(CpuAmountFormatter.threads(value)).withStyle(Tooltips.NUMBER_TEXT);
    }

    @Inject(method = "formatStorage", at = @At("HEAD"), cancellable = true)
    private void ufo$formatVisibleStorage(CraftingStatusMenu.CraftingCpuListEntry cpu,
                                          CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(CpuAmountFormatter.storage(cpu.storage()));
    }

    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"
            )
    )
    private String ufo$formatVisibleCoProcessors(int coProcessors) {
        return CpuAmountFormatter.threads(coProcessors);
    }
}

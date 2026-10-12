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
import org.spongepowered.asm.mixin.injection.Group;
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

    // AE2 19.2.18 inlines storage formatting and changes the coprocessor formatter.
    // Each group must match one version's path; neither path is required on its own.
    @Group(name = "ufo$visibleStorage", min = 1, max = 1)
    @Inject(method = "formatStorage", at = @At("HEAD"), cancellable = true, require = 0)
    private void ufo$formatVisibleStorage(CraftingStatusMenu.CraftingCpuListEntry cpu,
                                          CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(CpuAmountFormatter.storage(cpu.storage()));
    }

    @Group(name = "ufo$visibleStorage", min = 1, max = 1)
    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/localization/Tooltips;getByteAmount(J)Lappeng/core/localization/Tooltips$Amount;"
            ),
            require = 0
    )
    private Tooltips.Amount ufo$formatVisibleStorageAmount(long bytes) {
        return new Tooltips.Amount(CpuAmountFormatter.storage(bytes), "");
    }

    @Group(name = "ufo$visibleCoProcessors", min = 1, max = 1)
    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"
            ),
            require = 0
    )
    private String ufo$formatVisibleCoProcessors(int coProcessors) {
        return CpuAmountFormatter.threads(coProcessors);
    }

    @Group(name = "ufo$visibleCoProcessors", min = 1, max = 1)
    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/localization/Tooltips;getAmount(J)Lappeng/core/localization/Tooltips$Amount;"
            ),
            require = 0
    )
    private Tooltips.Amount ufo$formatVisibleCoProcessorAmount(long coProcessors) {
        return new Tooltips.Amount(CpuAmountFormatter.threads(coProcessors), "");
    }
}

package com.raishxn.ufo.mixin;

import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.PatternEncodingLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PatternEncodingTermMenu.class)
public interface AccessorPatternEncodingTermMenu {
    @Accessor("encodingLogic")
    PatternEncodingLogic ufo$getEncodingLogic();
}

package com.raishxn.ufo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "appeng.crafting.execution.ExecutingCraftingJob$TaskProgress", remap = false)
public interface AccessorCraftingTaskProgress {
    @Accessor("value")
    long ufo$getValue();

    @Accessor("value")
    void ufo$setValue(long value);
}

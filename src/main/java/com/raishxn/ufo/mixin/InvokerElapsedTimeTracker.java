package com.raishxn.ufo.mixin;

import appeng.api.stacks.AEKeyType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "appeng.crafting.execution.ElapsedTimeTracker", remap = false)
public interface InvokerElapsedTimeTracker {
    @Invoker("addMaxItems")
    void ufo$addMaxItems(long amount, AEKeyType type);
}

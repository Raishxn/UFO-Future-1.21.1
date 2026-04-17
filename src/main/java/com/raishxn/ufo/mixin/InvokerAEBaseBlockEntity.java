package com.raishxn.ufo.mixin;

import appeng.blockentity.AEBaseBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AEBaseBlockEntity.class, remap = false)
public interface InvokerAEBaseBlockEntity {
    @Invoker("onReady")
    void ufo$invokeAeBaseOnReady();
}

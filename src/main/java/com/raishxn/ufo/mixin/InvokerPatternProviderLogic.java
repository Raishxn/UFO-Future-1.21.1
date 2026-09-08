package com.raishxn.ufo.mixin;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;
import appeng.helpers.patternprovider.PatternProviderLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.List;

/** Retain AE2's pending-delivery guard and crafting lock lifecycle for remote dispatch. */
@Mixin(PatternProviderLogic.class)
public interface InvokerPatternProviderLogic {
    @Invoker("onPushPatternSuccess") void ufo$onPushPatternSuccess(IPatternDetails pattern);
    @Accessor("sendList") List<GenericStack> ufo$getSendList();
    @Accessor("patternInputs") java.util.Set<appeng.api.stacks.AEKey> ufo$getPatternInputs();
    @Accessor("sendDirection") void ufo$setSendDirection(net.minecraft.core.Direction direction);
    @Invoker("addToSendList") void ufo$addToSendList(appeng.api.stacks.AEKey key, long amount);
    @Invoker("sendStacksOut") boolean ufo$sendStacksOut();
}

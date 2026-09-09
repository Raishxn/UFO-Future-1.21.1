package com.raishxn.ufo.mixin;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;
import appeng.me.service.CraftingService;
import com.raishxn.ufo.compat.AggregatePatternRecovery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CraftingService.class, remap = false)
public abstract class MixinAggregatePatternRecovery {
    @Inject(method = "getProviders", at = @At("RETURN"), cancellable = true)
    private void ufo$recoverAggregateProvider(IPatternDetails saved,
            CallbackInfoReturnable<Iterable<ICraftingProvider>> cir) {
        // Live wrappers already have normal routes. Restrict recovery to AE2's
        // decoded processing class, avoiding recursion and affecting no other mods.
        if (!saved.getClass().getName().equals("appeng.crafting.pattern.AEProcessingPattern")
                || AggregatePatternRecovery.route(saved) == null
                || cir.getReturnValue().iterator().hasNext()) return;
        var service = (CraftingService) (Object) this;
        var recovered = new ArrayList<ICraftingProvider>();
        for (var live : service.getCraftingFor(saved.getPrimaryOutput().what())) {
            if (live == saved || live.getClass() == saved.getClass()
                    || !AggregatePatternRecovery.matches(saved, live)) continue;
            for (var provider : service.getProviders(live)) {
                recovered.add(new ICraftingProvider() {
                    @Override public List<IPatternDetails> getAvailablePatterns() { return List.of(saved); }
                    @Override public boolean isBusy() { return provider.isBusy(); }
                    @Override public int getPatternPriority() { return provider.getPatternPriority(); }
                    @Override public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputs) {
                        return provider.pushPattern(live, inputs);
                    }
                });
            }
        }
        if (!recovered.isEmpty()) cir.setReturnValue(recovered);
    }
}

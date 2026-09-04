package com.raishxn.ufo.compat.emi;

import com.raishxn.ufo.screen.StellarNexusControllerScreen;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;

/** Optional EMI integration for UFO screens. */
@EmiEntrypoint
public final class UfoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(StellarNexusControllerScreen.class, (screen, consumer) -> {
            var area = screen.getRequirementsExclusionArea();
            consumer.accept(new Bounds(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
        });
    }
}

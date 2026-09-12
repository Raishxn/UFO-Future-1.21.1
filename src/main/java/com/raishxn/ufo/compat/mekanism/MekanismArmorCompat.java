package com.raishxn.ufo.compat.mekanism;

import com.raishxn.ufo.item.ModArmor;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Loaded only when Mekanism is present, including lambda return types. */
public final class MekanismArmorCompat {
    private MekanismArmorCompat() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerItem(mekanism.common.capabilities.Capabilities.RADIATION_SHIELDING, (stack, context) -> () -> 1.0,
                    ModArmor.ASTRAL_NEXUS_HELMET.get(),
                    ModArmor.ASTRAL_NEXUS_CHESTPLATE.get(),
                    ModArmor.ASTRAL_NEXUS_LEGGINGS.get(),
                    ModArmor.ASTRAL_NEXUS_BOOTS.get()
            );
    }
}

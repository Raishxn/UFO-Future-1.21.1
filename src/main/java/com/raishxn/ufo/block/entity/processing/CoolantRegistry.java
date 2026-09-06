package com.raishxn.ufo.block.entity.processing;

import com.raishxn.ufo.fluid.ModFluids;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Exact identity matching for coolant fluids (source and flowing variants).
 * Replaces the legacy substring matching on registry IDs, which granted coolant
 * strength to third-party fluids whose IDs merely contained a keyword and made
 * balance depend on ID spelling.
 */
public final class CoolantRegistry {
    private static final Map<Fluid, CoolantTuning.CoolantKind> KIND_BY_FLUID = new IdentityHashMap<>();

    static {
        register(CoolantTuning.CoolantKind.TEMPORAL,
                ModFluids.SOURCE_TEMPORAL_FLUID, ModFluids.FLOWING_TEMPORAL_FLUID);
        register(CoolantTuning.CoolantKind.STABLE,
                ModFluids.SOURCE_STABLE_COOLANT, ModFluids.FLOWING_STABLE_COOLANT);
        register(CoolantTuning.CoolantKind.STARLIGHT,
                ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID, ModFluids.FLOWING_LIQUID_STARLIGHT_FLUID);
        register(CoolantTuning.CoolantKind.GELID,
                ModFluids.SOURCE_GELID_CRYOTHEUM, ModFluids.FLOWING_GELID_CRYOTHEUM);
    }

    private CoolantRegistry() {
    }

    private static void register(CoolantTuning.CoolantKind kind,
            DeferredHolder<Fluid, ? extends Fluid> source,
            DeferredHolder<Fluid, ? extends Fluid> flowing) {
        KIND_BY_FLUID.put(source.get(), kind);
        KIND_BY_FLUID.put(flowing.get(), kind);
    }

    public static CoolantTuning.CoolantKind kindOf(Fluid fluid) {
        return KIND_BY_FLUID.getOrDefault(fluid, CoolantTuning.CoolantKind.GENERIC);
    }
}

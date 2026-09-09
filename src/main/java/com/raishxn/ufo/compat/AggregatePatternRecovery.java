package com.raishxn.ufo.compat;

import appeng.api.crafting.IPatternDetails;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Strict matching for processing patterns restored without AE All Pattern's wrapper. */
public final class AggregatePatternRecovery {
    private static final ResourceLocation MARKER = ResourceLocation.fromNamespaceAndPath(
            "aeallpattern", "virtual_pattern_id");

    private AggregatePatternRecovery() {}

    public static String route(IPatternDetails pattern) {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(MARKER);
        if (type == null) return null;
        Object value = pattern.getDefinition().toStack().get(type);
        return value instanceof String id && id.startsWith("aggregate:") ? id : null;
    }

    public static String stableRoute(String id) {
        int suffix = id.lastIndexOf(":inputs=");
        return suffix < 0 ? id : id.substring(0, suffix);
    }

    public static boolean matches(IPatternDetails saved, IPatternDetails live) {
        String oldId = route(saved), newId = route(live);
        if (oldId == null || newId == null || !stableRoute(oldId).equals(stableRoute(newId))) return false;
        var type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(MARKER);
        var oldStack = saved.getDefinition().toStack();
        var newStack = live.getDefinition().toStack();
        oldStack.remove(type);
        newStack.remove(type);
        // Require the entire encoded recipe, components and output quantities to
        // match. Only the session-dependent routing marker may differ.
        if (!ItemStack.isSameItemSameComponents(oldStack, newStack)
                || !saved.getOutputs().equals(live.getOutputs())) return false;
        var oldInputs = saved.getInputs();
        var newInputs = live.getInputs();
        if (oldInputs.length != newInputs.length) return false;
        for (int i = 0; i < oldInputs.length; i++) {
            if (oldInputs[i].getMultiplier() != newInputs[i].getMultiplier()
                    || !java.util.Arrays.equals(oldInputs[i].getPossibleInputs(), newInputs[i].getPossibleInputs())) {
                return false;
            }
        }
        return true;
    }
}

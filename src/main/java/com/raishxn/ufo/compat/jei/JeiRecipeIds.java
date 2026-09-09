package com.raishxn.ufo.compat.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** Keeps the vanilla holder identity while JEI categories render recipe values. */
final class JeiRecipeIds {
    private static final Map<Object, ResourceLocation> IDS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private JeiRecipeIds() {
    }

    static void clear() {
        IDS.clear();
    }

    static <T extends Recipe<?>> T remember(RecipeHolder<T> holder) {
        T recipe = holder.value();
        IDS.put(recipe, holder.id());
        return recipe;
    }

    static @Nullable ResourceLocation get(Object recipe) {
        return IDS.get(recipe);
    }
}

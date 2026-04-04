package com.raishxn.ufo.item;

import com.raishxn.ufo.util.ModTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

import java.util.Properties;

public class ModToolTiers {
    public static final Tier UFO = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_UFO_TOOL,
            10000, 10f, 10f, 50, () -> Ingredient.of(ModTags.Items.INGREDIENTS_UFO));

}
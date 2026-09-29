package com.raishxn.ufo.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;

/** Exercises the actual configurable-bee spawn egg path that crashed in the player report. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class ProductiveBeeGameTests {
    private ProductiveBeeGameTests() { }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void ufoBeeSpawnEggsSurviveWorldTicks(GameTestHelper helper) {
        if (!ModList.get().isLoaded("productivebees")) {
            helper.succeed();
            return;
        }
        var egg = BuiltInRegistries.ITEM.get(ResourceLocation.parse("productivebees:spawn_egg_configurable_bee"));
        var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("productivebees:configurable_bee"));
        var bees = new ArrayList<Entity>();
        String[] variants = {"scrap", "scrap_box", "matter_ball"};
        for (int i = 0; i < variants.length; i++) {
            ItemStack stack = new ItemStack(egg);
            CompoundTag data = new CompoundTag();
            data.putString("id", "productivebees:configurable_bee");
            data.putString("type", "productivebees:" + variants[i]);
            stack.set(DataComponents.ENTITY_DATA, CustomData.of(data));
            Entity bee = type.spawn(helper.getLevel(), stack, null,
                    helper.absolutePos(new BlockPos(i * 2, 2, 1)), MobSpawnType.SPAWN_EGG, true, false);
            helper.assertTrue(bee != null, "Failed to spawn UFO bee: " + variants[i]);
            bees.add(bee);
        }
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(bees.stream().noneMatch(Entity::isRemoved),
                    "A UFO bee disappeared after ticking in the world");
            for (int i = 0; i < bees.size(); i++) {
                CompoundTag saved = bees.get(i).saveWithoutId(new CompoundTag());
                helper.assertTrue(saved.getString("type").equals("productivebees:" + variants[i]),
                        "Spawn egg created the wrong bee variant: " + variants[i]);
            }
            helper.succeed();
        });
    }
}

package com.raishxn.ufo.gametest;

import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.armor.UfoArmorSetting;
import com.raishxn.ufo.item.ModItems;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Catalog contract for the modular armor. A module is declared in an enum, but its item, recipe,
 * translation and server cap live elsewhere, so "added a module" is easy to leave half-done. These
 * tests fail when the pieces drift apart, and they run against the real registries.
 */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class ArmorCatalogGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void everyModuleIsRegisteredTranslatedAndCraftable(GameTestHelper helper) {
        var level = helper.getLevel();
        List<RecipeHolder<?>> recipes = List.copyOf(level.getRecipeManager().getRecipes());
        Set<Item> craftable = new HashSet<>();
        for (RecipeHolder<?> holder : recipes) {
            ItemStack result = holder.value().getResultItem(level.registryAccess());
            if (!result.isEmpty()) craftable.add(result.getItem());
        }

        Set<String> itemIds = new HashSet<>();
        for (UfoArmorModule module : UfoArmorModule.values()) {
            helper.assertTrue(itemIds.add(module.itemId()),
                    module + " reuses the card id " + module.itemId());

            var card = ModItems.moduleCard(module);
            helper.assertTrue(card != null && card.isBound(),
                    module + " has no registered card item");
            Item item = card.get();
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).getPath().equals(module.itemId()),
                    module + " card is registered under an unexpected id");
            helper.assertTrue(UfoArmorModule.capacity(module.armorType()) > 0,
                    module + " is bound to a slot with no module capacity");
            helper.assertTrue(module.energyCost() >= 0,
                    module + " declares a negative energy cost");

            String translated = Component.translatable(module.translationKey()).getString();
            helper.assertTrue(!translated.equals(module.translationKey()),
                    module + " has no name translation");

            boolean hasRecipe = craftable.contains(item)
                    || recipes.stream().anyMatch(holder -> holder.id().getPath().equals(module.itemId()));
            helper.assertTrue(hasRecipe, module + " card has no recipe");
        }

        helper.assertTrue(ModItems.UFO_ARMOR_MODULE_CARDS.size() == UfoArmorModule.values().length,
                "the registered card list and the module enum disagree");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void everySettingIsTranslatedAndClampedByTheServer(GameTestHelper helper) {
        for (UfoArmorSetting setting : UfoArmorSetting.values()) {
            String translated = Component.translatable(setting.translationKey()).getString();
            helper.assertTrue(!translated.equals(setting.translationKey()),
                    setting + " has no name translation");
            helper.assertTrue(setting.min() <= setting.defaultValue() && setting.defaultValue() <= setting.max(),
                    setting + " has a default outside its own range");
            helper.assertTrue(setting.step() > 0 && (setting.max() - setting.min()) % setting.step() == 0,
                    setting + " declares a range that is not a whole number of steps");
            helper.assertTrue(UfoArmorSetting.forModule(setting.module()).contains(setting),
                    setting + " is not listed for its own module");

            // The server cap may only tighten the design maximum, never raise it.
            int cap = UFOConfig.armorCap(setting);
            helper.assertTrue(cap >= setting.min() && cap <= setting.max(),
                    setting + " has a server cap outside the design range: " + cap);

            // A crafted packet carries a VAR_INT, so the clamp has to hold at the wire extremes.
            helper.assertTrue(UFOConfig.clampArmorSetting(setting, Integer.MIN_VALUE) == setting.min(),
                    setting + " does not clamp the smallest wire value to its minimum");
            helper.assertTrue(UFOConfig.clampArmorSetting(setting, Integer.MAX_VALUE) == setting.max(),
                    setting + " does not clamp the largest wire value to its maximum");
        }
        helper.succeed();
    }
}

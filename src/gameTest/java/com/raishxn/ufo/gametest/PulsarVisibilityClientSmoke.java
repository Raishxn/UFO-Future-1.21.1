package com.raishxn.ufo.gametest;

import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModCreativeModeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Development-only smoke check after joining a disposable world. */
@EventBusSubscriber(modid = "ufo_tests", value = Dist.CLIENT)
public final class PulsarVisibilityClientSmoke {
    private static boolean complete;
    private static int ticks;

    @SubscribeEvent
    public static void afterClientTick(ClientTickEvent.Post event) throws ReflectiveOperationException {
        if (complete || !Boolean.getBoolean("ufo.visibilitySmoke")) return;
        var minecraft = Minecraft.getInstance();
        if (++ticks > 2400) throw new AssertionError("Pulsar viewer smoke timed out");
        if (minecraft.level == null || minecraft.player == null) return;
        boolean jeiLoaded = ModList.get().isLoaded("jei");
        if (jeiLoaded && !JeiChecks.isLoaded()) return;
        boolean emiLoaded = ModList.get().isLoaded("emi");
        if (emiLoaded && !EmiChecks.isLoaded()) return;
        boolean expected = ModList.get().isLoaded("mekanism");
        var creative = ModCreativeModeTabs.UFO_ITEMS_TAB.get().getDisplayItems();
        for (var item : ModCellItems.chemicalStorageItems()) {
            check(creative.stream().anyMatch(stack -> stack.is(item)) == expected, "creative", item);
            if (jeiLoaded) JeiChecks.checkIndex(item, expected);
            if (emiLoaded) {
                EmiChecks.checkIndex(item, expected);
            }
        }
        // The existing JEMI integration must continue exposing UFO's recipe categories.
        if (emiLoaded && ModList.get().isLoaded("jei")) {
            EmiChecks.checkUfoCategories();
        }
        complete = true;
        com.mojang.logging.LogUtils.getLogger().info("UFO_PULSAR_VISIBILITY_SMOKE_PASSED mekanism={} jei={} emi={}",
                expected, jeiLoaded, emiLoaded);
        minecraft.stop();
    }

    private static void check(boolean success, String viewer, net.minecraft.world.item.Item item) {
        if (!success) throw new AssertionError(viewer + " visibility failed for "
                + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item));
    }

    private static final class EmiChecks {
        private static boolean isLoaded() {
            return dev.emi.emi.runtime.EmiReloadManager.isLoaded();
        }

        private static void checkIndex(net.minecraft.world.item.Item item, boolean expected) {
            check(dev.emi.emi.registry.EmiStackList.filteredStacks.stream()
                    .anyMatch(stack -> stack.getItemStack().is(item)) == expected, "EMI", item);
        }

        private static void checkUfoCategories() {
            check(dev.emi.emi.api.EmiApi.getRecipeManager().getRecipes().stream()
                    .anyMatch(recipe -> recipe.getCategory().getId().getNamespace().equals("ufo")),
                    "JEMI recipe categories", ModCellItems.ITEM_CELL_40M.get());
        }
    }

    private static final class JeiChecks {
        private static mezz.jei.api.runtime.IJeiRuntime runtime() throws ReflectiveOperationException {
            var field = com.raishxn.ufo.compat.jei.UfoJeiPlugin.class.getDeclaredField("runtime");
            field.setAccessible(true);
            return (mezz.jei.api.runtime.IJeiRuntime) field.get(null);
        }

        private static boolean isLoaded() throws ReflectiveOperationException {
            return runtime() != null;
        }

        private static void checkIndex(net.minecraft.world.item.Item item, boolean expected)
                throws ReflectiveOperationException {
            check(runtime().getJeiHelpers().getIngredientVisibility().isIngredientVisible(
                    mezz.jei.api.constants.VanillaTypes.ITEM_STACK, new ItemStack(item)) == expected,
                    "JEI", item);
        }
    }
}

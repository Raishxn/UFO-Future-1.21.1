package com.raishxn.ufo.gametest;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.me.storage.NetworkStorage;
import appeng.menu.me.common.GridInventoryEntry;
import com.raishxn.ufo.item.ModCells;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Exercises AE2's actual aggregation and terminal visibility with controlled external stock. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class InfinityCellVisibilityGameTests {
    private InfinityCellVisibilityGameTests() {
    }

    @GameTest(template = "empty")
    public static void fixedInfinityCellsRemainVisibleAlongsideExternalStock(GameTestHelper helper) {
        for (var item : new net.minecraft.world.item.Item[] {
                ModCells.INFINITY_GLASS_CELL.get(), ModCells.INFINITY_OBSIDIAN_CELL.get(),
                ModCells.INFINITY_WATER_CELL.get() }) {
            var cell = inventory(helper, new ItemStack(item));
            var key = cell.getAvailableStacks().getFirstKey();
            helper.assertTrue(key != null, "Infinity cell advertised no resource");
            verifyMixedStorage(helper, cell, inventory(helper, new ItemStack(item)), key);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void genesisRemainsVisibleAlongsideExternalStock(GameTestHelper helper) {
        var cell = inventory(helper, new ItemStack(ModCells.INFINITY_GENESIS_CELL.get()));
        var second = inventory(helper, new ItemStack(ModCells.INFINITY_GENESIS_CELL.get()));
        for (var item : new net.minecraft.world.item.Item[] { Items.GLASS, Items.OBSIDIAN }) {
            var key = AEItemKey.of(item);
            cell.insert(key, 1, Actionable.MODULATE, IActionSource.empty());
            second.insert(key, 1, Actionable.MODULATE, IActionSource.empty());
        }
        verifyMixedStorage(helper, cell, second, AEItemKey.of(Items.GLASS));
        verifyMixedStorage(helper, cell, second, AEItemKey.of(Items.OBSIDIAN));
        helper.succeed();
    }

    private static StorageCell inventory(GameTestHelper helper, ItemStack stack) {
        var cell = StorageCells.getCellInventory(stack, null);
        helper.assertTrue(cell != null, "Infinity cell handler was not registered");
        return cell;
    }

    private static void verifyMixedStorage(GameTestHelper helper, StorageCell cell, StorageCell second, AEKey key) {
        var unrelated = AEItemKey.of(Items.DIAMOND);
        MEStorage external = new MEStorage() {
            @Override
            public void getAvailableStacks(KeyCounter out) {
                // A full external source plus ordinary drawer stock for the same resource.
                out.add(key, Integer.MAX_VALUE);
                out.add(key, 64);
                out.add(unrelated, 37);
            }

            @Override
            public Component getDescription() {
                return Component.literal("External stock fixture");
            }
        };
        long advertised = cell.getAvailableStacks().get(key);
        long externalAmount = (long) Integer.MAX_VALUE + 64;
        for (int priority : new int[] { -10, 10 }) {
            var network = new NetworkStorage();
            network.mount(0, external);
            network.mount(priority, cell);
            assertVisible(helper, network, key, advertised + externalAmount);
            network.mount(priority, second);
            assertVisible(helper, network, key, advertised * 2 + externalAmount);
            helper.assertTrue(network.getAvailableStacks().get(unrelated) == 37, "Unrelated stock changed");
            for (var mode : Actionable.values()) {
                helper.assertTrue(cell.extract(key, Long.MAX_VALUE, mode, IActionSource.empty()) == Long.MAX_VALUE,
                        "Advertised count limited infinite extraction");
                helper.assertTrue(cell.extract(key, 64, mode, IActionSource.empty()) == 64,
                        "Infinite source was depleted");
            }
            assertVisible(helper, network, key, advertised * 2 + externalAmount);
            network.unmount(second);
            assertVisible(helper, network, key, advertised + externalAmount);
            network.unmount(cell);
            assertVisible(helper, network, key, externalAmount);
            network.mount(priority, cell);
            assertVisible(helper, network, key, advertised + externalAmount);
        }
    }

    private static void assertVisible(GameTestHelper helper, NetworkStorage network, AEKey key, long expected) {
        long actual = network.getAvailableStacks().get(key);
        helper.assertTrue(actual > 0, "Mixed Infinity/external stock overflowed: " + actual);
        helper.assertTrue(actual == expected, "Mixed stock was not summed correctly");
        helper.assertTrue(new GridInventoryEntry(1, key, actual, 0, false).isMeaningful(),
                "Terminal would remove the mixed stock entry");
    }
}

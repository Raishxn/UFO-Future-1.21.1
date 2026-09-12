package com.raishxn.ufo.gametest;

import appeng.api.config.Actionable;
import appeng.api.networking.GridHelper;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mod("ufo_tests")
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class QuantumGridLinkRecoveryGameTests {
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void optionalMekanismCellsStaySafe(GameTestHelper helper) {
        boolean loaded = net.neoforged.fml.ModList.get().isLoaded("mekanism");
        var chemicalCell = com.raishxn.ufo.item.ModCellItems.CHEMICAL_CELL_40M.get();
        var cellStack = new ItemStack(chemicalCell);
        helper.assertTrue(chemicalCell.isBlackListed(cellStack,
                appeng.api.stacks.AEFluidKey.of(net.minecraft.world.level.material.Fluids.WATER)),
                "Chemical cell must reject ordinary fluids, including without Mekanism");
        var infinityCell = (com.raishxn.ufo.item.InfinityCell)
                com.raishxn.ufo.item.ModCells.INFINITY_ANTIMATTER_PELLET_CELL.get();
        helper.assertTrue((infinityCell.getRecord() != null) == loaded,
                "Mekanism Infinity cell must only supply resources when Mekanism is installed");
        helper.succeed();
    }

    private static final BlockPos LINK = new BlockPos(1, 1, 1);
    private static final AEItemKey OUTPUT = AEItemKey.of(Items.DIAMOND);
    private static final long OUTPUT_AMOUNT = 4_000_000_000L;

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void fullNetworkBreakRecoversResultsAndOneCatalyst(GameTestHelper helper) {
        withNetwork(helper, fixture -> {
            AEItemKey catalyst = catalyst();
            seed(helper, fixture.link, List.of(Map.of(OUTPUT, OUTPUT_AMOUNT, catalyst, 1L)), false);
            var node = fixture.link.getMainNode().getNode();
            fixture.link.tickingRequest(node, 1); // Restored-output grace tick.
            fixture.link.tickingRequest(node, 1);
            helper.assertTrue(fixture.link.getPendingCraftingRouteCount() == 1, "full network must retain the route");
            breakLink(helper);
            assertRecovered(helper, OUTPUT, OUTPUT_AMOUNT);
            assertRecovered(helper, catalyst, 1L);
            helper.assertTrue(fixture.storage.inserted.isEmpty(), "full network accepted unexpected results");
            fixture.link.unlinkForRemoval();
            assertRecovered(helper, OUTPUT, OUTPUT_AMOUNT);
            assertRecovered(helper, catalyst, 1L);
            helper.assertTrue(fixture.link.getPendingCraftingRouteCount() == 0, "recovered route must be cleared");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void partialNetworkBreakEjectsOnlyRejectedBalance(GameTestHelper helper) {
        withNetwork(helper, fixture -> {
            fixture.storage.room = 17L;
            seed(helper, fixture.link, List.of(Map.of(OUTPUT, OUTPUT_AMOUNT)), false);
            breakLink(helper);
            helper.assertTrue(fixture.storage.inserted.get(OUTPUT) == 17L, "partial insertion was not accounted for");
            assertRecovered(helper, OUTPUT, OUTPUT_AMOUNT - 17L);
            fixture.link.unlinkForRemoval();
            helper.assertTrue(fixture.storage.inserted.get(OUTPUT) == 17L, "removal inserted results twice");
            assertRecovered(helper, OUTPUT, OUTPUT_AMOUNT - 17L);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void availableNetworkRecoversWithoutPackages(GameTestHelper helper) {
        withNetwork(helper, fixture -> {
            fixture.storage.room = OUTPUT_AMOUNT;
            seed(helper, fixture.link, List.of(Map.of(OUTPUT, OUTPUT_AMOUNT)), false);
            breakLink(helper);
            helper.assertTrue(fixture.storage.inserted.get(OUTPUT) == OUTPUT_AMOUNT, "available network lost output");
            helper.assertTrue(packages(helper).isEmpty(), "accepted output was also ejected");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void offlineBreakPreservesTwoMaximumSizeRoutes(GameTestHelper helper) {
        var link = placeLink(helper);
        seed(helper, link, List.of(Map.of(OUTPUT, Long.MAX_VALUE), Map.of(OUTPUT, Long.MAX_VALUE)), false);
        breakLink(helper);
        var packages = packages(helper);
        helper.assertTrue(packages.size() == 2, "maximum routes must not merge or overflow");
        helper.assertTrue(packages.stream().allMatch(stack -> stack.what().equals(OUTPUT)
                && stack.amount() == Long.MAX_VALUE), "64-bit route balances were changed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacySaveReloadRetainsOutputUntilPhysicalRemoval(GameTestHelper helper) {
        var link = placeLink(helper);
        seed(helper, link, List.of(Map.of(OUTPUT, OUTPUT_AMOUNT, catalyst(), 1L)), true);
        CompoundTag saved = new CompoundTag();
        link.saveAdditional(saved, helper.getLevel().registryAccess());
        link.loadTag(saved, helper.getLevel().registryAccess());
        helper.assertTrue(link.getPendingCraftingRouteCount() == 1, "legacy output did not survive reload");
        helper.assertTrue(packages(helper).isEmpty(), "reload must not eject results");
        breakLink(helper);
        assertRecovered(helper, OUTPUT, OUTPUT_AMOUNT);
        assertRecovered(helper, catalyst(), 1L);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unloadDoesNotRecoverOrClearPendingRoutes(GameTestHelper helper) {
        var link = placeLink(helper);
        seed(helper, link, List.of(Map.of(OUTPUT, OUTPUT_AMOUNT)), false);
        link.setRemoved(); // The BE lifecycle used by chunk unload, not block destruction.
        helper.assertTrue(link.getPendingCraftingRouteCount() == 1, "unload cleared the route");
        helper.assertTrue(packages(helper).isEmpty(), "unload ejected output that remains in the save");
        helper.succeed();
    }

    private static QuantumGridLinkBE placeLink(GameTestHelper helper) {
        helper.setBlock(LINK, MultiblockBlocks.QUANTUM_GRID_LINK.get());
        return (QuantumGridLinkBE) helper.getLevel().getBlockEntity(helper.absolutePos(LINK));
    }

    private static void withNetwork(GameTestHelper helper, Consumer<Fixture> test) {
        var link = placeLink(helper);
        BlockPos powerPos = new BlockPos(3, 1, 1);
        helper.setBlock(powerPos, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.runAfterDelay(2, () -> {
            var power = (AENetworkedBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(powerPos));
            var connection = GridHelper.createConnection(link.getMainNode().getNode(), power.getMainNode().getNode());
            var storage = new LimitedStorage();
            var grid = link.getMainNode().getNode().getGrid();
            grid.getStorageService().addGlobalStorageProvider(mounts -> mounts.mount(storage, 0));
            helper.runAfterDelay(25, () -> {
                helper.assertTrue(link.getGrid() != null, "fixture ME grid did not become active");
                test.accept(new Fixture(link, storage));
                connection.destroy();
            });
        });
    }

    private static void seed(GameTestHelper helper, QuantumGridLinkBE link,
                             List<Map<AEKey, Long>> routes, boolean legacy) {
        CompoundTag saved = new CompoundTag();
        ListTag routeTags = new ListTag();
        for (var route : routes) {
            ListTag outputs = new ListTag();
            route.forEach((key, amount) -> outputs.add(GenericStack.writeTag(
                    helper.getLevel().registryAccess(), new GenericStack(key, amount))));
            if (legacy) saved.put("pendingCraftingOutputs", outputs);
            else {
                CompoundTag tag = new CompoundTag();
                tag.put("outputs", outputs);
                routeTags.add(tag);
            }
        }
        if (!legacy) saved.put("pendingCraftingRoutes", routeTags);
        link.loadTag(saved, helper.getLevel().registryAccess());
    }

    private static void breakLink(GameTestHelper helper) {
        helper.getLevel().destroyBlock(helper.absolutePos(LINK), true);
    }

    private static List<GenericStack> packages(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                        new AABB(helper.absolutePos(LINK)).inflate(1.5D)).stream()
                .map(entity -> GenericStack.unwrapItemStack(entity.getItem()))
                .filter(java.util.Objects::nonNull).toList();
    }

    private static void assertRecovered(GameTestHelper helper, AEKey key, long amount) {
        long actual = packages(helper).stream().filter(stack -> stack.what().equals(key))
                .mapToLong(GenericStack::amount).sum();
        helper.assertTrue(actual == amount, "recovered balance mismatch: expected " + amount + ", got " + actual);
    }

    private static AEItemKey catalyst() {
        ItemStack stack = new ItemStack(Items.NETHER_STAR);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Reusable catalyst"));
        return AEItemKey.of(stack);
    }

    private record Fixture(QuantumGridLinkBE link, LimitedStorage storage) { }

    private static final class LimitedStorage implements MEStorage {
        long room;
        final KeyCounter inserted = new KeyCounter();
        @Override public Component getDescription() { return Component.literal("Recovery test storage"); }
        @Override public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
            long accepted = Math.min(room, amount);
            if (mode == Actionable.MODULATE) {
                room -= accepted;
                if (accepted > 0L) inserted.add(key, accepted);
            }
            return accepted;
        }
    }
}

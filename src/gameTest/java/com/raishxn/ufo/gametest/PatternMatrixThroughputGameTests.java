package com.raishxn.ufo.gametest;

import appeng.api.config.Actionable;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.GridHelper;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import com.raishxn.ufo.block.entity.pattern.QuantumPatternFabricationMatrixPatternFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class PatternMatrixThroughputGameTests {
    @GameTest(template = "event_driven_structure", timeoutTicks = 100)
    public static void mk1BatchesAndBackpressure(GameTestHelper helper) {
        exercise(helper, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get().defaultBlockState(), 16);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 100)
    public static void mk2BatchesAndBackpressure(GameTestHelper helper) {
        exercise(helper, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get().defaultBlockState(), 32);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 100)
    public static void mk3BatchesAndBackpressure(GameTestHelper helper) {
        exercise(helper, MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get().defaultBlockState(), 64);
    }

    private static void exercise(GameTestHelper helper, BlockState field, int limit) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(20, 5, 20));
        level.setBlockAndUpdate(pos, MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get().defaultBlockState());
        var definition = QuantumPatternFabricationMatrixPatternFactory.getDefinition();
        var states = new java.util.HashMap<>(definition.defaultCreativeStates());
        states.put('F', field);
        definition.pattern().assembleAsCreative(level, pos, Direction.EAST, states);
        var matrix = (QuantumPatternFabricationMatrixControllerBE) level.getBlockEntity(pos);
        matrix.scanStructure(level);
        helper.assertTrue(matrix.isAssembled(), "Matrix did not form");
        helper.assertTrue(matrix.getCraftingRouteLimit() == limit, "Wrong field throughput");
        var link = (QuantumGridLinkBE) level.getBlockEntity(matrix.getParts().getFirst());
        BlockPos powerPos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlockAndUpdate(powerPos, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
        helper.runAfterDelay(2, () -> {
            var power = (AENetworkedBlockEntity) level.getBlockEntity(powerPos);
            var connection = GridHelper.createConnection(link.getMainNode().getNode(), power.getMainNode().getNode());
            var storage = new ProbeStorage();
            link.getMainNode().getNode().getGrid().getStorageService()
                    .addGlobalStorageProvider(mounts -> mounts.mount(storage, 0));
            helper.runAfterDelay(25, () -> {
                var holder = level.getRecipeManager().byKey(ResourceLocation.withDefaultNamespace("stone_bricks")).orElseThrow();
                var stone = new ItemStack(Items.STONE);
                var empty = ItemStack.EMPTY;
                var encoded = PatternDetailsHelper.encodeCraftingPattern(
                        new RecipeHolder<>(holder.id(), (CraftingRecipe) holder.value()),
                        new ItemStack[]{stone, stone, empty, stone, stone, empty, empty, empty, empty},
                        new ItemStack(Items.STONE_BRICKS, 4), false, false);
                helper.assertTrue(matrix.insertEncodedPattern(encoded), "Could not install pattern");
                var details = matrix.getAvailablePatterns().getFirst();
                KeyCounter[] inputs = new KeyCounter[4];
                for (int i = 0; i < inputs.length; i++) {
                    inputs[i] = new KeyCounter();
                    inputs[i].add(AEItemKey.of(Items.STONE), 1);
                }
                long copies = 1_000_000L;
                for (int i = 0; i < limit; i++) {
                    helper.assertFalse(link.isBusy(), "Matrix serialized pending batches");
                    helper.assertTrue(link.pushAggregate(details, inputs, copies) == 0, "Batch rejected early");
                }
                helper.assertTrue(link.isBusy(), "Full queue must apply backpressure");
                helper.assertTrue(link.pushAggregate(details, inputs, 1) == 1, "Full queue accepted extra work");
                CompoundTag saved = new CompoundTag();
                link.saveAdditional(saved, level.registryAccess());
                link.loadTag(saved, level.registryAccess());
                helper.assertTrue(link.getPendingCraftingRouteCount() == limit, "Reload lost pending batches");
                var node = link.getMainNode().getNode();
                link.tickingRequest(node, 1); // Reload grace tick.
                link.tickingRequest(node, 1); // Storage is blocked.
                helper.assertTrue(link.getPendingCraftingRouteCount() == limit, "Blocked output lost batches");
                storage.room = copies * 4 * limit - 1;
                link.tickingRequest(node, 1);
                helper.assertTrue(link.getPendingCraftingRouteCount() == 1, "Did not drain several batches in one tick: pending=" + link.getPendingCraftingRouteCount() + ", inserted=" + storage.inserted + ", grid=" + (link.getGrid() != null) + ", owner=" + link.getControllerPos() + ", limit=" + matrix.getCraftingRouteLimit());
                storage.room = 1;
                link.tickingRequest(node, 1);
                helper.assertTrue(link.getPendingCraftingRouteCount() == 0, "Partial balance was not retried");
                helper.assertTrue(storage.inserted == copies * 4 * limit, "Output duplicated or lost");
                helper.assertFalse(link.isBusy(), "Drained Matrix stayed busy");
                link.patternsChanged();
                helper.runAfterDelay(2, () -> {
                    var inventory = new appeng.crafting.inv.ListCraftingInventory(key -> {});
                    var waiting = new appeng.crafting.inv.ListCraftingInventory(key -> {});
                    // One single-copy task followed by a million-copy task use the same scheduler.
                    for (long count : new long[]{1L, copies}) {
                        inventory.insert(AEItemKey.of(Items.STONE), 4 * count, Actionable.MODULATE);
                        var tasks = new java.util.LinkedHashMap<appeng.api.crafting.IPatternDetails, long[]>();
                        tasks.put(details, new long[]{count});
                        var grid = link.getGrid();
                        var result = com.raishxn.ufo.crafting.AggregateCraftingExecutor.dispatch(
                                1, 0, (appeng.me.service.CraftingService) grid.getCraftingService(),
                                grid.getEnergyService(), level, inventory, tasks, waiting,
                                task -> ((long[]) task)[0], (task, value) -> ((long[]) task)[0] = value,
                                (amount, type) -> {}, () -> {});
                        helper.assertTrue(result.consumedOperations() == 1 && tasks.isEmpty(),
                                "CPU scheduler did not execute " + count + " copies as one operation");
                        helper.assertTrue(inventory.extract(AEItemKey.of(Items.STONE), Long.MAX_VALUE,
                                Actionable.SIMULATE) == 0, "CPU input accounting mismatch");
                    }
                    helper.assertTrue(waiting.extract(AEItemKey.of(Items.STONE_BRICKS), Long.MAX_VALUE,
                            Actionable.SIMULATE) == 4 * (copies + 1), "CPU waiting-output accounting mismatch");
                    helper.assertTrue(link.getPendingCraftingRouteCount() == 2,
                            "Single-copy and bulk tasks did not queue concurrently");
                    connection.destroy();
                    helper.succeed();
                });
            });
        });
    }

    private static final class ProbeStorage implements MEStorage {
        long room;
        long inserted;
        @Override public Component getDescription() { return Component.literal("Matrix throughput probe"); }
        @Override public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
            if (!key.equals(AEItemKey.of(Items.STONE_BRICKS))) return 0;
            long accepted = Math.min(room, amount);
            if (mode == Actionable.MODULATE) { room -= accepted; inserted += accepted; }
            return accepted;
        }
    }
}

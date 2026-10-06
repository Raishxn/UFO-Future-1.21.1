package com.raishxn.ufo.gametest;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.CraftingPlan;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.mojang.logging.LogUtils;
import com.raishxn.ufo.api.ae.NexusVirtualCpuHost;
import com.raishxn.ufo.api.ae.NexusVirtualCraftingClusterBridge;
import com.raishxn.ufo.crafting.NexusSharedCraftingCpuPool;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Regression coverage for the Quantum Computation Nexus parallel crafting budget.
 *
 * <p>The pre-fix pool capped every job at a share of a fixed 2,048-operation window shared by the
 * whole pool, so four jobs on 16,384 lanes moved roughly one eighth of the work of four equivalent
 * stand-alone AE2 CPUs. The fixed pool shares the installed lanes between jobs, bounds execution by
 * the operator's per-tick ceiling (default 16,384) and waives pattern energy by default, so both
 * scenarios must match the stand-alone CPUs exactly instead of collapsing with the buffer.
 */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class NexusParallelCraftingGameTests {
    private static final int COPIES_PER_JOB = 100_000;
    private static final int ROUNDS = 8;

    private NexusParallelCraftingGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void nexusMatchesStandaloneCpusWithFullPower(GameTestHelper helper) {
        compare(helper, false);
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void nexusKeepsFullThroughputWithCriticalEnergy(GameTestHelper helper) {
        compare(helper, true);
    }

    private static void compare(GameTestHelper helper, boolean lowPercentage) {
        var level = helper.getLevel();
        var holder = level.getRecipeManager().byKey(ResourceLocation.withDefaultNamespace("stone_bricks")).orElseThrow();
        var stone = new ItemStack(Items.STONE);
        var empty = ItemStack.EMPTY;
        var encoded = PatternDetailsHelper.encodeCraftingPattern(
                new RecipeHolder<>(holder.id(), (CraftingRecipe) holder.value()),
                new ItemStack[]{stone, stone, empty, stone, stone, empty, empty, empty, empty},
                new ItemStack(Items.STONE_BRICKS, 4), false, false);
        var pattern = PatternDetailsHelper.decodePattern(encoded, level);
        var provider = new Provider(pattern);
        var node = GridHelper.createManagedNode(provider, (owner, changed) -> {})
                .setInWorldNode(false).addService(ICraftingProvider.class, provider);
        node.create(level, helper.absolutePos(new BlockPos(1, 1, 1)));
        helper.setBlock(new BlockPos(2, 1, 1), AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.runAfterDelay(2, () -> {
            var power = (AENetworkedBlockEntity) level.getBlockEntity(helper.absolutePos(new BlockPos(2, 1, 1)));
            var connection = GridHelper.createConnection(node.getNode(), power.getMainNode().getNode());
            var grid = node.getNode().getGrid();
            grid.getStorageService().addGlobalStorageProvider(mounts -> mounts.mount(new Stock(), 0));
            helper.runAfterDelay(25, () -> {
                var service = (CraftingService) grid.getCraftingService();
                helper.assertTrue(service.getProviders(pattern).iterator().hasNext(), "Provider not registered");
                var host = new Host(grid, node.getNode(), level);
                IEnergyService energy = grid.getEnergyService();
                if (lowPercentage) {
                    // The pool waives pattern energy by default: a critical 1% buffer estimate must
                    // not change Nexus throughput at all.
                    var realEnergy = energy;
                    energy = (IEnergyService) Proxy.newProxyInstance(IEnergyService.class.getClassLoader(),
                            new Class<?>[]{IEnergyService.class}, (proxy, method, args) -> {
                                if (method.getName().equals("getStoredPower")) return 1_000_000.0;
                                if (method.getName().equals("getMaxStoredPower")) return 100_000_000.0;
                                return method.invoke(realEnergy, args);
                            });
                }
                var pool = new NexusSharedCraftingCpuPool(host);
                pool.reconfigure(1_000_000, 4 * 4_096, false);
                var normal = new ArrayList<CraftingCPUCluster>();
                for (int i = 0; i < 4; i++) {
                    helper.assertTrue(pool.submitJob(grid, plan(pattern), IActionSource.empty(), null).successful(),
                            "Pool rejected parallel job");
                    var cpu = new CraftingCPUCluster(BlockPos.ZERO, BlockPos.ZERO);
                    ((NexusVirtualCraftingClusterBridge) (Object) cpu).ufo$configureVirtualCpu(host, 100_000, 4_096);
                    helper.assertTrue(cpu.submitJob(grid, plan(pattern), IActionSource.empty(), null).successful(),
                            "AE2 baseline CPU rejected job");
                    normal.add(cpu);
                }
                for (int tick = 0; tick < ROUNDS; tick++) pool.tickCraftingLogic(energy, service);
                long nexusPushes = provider.pushes;
                provider.pushes = 0;
                for (int tick = 0; tick < ROUNDS; tick++) {
                    for (var cpu : normal) cpu.craftingLogic.tickCraftingLogic(energy, service);
                }
                long normalPushes = provider.pushes;
                LogUtils.getLogger().info("NEXUS_PARALLEL_REGRESSION lowPercentage={} jobs=4 totalCoProcessors=16384 rounds={} nexusPushes={} ae2BaselinePushes={}",
                        lowPercentage, ROUNDS, nexusPushes, normalPushes);
                // The fixture itself: four CPUs with 4,096 lanes each push 4,097 per four-tick window.
                helper.assertTrue(normalPushes == 32_776, "Unexpected AE2 baseline: " + normalPushes);
                // Installed lanes are shared and pattern energy is waived by default, so the Nexus
                // must match the stand-alone CPUs in both energy scenarios. A server operator may
                // lower the per-tick ceiling, so allow a 1% margin instead of pinning the default.
                // The pre-fix pool produced 4,096 pushes with full power and only 4 with a critical buffer.
                helper.assertTrue(nexusPushes * 100 >= normalPushes * 99,
                        "Nexus lost parallel throughput: " + nexusPushes + " vs " + normalPushes);
                // Identical inputs and output quantities; waiting outputs confirm accepted work per job.
                long waiting = pool.getRequestedAmount(AEItemKey.of(Items.STONE_BRICKS));
                helper.assertTrue(waiting == nexusPushes * 4, "Nexus output accounting mismatch");
                connection.destroy();
                node.destroy();
                helper.succeed();
            });
        });
    }

    private static CraftingPlan plan(IPatternDetails pattern) {
        var used = new KeyCounter();
        used.add(AEItemKey.of(Items.STONE), 4L * COPIES_PER_JOB);
        return new CraftingPlan(new GenericStack(AEItemKey.of(Items.STONE_BRICKS), COPIES_PER_JOB * 4L), 8,
                false, false, used, new KeyCounter(), new KeyCounter(), Map.of(pattern, (long) COPIES_PER_JOB));
    }

    private static final class Provider implements ICraftingProvider {
        private final IPatternDetails pattern;
        private long pushes;

        private Provider(IPatternDetails pattern) {
            this.pattern = pattern;
        }

        @Override
        public List<IPatternDetails> getAvailablePatterns() {
            return List.of(pattern);
        }

        @Override
        public boolean isBusy() {
            return false;
        }

        @Override
        public boolean pushPattern(IPatternDetails details, KeyCounter[] inputs) {
            pushes++;
            return true;
        }
    }

    private static final class Stock implements MEStorage {
        @Override
        public Component getDescription() {
            return Component.literal("Nexus regression stock");
        }

        @Override
        public long extract(AEKey key, long amount, Actionable mode, IActionSource source) {
            return key.equals(AEItemKey.of(Items.STONE)) ? amount : 0;
        }
    }

    private record Host(IGrid grid, IGridNode node, Level level) implements NexusVirtualCpuHost {
        @Override
        public IGrid ufo$getCpuGrid() {
            return grid;
        }

        @Override
        public IGridNode ufo$getCpuNode() {
            return node;
        }

        @Override
        public Level ufo$getCpuLevel() {
            return level;
        }

        @Override
        public IActionSource ufo$getCpuActionSource() {
            return IActionSource.empty();
        }

        @Override
        public boolean ufo$isCpuActive() {
            return true;
        }

        @Override
        public void ufo$markCpuDirty() {
        }

        @Override
        public Component ufo$getCpuName() {
            return Component.literal("Nexus regression CPU");
        }
    }
}

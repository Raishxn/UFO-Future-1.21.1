package com.raishxn.ufo.load;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.CraftingPlan;
import appeng.me.service.CraftingService;
import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.api.ae.NexusVirtualCpuHost;
import com.raishxn.ufo.crafting.NexusSharedCraftingCpuPool;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPool;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPoolProvider;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;

/**
 * TPS guard for the Quantum Computation Nexus scheduler.
 *
 * <p>The pool runs through its production path: RaishxCore registers it on the grid's crafting
 * service and ticks it once per server tick, while the jobs keep pushing patterns into a provider
 * that only counts calls. With the default per-tick ceiling the pool dispatches its full budget
 * every tick, which is the worst scheduler load the mod can produce; the test fails if the
 * accelerated average tick time exceeds the configured budget or if the fixture never dispatched.
 */
@GameTestHolder("ufo_load_tests")
@PrefixGameTestTemplate(false)
public final class NexusCraftingLoadGameTests {
    private static final String STRUCTURE = "ufo_load_tests:event_driven_structure";
    private static final int JOBS = positiveInteger("ufo.nexus.loadJobs", 16);
    private static final long LANES = positiveLong("ufo.nexus.loadLanes", 1_638_400L);
    private static final int MEASURED_TICKS = positiveInteger("ufo.nexus.loadTicks", 1_000);
    private static final double MAX_AVERAGE_TICK_MILLIS =
            positiveDouble("ufo.nexus.loadMaxAverageTickMillis", 50.0D);
    private static final long COPIES_PER_JOB = 1_000_000_000L;
    private static final Logger LOGGER = LogUtils.getLogger();

    private NexusCraftingLoadGameTests() {
    }

    @GameTestGenerator
    public static Collection<TestFunction> nexusSchedulerLoad() {
        return List.of(new TestFunction("ufo_nexus_load", "ufo_load_tests.nexus_scheduler_load",
                STRUCTURE, Rotation.NONE, Math.addExact(MEASURED_TICKS, 400), 0L, true, false, 1, 1, false,
                NexusCraftingLoadGameTests::exercise));
    }

    private static void exercise(GameTestHelper helper) {
        var level = helper.getLevel();
        var holder = level.getRecipeManager().byKey(ResourceLocation.withDefaultNamespace("stone_bricks")).orElseThrow();
        var stone = new ItemStack(Items.STONE);
        var empty = ItemStack.EMPTY;
        var encoded = PatternDetailsHelper.encodeCraftingPattern(
                new RecipeHolder<>(holder.id(), (CraftingRecipe) holder.value()),
                new ItemStack[]{stone, stone, empty, stone, stone, empty, empty, empty, empty},
                new ItemStack(Items.STONE_BRICKS, 4), false, false);
        var pattern = PatternDetailsHelper.decodePattern(encoded, level);
        var provider = new CountingProvider(pattern);
        var providerNode = GridHelper.createManagedNode(provider, (owner, changed) -> {})
                .setInWorldNode(false).addService(ICraftingProvider.class, provider);
        providerNode.create(level, helper.absolutePos(new BlockPos(1, 1, 1)));
        helper.setBlock(new BlockPos(2, 1, 1), AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.runAfterDelay(2, () -> {
            var power = (AENetworkedBlockEntity) level.getBlockEntity(helper.absolutePos(new BlockPos(2, 1, 1)));
            var providerConnection = GridHelper.createConnection(providerNode.getNode(), power.getMainNode().getNode());
            var grid = providerNode.getNode().getGrid();
            helper.assertTrue(grid != null, "Nexus TPS load grid never formed");
            var craftingService = (CraftingService) grid.getCraftingService();
            grid.getStorageService().addGlobalStorageProvider(mounts -> mounts.mount(new Stock(), 0));

            var host = new Host(grid, providerNode.getNode(), level);
            var pool = new NexusSharedCraftingCpuPool(host);
            pool.reconfigure(1_000_000_000L, (int) Math.min(Integer.MAX_VALUE - 2L, LANES), false);
            var poolNode = GridHelper.createManagedNode(new PoolOwner(pool), (owner, changed) -> {})
                    .setInWorldNode(false);
            poolNode.create(level, helper.absolutePos(new BlockPos(3, 1, 1)));
            var poolConnection = GridHelper.createConnection(poolNode.getNode(), power.getMainNode().getNode());

            await(helper, 80, () -> craftingService.getProviders(pattern).iterator().hasNext(),
                    "Nexus TPS load provider never registered", () -> await(helper, 80,
                            () -> craftingService.hasCpu(pool),
                            "Nexus TPS load pool never registered with RaishxCore", () -> {
                                for (int index = 0; index < JOBS; index++) {
                                    helper.assertTrue(pool.submitJob(grid, plan(pattern), IActionSource.empty(), null).successful(),
                                            "Nexus TPS load pool rejected job " + index);
                                }
                                // Let the four-tick ramp finish and the Core take over the ticking.
                                helper.runAfterDelay(5, () -> {
                                    long startedNanos = System.nanoTime();
                                    helper.runAfterDelay(MEASURED_TICKS, () -> {
                                        long elapsedNanos = System.nanoTime() - startedNanos;
                                        long pushes = provider.pushes;
                                        double averageTickMillis = elapsedNanos / 1_000_000.0D / MEASURED_TICKS;
                                        // AE2 meters operations over a four-tick window, so the steady
                                        // per-tick average is one quarter of the shared window.
                                        long sharedWindow = Math.min(LANES + JOBS,
                                                4L * UFOConfig.maxNexusPatternDispatchesPerTick());
                                        long expectedPerTick = Math.max(1L, sharedWindow / 4L);
                                        long floor = expectedPerTick * MEASURED_TICKS * 9L / 10L;
                                        long ceiling = expectedPerTick * MEASURED_TICKS * 105L / 100L;
                                        LOGGER.info("UFO Nexus TPS load: jobs={}, lanes={}, ticks={}, pushes={}, perTick={}, averageTickMs={}",
                                                JOBS, LANES, MEASURED_TICKS, pushes, pushes / (double) MEASURED_TICKS,
                                                averageTickMillis);
                                        helper.assertTrue(pushes >= floor,
                                                "Nexus TPS load dispatched too little: " + pushes + " < " + floor);
                                        helper.assertTrue(pushes <= ceiling,
                                                "Nexus TPS load exceeded the per-tick ceiling: " + pushes + " > " + ceiling);
                                        helper.assertTrue(averageTickMillis <= MAX_AVERAGE_TICK_MILLIS,
                                                "Nexus TPS load exceeded average tick budget: " + averageTickMillis
                                                        + " ms > " + MAX_AVERAGE_TICK_MILLIS + " ms");
                                        poolConnection.destroy();
                                        poolNode.destroy();
                                        providerConnection.destroy();
                                        providerNode.destroy();
                                        helper.succeed();
                                    });
                                });
                            }));
        });
    }

    private static CraftingPlan plan(IPatternDetails pattern) {
        var used = new KeyCounter();
        used.add(AEItemKey.of(Items.STONE), 4L * COPIES_PER_JOB);
        return new CraftingPlan(new GenericStack(AEItemKey.of(Items.STONE_BRICKS), 4L * COPIES_PER_JOB), 8,
                false, false, used, new KeyCounter(), new KeyCounter(), Map.of(pattern, COPIES_PER_JOB));
    }

    private static void await(GameTestHelper helper, int remaining, BooleanSupplier condition,
                              String message, Runnable next) {
        if (condition.getAsBoolean()) {
            next.run();
            return;
        }
        helper.assertTrue(remaining > 0, message);
        helper.runAfterDelay(1, () -> await(helper, remaining - 1, condition, message, next));
    }

    private static int positiveInteger(String property, int fallback) {
        int value = Integer.getInteger(property, fallback);
        if (value <= 0) throw new IllegalArgumentException(property + " must be positive");
        return value;
    }

    private static long positiveLong(String property, long fallback) {
        long value = Long.getLong(property, fallback);
        if (value <= 0L) throw new IllegalArgumentException(property + " must be positive");
        return value;
    }

    private static double positiveDouble(String property, double fallback) {
        double value = Double.parseDouble(System.getProperty(property, Double.toString(fallback)));
        if (!Double.isFinite(value) || value <= 0.0D) throw new IllegalArgumentException(property + " must be positive");
        return value;
    }

    private static final class CountingProvider implements ICraftingProvider {
        private final IPatternDetails pattern;
        private long pushes;

        private CountingProvider(IPatternDetails pattern) {
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
            return Component.literal("Nexus TPS load stock");
        }

        @Override
        public long extract(AEKey key, long amount, Actionable mode, IActionSource source) {
            return key.equals(AEItemKey.of(Items.STONE)) ? amount : 0L;
        }
    }

    private static final class PoolOwner implements SharedCraftingCpuPoolProvider {
        private final SharedCraftingCpuPool pool;

        private PoolOwner(SharedCraftingCpuPool pool) {
            this.pool = pool;
        }

        @Override
        public SharedCraftingCpuPool getSharedCraftingCpuPool() {
            return pool;
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
            return Component.literal("Nexus TPS load CPU");
        }
    }
}

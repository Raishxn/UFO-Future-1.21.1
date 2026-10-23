package com.raishxn.ufo.load;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEBlocks;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import com.raishxn.ufo.diagnostic.MachinePerformanceReport;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Early-game TPS gate: a fleet of Dimensional Matter Assemblers running a real recipe
 * ({@code ufo:dma/dust_blizz_bootstrap}) on isolated AE2 grids. Besides the wall-time budget,
 * it asserts operation counts that do not depend on the machine running the test:
 * <ul>
 *     <li>powered: every DMA keeps progressing and produces output;</li>
 *     <li>starved (power removed): the DMA backs off instead of polling energy every tick;</li>
 *     <li>Applied Flux FE in the ME network: a DMA with a full buffer does not extract and
 *     re-insert network FE every tick.</li>
 * </ul>
 */
@GameTestHolder("ufo_load_tests")
@PrefixGameTestTemplate(false)
public final class DmaFleetLoadGameTests {
    private static final String BATCH = "ufo_dma_fleet_load";
    private static final int MACHINES = positiveInteger("ufo.dma.loadMachines", 64, 1024);
    private static final int POWERED_TICKS = positiveInteger("ufo.dma.loadTicks", 600, 1_000_000);
    private static final int STARVED_TICKS = 200;
    private static final int APPFLUX_TICKS = 200;
    private static final int MAINTENANCE_INTERVAL = 20;
    private static final double MAX_AVERAGE_TICK_MILLIS =
            Double.parseDouble(System.getProperty("ufo.dma.loadMaxAverageTickMillis", "50"));
    /** A starved DMA may run at most this share of the ticks in the window (backoff to 20-tick polling). */
    private static final double MAX_STARVED_CALL_RATIO = 0.25D;
    /** Network FE operations allowed per machine over the whole Applied Flux window (initial buffer fill). */
    private static final long MAX_FLUX_OPERATIONS_PER_MACHINE = 4L;
    private static final AEFluidKey WATER = AEFluidKey.of(Fluids.WATER);
    private static final Set<ChunkPos> FORCED = new LinkedHashSet<>();

    private DmaFleetLoadGameTests() {
    }

    @GameTestGenerator
    public static Collection<TestFunction> dmaFleet() {
        int timeout = Math.addExact(POWERED_TICKS, STARVED_TICKS + APPFLUX_TICKS + 800);
        return List.of(new TestFunction(BATCH, "ufo_load_tests.dma_fleet", "ufo_load_tests:event_driven_structure",
                Rotation.NONE, timeout, 0L, true, false, 1, 1, false, DmaFleetLoadGameTests::exercise));
    }

    @AfterBatch(batch = BATCH)
    public static void cleanup(ServerLevel level) {
        FORCED.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false));
        FORCED.clear();
    }

    private static void exercise(GameTestHelper helper) {
        helper.assertTrue(Double.isFinite(MAX_AVERAGE_TICK_MILLIS) && MAX_AVERAGE_TICK_MILLIS > 0,
                "invalid average tick budget");
        MachinePerformanceRegistry.INSTANCE.reset();
        var fleet = build(helper);
        await(helper, 400, () -> fleet.stream().allMatch(p -> p.dma.getMainNode().isActive()),
                "DMA fleet grids never became active", () -> {
                    fleet.forEach(DmaFleetLoadGameTests::maintain);
                    var report = new JsonObject();
                    report.addProperty("schemaVersion", 1);
                    report.addProperty("capturedAtUtc", Instant.now().toString());
                    report.addProperty("machines", MACHINES);
                    report.addProperty("recipe", "ufo:dma/dust_blizz_bootstrap");
                    report.addProperty("measurementContract", "Headless GameTest server; wall time includes all "
                            + "server work, not live-modpack TPS. Call and FE-operation counts are machine independent.");
                    poweredPhase(helper, fleet, report);
                });
    }

    private static List<Probe> build(GameTestHelper helper) {
        var level = helper.getLevel();
        int columns = (int) Math.ceil(Math.sqrt(MACHINES));
        var fleet = new ArrayList<Probe>();
        for (int i = 0; i < MACHINES; i++) {
            // Three blocks apart: each DMA and its creative cell form an isolated grid.
            BlockPos dmaPos = helper.absolutePos(new BlockPos(2 + i % columns * 3, 3, 2 + i / columns * 3));
            BlockPos cellPos = dmaPos.below();
            force(level, new ChunkPos(dmaPos));
            level.setBlockAndUpdate(cellPos, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
            level.setBlockAndUpdate(dmaPos, ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER_BLOCK.get().defaultBlockState());
            var dma = (DimensionalMatterAssemblerBlockEntity) level.getBlockEntity(dmaPos);
            helper.assertTrue(dma != null, "DMA block entity missing at " + dmaPos);
            fleet.add(new Probe(dmaPos, cellPos, dma));
        }
        return fleet;
    }

    /** Tops inputs up and drains outputs; done on an interval so the inventory listener stays quiet. */
    private static void maintain(Probe p) {
        var input = p.dma.getInput();
        topUp(input, 0, new ItemStack(Items.SNOWBALL, 16));
        topUp(input, 1, new ItemStack(Items.SNOWBALL, 16));
        topUp(input, 2, new ItemStack(Items.SNOWBALL, 16));
        topUp(input, 3, new ItemStack(Items.SNOWBALL, 16));
        topUp(input, 4, new ItemStack(Items.PACKED_ICE, 64));
        var tank = p.dma.getTank();
        long water = tank.getAmount(3);
        if (water < 8_000) {
            tank.insert(WATER, 16_000 - water, Actionable.MODULATE, IActionSource.empty());
        }
        var output = p.dma.getOutput();
        for (int slot = 0; slot < output.size(); slot++) {
            var stack = output.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                p.produced += stack.getCount();
                output.setItemDirect(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void topUp(appeng.api.inventories.InternalInventory inventory, int slot, ItemStack full) {
        var current = inventory.getStackInSlot(slot);
        if (current.isEmpty() || current.getCount() < full.getCount() / 2) {
            inventory.setItemDirect(slot, full.copy());
        }
    }

    private static void poweredPhase(GameTestHelper helper, List<Probe> fleet, JsonObject report) {
        long startTick = helper.getTick();
        long startNanos = System.nanoTime();
        fleet.forEach(p -> p.callsBefore = calls(helper, p));
        runFor(helper, fleet, POWERED_TICKS, () -> {
            long measuredTicks = helper.getTick() - startTick;
            double wallMsPerTick = (System.nanoTime() - startNanos) / 1_000_000D / measuredTicks;
            long minProduced = Long.MAX_VALUE;
            long totalProduced = 0;
            for (Probe p : fleet) {
                minProduced = Math.min(minProduced, p.produced);
                totalProduced += p.produced;
            }
            report.addProperty("poweredTicks", measuredTicks);
            report.addProperty("poweredWallMillisPerTick", wallMsPerTick);
            report.addProperty("poweredTotalOutput", totalProduced);
            report.addProperty("poweredMinOutputPerMachine", minProduced);
            LogUtils.getLogger().info("UFO DMA fleet powered phase: machines={}, ticks={}, wallMsPerTick={}, output={}",
                    MACHINES, measuredTicks, wallMsPerTick, totalProduced);
            helper.assertTrue(minProduced > 0, "a powered DMA produced nothing");
            helper.assertTrue(wallMsPerTick <= MAX_AVERAGE_TICK_MILLIS,
                    "DMA fleet exceeded the average wall-time budget: " + wallMsPerTick + " ms/tick");
            starvedPhase(helper, fleet, report);
        });
    }

    private static void starvedPhase(GameTestHelper helper, List<Probe> fleet, JsonObject report) {
        var level = helper.getLevel();
        fleet.forEach(p -> level.setBlockAndUpdate(p.cellPos, Blocks.AIR.defaultBlockState()));
        // Let the grid split settle before counting.
        helper.runAfterDelay(20, () -> {
            fleet.forEach(p -> p.callsBefore = calls(helper, p));
            long startTick = helper.getTick();
            runFor(helper, fleet, STARVED_TICKS, () -> {
                long window = helper.getTick() - startTick;
                long maxCalls = 0;
                for (Probe p : fleet) {
                    maxCalls = Math.max(maxCalls, calls(helper, p) - p.callsBefore);
                }
                double ratio = (double) maxCalls / window;
                report.addProperty("starvedTicks", window);
                report.addProperty("starvedMaxCallsPerMachine", maxCalls);
                LogUtils.getLogger().info("UFO DMA fleet starved phase: window={}, maxCallsPerMachine={}, ratio={}",
                        window, maxCalls, ratio);
                helper.assertTrue(ratio <= MAX_STARVED_CALL_RATIO,
                        "starved DMA kept polling: " + maxCalls + " calls in " + window + " ticks");
                appFluxPhase(helper, fleet, report);
            });
        });
    }

    private static void appFluxPhase(GameTestHelper helper, List<Probe> fleet, JsonObject report) {
        if (!ModList.get().isLoaded("appflux")) {
            report.addProperty("appFluxPhase", "skipped: Applied Flux not loaded");
            finish(helper, fleet, report);
            return;
        }
        var level = helper.getLevel();
        fleet.forEach(p -> level.setBlockAndUpdate(p.cellPos, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState()));
        await(helper, 200, () -> fleet.stream().allMatch(p -> p.dma.getMainNode().isActive()),
                "DMA fleet did not reconnect after power returned", () -> {
                    var flux = new FluxStock();
                    for (Probe p : fleet) {
                        IGrid grid = p.dma.getMainNode().getGrid();
                        grid.getStorageService().addGlobalStorageProvider(mounts -> mounts.mount(flux, 0));
                    }
                    // Allow the first recharge to fill each buffer, then count steady-state operations.
                    helper.runAfterDelay(40, () -> {
                        long before = flux.modulated;
                        long startTick = helper.getTick();
                        runFor(helper, fleet, APPFLUX_TICKS, () -> {
                            long window = helper.getTick() - startTick;
                            long operations = flux.modulated - before;
                            report.addProperty("appFluxTicks", window);
                            report.addProperty("appFluxModulatedOperations", operations);
                            LogUtils.getLogger().info("UFO DMA fleet Applied Flux phase: window={}, feOperations={}",
                                    window, operations);
                            helper.assertTrue(operations <= MAX_FLUX_OPERATIONS_PER_MACHINE * MACHINES,
                                    "DMAs with full buffers touched network FE " + operations + " times in "
                                            + window + " ticks");
                            finish(helper, fleet, report);
                        });
                    });
                });
    }

    private static void finish(GameTestHelper helper, List<Probe> fleet, JsonObject report) {
        try {
            var directory = FMLPaths.GAMEDIR.get().resolve("ufo-diagnostics");
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("dma-fleet-load.json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n");
            Files.writeString(directory.resolve("dma-fleet-machine-metrics.json"), MachinePerformanceReport.toJson(
                    "dma-fleet", Instant.now().toString(), MachinePerformanceRegistry.INSTANCE.snapshots()));
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("could not save DMA fleet diagnostics", failure);
        }
        LogUtils.getLogger().info("UFO DMA fleet load passed: {}", report);
        cleanup(helper.getLevel());
        helper.succeed();
    }

    private static void runFor(GameTestHelper helper, List<Probe> fleet, int ticks, Runnable next) {
        if (ticks <= 0) {
            next.run();
            return;
        }
        if (ticks % MAINTENANCE_INTERVAL == 0) {
            fleet.forEach(DmaFleetLoadGameTests::maintain);
        }
        helper.runAfterDelay(1, () -> runFor(helper, fleet, ticks - 1, next));
    }

    private static long calls(GameTestHelper helper, Probe p) {
        return MachinePerformanceRegistry.INSTANCE
                .snapshot(helper.getLevel().dimension().location().toString(), p.pos.asLong())
                .map(MachinePerformanceRegistry.Snapshot::tickCount)
                .orElse(0L);
    }

    private static void await(GameTestHelper helper, int remaining, BooleanSupplier condition, String message,
                              Runnable next) {
        if (condition.getAsBoolean()) {
            next.run();
            return;
        }
        helper.assertTrue(remaining > 0, message);
        helper.runAfterDelay(1, () -> await(helper, remaining - 1, condition, message, next));
    }

    private static void force(ServerLevel level, ChunkPos chunk) {
        if (FORCED.add(chunk)) level.setChunkForced(chunk.x, chunk.z, true);
    }

    private static int positiveInteger(String key, int fallback, int max) {
        int value = Integer.parseInt(System.getProperty(key, Integer.toString(fallback)));
        if (value <= 0 || value > max) throw new IllegalArgumentException(key);
        return value;
    }

    private static final class Probe {
        final BlockPos pos, cellPos;
        final DimensionalMatterAssemblerBlockEntity dma;
        long produced, callsBefore;

        Probe(BlockPos pos, BlockPos cellPos, DimensionalMatterAssemblerBlockEntity dma) {
            this.pos = pos;
            this.cellPos = cellPos;
            this.dma = dma;
        }
    }

    /** Unlimited Applied Flux FE in the ME network that counts every modulated FE operation. */
    private static final class FluxStock implements MEStorage {
        private static final long STOCK = Long.MAX_VALUE / 4;
        long modulated;

        private static final AEKey FE = com.glodblock.github.appflux.common.me.key.FluxKey.of(
                com.glodblock.github.appflux.common.me.key.type.EnergyType.FE);

        private static boolean isFe(AEKey key) {
            return FE.equals(key);
        }

        @Override
        public long extract(AEKey key, long amount, Actionable mode, IActionSource source) {
            if (!isFe(key) || amount <= 0) return 0;
            if (mode == Actionable.MODULATE) modulated++;
            return amount;
        }

        @Override
        public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
            if (!isFe(key) || amount <= 0) return 0;
            if (mode == Actionable.MODULATE) modulated++;
            return amount;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            out.add(FE, STOCK);
        }

        @Override
        public net.minecraft.network.chat.Component getDescription() {
            return net.minecraft.network.chat.Component.literal("Counting Applied Flux FE stock");
        }
    }
}

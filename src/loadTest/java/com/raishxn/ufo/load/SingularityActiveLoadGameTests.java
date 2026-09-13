package com.raishxn.ufo.load;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.execution.CraftingCpuHelper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.raishxn.ufo.api.multiblock.StructureMembershipIndex;
import com.raishxn.ufo.block.InfinityFabricationSingularityControllerBlock;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.InfinityFabricationSingularityControllerBE;
import com.raishxn.ufo.block.entity.MassiveOutputHatchBE;
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import com.raishxn.ufo.block.entity.pattern.InfinityFabricationSingularityPatternFactory;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import com.raishxn.ufo.diagnostic.MachinePerformanceReport;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Actual automatic recipes on a headless fleet; controlled MEStorage is the stock ledger. */
@Mod("ufo_load_tests")
@GameTestHolder("ufo_load_tests")
@PrefixGameTestTemplate(false)
public final class SingularityActiveLoadGameTests {
    private static final String BATCH = "ufo_active_load";
    private static final int MACHINES = positiveInteger("ufo.load.machines", 102, 512);
    private static final int ACTIVE_TICKS = positiveInteger("ufo.load.ticks", 10_000, 1_000_000);
    private static final int BLOCKED_TICKS = 700;
    private static final long INITIAL_STONE = 100_000_000L;
    private static final double MAX_AVERAGE_TICK_MILLIS = Double.parseDouble(System.getProperty("ufo.load.maxAverageTickMillis", "50"));
    private static final AEItemKey STONE = AEItemKey.of(Items.STONE);
    private static final AEItemKey BRICKS = AEItemKey.of(Items.STONE_BRICKS);
    private static final Set<ChunkPos> FORCED = new LinkedHashSet<>();
    private static boolean recordingOwned;
    private static Path directory;
    private static long startedNanos;
    private static long startedTick;
    private static boolean outageObserved;
    private static boolean reconnectObserved;

    @GameTestGenerator
    public static Collection<TestFunction> activeFleet() {
        return List.of(new TestFunction(BATCH, "ufo_load_tests.active_fleet", "ufo_load_tests:event_driven_structure", Rotation.NONE,
                Math.addExact(ACTIVE_TICKS, BLOCKED_TICKS + 1500), 0L, true, false, 1, 1, false,
                SingularityActiveLoadGameTests::exercise));
    }

    @AfterBatch(batch = BATCH)
    public static void cleanup(ServerLevel level) {
        try { finishRecording(); }
        finally {
            FORCED.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false));
            FORCED.clear();
        }
    }

    private static void exercise(GameTestHelper helper) {
        helper.assertTrue(Double.isFinite(MAX_AVERAGE_TICK_MILLIS) && MAX_AVERAGE_TICK_MILLIS > 0, "invalid average tick budget");
        helper.assertTrue(ACTIVE_TICKS >= 200, "active window must be at least 200 ticks to observe outage and recovery");
        MachinePerformanceRegistry.INSTANCE.reset();
        StructureMembershipIndex.INSTANCE.reset();
        outageObserved = false; reconnectObserved = false;
        var fleet = build(helper);
        var level = helper.getLevel();
        long deadline = System.nanoTime() + 30_000_000_000L;
        level.getServer().managedBlock(() -> {
            if (FORCED.stream().allMatch(c -> level.getChunkSource().isPositionTicking(c.toLong())) || System.nanoTime() >= deadline) return true;
            level.getChunkSource().pollTask(); return false;
        });
        helper.assertTrue(FORCED.stream().allMatch(c -> level.getChunkSource().isPositionTicking(c.toLong())), "active fleet chunk futures were not tickable");
        await(helper, 1000, () -> fleet.stream().allMatch(p -> ready(level, p)), "active fleet did not complete physical ME/AE2 initialization", () -> awaitQuietStartup(helper, fleet, 1000, List.of(), 0, () -> {
            for (Probe p : fleet) {
                mount(p);
                helper.assertTrue(p.controller.getAccessiblePatternInventory().addItems(pattern(helper)).isEmpty(), "pattern insertion failed");
                if (!p.controller.isPatternEnabled(0)) p.controller.togglePatternEnabled(0);
                helper.assertTrue(p.hatch.getStoredExternalEnergyAE() == 0D, "energy fixture must start empty");
                p.baselineScans = scans(helper, p);
            }
            startRecording();
            startedNanos = System.nanoTime(); startedTick = helper.getTick();
            LogUtils.getLogger().info("UFO active load ready: machines={}, blockedTicks={}, activeTicks={}, chunks={}", MACHINES, BLOCKED_TICKS, ACTIVE_TICKS, FORCED.size());
            step(helper, fleet, 0);
        }));
    }

    private static void awaitQuietStartup(GameTestHelper helper, List<Probe> fleet, int remaining, List<Long> previous, int quietTicks, Runnable next) {
        var current = fleet.stream().map(p -> scans(helper, p)).toList();
        boolean initialized = fleet.stream().allMatch(p -> ready(helper.getLevel(), p));
        int quiet = initialized && current.equals(previous) ? quietTicks + 1 : 0;
        if (!previous.isEmpty() && !current.equals(previous))
            LogUtils.getLogger().info("UFO active startup still settling: tick={}, scansChanged={}", helper.getTick(), !current.equals(previous));
        if (quiet >= 40) { next.run(); return; }
        helper.assertTrue(remaining > 0, "active startup never reached 40 initialized ticks without scans");
        helper.runAfterDelay(1, () -> awaitQuietStartup(helper, fleet, remaining - 1, current, quiet, next));
    }

    private static List<Probe> build(GameTestHelper helper) {
        var level = helper.getLevel();
        var definition = InfinityFabricationSingularityPatternFactory.getDefinition();
        int columns = (int) Math.ceil(Math.sqrt(MACHINES));
        var fleet = new ArrayList<Probe>();
        for (int i = 0; i < MACHINES; i++) {
            BlockPos pos = helper.absolutePos(new BlockPos(20 + i % columns * 48, 5, 20 + i / columns * 48));
            var positions = definition.pattern().getTrackedPositions(pos, Direction.EAST);
            positions.forEach(p -> force(level, new ChunkPos(p)));
            level.setBlockAndUpdate(pos, MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get().defaultBlockState()
                    .setValue(InfinityFabricationSingularityControllerBlock.FACING, Direction.NORTH));
            definition.pattern().assembleAsCreative(level, pos, Direction.EAST, definition.defaultCreativeStates());
            var controller = (InfinityFabricationSingularityControllerBE) level.getBlockEntity(pos);
            controller.scanStructure(level);
            var linkPos = definition.pattern().getExpectedPositions(pos, Direction.EAST, 'L').getFirst();
            var link = (QuantumGridLinkBE) level.getBlockEntity(linkPos);
            var power = linkPos.relative(link.getBlockState().getValue(DirectionalBlock.FACING));
            force(level, new ChunkPos(power));
            helper.assertTrue(level.getBlockState(power).isAir(), "ME fixture overlaps machine shell");
            level.setBlockAndUpdate(power, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
            var entities = positions.stream().map(level::getBlockEntity).filter(AEBaseBlockEntity.class::isInstance).map(AEBaseBlockEntity.class::cast).toList();
            var hatches = entities.stream().filter(MassiveOutputHatchBE.class::isInstance).map(MassiveOutputHatchBE.class::cast).filter(MassiveOutputHatchBE::supportsEnergyInput).toList();
            helper.assertTrue(hatches.size() == 1, "active recipe fixture needs exactly one local FE hatch");
            fleet.add(new Probe(pos, power, controller, link, hatches.getFirst(), entities));
        }
        return fleet;
    }

    private static boolean ready(ServerLevel level, Probe p) {
        return p.controller.isAssembled() && p.link.isNetworkReady() && p.controller.getReadyInvoked() > 0
                && p.entities.stream().allMatch(e -> !e.isRemoved() && e.getQueuedForReady() == e.getReadyInvoked())
                && level.getChunkSource().isPositionTicking(new ChunkPos(p.pos).toLong());
    }

    private static void step(GameTestHelper helper, List<Probe> fleet, int tick) {
        if (tick == BLOCKED_TICKS) {
            for (Probe p : fleet) {
                helper.assertTrue(p.link.getPendingCraftingRouteCount() == p.controller.getRouteLimit(), "blocked recipes did not reach the bounded route limit");
                helper.assertTrue(p.storage.bricks == 0 && p.storage.rejected > 0, "full ME storage must reject real recipe products");
                helper.assertTrue(INITIAL_STONE - p.storage.stone == pending(helper, p), "blocked recipe consumed inputs without preserving exact output");
                p.blockedConsumed = INITIAL_STONE - p.storage.stone;
                p.storage.acceptOutput = true;
            }
            LogUtils.getLogger().info("UFO active load blocked checkpoint passed: machines={}, routesPerMachine={}", MACHINES, fleet.getFirst().controller.getRouteLimit());
        }
        if (tick == BLOCKED_TICKS + ACTIVE_TICKS / 2) disconnect(helper, fleet.getFirst());
        if (tick % 20 == 0 && tick < BLOCKED_TICKS + ACTIVE_TICKS) {
            for (Probe p : fleet) p.suppliedFe += p.hatch.getExternalEnergyHandler(null).receiveEnergy(1000, false);
        }
        if (tick == BLOCKED_TICKS + ACTIVE_TICKS) {
            fleet.forEach(p -> { if (p.controller.isPatternEnabled(0)) p.controller.togglePatternEnabled(0); });
            await(helper, 200, () -> fleet.stream().allMatch(p -> p.link.getPendingCraftingRouteCount() == 0), "real recipe outputs did not drain", () -> finish(helper, fleet));
            return;
        }
        helper.runAfterDelay(1, () -> step(helper, fleet, tick + 1));
    }

    private static void disconnect(GameTestHelper helper, Probe p) {
        var level = helper.getLevel();
        helper.assertTrue(scans(helper, p) == p.baselineScans, "unchanged active machine rescanned before physical outage");
        long beforeOutage = scans(helper, p);
        level.setBlockAndUpdate(p.power, Blocks.AIR.defaultBlockState());
        await(helper, 100, () -> !p.link.isNetworkReady(), "physical power removal did not deactivate the grid", () -> {
            outageObserved = true;
            long stone = p.storage.stone, outputs = pending(helper, p);
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(p.storage.stone == stone && pending(helper, p) == outputs, "offline machine changed real input/output balances");
                long afterRemoval = scans(helper, p);
                helper.assertTrue(afterRemoval - beforeOutage <= 1, "power-removal invalidation produced repeated scans");
                level.setBlockAndUpdate(p.power, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
                await(helper, 200, () -> p.link.isNetworkReady(), "active machine did not reconnect physically", () -> {
                    mount(p);
                    helper.runAfterDelay(40, () -> {
                        p.eventScans = scans(helper, p) - beforeOutage;
                        helper.assertTrue(p.eventScans <= 2 && scans(helper, p) - afterRemoval <= 1, "power-replacement invalidation produced repeated scans");
                        reconnectObserved = true;
                    });
                });
            });
        });
    }

    private static void finish(GameTestHelper helper, List<Probe> fleet) {
        long measuredTicks = helper.getTick() - startedTick;
        double elapsedMillis = (System.nanoTime() - startedNanos) / 1_000_000D;
        var report = new JsonObject(); report.addProperty("schemaVersion", 1);
        report.addProperty("capturedAtUtc", Instant.now().toString()); report.addProperty("machines", MACHINES);
        report.addProperty("blockedTicks", BLOCKED_TICKS); report.addProperty("requestedActiveTicks", ACTIVE_TICKS);
        report.addProperty("observedGameTestTicks", measuredTicks); report.addProperty("elapsedWallMillis", elapsedMillis);
        report.addProperty("wallMillisPerObservedGameTestTick", elapsedMillis / measuredTicks);
        report.addProperty("measurementContract", "Headless real Singularity recipes; controlled MEStorage, finite FE feed. Accelerated GameTest wall intervals include server work, not live-modpack TPS; controller percentiles cover last <=256 recorded calls. No player/client traffic.");
        report.addProperty("jfrContract", "Minecraft built-in JFR configuration, including server tick-time and packet events; headless fleet has no player network traffic.");
        report.addProperty("recipe", "minecraft:stone_bricks (4 stone -> 4 bricks)");
        report.addProperty("outageObserved", outageObserved); report.addProperty("reconnectObserved", reconnectObserved);
        var machines = new JsonArray();
        for (Probe p : fleet) {
            var entry = new JsonObject(); entry.addProperty("controllerPos", p.pos.asLong());
            entry.addProperty("consumedStone", INITIAL_STONE - p.storage.stone); entry.addProperty("insertedBricks", p.storage.bricks);
            entry.addProperty("blockedConsumedStone", p.blockedConsumed); entry.addProperty("pendingBricks", pending(helper, p));
            entry.addProperty("suppliedFe", p.suppliedFe); entry.addProperty("remainingLocalAe", p.hatch.getStoredExternalEnergyAE());
            double consumedAe = p.suppliedFe * PowerUnit.FE.convertTo(PowerUnit.AE, 1D) - p.hatch.getStoredExternalEnergyAE();
            var oneCopy = new KeyCounter(); oneCopy.add(STONE, 4L);
            double expectedAe = (INITIAL_STONE - p.storage.stone) / 4D * CraftingCpuHelper.calculatePatternPower(new KeyCounter[]{oneCopy})
                    * p.controller.getCraftingMode().energyMultiplier() * PowerMultiplier.CONFIG.multiply(1D);
            entry.addProperty("consumedLocalAe", consumedAe); entry.addProperty("expectedRecipeAe", expectedAe);
            entry.addProperty("simulatedStorageCalls", p.storage.simulated); entry.addProperty("modulatedStorageCalls", p.storage.modulated);
            entry.addProperty("rejectedProductAmount", p.storage.rejected); entry.addProperty("additionalScans", scans(helper, p) - p.baselineScans);
            entry.addProperty("physicalPowerChangeScans", p.eventScans);
            entry.addProperty("unattributedScans", scans(helper, p) - p.baselineScans - p.eventScans);
            machines.add(entry);
        }
        report.add("stockAndEnergyLedgers", machines);
        write("active-load.json", new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n");
        write("machine-metrics.json", MachinePerformanceReport.toJson("singularity-active-recipes", Instant.now().toString(), MachinePerformanceRegistry.INSTANCE.snapshots()));
        finishRecording();
        helper.assertTrue(outageObserved && reconnectObserved, "physical outage/reconnect was not observed");
        for (int i = 0; i < fleet.size(); i++) {
            Probe p = fleet.get(i); var entry = machines.get(i).getAsJsonObject();
            helper.assertTrue(p.link.isNetworkReady() && p.controller.isAssembled(), "active machine did not remain operational");
            helper.assertTrue(p.storage.bricks == INITIAL_STONE - p.storage.stone && p.storage.bricks > p.blockedConsumed, "active recipe conservation/continued throughput failed");
            helper.assertTrue(p.link.getPendingCraftingRouteCount() == 0, "active recipe output was retained after drain");
            helper.assertTrue(Math.abs(entry.get("consumedLocalAe").getAsDouble() - entry.get("expectedRecipeAe").getAsDouble()) < 0.01D,
                    "recipe energy was not accounted for exclusively by the finite local FE supply");
            helper.assertTrue(scans(helper, p) - p.baselineScans == p.eventScans, "unchanged active output processing introduced a structural rescan");
            var metric = MachinePerformanceRegistry.INSTANCE.snapshot(helper.getLevel().dimension().location().toString(), p.pos.asLong()).orElseThrow();
            helper.assertTrue(metric.tickCount() >= measuredTicks, "controller profiling missed real server ticks");
        }
        helper.assertTrue(elapsedMillis / measuredTicks <= MAX_AVERAGE_TICK_MILLIS, "active fleet exceeded average wall-time budget");
        LogUtils.getLogger().info("UFO active load passed: machines={}, measuredTicks={}, wallMsPerTick={}, exact stock/FE, zero scans outside physical power-change windows", MACHINES, measuredTicks, elapsedMillis / measuredTicks);
        cleanup(helper.getLevel()); helper.succeed();
    }

    private static long pending(GameTestHelper helper, Probe p) {
        var tag = new CompoundTag(); p.link.saveAdditional(tag, helper.getLevel().registryAccess());
        long amount = 0;
        for (var route : tag.getList("pendingCraftingRoutes", 10)) {
            for (var output : ((CompoundTag) route).getList("outputs", 10)) {
                var stack = GenericStack.readTag(helper.getLevel().registryAccess(), (CompoundTag) output);
                helper.assertTrue(stack != null && stack.what().equals(BRICKS), "real recipe produced an unexpected pending key");
                amount = Math.addExact(amount, stack.amount());
            }
        }
        return amount;
    }

    private static ItemStack pattern(GameTestHelper helper) {
        var holder = helper.getLevel().getRecipeManager().byKey(ResourceLocation.withDefaultNamespace("stone_bricks")).orElseThrow();
        if (!(holder.value() instanceof CraftingRecipe recipe)) throw new IllegalStateException("stone bricks recipe is not crafting");
        var stone = new ItemStack(Items.STONE); var empty = ItemStack.EMPTY;
        return PatternDetailsHelper.encodeCraftingPattern(new RecipeHolder<>(holder.id(), recipe),
                new ItemStack[]{stone, stone, empty, stone, stone, empty, empty, empty, empty}, new ItemStack(Items.STONE_BRICKS, 4), false, false);
    }

    private static void mount(Probe p) {
        IGrid grid = p.link.getGrid();
        if (grid != p.mounted) { grid.getStorageService().addGlobalStorageProvider(mounts -> mounts.mount(p.storage, 0)); p.mounted = grid; }
    }
    private static void force(ServerLevel level, ChunkPos chunk) { if (FORCED.add(chunk)) level.setChunkForced(chunk.x, chunk.z, true); }
    private static long scans(GameTestHelper helper, Probe p) { return MachinePerformanceRegistry.INSTANCE.snapshot(helper.getLevel().dimension().location().toString(), p.pos.asLong()).orElseThrow().scanCount(); }
    private static void await(GameTestHelper helper, int remaining, BooleanSupplier condition, String message, Runnable next) {
        if (condition.getAsBoolean()) { next.run(); return; }
        helper.assertTrue(remaining > 0, message);
        helper.runAfterDelay(1, () -> await(helper, remaining - 1, condition, message, next));
    }
    private static int positiveInteger(String key, int fallback, int max) {
        int value = Integer.parseInt(System.getProperty(key, Integer.toString(fallback)));
        if (value <= 0 || value > max) throw new IllegalArgumentException(key); return value;
    }
    private static void startRecording() {
        try {
            directory = FMLPaths.GAMEDIR.get().resolve("ufo-diagnostics"); Files.createDirectories(directory);
            if (!JvmProfiler.INSTANCE.isAvailable() || JvmProfiler.INSTANCE.isRunning())
                throw new IllegalStateException("Minecraft JFR must be available and idle in the isolated load server");
            recordingOwned = JvmProfiler.INSTANCE.start(Environment.SERVER);
            if (!recordingOwned) throw new IllegalStateException("Minecraft JFR did not start");
        } catch (Exception failure) { throw new IllegalStateException("could not start active-load JFR", failure); }
    }
    private static void finishRecording() {
        if (!recordingOwned) return;
        try {
            Path nativeRecording = JvmProfiler.INSTANCE.stop();
            Files.copy(nativeRecording, directory.resolve("active-load.jfr"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception failure) { throw new IllegalStateException("could not save active-load JFR", failure); }
        finally { recordingOwned = false; }
    }
    private static void write(String name, String content) {
        try { Files.writeString(directory.resolve(name), content); }
        catch (java.io.IOException failure) { throw new IllegalStateException("could not save active-load diagnostics", failure); }
    }
    private static final class Probe {
        final BlockPos pos, power;
        final InfinityFabricationSingularityControllerBE controller;
        final QuantumGridLinkBE link;
        final MassiveOutputHatchBE hatch;
        final List<AEBaseBlockEntity> entities;
        final Stock storage = new Stock();
        IGrid mounted;
        long baselineScans, suppliedFe, blockedConsumed, eventScans;
        Probe(BlockPos pos, BlockPos power, InfinityFabricationSingularityControllerBE controller, QuantumGridLinkBE link, MassiveOutputHatchBE hatch, List<AEBaseBlockEntity> entities) {
            this.pos = pos; this.power = power; this.controller = controller; this.link = link; this.hatch = hatch; this.entities = entities;
        }
    }
    private static final class Stock implements MEStorage {
        long stone = INITIAL_STONE, bricks, rejected, simulated, modulated;
        boolean acceptOutput;
        public long extract(AEKey key, long amount, Actionable mode, IActionSource source) {
            if (mode == Actionable.SIMULATE) simulated++; else modulated++;
            if (!STONE.equals(key) || amount <= 0) return 0;
            long taken = Math.min(amount, stone); if (mode == Actionable.MODULATE) stone -= taken; return taken;
        }
        public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
            if (mode == Actionable.SIMULATE) simulated++; else modulated++;
            if (amount <= 0) return 0;
            if (STONE.equals(key)) { if (mode == Actionable.MODULATE) stone += amount; return amount; }
            if (!BRICKS.equals(key)) return 0;
            if (!acceptOutput) { if (mode == Actionable.MODULATE) rejected += amount; return 0; }
            if (mode == Actionable.MODULATE) bricks += amount; return amount;
        }
        public void getAvailableStacks(KeyCounter out) { out.add(STONE, stone); if (bricks > 0) out.add(BRICKS, bricks); }
        public net.minecraft.network.chat.Component getDescription() { return net.minecraft.network.chat.Component.literal("Controlled active recipe stock"); }
    }
}

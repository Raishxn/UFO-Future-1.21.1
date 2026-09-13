package com.raishxn.ufo.soak;

import appeng.blockentity.AEBaseBlockEntity;
import com.mojang.logging.LogUtils;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.StructureMembershipIndex;
import com.raishxn.ufo.block.InfinityFabricationSingularityControllerBlock;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.QuantumComputationNexusControllerBlock;
import com.raishxn.ufo.block.QuantumPatternFabricationMatrixControllerBlock;
import com.raishxn.ufo.block.entity.InfinityFabricationSingularityControllerBE;
import com.raishxn.ufo.block.entity.QuantumComputationNexusControllerBE;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import com.raishxn.ufo.block.entity.pattern.InfinityFabricationSingularityPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumComputationNexusPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumPatternFabricationMatrixPatternFactory;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Dedicated fleet workload and startup regression. Neither is part of the release JAR. */
@Mod(EndgameIdleSoakGameTests.MOD_ID)
@GameTestHolder(EndgameIdleSoakGameTests.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EndgameIdleSoakGameTests {
    static final String MOD_ID = "ufo_soak_tests";
    private static final String BATCH = "ufo_idle_soak";
    private static final String STARTUP_BATCH = "ufo_soak_startup";
    private static final String STRUCTURE = MOD_ID + ":event_driven_structure";
    private static final BlockPos FIRST_CONTROLLER = new BlockPos(20, 5, 20);
    private static final int MACHINE_SPACING = 48;
    private static final int MACHINE_COUNT = positiveInteger("ufo.soak.machines", 102);
    private static final int SOAK_TICKS = positiveInteger("ufo.soak.ticks", 10_000);
    private static final double MAX_AVERAGE_TICK_MILLIS = positiveDouble(
            "ufo.soak.maxAverageTickMillis", 50.0D);
    private static final int STARTUP_TIMEOUT_TICKS = 400;
    // A restored 20-tick structural poll must prevent startup from settling, not get absorbed into the baseline.
    private static final int QUIET_TICKS = 40;
    private static final int DELAYED_START_TICKS = 40;
    private static final int TIMEOUT_TICKS = Math.addExact(SOAK_TICKS, STARTUP_TIMEOUT_TICKS + 20);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<ChunkPos> FORCED_CHUNKS = new LinkedHashSet<>();
    private static final Set<ChunkPos> HELD_CHUNKS = new LinkedHashSet<>();

    @BeforeBatch(batch = BATCH)
    public static void beforeBatch(ServerLevel level) {
        resetBatch();
        LOGGER.info("Starting UFO idle soak: machines={}, ticks={}, maxAverageTickMs={}",
                MACHINE_COUNT, SOAK_TICKS, MAX_AVERAGE_TICK_MILLIS);
    }

    @BeforeBatch(batch = STARTUP_BATCH)
    public static void beforeStartupBatch(ServerLevel level) {
        resetBatch();
        LOGGER.info("Starting UFO delayed initialization regression: machines=3, heldTicks={}", DELAYED_START_TICKS);
    }

    private static void resetBatch() {
        MachinePerformanceRegistry.INSTANCE.reset();
        StructureMembershipIndex.INSTANCE.reset();
        FORCED_CHUNKS.clear();
        HELD_CHUNKS.clear();
    }

    @AfterBatch(batch = BATCH)
    public static void afterBatch(ServerLevel level) {
        releaseTickets(level);
    }

    @AfterBatch(batch = STARTUP_BATCH)
    public static void afterStartupBatch(ServerLevel level) {
        releaseTickets(level);
    }

    private static void releaseTickets(ServerLevel level) {
        FORCED_CHUNKS.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false));
        HELD_CHUNKS.forEach(chunk -> level.getChunkSource().removeRegionTicket(TicketType.START, chunk, 0, Unit.INSTANCE));
        LOGGER.info("Released {} forced chunks and {} FULL holds after UFO soak batch", FORCED_CHUNKS.size(), HELD_CHUNKS.size());
        FORCED_CHUNKS.clear();
        HELD_CHUNKS.clear();
    }

    @GameTestGenerator
    public static Collection<TestFunction> idleFleet() {
        return List.of(new TestFunction(BATCH, MOD_ID + ".idle_fleet", STRUCTURE, Rotation.NONE,
                TIMEOUT_TICKS, 0L, true, false, 1, 1, false,
                helper -> exerciseFleet(helper, MACHINE_COUNT, SOAK_TICKS, false)));
    }

    @GameTestGenerator
    public static Collection<TestFunction> delayedInitialization() {
        return List.of(new TestFunction(STARTUP_BATCH, MOD_ID + ".delayed_initialization", STRUCTURE, Rotation.NONE,
                STARTUP_TIMEOUT_TICKS + 220, 0L, true, false, 1, 1, false,
                helper -> exerciseFleet(helper, 3, 200, true)));
    }

    private static void exerciseFleet(GameTestHelper helper, int machines, int ticks, boolean delayLastMachine) {
        List<MachineProbe> fleet = buildFleet(helper, machines, delayLastMachine);
        var regression = delayLastMachine ? new StartupRegression(fleet.getLast()) : null;
        if (regression != null) {
            helper.runAfterDelay(10, () -> {
                var probe = regression.probe;
                helper.assertTrue(probe.controller.isAssembled(), "delayed fixture did not form before the legacy baseline");
                helper.assertTrue(probe.entity.getQueuedForReady() > probe.entity.getReadyInvoked(),
                        "delayed fixture must still have a real AE2 onReady pending at tick 10");
                helper.assertTrue(!chunksTicking(helper.getLevel(), probe.chunks),
                        "delayed fixture unexpectedly became tickable before release");
                regression.legacyScanCount = scanCount(helper, probe.controllerPos);
                LOGGER.info("Legacy 10-tick baseline captured before onReady: machine={}, scans={}, queued={}, ready={}",
                        probe.description, regression.legacyScanCount,
                        probe.entity.getQueuedForReady(), probe.entity.getReadyInvoked());
            });
            helper.runAfterDelay(DELAYED_START_TICKS, () -> {
                var level = helper.getLevel();
                for (ChunkPos chunk : regression.probe.chunks) {
                    level.getChunkSource().removeRegionTicket(TicketType.START, chunk, 0, Unit.INSTANCE);
                    HELD_CHUNKS.remove(chunk);
                    if (FORCED_CHUNKS.add(chunk)) level.setChunkForced(chunk.x, chunk.z, true);
                }
                LOGGER.info("Released delayed fixture to ENTITY_TICKING: machine={}, tick={}",
                        regression.probe.description, helper.getTick());
            });
        }
        awaitStartup(helper, fleet, ticks, regression, null, 0, STARTUP_TIMEOUT_TICKS);
    }

    private static void awaitStartup(GameTestHelper helper, List<MachineProbe> fleet, int ticks,
                                     StartupRegression regression, List<MachineBaseline> previous,
                                     int quietTicks, int remainingTicks) {
        helper.runAfterDelay(1, () -> {
            List<MachineBaseline> current = fleet.stream().map(probe -> new MachineBaseline(
                    probe, scanCount(helper, probe.controllerPos), probe.entity.getReadyInvoked())).toList();
            boolean unchanged = previous != null;
            if (previous != null) {
                for (int index = 0; index < current.size(); index++) {
                    var before = previous.get(index);
                    var after = current.get(index);
                    if (before.scanCount != after.scanCount) {
                        unchanged = false;
                        LOGGER.info("Startup scan changed: machine={}, tick={}, scans={}->{}, ready={}->{}, state={}",
                                after.probe.description, helper.getTick(), before.scanCount, after.scanCount,
                                before.readyInvoked, after.readyInvoked, readiness(helper.getLevel(), after.probe));
                    }
                }
            }
            MachineProbe pending = fleet.stream().filter(probe -> !isReady(helper.getLevel(), probe)).findFirst().orElse(null);
            int nextQuietTicks = pending == null && unchanged ? quietTicks + 1 : 0;
            if (nextQuietTicks >= QUIET_TICKS) {
                if (regression != null) {
                    long restoredScans = scanCount(helper, regression.probe.controllerPos);
                    helper.assertTrue(regression.legacyScanCount >= 0 && restoredScans > regression.legacyScanCount,
                            "real delayed onReady did not invalidate the legacy 10-tick scan baseline");
                    LOGGER.info("Delayed initialization regression passed: legacyScans={}, settledScans={}, ready={}",
                            regression.legacyScanCount, restoredScans, regression.probe.entity.getReadyInvoked());
                }
                LOGGER.info("UFO soak baseline ready: machines={}, tick={}, quietTicks={}, measuredTicks={}",
                        fleet.size(), helper.getTick(), nextQuietTicks, ticks);
                measureIdle(helper, current, ticks);
            } else if (remainingTicks <= 1) {
                helper.fail("fleet startup did not settle within " + STARTUP_TIMEOUT_TICKS + " ticks; quietTicks="
                        + nextQuietTicks + "; pending=" + (pending == null ? "none (scans keep changing)"
                        : pending.description + " " + readiness(helper.getLevel(), pending)));
            } else {
                if (previous == null || pending != null && remainingTicks % 20 == 0) {
                    LOGGER.info("Waiting for UFO soak startup: tick={}, quietTicks={}, pending={}", helper.getTick(),
                            nextQuietTicks, pending == null ? "none" : pending.description + " " + readiness(helper.getLevel(), pending));
                }
                awaitStartup(helper, fleet, ticks, regression, current, nextQuietTicks, remainingTicks - 1);
            }
        });
    }

    private static boolean chunksTicking(ServerLevel level, Set<ChunkPos> chunks) {
        return chunks.stream().allMatch(chunk -> level.shouldTickBlocksAt(chunk.toLong())
                && level.areEntitiesLoaded(chunk.toLong()));
    }

    private static boolean isReady(ServerLevel level, MachineProbe probe) {
        return !probe.entity.isRemoved() && probe.controller.isAssembled()
                && probe.entity.getReadyInvoked() > 0 && chunksTicking(level, probe.chunks)
                && probe.initEntities.stream().allMatch(entity -> !entity.isRemoved()
                && entity.getQueuedForReady() == entity.getReadyInvoked());
    }

    private static String readiness(ServerLevel level, MachineProbe probe) {
        long pendingEntities = probe.initEntities.stream().filter(entity -> entity.isRemoved()
                || entity.getQueuedForReady() != entity.getReadyInvoked()).count();
        return "formed=" + probe.controller.isAssembled() + ", removed=" + probe.entity.isRemoved()
                + ", queued=" + probe.entity.getQueuedForReady() + ", ready=" + probe.entity.getReadyInvoked()
                + ", chunksTicking=" + chunksTicking(level, probe.chunks) + ", pendingEntities=" + pendingEntities;
    }

    private static void measureIdle(GameTestHelper helper, List<MachineBaseline> baselines, int ticks) {
        long startedAt = System.nanoTime();
        helper.runAfterDelay(ticks, () -> {
            long elapsedNanos = System.nanoTime() - startedAt;
            for (MachineBaseline baseline : baselines) {
                helper.assertTrue(isReady(helper.getLevel(), baseline.probe),
                        baseline.probe.description + " stopped being ready while idle: "
                                + readiness(helper.getLevel(), baseline.probe));
                assertScanCount(helper, baseline.probe.controllerPos, baseline.scanCount,
                        baseline.probe.description + " rescanned while idle");
            }
            double averageTickMillis = elapsedNanos / 1_000_000.0D / ticks;
            helper.assertTrue(averageTickMillis <= MAX_AVERAGE_TICK_MILLIS,
                    "fleet exceeded average tick budget: " + averageTickMillis
                            + " ms > " + MAX_AVERAGE_TICK_MILLIS + " ms");
            LOGGER.info("UFO idle soak result: machines={}, ticks={}, elapsedMs={}, averageTickMs={}, forcedChunks={}",
                    baselines.size(), ticks, elapsedNanos / 1_000_000L,
                    averageTickMillis, FORCED_CHUNKS.size());
            helper.succeed();
        });
    }

    private static List<MachineProbe> buildFleet(GameTestHelper helper, int machines, boolean delayLastMachine) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(FIRST_CONTROLLER);
        // Separate batches share a world; put the regression away from the positive-Z fleet and template tickets.
        if (delayLastMachine) origin = origin.offset(0, 0, -1024);
        int columns = (int) Math.ceil(Math.sqrt(machines));
        List<MachineProbe> fleet = new ArrayList<>(machines);
        MachineVariant[] variants = MachineVariant.values();
        for (int index = 0; index < machines; index++) {
            MachineVariant variant = variants[index % variants.length];
            boolean held = delayLastMachine && index == machines - 1;
            BlockPos controllerPos = origin.offset(
                    (index % columns) * MACHINE_SPACING, 0,
                    (index / columns) * MACHINE_SPACING);
            if (held) controllerPos = origin.offset(512, 0, -512);
            Set<ChunkPos> chunks = forceFootprint(level, controllerPos, variant, held);
            level.setBlockAndUpdate(controllerPos, variant.controllerState());
            variant.definition().pattern().assembleAsCreative(
                    level, controllerPos, variant.patternFacing, variant.definition().defaultCreativeStates());
            BlockEntity controllerEntity = variant.controllerType.cast(level.getBlockEntity(controllerPos));
            helper.assertTrue(controllerEntity instanceof IMultiblockController,
                    variant.id + "[" + index + "] controller type mismatch");
            IMultiblockController controller = (IMultiblockController) controllerEntity;
            controller.scanStructure(level);
            var initEntities = new LinkedHashSet<AEBaseBlockEntity>();
            for (char symbol : variant.definition().pattern().getSymbols()) {
                for (BlockPos pos : variant.definition().pattern().getExpectedPositions(controllerPos, variant.patternFacing, symbol)) {
                    if (level.getBlockEntity(pos) instanceof AEBaseBlockEntity entity) initEntities.add(entity);
                }
            }
            fleet.add(new MachineProbe(controllerPos.immutable(), controller, (AEBaseBlockEntity) controllerEntity,
                    chunks, List.copyOf(initEntities), variant.id + "[" + index + "]"));
        }
        return List.copyOf(fleet);
    }

    private static Set<ChunkPos> forceFootprint(ServerLevel level, BlockPos controllerPos, MachineVariant variant, boolean held) {
        Set<ChunkPos> machineChunks = new LinkedHashSet<>();
        machineChunks.add(new ChunkPos(controllerPos));
        var pattern = variant.definition().pattern();
        for (char symbol : pattern.getSymbols()) {
            pattern.getExpectedPositions(controllerPos, variant.patternFacing, symbol).stream()
                    .map(ChunkPos::new).forEach(machineChunks::add);
        }
        for (ChunkPos chunk : machineChunks) {
            if (held) {
                HELD_CHUNKS.add(chunk);
                level.getChunkSource().addRegionTicket(TicketType.START, chunk, 0, Unit.INSTANCE);
                level.getChunk(chunk.x, chunk.z);
            } else if (FORCED_CHUNKS.add(chunk)) level.setChunkForced(chunk.x, chunk.z, true);
        }
        return Set.copyOf(machineChunks);
    }

    private static long scanCount(GameTestHelper helper, BlockPos controllerPos) {
        String dimension = helper.getLevel().dimension().location().toString();
        var snapshot = MachinePerformanceRegistry.INSTANCE.snapshot(dimension, controllerPos.asLong());
        helper.assertTrue(snapshot.isPresent(), "missing scan metrics for " + controllerPos.toShortString());
        return snapshot.orElseThrow().scanCount();
    }

    private static void assertScanCount(GameTestHelper helper, BlockPos pos, long expected, String message) {
        long actual = scanCount(helper, pos);
        helper.assertTrue(actual == expected, message + ": expected " + expected + ", got " + actual);
    }

    private static int positiveInteger(String property, int fallback) {
        int value = Integer.getInteger(property, fallback);
        if (value <= 0) throw new IllegalArgumentException(property + " must be positive");
        return value;
    }

    private static double positiveDouble(String property, double fallback) {
        double value;
        try {
            value = Double.parseDouble(System.getProperty(property, Double.toString(fallback)));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(property + " must be a number", exception);
        }
        if (!Double.isFinite(value) || value <= 0.0D) {
            throw new IllegalArgumentException(property + " must be positive and finite");
        }
        return value;
    }

    private enum MachineVariant {
        COMPUTATION_NEXUS("computation_nexus", Direction.NORTH, QuantumComputationNexusControllerBE.class) {
            @Override BlockState controllerState() {
                return MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get().defaultBlockState()
                        .setValue(QuantumComputationNexusControllerBlock.FACING, Direction.NORTH);
            }
            @Override MultiblockDefinition definition() {
                return QuantumComputationNexusPatternFactory.getDefinition();
            }
        },
        PATTERN_MATRIX("pattern_matrix", Direction.EAST, QuantumPatternFabricationMatrixControllerBE.class) {
            @Override BlockState controllerState() {
                return MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get().defaultBlockState()
                        .setValue(QuantumPatternFabricationMatrixControllerBlock.FACING, Direction.NORTH);
            }
            @Override MultiblockDefinition definition() {
                return QuantumPatternFabricationMatrixPatternFactory.getDefinition();
            }
        },
        FABRICATION_SINGULARITY("fabrication_singularity", Direction.EAST,
                InfinityFabricationSingularityControllerBE.class) {
            @Override BlockState controllerState() {
                return MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get().defaultBlockState()
                        .setValue(InfinityFabricationSingularityControllerBlock.FACING, Direction.NORTH);
            }
            @Override MultiblockDefinition definition() {
                return InfinityFabricationSingularityPatternFactory.getDefinition();
            }
        };

        private final String id;
        private final Direction patternFacing;
        private final Class<? extends BlockEntity> controllerType;

        MachineVariant(String id, Direction patternFacing, Class<? extends BlockEntity> controllerType) {
            this.id = id;
            this.patternFacing = patternFacing;
            this.controllerType = controllerType;
        }

        abstract BlockState controllerState();
        abstract MultiblockDefinition definition();
    }

    private record MachineProbe(BlockPos controllerPos, IMultiblockController controller, AEBaseBlockEntity entity,
                                Set<ChunkPos> chunks, List<AEBaseBlockEntity> initEntities, String description) { }

    private record MachineBaseline(MachineProbe probe, long scanCount, int readyInvoked) { }

    private static final class StartupRegression {
        private final MachineProbe probe;
        private long legacyScanCount = -1;

        private StartupRegression(MachineProbe probe) { this.probe = probe; }
    }
}

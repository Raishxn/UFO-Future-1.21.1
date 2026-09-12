package com.raishxn.ufo.soak;

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

/** Dedicated fleet workload. It is not part of the release JAR or per-push suite. */
@Mod(EndgameIdleSoakGameTests.MOD_ID)
@GameTestHolder(EndgameIdleSoakGameTests.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EndgameIdleSoakGameTests {
    static final String MOD_ID = "ufo_soak_tests";
    private static final String BATCH = "ufo_idle_soak";
    private static final String STRUCTURE = MOD_ID + ":event_driven_structure";
    private static final BlockPos FIRST_CONTROLLER = new BlockPos(20, 5, 20);
    private static final int MACHINE_SPACING = 48;
    private static final int MACHINE_COUNT = positiveInteger("ufo.soak.machines", 102);
    private static final int SOAK_TICKS = positiveInteger("ufo.soak.ticks", 10_000);
    private static final double MAX_AVERAGE_TICK_MILLIS = positiveDouble(
            "ufo.soak.maxAverageTickMillis", 50.0D);
    private static final int TIMEOUT_TICKS = Math.addExact(SOAK_TICKS, 500);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<ChunkPos> FORCED_CHUNKS = new LinkedHashSet<>();

    @BeforeBatch(batch = BATCH)
    public static void beforeBatch(ServerLevel level) {
        MachinePerformanceRegistry.INSTANCE.reset();
        StructureMembershipIndex.INSTANCE.reset();
        FORCED_CHUNKS.clear();
        LOGGER.info("Starting UFO idle soak: machines={}, ticks={}, maxAverageTickMs={}",
                MACHINE_COUNT, SOAK_TICKS, MAX_AVERAGE_TICK_MILLIS);
    }

    @AfterBatch(batch = BATCH)
    public static void afterBatch(ServerLevel level) {
        FORCED_CHUNKS.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false));
        LOGGER.info("Released {} forced chunks after UFO idle soak", FORCED_CHUNKS.size());
        FORCED_CHUNKS.clear();
    }

    @GameTestGenerator
    public static Collection<TestFunction> idleFleet() {
        return List.of(new TestFunction(BATCH, MOD_ID + ".idle_fleet", STRUCTURE, Rotation.NONE,
                TIMEOUT_TICKS, 0L, true, false, 1, 1, false,
                EndgameIdleSoakGameTests::exerciseFleet));
    }

    private static void exerciseFleet(GameTestHelper helper) {
        List<MachineProbe> fleet = buildFleet(helper);
        helper.runAfterDelay(10, () -> {
            List<MachineBaseline> baselines = fleet.stream().map(probe -> {
                helper.assertTrue(probe.controller.isAssembled(), probe.description + " did not form");
                return new MachineBaseline(probe, scanCount(helper, probe.controllerPos));
            }).toList();
            long startedAt = System.nanoTime();
            helper.runAfterDelay(SOAK_TICKS, () -> {
                long elapsedNanos = System.nanoTime() - startedAt;
                for (MachineBaseline baseline : baselines) {
                    helper.assertTrue(baseline.probe.controller.isAssembled(),
                            baseline.probe.description + " deformed while idle");
                    assertScanCount(helper, baseline.probe.controllerPos, baseline.scanCount,
                            baseline.probe.description + " rescanned while idle");
                }
                double averageTickMillis = elapsedNanos / 1_000_000.0D / SOAK_TICKS;
                helper.assertTrue(averageTickMillis <= MAX_AVERAGE_TICK_MILLIS,
                        "fleet exceeded average tick budget: " + averageTickMillis
                                + " ms > " + MAX_AVERAGE_TICK_MILLIS + " ms");
                LOGGER.info("UFO idle soak result: machines={}, ticks={}, elapsedMs={}, averageTickMs={}, forcedChunks={}",
                        MACHINE_COUNT, SOAK_TICKS, elapsedNanos / 1_000_000L,
                        averageTickMillis, FORCED_CHUNKS.size());
                helper.succeed();
            });
        });
    }

    private static List<MachineProbe> buildFleet(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(FIRST_CONTROLLER);
        int columns = (int) Math.ceil(Math.sqrt(MACHINE_COUNT));
        List<MachineProbe> fleet = new ArrayList<>(MACHINE_COUNT);
        MachineVariant[] variants = MachineVariant.values();
        for (int index = 0; index < MACHINE_COUNT; index++) {
            MachineVariant variant = variants[index % variants.length];
            BlockPos controllerPos = origin.offset(
                    (index % columns) * MACHINE_SPACING, 0,
                    (index / columns) * MACHINE_SPACING);
            forceFootprint(level, controllerPos, variant);
            level.setBlockAndUpdate(controllerPos, variant.controllerState());
            variant.definition().pattern().assembleAsCreative(
                    level, controllerPos, variant.patternFacing, variant.definition().defaultCreativeStates());
            BlockEntity controllerEntity = variant.controllerType.cast(level.getBlockEntity(controllerPos));
            helper.assertTrue(controllerEntity instanceof IMultiblockController,
                    variant.id + "[" + index + "] controller type mismatch");
            IMultiblockController controller = (IMultiblockController) controllerEntity;
            controller.scanStructure(level);
            fleet.add(new MachineProbe(controllerPos.immutable(), controller, variant.id + "[" + index + "]"));
        }
        return List.copyOf(fleet);
    }

    private static void forceFootprint(ServerLevel level, BlockPos controllerPos, MachineVariant variant) {
        Set<ChunkPos> machineChunks = new LinkedHashSet<>();
        machineChunks.add(new ChunkPos(controllerPos));
        var pattern = variant.definition().pattern();
        for (char symbol : pattern.getSymbols()) {
            pattern.getExpectedPositions(controllerPos, variant.patternFacing, symbol).stream()
                    .map(ChunkPos::new).forEach(machineChunks::add);
        }
        for (ChunkPos chunk : machineChunks) {
            if (FORCED_CHUNKS.add(chunk)) level.setChunkForced(chunk.x, chunk.z, true);
        }
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

    private record MachineProbe(
            BlockPos controllerPos, IMultiblockController controller, String description) { }

    private record MachineBaseline(MachineProbe probe, long scanCount) { }
}

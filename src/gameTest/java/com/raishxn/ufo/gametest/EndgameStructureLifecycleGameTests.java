package com.raishxn.ufo.gametest;

import appeng.core.definitions.AEBlocks;
import appeng.api.networking.GridHelper;
import appeng.blockentity.grid.AENetworkedBlockEntity;
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
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import com.raishxn.ufo.block.entity.pattern.InfinityFabricationSingularityPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumComputationNexusPatternFactory;
import com.raishxn.ufo.block.entity.pattern.QuantumPatternFabricationMatrixPatternFactory;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Real structures proving that topology scans only run after lifecycle invalidation. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class EndgameStructureLifecycleGameTests {
    private static final BlockPos CONTROLLER = new BlockPos(20, 5, 20);
    private static final int IDLE_TICKS = 80;
    private static final int UNLOAD_WAIT_TICKS = 600;
    private static final Logger LOGGER = LogUtils.getLogger();

    @GameTest(template = "event_driven_structure", timeoutTicks = 300)
    public static void computationNexusIsEventDriven(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumComputationNexusControllerBlock.FACING, Direction.NORTH);
        exercise(helper, state, QuantumComputationNexusPatternFactory.getDefinition(), Direction.NORTH,
                QuantumComputationNexusControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 300)
    public static void patternMatrixIsEventDriven(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumPatternFabricationMatrixControllerBlock.FACING, Direction.NORTH);
        exercise(helper, state, QuantumPatternFabricationMatrixPatternFactory.getDefinition(), Direction.EAST,
                QuantumPatternFabricationMatrixControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 300)
    public static void fabricationSingularityIsEventDriven(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get()
                .defaultBlockState().setValue(InfinityFabricationSingularityControllerBlock.FACING, Direction.NORTH);
        exercise(helper, state, InfinityFabricationSingularityPatternFactory.getDefinition(), Direction.EAST,
                InfinityFabricationSingularityControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 1500)
    public static void computationNexusSurvivesPhysicalChunkReload(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumComputationNexusControllerBlock.FACING, Direction.NORTH);
        exerciseChunkReload(helper, new BlockPos(532, 5, 20), state,
                QuantumComputationNexusPatternFactory.getDefinition(), Direction.NORTH,
                QuantumComputationNexusControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 1500)
    public static void patternMatrixSurvivesPhysicalChunkReload(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumPatternFabricationMatrixControllerBlock.FACING, Direction.NORTH);
        exerciseChunkReload(helper, new BlockPos(1044, 5, 20), state,
                QuantumPatternFabricationMatrixPatternFactory.getDefinition(), Direction.EAST,
                QuantumPatternFabricationMatrixControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 1500)
    public static void fabricationSingularitySurvivesPhysicalChunkReload(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get()
                .defaultBlockState().setValue(InfinityFabricationSingularityControllerBlock.FACING, Direction.NORTH);
        exerciseChunkReload(helper, new BlockPos(1556, 5, 20), state,
                InfinityFabricationSingularityPatternFactory.getDefinition(), Direction.EAST,
                InfinityFabricationSingularityControllerBE.class);
    }

    private static <T extends BlockEntity & IMultiblockController> void exercise(
            GameTestHelper helper, BlockState controllerState, MultiblockDefinition definition,
            Direction patternFacing, Class<T> controllerType) {
        BlockPos controllerPos = helper.absolutePos(CONTROLLER);
        helper.getLevel().setBlockAndUpdate(controllerPos, controllerState);
        definition.pattern().assembleAsCreative(
                helper.getLevel(), controllerPos, patternFacing, definition.defaultCreativeStates());
        T controller = controllerType.cast(helper.getLevel().getBlockEntity(controllerPos));
        controller.scanStructure(helper.getLevel());

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(controller.isAssembled(), controllerType.getSimpleName() + " did not form");
            long initialScans = scanCount(helper, controllerPos);
            helper.runAfterDelay(IDLE_TICKS, () -> {
                assertScanCount(helper, controllerPos, initialScans,
                        controllerType.getSimpleName() + " rescanned while idle");
                exerciseStructureChange(helper, controller, definition, patternFacing, controllerPos,
                        controllerType.getSimpleName(), initialScans);
            });
        });
    }

    private static <T extends BlockEntity & IMultiblockController> void exerciseChunkReload(
            GameTestHelper helper, BlockPos relativeControllerPos, BlockState controllerState,
            MultiblockDefinition definition, Direction patternFacing, Class<T> controllerType) {
        ServerLevel level = helper.getLevel();
        // Outside the template tickets, with separate distant footprints for concurrent tests.
        BlockPos controllerPos = helper.absolutePos(relativeControllerPos);
        Set<ChunkPos> footprintChunks = footprintChunks(controllerPos, definition, patternFacing);
        setChunksForced(level, footprintChunks, true);
        level.setBlockAndUpdate(controllerPos, controllerState);
        definition.pattern().assembleAsCreative(level, controllerPos, patternFacing, definition.defaultCreativeStates());
        T originalController = controllerType.cast(level.getBlockEntity(controllerPos));
        originalController.scanStructure(level);

        helper.runAfterDelay(10, () -> {
            String machineName = controllerType.getSimpleName();
            helper.assertTrue(originalController.isAssembled(), machineName + " did not form before chunk unload");
            long scansBeforeUnload = scanCount(helper, controllerPos);
            releaseAndAwaitPhysicalUnload(helper, footprintChunks, controllerPos, originalController,
                    UNLOAD_WAIT_TICKS, () -> reloadAndVerify(helper, footprintChunks, controllerPos,
                            originalController, controllerType, definition, patternFacing, scansBeforeUnload));
        });
    }

    private static <T extends BlockEntity & IMultiblockController> void reloadAndVerify(
            GameTestHelper helper, Set<ChunkPos> footprintChunks, BlockPos controllerPos,
            T originalController, Class<T> controllerType, MultiblockDefinition definition,
            Direction patternFacing, long scansBeforeUnload) {
        ServerLevel level = helper.getLevel();
        long controllerChunk = new ChunkPos(controllerPos).toLong();

        setChunksForced(level, footprintChunks, true);
        footprintChunks.forEach(chunk -> level.getChunk(chunk.x, chunk.z));
        // FULL chunks may precede entity loading and ticker activation, especially in accelerated GameTests.
        awaitCondition(helper, UNLOAD_WAIT_TICKS, () -> {
            var chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(controllerChunk), ChunkPos.getZ(controllerChunk));
            return chunk != null && footprintChunks.stream().allMatch(pos ->
                    level.shouldTickBlocksAt(pos.toLong()) && level.areEntitiesLoaded(pos.toLong()))
                    && chunk.getBlockEntity(controllerPos) instanceof IMultiblockController controller
                    && controller.isAssembled();
        }, controllerType.getSimpleName() + " did not form with ticking chunks after reload", () ->
                helper.runAfterDelay(20, () -> verifyReloadedController(helper, footprintChunks,
                        controllerPos, originalController, controllerType, definition, patternFacing, scansBeforeUnload)));
    }

    private static <T extends BlockEntity & IMultiblockController> void verifyReloadedController(
            GameTestHelper helper, Set<ChunkPos> footprintChunks, BlockPos controllerPos,
            T originalController, Class<T> controllerType, MultiblockDefinition definition,
            Direction patternFacing, long scansBeforeUnload) {
        ServerLevel level = helper.getLevel();
        String machineName = controllerType.getSimpleName();
        T reloadedController = controllerType.cast(level.getBlockEntity(controllerPos));
        helper.assertTrue(reloadedController != originalController, machineName + " reused the removed BE instance");
        helper.assertTrue(reloadedController.isAssembled(), machineName + " did not remain formed after reload");
        helper.assertTrue(definition.pattern().match(level, controllerPos, patternFacing).isValid(),
                machineName + " structure blocks did not survive physical reload");
        helper.assertTrue(StructureMembershipIndex.INSTANCE.controllersInChunk(
                        level.dimension().location().toString(), new ChunkPos(controllerPos).toLong())
                        .contains(controllerPos.asLong()),
                machineName + " did not rebuild the structure index after reload");
        long scansAfterReload = scanCount(helper, controllerPos);
        helper.assertTrue(scansAfterReload > scansBeforeUnload, machineName + " did not scan after physical reload");
        helper.runAfterDelay(IDLE_TICKS, () -> {
            assertScanCount(helper, controllerPos, scansAfterReload, machineName + " resumed polling after physical reload");
            LOGGER.info("Physical chunk reload passed: machine={}, footprintChunks={}, scansBefore={}, scansAfter={}, idleTicks={}",
                    machineName, footprintChunks.size(), scansBeforeUnload, scansAfterReload, IDLE_TICKS);
            setChunksForced(level, footprintChunks, false);
            helper.succeed();
        });
    }

    static void awaitCondition(GameTestHelper helper, int ticksRemaining,
            BooleanSupplier condition, String failureMessage, Runnable continuation) {
        helper.runAfterDelay(1, () -> {
            if (condition.getAsBoolean()) {
                continuation.run();
            } else if (ticksRemaining <= 1) {
                helper.fail(failureMessage);
            } else {
                awaitCondition(helper, ticksRemaining - 1, condition, failureMessage, continuation);
            }
        });
    }

    static Set<ChunkPos> footprintChunks(
            BlockPos controllerPos, MultiblockDefinition definition, Direction patternFacing) {
        Set<ChunkPos> chunks = new LinkedHashSet<>();
        chunks.add(new ChunkPos(controllerPos));
        var pattern = definition.pattern();
        for (char symbol : pattern.getSymbols()) {
            pattern.getExpectedPositions(controllerPos, patternFacing, symbol).stream()
                    .map(ChunkPos::new).forEach(chunks::add);
        }
        return Set.copyOf(chunks);
    }

    static void setChunksForced(ServerLevel level, Set<ChunkPos> chunks, boolean forced) {
        chunks.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, forced));
    }

    @AfterBatch(batch = "defaultBatch")
    public static void cleanUpUnloadObservers(ServerLevel level) {
        ChunkUnloadObservation.closeAll();
        LoadedBlockEntityLookupGameTests.releaseFixtures(level);
    }

    static void releaseAndAwaitPhysicalUnload(
            GameTestHelper helper, Set<ChunkPos> chunks, BlockPos controllerPos,
            BlockEntity originalController, int ticksRemaining, Runnable continuation) {
        // Capture every instantiated BE and subscribe BEFORE dropping tickets.
        ChunkUnloadObservation observation = new ChunkUnloadObservation(helper, chunks);
        setChunksForced(helper.getLevel(), chunks, false);
        awaitPhysicalUnload(helper, chunks, controllerPos, originalController,
                observation, ticksRemaining, continuation);
    }

    private static void awaitPhysicalUnload(
            GameTestHelper helper, Set<ChunkPos> chunks, BlockPos controllerPos,
            BlockEntity originalController, ChunkUnloadObservation observation,
            int ticksRemaining, Runnable continuation) {
        helper.runAfterDelay(1, () -> {
            ServerLevel level = helper.getLevel();
            boolean holdersInvisible = chunks.stream().allMatch(chunk ->
                    level.getChunkSource().chunkMap.getVisibleChunkIfPresent(chunk.toLong()) == null);
            boolean controllerUnindexed = !StructureMembershipIndex.INSTANCE.controllersInChunk(
                    level.dimension().location().toString(), new ChunkPos(controllerPos).toLong())
                    .contains(controllerPos.asLong());
            // Unload is posted before ServerLevel.unload removes BEs: require both.
            if (holdersInvisible && observation.allUnloadedAndRemoved()
                    && originalController.isRemoved() && controllerUnindexed) {
                LOGGER.info("Physical chunk unload observed: machine={}, footprintChunks={}, {}, tick={}",
                        originalController.getClass().getSimpleName(), chunks.size(),
                        observation.diagnostic(), helper.getTick());
                observation.close();
                continuation.run();
            } else if (ticksRemaining <= 1) {
                observation.close();
                helper.fail("physical structure unload did not finish"
                        + " (holdersInvisible=" + holdersInvisible
                        + ", " + observation.diagnostic()
                        + ", controllerRemoved=" + originalController.isRemoved()
                        + ", controllerUnindexed=" + controllerUnindexed + ")");
            } else {
                awaitPhysicalUnload(helper, chunks, controllerPos, originalController,
                        observation, ticksRemaining - 1, continuation);
            }
        });
    }

    private static void exerciseStructureChange(
            GameTestHelper helper, IMultiblockController controller, MultiblockDefinition definition,
            Direction patternFacing, BlockPos controllerPos, String machineName, long initialScans) {
        BlockPos casingPos = definition.pattern().getExpectedPositions(controllerPos, patternFacing, 'C').getFirst();
        BlockState casingState = definition.defaultCreativeStates().get('C');
        helper.getLevel().destroyBlock(casingPos, false);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(!controller.isAssembled(), machineName + " ignored a broken casing");
            assertScanCount(helper, controllerPos, initialScans + 1,
                    machineName + " did not debounce the break to one scan");
            helper.getLevel().setBlockAndUpdate(casingPos, casingState);
            helper.runAfterDelay(5, () -> {
                helper.assertTrue(controller.isAssembled(), machineName + " did not reform after casing restoration");
                assertScanCount(helper, controllerPos, initialScans + 2,
                        machineName + " did not debounce restoration to one scan");
                exerciseNetworkReconnect(helper, controller, definition, patternFacing, controllerPos,
                        machineName, initialScans + 2);
            });
        });
    }

    private static void exerciseNetworkReconnect(
            GameTestHelper helper, IMultiblockController controller, MultiblockDefinition definition,
            Direction patternFacing, BlockPos controllerPos, String machineName, long structuralScans) {
        BlockPos linkPos = definition.pattern().getExpectedPositions(controllerPos, patternFacing, 'L').getFirst();
        QuantumGridLinkBE link = (QuantumGridLinkBE) helper.getLevel().getBlockEntity(linkPos);
        BlockPos powerPos = helper.absolutePos(new BlockPos(38, 1, 38));
        helper.assertTrue(helper.getLevel().getBlockState(powerPos).isAir(),
                machineName + " network fixture position is obstructed by the structure");
        helper.getLevel().setBlockAndUpdate(powerPos, AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
        helper.runAfterDelay(2, () -> {
            var power = (AENetworkedBlockEntity) helper.getLevel().getBlockEntity(powerPos);
            var connection = GridHelper.createConnection(link.getMainNode().getNode(), power.getMainNode().getNode());
            helper.runAfterDelay(25, () -> {
                helper.assertTrue(link.isNetworkReady(), machineName + " did not connect to the ME network");
                assertScanCount(helper, controllerPos, structuralScans,
                        machineName + " used a structural scan for network connection");
                connection.destroy();
                helper.runAfterDelay(10, () -> {
                    helper.assertTrue(!link.isNetworkReady(), machineName + " stayed connected after network removal");
                    assertScanCount(helper, controllerPos, structuralScans,
                            machineName + " used a structural scan for network loss");
                    GridHelper.createConnection(link.getMainNode().getNode(), power.getMainNode().getNode());
                    helper.runAfterDelay(25, () -> {
                        helper.assertTrue(link.isNetworkReady(), machineName + " did not reconnect to the ME network");
                        assertScanCount(helper, controllerPos, structuralScans,
                                machineName + " used a structural scan for network reconnection");
                        helper.runAfterDelay(IDLE_TICKS, () -> {
                            assertScanCount(helper, controllerPos, structuralScans,
                                    machineName + " resumed polling after network reconnection");
                            helper.succeed();
                        });
                    });
                });
            });
        });
    }

    static long scanCount(GameTestHelper helper, BlockPos controllerPos) {
        String dimension = helper.getLevel().dimension().location().toString();
        var snapshot = MachinePerformanceRegistry.INSTANCE.snapshot(dimension, controllerPos.asLong());
        helper.assertTrue(snapshot.isPresent(), "missing structure scan metrics for controller");
        return snapshot.orElseThrow().scanCount();
    }

    static void assertScanCount(GameTestHelper helper, BlockPos controllerPos, long expected, String message) {
        long actual = scanCount(helper, controllerPos);
        helper.assertTrue(actual == expected, message + ": expected " + expected + ", got " + actual);
    }
}

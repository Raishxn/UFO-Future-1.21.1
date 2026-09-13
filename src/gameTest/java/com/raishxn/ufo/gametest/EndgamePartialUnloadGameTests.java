package com.raishxn.ufo.gametest;

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
import com.raishxn.ufo.util.LoadedBlockEntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Partial chunk lifecycle for the endgame trio. Vanilla releases a chunk holder only
 * when its ticket level exceeds MAX_LEVEL (41); the band 34..41 is beyond the FULL
 * window, and every trio footprint sits inside ring one of its controller chunk.
 * Keeping the controller chunk loaded with any region ticket therefore also keeps
 * the members above the unload threshold: the reachable "controller loaded, members
 * gone" state is a demotion (chunks leave the FULL window, holders survive, no
 * unload events fire), not a physical unload. These tests observe exactly that state
 * and prove it neither deforms nor polls the machines. Physical unload coverage lives
 * in the two QCN ticket-request recovery tests, where every fixture chunk is
 * physically unloaded before reload. These vary which tickets are requested;
 * propagation does not guarantee the effective Load/onReady order.
 * The build positions are fixed
 * absolute world coordinates aligned to chunk borders, far from every other distant
 * fixture and 512 blocks apart from each other.
 */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class EndgamePartialUnloadGameTests {
    private static final int IDLE_TICKS = 80;
    private static final int UNLOAD_WAIT_TICKS = 600;
    private static final int SETTLE_TICKS = 40;
    /** ENTITY_TICKING ticket level (33 - 2); the only status whose entities load, so it is
     * the first level at which the controller's block entity ticker actually runs. */
    private static final int RESUME_TICK_DISTANCE = 2;
    private static final Logger LOGGER = LogUtils.getLogger();

    @GameTest(template = "event_driven_structure", timeoutTicks = 2000)
    public static void computationNexusToleratesMemberChunkDemotion(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumComputationNexusControllerBlock.FACING, Direction.NORTH);
        exerciseDemotion(helper, new BlockPos(4112, 5, 16), state,
                QuantumComputationNexusPatternFactory.getDefinition(), Direction.NORTH,
                QuantumComputationNexusControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 2000)
    public static void patternMatrixToleratesMemberChunkDemotion(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumPatternFabricationMatrixControllerBlock.FACING, Direction.NORTH);
        exerciseDemotion(helper, new BlockPos(4624, 5, 16), state,
                QuantumPatternFabricationMatrixPatternFactory.getDefinition(), Direction.EAST,
                QuantumPatternFabricationMatrixControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 2000)
    public static void fabricationSingularityToleratesMemberChunkDemotion(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get()
                .defaultBlockState().setValue(InfinityFabricationSingularityControllerBlock.FACING, Direction.NORTH);
        exerciseDemotion(helper, new BlockPos(5136, 5, 16), state,
                InfinityFabricationSingularityPatternFactory.getDefinition(), Direction.EAST,
                InfinityFabricationSingularityControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 2400)
    public static void computationNexusRecoversAfterControllerTicketRequest(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumComputationNexusControllerBlock.FACING, Direction.NORTH);
        exerciseControllerTicketRequest(helper, new BlockPos(5648, 5, 16), state,
                QuantumComputationNexusPatternFactory.getDefinition(), Direction.NORTH,
                QuantumComputationNexusControllerBE.class);
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 2400)
    public static void computationNexusRecoversAfterMemberTicketRequests(GameTestHelper helper) {
        BlockState state = MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get()
                .defaultBlockState().setValue(QuantumComputationNexusControllerBlock.FACING, Direction.NORTH);
        exerciseMemberTicketRequests(helper, new BlockPos(6160, 5, 16), state,
                QuantumComputationNexusPatternFactory.getDefinition(), Direction.NORTH,
                QuantumComputationNexusControllerBE.class);
    }

    /**
     * A FULL-level hold keeps the controller chunk visible while every member chunk
     * drops out of the FULL window into the unload grace band. The machines must not
     * scan (their controller cannot tick), must not deform, and must resume seamlessly
     * when the members re-enter the FULL window.
     */
    private static <T extends BlockEntity & IMultiblockController> void exerciseDemotion(
            GameTestHelper helper, BlockPos controllerPos, BlockState controllerState,
            MultiblockDefinition definition, Direction patternFacing, Class<T> controllerType) {
        ServerLevel level = helper.getLevel();
        Set<ChunkPos> footprintChunks = EndgameStructureLifecycleGameTests.footprintChunks(
                controllerPos, definition, patternFacing);
        Set<ChunkPos> memberChunks = memberChunks(footprintChunks, controllerPos);
        T controller = buildAndForm(helper, controllerPos, controllerState, definition, patternFacing,
                controllerType, memberChunks, footprintChunks);

        helper.runAfterDelay(10, () -> {
            String machineName = controllerType.getSimpleName();
            helper.assertTrue(controller.isAssembled(), machineName + " did not form before member demotion");
            // FULL-level hold on the controller chunk; dropping the FORCED tickets demotes
            // the ring-one members out of the FULL window without releasing their holders.
            level.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(controllerPos), 0, Unit.INSTANCE);
            ChunkUnloadObservation observation = new ChunkUnloadObservation(helper, footprintChunks);
            EndgameStructureLifecycleGameTests.setChunksForced(level, footprintChunks, false);
            awaitMemberDemotion(helper, memberChunks, controllerPos, UNLOAD_WAIT_TICKS, () -> {
                long scansWhileDemoted = EndgameStructureLifecycleGameTests.scanCount(helper, controllerPos);
                helper.runAfterDelay(IDLE_TICKS, () -> {
                    helper.assertTrue(!controller.isRemoved(),
                            machineName + " removed its BE while the controller chunk stayed loaded");
                    helper.assertTrue(controller.isAssembled(),
                            machineName + " deformed while its member chunks were demoted");
                    helper.assertTrue(StructureMembershipIndex.INSTANCE.controllersInChunk(
                                    level.dimension().location().toString(), new ChunkPos(controllerPos).toLong())
                            .contains(controllerPos.asLong()),
                            machineName + " dropped its structure index entry during member demotion");
                    EndgameStructureLifecycleGameTests.assertScanCount(helper, controllerPos, scansWhileDemoted,
                            machineName + " scanned while its controller chunk could not tick");
                    assertMembersDemoted(helper, memberChunks,
                            machineName + " member chunks left the unload grace band");
                    helper.assertTrue(observation.noUnloadsAndAllRetained(),
                            machineName + " emitted unload events or removed BEs during demotion: " + observation.diagnostic());
                    observation.close();
                    LOGGER.info("Member chunk demotion tolerated: machine={}, memberChunks={}, scansWhileDemoted={}, idleTicks={}",
                            machineName, memberChunks.size(), scansWhileDemoted, IDLE_TICKS);
                    // Bring the members back into the FULL window and resume ticking; the
                    // controller must continue from its retained state without deforming.
                    reloadMembers(level, memberChunks);
                    resumeTicking(level, controllerPos);
                    EndgameStructureLifecycleGameTests.awaitCondition(helper, UNLOAD_WAIT_TICKS,
                            () -> allChunksTicking(level, memberChunks)
                                    && isChunkMaterialized(level, new ChunkPos(controllerPos))
                                    && definition.pattern().match(level, controllerPos, patternFacing).isValid()
                                    && controllerType.cast(LoadedBlockEntityLookup.get(level, controllerPos)).isAssembled(),
                            machineName + " did not resume after its member chunks re-entered the FULL window",
                            () -> helper.runAfterDelay(SETTLE_TICKS, () -> {
                                helper.assertTrue(!controller.isRemoved(), machineName + " lost its BE across recovery");
                                helper.assertTrue(controller.isAssembled(),
                                        machineName + " did not stay formed across recovery");
                                helper.assertTrue(definition.pattern().match(level, controllerPos, patternFacing).isValid(),
                                        machineName + " structure blocks did not survive the demotion cycle");
                                long scansAfterRecovery = EndgameStructureLifecycleGameTests.scanCount(helper, controllerPos);
                                helper.runAfterDelay(IDLE_TICKS, () -> {
                                    EndgameStructureLifecycleGameTests.assertScanCount(helper, controllerPos,
                                            scansAfterRecovery, machineName + " resumed polling after recovery");
                                    LOGGER.info("Member chunk demotion recovery passed: machine={}, memberChunks={}, scansAfterRecovery={}, idleTicks={}",
                                            machineName, memberChunks.size(), scansAfterRecovery, IDLE_TICKS);
                                    level.getChunkSource().removeRegionTicket(TicketType.START, new ChunkPos(controllerPos),
                                            RESUME_TICK_DISTANCE, Unit.INSTANCE);
                                    EndgameStructureLifecycleGameTests.setChunksForced(level, footprintChunks, false);
                                    helper.succeed();
                                });
                            }));
                });
            });
        });
    }

    /** Physical unload followed by a ticket request for the controller chunk.
     * Ticket propagation may also load members; no effective Load/ready order is asserted. */
    private static <T extends BlockEntity & IMultiblockController> void exerciseControllerTicketRequest(
            GameTestHelper helper, BlockPos controllerPos, BlockState controllerState,
            MultiblockDefinition definition, Direction patternFacing, Class<T> controllerType) {
        ServerLevel level = helper.getLevel();
        Set<ChunkPos> footprintChunks = EndgameStructureLifecycleGameTests.footprintChunks(
                controllerPos, definition, patternFacing);
        Set<ChunkPos> memberChunks = memberChunks(footprintChunks, controllerPos);
        T controller = buildAndForm(helper, controllerPos, controllerState, definition, patternFacing,
                controllerType, memberChunks, footprintChunks);

        helper.runAfterDelay(10, () -> {
            String machineName = controllerType.getSimpleName();
            helper.assertTrue(controller.isAssembled(), machineName + " did not form before chunk unload");
            EndgameStructureLifecycleGameTests.releaseAndAwaitPhysicalUnload(helper, footprintChunks, controllerPos,
                    controller, UNLOAD_WAIT_TICKS, () -> {
                        ChunkPos controllerChunk = new ChunkPos(controllerPos);
                        EndgameStructureLifecycleGameTests.setChunksForced(level, Set.of(controllerChunk), true);
                        level.getChunk(controllerChunk.x, controllerChunk.z);
                        EndgameStructureLifecycleGameTests.awaitCondition(helper, UNLOAD_WAIT_TICKS,
                                () -> isChunkMaterialized(level, controllerChunk)
                                        && LoadedBlockEntityLookup.get(level, controllerPos)
                                                instanceof IMultiblockController reloaded
                                        && reloaded.isAssembled()
                                        && memberChunks.stream().allMatch(chunk -> isChunkMaterialized(level, chunk)),
                                machineName + " did not reform after the controller ticket request",
                                () -> helper.runAfterDelay(SETTLE_TICKS, () -> {
                                    T reloadedController = controllerType.cast(
                                            LoadedBlockEntityLookup.get(level, controllerPos));
                                    helper.assertTrue(reloadedController != controller,
                                            machineName + " reused the removed BE instance");
                                    verifyRecovered(helper, memberChunks, controllerPos, controllerType,
                                            definition, patternFacing, reloadedController, footprintChunks);
                                }));
                    });
        });
    }

    /** Physical unload followed by ticket requests for member chunks.
     * Ticket propagation may also load the controller; no effective Load/ready order is asserted. */
    private static <T extends BlockEntity & IMultiblockController> void exerciseMemberTicketRequests(
            GameTestHelper helper, BlockPos controllerPos, BlockState controllerState,
            MultiblockDefinition definition, Direction patternFacing, Class<T> controllerType) {
        ServerLevel level = helper.getLevel();
        Set<ChunkPos> footprintChunks = EndgameStructureLifecycleGameTests.footprintChunks(
                controllerPos, definition, patternFacing);
        Set<ChunkPos> memberChunks = memberChunks(footprintChunks, controllerPos);
        T controller = buildAndForm(helper, controllerPos, controllerState, definition, patternFacing,
                controllerType, memberChunks, footprintChunks);

        helper.runAfterDelay(10, () -> {
            String machineName = controllerType.getSimpleName();
            helper.assertTrue(controller.isAssembled(), machineName + " did not form before chunk unload");
            long scansBeforeUnload = EndgameStructureLifecycleGameTests.scanCount(helper, controllerPos);
            EndgameStructureLifecycleGameTests.releaseAndAwaitPhysicalUnload(helper, footprintChunks, controllerPos,
                    controller, UNLOAD_WAIT_TICKS, () -> {
                        EndgameStructureLifecycleGameTests.setChunksForced(level, memberChunks, true);
                        memberChunks.forEach(chunk -> level.getChunk(chunk.x, chunk.z));
                        EndgameStructureLifecycleGameTests.awaitCondition(helper, UNLOAD_WAIT_TICKS,
                                () -> allChunksTicking(level, memberChunks)
                                        && LoadedBlockEntityLookup.get(level, controllerPos)
                                                instanceof IMultiblockController reloaded
                                        && reloaded.isAssembled()
                                        && EndgameStructureLifecycleGameTests.scanCount(helper, controllerPos)
                                                > scansBeforeUnload,
                                machineName + " did not recover after member ticket requests",
                                () -> helper.runAfterDelay(SETTLE_TICKS, () -> {
                                    T reloadedController = controllerType.cast(
                                            LoadedBlockEntityLookup.get(level, controllerPos));
                                    helper.assertTrue(reloadedController != controller,
                                            machineName + " reused the removed BE instance");
                                    verifyRecovered(helper, memberChunks, controllerPos, controllerType,
                                            definition, patternFacing, reloadedController, footprintChunks);
                                }));
                    });
        });
    }

    private static <T extends BlockEntity & IMultiblockController> T buildAndForm(
            GameTestHelper helper, BlockPos controllerPos, BlockState controllerState,
            MultiblockDefinition definition, Direction patternFacing, Class<T> controllerType,
            Set<ChunkPos> memberChunks, Set<ChunkPos> chunksToForce) {
        helper.assertTrue(!memberChunks.isEmpty(),
                controllerType.getSimpleName() + " fixture must span a chunk besides the controller");
        ServerLevel level = helper.getLevel();
        EndgameStructureLifecycleGameTests.setChunksForced(level, chunksToForce, true);
        level.setBlockAndUpdate(controllerPos, controllerState);
        definition.pattern().assembleAsCreative(level, controllerPos, patternFacing, definition.defaultCreativeStates());
        T controller = controllerType.cast(level.getBlockEntity(controllerPos));
        controller.scanStructure(level);
        var diagnostic = definition.pattern().match(level, controllerPos, patternFacing);
        LOGGER.info("Partial unload fixture built: machine={}, controllerPos={}, patternFacing={}, matchValid={}, footprintChunks={}",
                controllerType.getSimpleName(), controllerPos, patternFacing, diagnostic.isValid(), chunksToForce.size());
        return controller;
    }

    /**
     * Demotion observation: every member chunk left the FULL window (the scanners see
     * it as absent) while its holder remains visible inside the unload grace band.
     * This gate alone does not observe unload events or BE removal.
     * The controller chunk must stay
     * materialized with its BE intact.
     */
    private static void awaitMemberDemotion(GameTestHelper helper, Set<ChunkPos> memberChunks,
            BlockPos controllerPos, int ticksRemaining, Runnable continuation) {
        helper.runAfterDelay(1, () -> {
            ServerLevel level = helper.getLevel();
            boolean membersDemoted = memberChunks.stream().allMatch(chunk ->
                    !isChunkLoadedForScanners(level, chunk) && !isHolderInvisible(level, chunk));
            boolean controllerVisible = isChunkLoadedForScanners(level, new ChunkPos(controllerPos));
            boolean controllerIntact = LoadedBlockEntityLookup.get(level, controllerPos) instanceof BlockEntity retained
                    && !retained.isRemoved();
            if (membersDemoted && controllerVisible && controllerIntact) {
                LOGGER.info("Member chunk demotion observed: controller={}, memberChunks={}, tick={}",
                        controllerPos, memberChunks.size(), helper.getTick());
                continuation.run();
            } else if (ticksRemaining <= 1) {
                helper.fail("member chunk demotion did not settle within " + UNLOAD_WAIT_TICKS + " ticks"
                        + " (membersDemoted=" + membersDemoted
                        + ", controllerVisible=" + controllerVisible
                        + ", controllerIntact=" + controllerIntact + ")");
            } else {
                awaitMemberDemotion(helper, memberChunks, controllerPos, ticksRemaining - 1, continuation);
            }
        });
    }

    private static void reloadMembers(ServerLevel level, Set<ChunkPos> memberChunks) {
        EndgameStructureLifecycleGameTests.setChunksForced(level, memberChunks, true);
        memberChunks.forEach(chunk -> level.getChunk(chunk.x, chunk.z));
    }

    /** Replaces the FULL-level hold with an ENTITY_TICKING-level hold on the controller chunk. */
    private static void resumeTicking(ServerLevel level, BlockPos controllerPos) {
        ChunkPos controllerChunk = new ChunkPos(controllerPos);
        level.getChunkSource().removeRegionTicket(TicketType.START, controllerChunk, 0, Unit.INSTANCE);
        level.getChunkSource().addRegionTicket(TicketType.START, controllerChunk, RESUME_TICK_DISTANCE, Unit.INSTANCE);
    }

    private static void verifyRecovered(GameTestHelper helper, Set<ChunkPos> memberChunks,
            BlockPos controllerPos, Class<? extends BlockEntity> controllerType,
            MultiblockDefinition definition, Direction patternFacing, BlockEntity controller,
            Set<ChunkPos> chunksToRelease) {
        ServerLevel level = helper.getLevel();
        String machineName = controllerType.getSimpleName();
        helper.assertTrue(!controller.isRemoved(), machineName + " lost its BE across recovery");
        helper.assertTrue(((IMultiblockController) controller).isAssembled(),
                machineName + " did not reform after its chunks reloaded");
        helper.assertTrue(definition.pattern().match(level, controllerPos, patternFacing).isValid(),
                machineName + " structure blocks did not survive the partial unload cycle");
        helper.assertTrue(StructureMembershipIndex.INSTANCE.controllersInChunk(
                        level.dimension().location().toString(), new ChunkPos(controllerPos).toLong())
                .contains(controllerPos.asLong()), machineName + " lost its structure index entry after recovery");
        assertMembersLoaded(helper, memberChunks, machineName + " member chunks were not loaded after recovery");
        long scansAfterRecovery = EndgameStructureLifecycleGameTests.scanCount(helper, controllerPos);
        helper.runAfterDelay(IDLE_TICKS, () -> {
            EndgameStructureLifecycleGameTests.assertScanCount(helper, controllerPos, scansAfterRecovery,
                    machineName + " resumed polling after recovery");
            LOGGER.info("Partial unload recovery passed: machine={}, memberChunks={}, scansAfterRecovery={}, idleTicks={}",
                    machineName, memberChunks.size(), scansAfterRecovery, IDLE_TICKS);
            EndgameStructureLifecycleGameTests.setChunksForced(level, chunksToRelease, false);
            helper.succeed();
        });
    }

    /** Visible-holder absence only; pendingUnloads may still contain the chunk.
     * This is not a physical-unload completion check. */
    private static boolean isHolderInvisible(ServerLevel level, ChunkPos chunk) {
        return level.getChunkSource().chunkMap.getVisibleChunkIfPresent(chunk.toLong()) == null;
    }

    /** The exact gate the structure scanners use ({@link Level#isLoaded}): holder ticket level at or below FULL. */
    private static boolean isChunkLoadedForScanners(ServerLevel level, ChunkPos chunk) {
        return level.getChunkSource().hasChunk(chunk.x, chunk.z);
    }

    private static boolean isChunkMaterialized(ServerLevel level, ChunkPos chunk) {
        var holder = level.getChunkSource().chunkMap.getVisibleChunkIfPresent(chunk.toLong());
        return holder != null && holder.getChunkIfPresentUnchecked(ChunkStatus.FULL) instanceof LevelChunk;
    }

    private static boolean allChunksTicking(ServerLevel level, Set<ChunkPos> chunks) {
        return chunks.stream().allMatch(chunk ->
                level.shouldTickBlocksAt(chunk.toLong()) && level.areEntitiesLoaded(chunk.toLong()));
    }

    private static void assertMembersDemoted(GameTestHelper helper, Set<ChunkPos> memberChunks, String message) {
        ServerLevel level = helper.getLevel();
        Set<ChunkPos> unexpected = memberChunks.stream().filter(chunk ->
                isChunkLoadedForScanners(level, chunk) || isHolderInvisible(level, chunk))
                .collect(java.util.stream.Collectors.toSet());
        helper.assertTrue(unexpected.isEmpty(), message + ": " + unexpected);
    }

    private static void assertMembersLoaded(GameTestHelper helper, Set<ChunkPos> memberChunks, String message) {
        ServerLevel level = helper.getLevel();
        Set<ChunkPos> stillUnloaded = memberChunks.stream().filter(chunk ->
                !isChunkMaterialized(level, chunk)).collect(java.util.stream.Collectors.toSet());
        helper.assertTrue(stillUnloaded.isEmpty(), message + ": " + stillUnloaded);
    }

    static Set<ChunkPos> memberChunks(Set<ChunkPos> footprintChunks, BlockPos controllerPos) {
        Set<ChunkPos> memberChunks = new LinkedHashSet<>(footprintChunks);
        memberChunks.remove(new ChunkPos(controllerPos));
        return Set.copyOf(memberChunks);
    }
}

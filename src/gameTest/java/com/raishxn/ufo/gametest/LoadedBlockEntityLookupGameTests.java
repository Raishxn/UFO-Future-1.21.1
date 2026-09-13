package com.raishxn.ufo.gametest;

import com.mojang.logging.LogUtils;
import com.raishxn.ufo.block.InfinityFabricationSingularityControllerBlock;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.InfinityFabricationSingularityControllerBE;
import com.raishxn.ufo.block.entity.QuantumGridLinkBE;
import com.raishxn.ufo.block.entity.pattern.InfinityFabricationSingularityPatternFactory;
import com.raishxn.ufo.util.LoadedBlockEntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Real holder-status and consumer regressions, not a simulated cache miss. */
@GameTestHolder("ufo_tests")
@PrefixGameTestTemplate(false)
public final class LoadedBlockEntityLookupGameTests {
    private static final Set<ChunkPos> FORCED = new LinkedHashSet<>();
    private static final Set<ChunkPos> HELD = new LinkedHashSet<>();

    static void releaseFixtures(ServerLevel level) {
        EndgameStructureLifecycleGameTests.setChunksForced(level, FORCED, false);
        HELD.forEach(pos -> level.getChunkSource().removeRegionTicket(TicketType.START, pos, 0, Unit.INSTANCE));
        FORCED.clear();
        HELD.clear();
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 200)
    public static void fullLookupSurvivesCacheEvictionAndRejectsWorkerThread(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(8208, 5, 16);
        ChunkPos target = new ChunkPos(pos);
        FORCED.add(target);
        level.setChunkForced(target.x, target.z, true);
        level.setBlockAndUpdate(pos, MultiblockBlocks.QUANTUM_GRID_LINK.get().defaultBlockState());
        var entity = level.getBlockEntity(pos);
        helper.assertTrue(entity instanceof QuantumGridLinkBE, "lookup fixture has no Grid Link");
        // Six different FULL reads evict the target from the four-entry last-access cache.
        for (int offset = 1; offset <= 6; offset++) {
            ChunkPos other = new ChunkPos(target.x + offset, target.z);
            FORCED.add(other);
            level.setChunkForced(other.x, other.z, true);
            level.getChunk(other.x, other.z);
        }
        helper.assertTrue(level.getChunkSource().getChunkNow(target.x, target.z) != null,
                "getChunkNow lost a FULL chunk after cache eviction");
        helper.assertTrue(LoadedBlockEntityLookup.get(level, pos) == entity,
                "lookup lost the existing FULL block entity");
        helper.assertTrue(CompletableFuture.supplyAsync(() -> LoadedBlockEntityLookup.get(level, pos)).join() == null,
                "lookup accessed a server block entity outside the server thread");
        LogUtils.getLogger().info("Lookup FULL/cache eviction and worker-thread contract passed");
        helper.succeed();
    }

    @GameTest(template = "event_driven_structure", timeoutTicks = 1200)
    public static void singularityTeardownDetachesMaterializedDemotedGridLink(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var definition = InfinityFabricationSingularityPatternFactory.getDefinition();
        BlockPos linkOffset = definition.pattern().getExpectedPositions(BlockPos.ZERO, Direction.EAST, 'L').getFirst();
        // Align the facing-adjusted offset across a chunk border.
        BlockPos controllerPos = new BlockPos(linkOffset.getX() > 0 ? 6687 : 6672, 5,
                linkOffset.getZ() > 0 ? 31 : 16);
        ChunkPos controllerChunk = new ChunkPos(controllerPos);
        var chunks = EndgameStructureLifecycleGameTests.footprintChunks(controllerPos, definition, Direction.EAST);
        var members = EndgamePartialUnloadGameTests.memberChunks(chunks, controllerPos);
        FORCED.addAll(chunks);
        EndgameStructureLifecycleGameTests.setChunksForced(level, chunks, true);
        level.setBlockAndUpdate(controllerPos, MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get()
                .defaultBlockState().setValue(InfinityFabricationSingularityControllerBlock.FACING, Direction.NORTH));
        definition.pattern().assembleAsCreative(level, controllerPos, Direction.EAST, definition.defaultCreativeStates());
        var controller = (InfinityFabricationSingularityControllerBE) level.getBlockEntity(controllerPos);
        controller.scanStructure(level);
        BlockPos linkPos = definition.pattern().getExpectedPositions(controllerPos, Direction.EAST, 'L').getFirst();
        ChunkPos linkChunk = new ChunkPos(linkPos);
        var link = (QuantumGridLinkBE) level.getBlockEntity(linkPos);
        helper.assertTrue(members.contains(linkChunk), "teardown fixture needs a Grid Link outside the controller chunk");
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(controller.isAssembled() && controllerPos.equals(link.getControllerPos()),
                    "Singularity did not link its Grid Link before demotion");
            // Keep the controller FULL, demote its ring-one members with real tickets.
            HELD.add(controllerChunk);
            level.getChunkSource().addRegionTicket(TicketType.START, controllerChunk, 0, Unit.INSTANCE);
            ChunkUnloadObservation observation = new ChunkUnloadObservation(helper, chunks);
            EndgameStructureLifecycleGameTests.setChunksForced(level, chunks, false);
            EndgameStructureLifecycleGameTests.awaitCondition(helper, 600, () -> {
                var holder = level.getChunkSource().chunkMap.getVisibleChunkIfPresent(linkChunk.toLong());
                return holder != null && holder.currentlyLoading == null
                        && holder.getChunkIfPresent(ChunkStatus.FULL) == null
                        && holder.getChunkIfPresentUnchecked(ChunkStatus.FULL) != null
                        && !level.getChunkSource().hasChunk(linkChunk.x, linkChunk.z)
                        && level.getChunkSource().getChunkNow(linkChunk.x, linkChunk.z) == null;
            }, "Grid Link never entered materialized-but-FULL-disallowed state", () -> {
                helper.assertTrue(!link.isRemoved(), "demoted Grid Link was removed");
                boolean foundBefore = LoadedBlockEntityLookup.get(level, linkPos) == link;
                helper.runAfterDelay(20, () -> {
                    helper.assertTrue(observation.noUnloadsAndAllRetained(), "demotion emitted unload events or removed BEs");
                    boolean foundAfter = LoadedBlockEntityLookup.get(level, linkPos) == link;
                    helper.assertTrue(!level.getChunkSource().hasChunk(linkChunk.x, linkChunk.z),
                            "lookup promoted the demoted chunk");
                    // Exercise the production teardown consumer, rather than merely testing a getter.
                    controller.onControllerBroken();
                    helper.assertTrue(link.getControllerPos() == null,
                            "Singularity teardown failed to detach the retained demoted Grid Link");
                    helper.assertTrue(foundBefore && foundAfter,
                            "lookup lost a materialized Grid Link because FULL status was disallowed");
                    LogUtils.getLogger().info("Lookup demotion regression passed: checkedFULL=null, uncheckedFULL=materialized, getChunkNow=null, retained Grid Link detached; {}",
                            observation.diagnostic());
                    observation.close();
                    helper.succeed();
                });
            });
        });
    }
}

package com.raishxn.ufo.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Observes the original chunk instances, without requesting chunks during unload. */
final class ChunkUnloadObservation implements AutoCloseable {
    private static final Set<ChunkUnloadObservation> ACTIVE = new LinkedHashSet<>();
    private final ServerLevel level;
    private final Map<ChunkPos, LevelChunk> originalChunks = new LinkedHashMap<>();
    private final List<BlockEntity> originalEntities = new ArrayList<>();
    private final Set<ChunkPos> unloaded = new LinkedHashSet<>();
    private final Consumer<ChunkEvent.Unload> listener = this::onUnload;

    ChunkUnloadObservation(GameTestHelper helper, Set<ChunkPos> chunks) {
        level = helper.getLevel();
        for (ChunkPos pos : chunks) {
            var holder = level.getChunkSource().chunkMap.getVisibleChunkIfPresent(pos.toLong());
            var chunk = holder == null ? null : holder.getChunkIfPresentUnchecked(ChunkStatus.FULL);
            helper.assertTrue(chunk instanceof LevelChunk, "unload observer needs a materialized chunk: " + pos);
            LevelChunk full = (LevelChunk) chunk;
            originalChunks.put(pos, full);
            originalEntities.addAll(full.getBlockEntities().values());
        }
        helper.assertTrue(!originalEntities.isEmpty(), "unload observer captured no block entities");
        NeoForge.EVENT_BUS.addListener(listener);
        ACTIVE.add(this);
    }

    private void onUnload(ChunkEvent.Unload event) {
        if (event.getLevel() == level
                && originalChunks.get(event.getChunk().getPos()) == event.getChunk()) {
            unloaded.add(event.getChunk().getPos());
        }
    }

    boolean allUnloadedAndRemoved() {
        return unloaded.containsAll(originalChunks.keySet())
                && originalEntities.stream().allMatch(BlockEntity::isRemoved);
    }

    boolean noUnloadsAndAllRetained() {
        return unloaded.isEmpty() && originalEntities.stream().noneMatch(BlockEntity::isRemoved);
    }

    String diagnostic() {
        return "unloadEvents=" + unloaded.size() + "/" + originalChunks.size()
                + ", removedBEs=" + originalEntities.stream().filter(BlockEntity::isRemoved).count()
                + "/" + originalEntities.size();
    }

    @Override
    public void close() {
        NeoForge.EVENT_BUS.unregister(listener);
        ACTIVE.remove(this);
    }

    static void closeAll() {
        List.copyOf(ACTIVE).forEach(ChunkUnloadObservation::close);
    }
}

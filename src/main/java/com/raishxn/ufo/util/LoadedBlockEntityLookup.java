package com.raishxn.ufo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Looks up a block entity without requesting or ticketing its chunk.
 *
 * <p>This is required for cross-chunk callbacks, especially chunk unload: using
 * {@link Level#getBlockEntity(BlockPos)} there may add a new FULL chunk ticket
 * while the server is already tearing the world down.</p>
 *
 * <p>Server lookups are restricted to the server thread, like
 * {@link net.minecraft.server.level.ServerChunkCache#getChunkNow(int, int)}.
 * That method checks both its cache and the visible holder, but the holder's
 * checked FULL lookup rejects chunks whose allowed status has been demoted.
 * We intentionally accept a completed, materialized FULL chunk still owned by
 * a visible holder during demotion, so teardown can detach retained members.
 * This does not wait, promote status, add tickets, or search pending unloads.
 * Unavailable chunks and calls outside the server thread return {@code null}.
 * The client retains its chunk source lookup.</p>
 */
public final class LoadedBlockEntityLookup {
    private LoadedBlockEntityLookup() {
    }

    @Nullable
    public static BlockEntity get(Level level, BlockPos pos) {
        if (level == null || level.isOutsideBuildHeight(pos)) return null;
        if (level instanceof ServerLevel serverLevel) {
            if (!serverLevel.getServer().isSameThread()) return null;
            ChunkHolder holder = serverLevel.getChunkSource().chunkMap.getVisibleChunkIfPresent(
                    ChunkPos.asLong(SectionPos.blockToSectionCoord(pos.getX()),
                            SectionPos.blockToSectionCoord(pos.getZ())));
            if (holder == null) return null;
            if (!(holder.getChunkIfPresentUnchecked(ChunkStatus.FULL) instanceof LevelChunk chunk)) return null;
            return chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ())
        );
        return chunk == null ? null : chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
    }
}

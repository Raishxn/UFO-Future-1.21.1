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
 * <p>On the server the chunk holder is resolved directly instead of going through
 * {@link net.minecraft.server.level.ServerChunkCache#getChunkNow(int, int)}, which
 * consults only the four-entry last-access cache and therefore reports loaded
 * chunks as absent, silently dropping cross-chunk invalidations. The client keeps
 * the previous lookup because its chunk source has the same contract.</p>
 */
public final class LoadedBlockEntityLookup {
    private LoadedBlockEntityLookup() {
    }

    @Nullable
    public static BlockEntity get(Level level, BlockPos pos) {
        if (level == null || level.isOutsideBuildHeight(pos)) return null;
        if (level instanceof ServerLevel serverLevel) {
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

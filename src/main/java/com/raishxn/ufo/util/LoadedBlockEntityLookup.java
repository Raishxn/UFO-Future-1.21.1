package com.raishxn.ufo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

/**
 * Looks up a block entity without requesting or ticketing its chunk.
 *
 * <p>This is required for cross-chunk callbacks, especially chunk unload: using
 * {@link Level#getBlockEntity(BlockPos)} there may add a new FULL chunk ticket
 * while the server is already tearing the world down.</p>
 */
public final class LoadedBlockEntityLookup {
    private LoadedBlockEntityLookup() {
    }

    @Nullable
    public static BlockEntity get(Level level, BlockPos pos) {
        if (level == null || level.isOutsideBuildHeight(pos)) return null;
        LevelChunk chunk = level.getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ())
        );
        return chunk == null ? null : chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
    }
}

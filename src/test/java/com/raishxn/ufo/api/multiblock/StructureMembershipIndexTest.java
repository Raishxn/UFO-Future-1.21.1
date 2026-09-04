package com.raishxn.ufo.api.multiblock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureMembershipIndexTest {
    private final StructureMembershipIndex index = StructureMembershipIndex.INSTANCE;

    @BeforeEach
    void reset() {
        index.reset();
    }

    @Test
    void resolvesOverlappingControllersByDimensionAndMember() {
        index.register("overworld", 10, List.of(100L, 101L));
        index.register("overworld", 20, List.of(101L, 102L));
        index.register("nether", 30, List.of(101L));

        assertEquals(java.util.Set.of(10L, 20L), index.controllersAt("overworld", 101L));
        assertEquals(java.util.Set.of(30L), index.controllersAt("nether", 101L));
    }

    @Test
    void replacingRegistrationRemovesStaleMembers() {
        index.register("overworld", 10, List.of(100L, 101L));
        index.register("overworld", 10, List.of(102L));

        assertTrue(index.controllersAt("overworld", 100L).isEmpty());
        assertEquals(java.util.Set.of(10L), index.controllersAt("overworld", 102L));
    }

    @Test
    void unregisterAndResetRemoveReverseMappings() {
        index.register("overworld", 10, List.of(100L));
        index.unregister("overworld", 10);
        assertTrue(index.controllersAt("overworld", 100L).isEmpty());

        index.register("overworld", 20, List.of(200L));
        index.reset();
        assertTrue(index.controllersAt("overworld", 200L).isEmpty());
    }

    @Test
    void indexesEveryFootprintChunkIncludingNegativeCoordinates() {
        long controller = blockPos(15, 64, 15);
        long sameChunk = blockPos(0, 10, 0);
        long eastChunk = blockPos(16, 10, 0);
        long negativeChunk = blockPos(-1, 10, -17);

        index.register("overworld", controller, List.of(sameChunk, eastChunk, negativeChunk));

        assertEquals(java.util.Set.of(controller), index.controllersInChunk("overworld", chunkPos(0, 0)));
        assertEquals(java.util.Set.of(controller), index.controllersInChunk("overworld", chunkPos(1, 0)));
        assertEquals(java.util.Set.of(controller), index.controllersInChunk("overworld", chunkPos(-1, -2)));
    }

    @Test
    void replacementUnregisterAndResetRemoveChunkMappings() {
        long controller = blockPos(0, 64, 0);
        index.register("overworld", controller, List.of(blockPos(32, 64, 0)));
        assertEquals(java.util.Set.of(controller), index.controllersInChunk("overworld", chunkPos(2, 0)));

        index.register("overworld", controller, List.of(blockPos(48, 64, 0)));
        assertTrue(index.controllersInChunk("overworld", chunkPos(2, 0)).isEmpty());
        assertEquals(java.util.Set.of(controller), index.controllersInChunk("overworld", chunkPos(3, 0)));

        index.unregister("overworld", controller);
        assertTrue(index.controllersInChunk("overworld", chunkPos(0, 0)).isEmpty());
        assertTrue(index.controllersInChunk("overworld", chunkPos(3, 0)).isEmpty());

        index.register("overworld", controller, List.of(blockPos(64, 64, 0)));
        index.reset();
        assertTrue(index.controllersInChunk("overworld", chunkPos(4, 0)).isEmpty());
    }

    @Test
    void identicalRegistrationIsIdempotentButAChangedFootprintIsApplied() {
        assertTrue(index.register("overworld", 10L, List.of(100L, 101L)));
        assertFalse(index.register("overworld", 10L, List.of(101L, 100L, 100L)));
        assertEquals(java.util.Set.of(10L), index.controllersAt("overworld", 100L));

        assertTrue(index.register("overworld", 10L, List.of(102L)));
        assertTrue(index.controllersAt("overworld", 100L).isEmpty());
        assertEquals(java.util.Set.of(10L), index.controllersAt("overworld", 102L));
    }

    private static long blockPos(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | (y & 0xFFFL);
    }

    private static long chunkPos(int x, int z) {
        return Integer.toUnsignedLong(x) | (Integer.toUnsignedLong(z) << 32);
    }
}

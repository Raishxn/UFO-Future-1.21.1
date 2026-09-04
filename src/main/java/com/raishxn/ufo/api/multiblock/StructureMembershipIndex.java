package com.raishxn.ufo.api.multiblock;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** In-memory reverse index from a structure footprint position to its controller. */
public final class StructureMembershipIndex {
    public static final StructureMembershipIndex INSTANCE = new StructureMembershipIndex();

    private final Map<String, Map<Long, Set<Long>>> controllersByMember = new HashMap<>();
    private final Map<String, Map<Long, Set<Long>>> controllersByChunk = new HashMap<>();
    private final Map<ControllerKey, Set<Long>> membersByController = new HashMap<>();

    public synchronized boolean register(String dimension, long controllerPos, Collection<Long> memberPositions) {
        ControllerKey key = new ControllerKey(dimension, controllerPos);
        Set<Long> members = new LinkedHashSet<>(memberPositions);
        if (members.equals(membersByController.get(key))) return false;

        unregister(dimension, controllerPos);
        membersByController.put(key, members);
        Map<Long, Set<Long>> dimensionIndex = controllersByMember.computeIfAbsent(dimension, ignored -> new HashMap<>());
        for (long memberPos : members) {
            dimensionIndex.computeIfAbsent(memberPos, ignored -> new LinkedHashSet<>()).add(controllerPos);
        }
        Map<Long, Set<Long>> chunkIndex = controllersByChunk.computeIfAbsent(dimension, ignored -> new HashMap<>());
        Set<Long> footprintChunks = new LinkedHashSet<>();
        footprintChunks.add(chunkKey(controllerPos));
        for (long memberPos : members) footprintChunks.add(chunkKey(memberPos));
        for (long chunkPos : footprintChunks) {
            chunkIndex.computeIfAbsent(chunkPos, ignored -> new LinkedHashSet<>()).add(controllerPos);
        }
        return true;
    }

    public synchronized void unregister(String dimension, long controllerPos) {
        Set<Long> members = membersByController.remove(new ControllerKey(dimension, controllerPos));
        Map<Long, Set<Long>> dimensionIndex = controllersByMember.get(dimension);
        if (members == null) return;
        if (dimensionIndex != null) {
            for (long memberPos : members) {
                Set<Long> controllers = dimensionIndex.get(memberPos);
                if (controllers == null) continue;
                controllers.remove(controllerPos);
                if (controllers.isEmpty()) dimensionIndex.remove(memberPos);
            }
            if (dimensionIndex.isEmpty()) controllersByMember.remove(dimension);
        }

        Map<Long, Set<Long>> chunkIndex = controllersByChunk.get(dimension);
        if (chunkIndex == null) return;
        Set<Long> footprintChunks = new LinkedHashSet<>();
        footprintChunks.add(chunkKey(controllerPos));
        for (long memberPos : members) footprintChunks.add(chunkKey(memberPos));
        for (long chunkPos : footprintChunks) {
            Set<Long> controllers = chunkIndex.get(chunkPos);
            if (controllers == null) continue;
            controllers.remove(controllerPos);
            if (controllers.isEmpty()) chunkIndex.remove(chunkPos);
        }
        if (chunkIndex.isEmpty()) controllersByChunk.remove(dimension);
    }

    public synchronized Set<Long> controllersAt(String dimension, long memberPos) {
        Map<Long, Set<Long>> dimensionIndex = controllersByMember.get(dimension);
        if (dimensionIndex == null) return Set.of();
        return Set.copyOf(dimensionIndex.getOrDefault(memberPos, Set.of()));
    }

    /** Controllers whose tracked footprint or controller itself intersects a chunk. */
    public synchronized Set<Long> controllersInChunk(String dimension, long chunkPos) {
        Map<Long, Set<Long>> dimensionIndex = controllersByChunk.get(dimension);
        if (dimensionIndex == null) return Set.of();
        return Set.copyOf(dimensionIndex.getOrDefault(chunkPos, Set.of()));
    }

    public synchronized void reset() {
        controllersByMember.clear();
        controllersByChunk.clear();
        membersByController.clear();
    }

    public static long chunkKey(long blockPos) {
        int blockX = (int) (blockPos >> 38);
        int blockZ = (int) (blockPos << 26 >> 38);
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        return Integer.toUnsignedLong(chunkX) | (Integer.toUnsignedLong(chunkZ) << 32);
    }

    private record ControllerKey(String dimension, long controllerPos) {
    }
}

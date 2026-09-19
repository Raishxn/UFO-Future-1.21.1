package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.api.multiblock.StructureMembershipIndex;
import com.raishxn.ufo.api.multiblock.StructureInvalidationTarget;
import com.raishxn.ufo.util.LoadedBlockEntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.LinkedHashSet;
import java.util.Set;

@EventBusSubscriber(modid = UfoMod.MOD_ID)
public final class MultiblockStructureEvents {
    private static volatile boolean acceptingChunkCallbacks;

    private MultiblockStructureEvents() {
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer().level() instanceof ServerLevel level) invalidate(level, event.getPos());
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) invalidate(level, event.getPos());
    }

    @SubscribeEvent
    public static void onNeighborsNotified(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel level) invalidate(level, event.getPos());
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (BlockPos affectedPos : event.getAffectedBlocks()) invalidate(level, affectedPos);
    }

    @SubscribeEvent
    public static void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) return;

        Set<BlockPos> affected = new LinkedHashSet<>();
        affected.add(event.getPos());
        affected.add(event.getFaceOffsetPos());
        affected.addAll(resolver.getToDestroy());
        for (BlockPos source : resolver.getToPush()) {
            affected.add(source);
            affected.add(source.relative(event.getDirection()));
        }
        level.getServer().execute(() -> affected.forEach(pos -> invalidate(level, pos)));
    }

    @SubscribeEvent
    public static void onChunkLoaded(ChunkEvent.Load event) {
        if (!acceptingChunkCallbacks || !(event.getLevel() instanceof ServerLevel level)) return;
        long chunkPos = event.getChunk().getPos().toLong();
        // ChunkEvent.Load fires before FULL promotion; defer every level/BE access.
        level.getServer().execute(() -> {
            if (acceptingChunkCallbacks) invalidateChunk(level, chunkPos, false);
        });
    }

    @SubscribeEvent
    public static void onChunkUnloaded(ChunkEvent.Unload event) {
        if (!acceptingChunkCallbacks || !(event.getLevel() instanceof ServerLevel level)) return;
        long chunkPos = event.getChunk().getPos().toLong();
        level.getServer().execute(() -> {
            if (acceptingChunkCallbacks) invalidateChunk(level, chunkPos, true);
        });
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        StructureMembershipIndex.INSTANCE.reset();
        acceptingChunkCallbacks = true;
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // stopServer drains ChunkMap unload work until its queue becomes empty. Cross-chunk
        // invalidations scheduled from an unload can mutate AE2 nodes/chunk holders and append more
        // work to that same queue, making the "Saving World" screen spin for minutes. Clearing the
        // transient index and closing both the event and already-enqueued callback paths makes
        // shutdown teardown read-only. A fresh server lifecycle rebuilds the index from chunk loads.
        acceptingChunkCallbacks = false;
        StructureMembershipIndex.INSTANCE.reset();
    }

    private static void invalidate(ServerLevel level, BlockPos changedPos) {
        String dimension = level.dimension().location().toString();
        for (long controllerPos : StructureMembershipIndex.INSTANCE.controllersAt(dimension, changedPos.asLong())) {
            BlockPos pos = BlockPos.of(controllerPos);
            if (LoadedBlockEntityLookup.get(level, pos) instanceof StructureInvalidationTarget controller) {
                controller.markStructureDirty();
            }
        }
    }

    private static void invalidateChunk(ServerLevel level, long chunkPos, boolean unregisterControllersInChunk) {
        String dimension = level.dimension().location().toString();
        for (long controllerPos : StructureMembershipIndex.INSTANCE.controllersInChunk(dimension, chunkPos)) {
            if (unregisterControllersInChunk && StructureMembershipIndex.chunkKey(controllerPos) == chunkPos) {
                StructureMembershipIndex.INSTANCE.unregister(dimension, controllerPos);
                continue;
            }
            BlockPos pos = BlockPos.of(controllerPos);
            if (LoadedBlockEntityLookup.get(level, pos) instanceof StructureInvalidationTarget controller) {
                controller.markStructureDirty();
            }
        }
    }
}

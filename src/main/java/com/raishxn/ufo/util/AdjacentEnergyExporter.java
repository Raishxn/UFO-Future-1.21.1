package com.raishxn.ufo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public final class AdjacentEnergyExporter {
    private static final Direction[] DIRECTIONS = Direction.values();

    private AdjacentEnergyExporter() {
    }

    public static void pushEnergy(Level level, BlockPos pos, IEnergyStorage source, int totalBudget, int perSideBudget) {
        pushEnergy(null, level, pos, source, totalBudget, perSideBudget);
    }

    /**
     * Same transfer as {@link #pushEnergy(Level, BlockPos, IEnergyStorage, int, int)}, resolving the
     * neighbours through the caller's per-block capability caches instead of a fresh lookup per side
     * per tick.
     */
    public static void pushEnergy(@Nullable Targets targets, Level level, BlockPos pos, IEnergyStorage source,
                                  int totalBudget, int perSideBudget) {
        if (level == null || level.isClientSide() || source == null || totalBudget <= 0 || perSideBudget <= 0) {
            return;
        }

        int remaining = totalBudget;
        for (Direction direction : DIRECTIONS) {
            if (remaining <= 0) {
                break;
            }

            IEnergyStorage target = targets != null
                    ? targets.get(level, pos, direction)
                    : level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target == null || !target.canReceive()) {
                continue;
            }

            int offer = Math.min(perSideBudget, remaining);
            if (offer <= 0) {
                continue;
            }

            int simulated = source.extractEnergy(offer, true);
            if (simulated <= 0) {
                continue;
            }

            int accepted = target.receiveEnergy(simulated, false);
            if (accepted <= 0) {
                continue;
            }

            int extracted = source.extractEnergy(accepted, false);
            if (extracted <= 0) {
                continue;
            }

            remaining -= extracted;
        }
    }

    /** Neighbour energy capabilities of one exporting block, kept valid by NeoForge's invalidation. */
    public static final class Targets {
        @SuppressWarnings({"rawtypes", "unchecked"})
        private final BlockCapabilityCache<IEnergyStorage, Direction>[] caches = new BlockCapabilityCache[6];
        @Nullable private ServerLevel cachedLevel;
        @Nullable private BlockPos cachedPos;

        @Nullable
        IEnergyStorage get(Level level, BlockPos pos, Direction direction) {
            if (!(level instanceof ServerLevel serverLevel)) {
                return level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(direction), direction.getOpposite());
            }
            if (serverLevel != this.cachedLevel || !pos.equals(this.cachedPos)) {
                java.util.Arrays.fill(this.caches, null);
                this.cachedLevel = serverLevel;
                this.cachedPos = pos.immutable();
            }
            BlockCapabilityCache<IEnergyStorage, Direction> cache = this.caches[direction.ordinal()];
            if (cache == null) {
                cache = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, serverLevel,
                        this.cachedPos.relative(direction), direction.getOpposite());
                this.caches[direction.ordinal()] = cache;
            }
            return cache.getCapability();
        }
    }
}

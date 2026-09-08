package com.raishxn.ufo.wireless;

import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.block.entity.AbstractParallelMultiblockControllerBE;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.block.entity.QuantumPatternHatchBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;

/** Server-local activity. Values never retain a Level or block entity. */
public final class QuantumWirelessActivity {
    private static final Map<Level, State> LEVELS = new WeakHashMap<>();
    private static final class State {
        final Set<BlockPos> hosts = new HashSet<>();
        final WirelessActivityWindow<UUID> activity = new WirelessActivityWindow<>();
        final Map<UUID, WirelessBonus> bonuses = new HashMap<>();
        long snapshotTick = Long.MIN_VALUE;
    }
    private static State state(Level level) { return LEVELS.computeIfAbsent(level, key -> new State()); }
    public static void register(QuantumPatternHatchBE hatch) {
        if (hatch.getLevel() != null && !hatch.getLevel().isClientSide()) {
            var state = state(hatch.getLevel());
            state.hosts.add(hatch.getBlockPos().immutable());
            state.snapshotTick = Long.MIN_VALUE;
        }
    }
    public static void unregister(QuantumPatternHatchBE hatch) {
        if (hatch.getLevel() == null || hatch.getLevel().isClientSide()) return;
        var state = LEVELS.get(hatch.getLevel());
        if (state != null) {
            state.hosts.remove(hatch.getBlockPos());
            state.snapshotTick = Long.MIN_VALUE;
        }
    }
    public static void markProgress(BlockEntity machine) {
        var level = machine.getLevel();
        if (level != null && !level.isClientSide() && machine.getPersistentData().hasUUID("ufoWirelessIdentity"))
            state(level).activity.mark(QuantumWirelessLinks.identity(machine), level.getGameTime());
    }
    public static WirelessBonus bonusFor(BlockEntity machine) {
        var level = machine.getLevel();
        if (level == null || level.isClientSide() || !machine.getPersistentData().hasUUID("ufoWirelessIdentity")) return WirelessBonus.NONE;
        refresh(level);
        return state(level).bonuses.getOrDefault(QuantumWirelessLinks.identity(machine), WirelessBonus.NONE);
    }
    public static void refresh(Level level) {
        if (level == null || level.isClientSide()) return;
        var state = state(level);
        long now = level.getGameTime();
        if (state.snapshotTick == now) return;
        state.snapshotTick = now;
        state.activity.prune(now);
        state.bonuses.clear();
        for (var pos : List.copyOf(state.hosts)) {
            if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof QuantumPatternHatchBE hatch)) continue;
            var display = hatch.bonusProfile;
            display.dmaMachines = 0; display.multiblockMachines = 0;
            display.dmaBonus = WirelessBonus.NONE; display.multiblockBonus = WirelessBonus.NONE;
            if (!hatch.wirelessLinks().enabled() || !hatch.getMainNode().isActive()) continue;
            Map<UUID, Boolean> eligible = new HashMap<>();
            for (var target : hatch.wirelessLinks().targets()) {
                var be = hatch.wirelessLinks().resolve(hatch, target);
                if (be instanceof DimensionalMatterAssemblerBlockEntity dma && !dma.isWirelessCreative()) eligible.put(target.identity(), true);
                else if (be instanceof AbstractParallelMultiblockControllerBE multi && multi.acceptsPlansForWirelessBonus()) eligible.put(target.identity(), false);
            }
            eligible.forEach((id, dma) -> {
                if (state.activity.active(id, now)) {
                    if (dma) display.dmaMachines++; else display.multiblockMachines++;
                }
            });
            if (!UFOConfig.WIRELESS_BUFFS.get()) continue;
            display.dmaBonus = calculate(display.dmaMachines, true);
            display.multiblockBonus = calculate(display.multiblockMachines, false);
            eligible.forEach((id, dma) -> state.bonuses.merge(id, dma ? display.dmaBonus : display.multiblockBonus,
                    (a, b) -> b.speed() > a.speed() || b.energy() < a.energy() || b.heat() < a.heat() ? b : a));
        }
    }
    private static WirelessBonus calculate(int count, boolean dma) {
        return WirelessBonus.automatic(count,
                (dma ? UFOConfig.WIRELESS_DMA_SATURATION : UFOConfig.WIRELESS_MULTI_SATURATION).get(),
                (dma ? UFOConfig.WIRELESS_DMA_SPEED : UFOConfig.WIRELESS_MULTI_SPEED).get(),
                (dma ? UFOConfig.WIRELESS_DMA_ENERGY : UFOConfig.WIRELESS_MULTI_ENERGY).get(),
                (dma ? UFOConfig.WIRELESS_DMA_HEAT : UFOConfig.WIRELESS_MULTI_HEAT).get());
    }
}

package com.raishxn.ufo.wireless;

import com.raishxn.ufo.UFOConfig;
import appeng.api.AECapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Persistent, face-specific links. Resolving a link never loads a chunk. */
public final class QuantumWirelessLinks {
    private static final String ID = "ufoWirelessIdentity";
    public record Target(BlockPos pos, Direction face, UUID identity) {}
    private final List<Target> targets = new ArrayList<>();
    private boolean enabled;
    private int cursor;
    private int linkRange = 32;
    public int range() {
        int maximum = UFOConfig.WIRELESS_RANGE.get();
        return maximum == 0 ? linkRange : Math.min(linkRange, maximum);
    }
    public void setRange(int requested) { linkRange = Math.clamp(requested, 1, 30_000_000); }

    public boolean enabled() { return enabled; }
    public int size() { return targets.size(); }
    public boolean contains(Target target) { return targets.contains(target); }
    public List<Target> targets() { return List.copyOf(targets); }
    public int connectedMachines(QuantumWirelessHost host) {
        return (int) targets.stream().filter(target -> resolve(host, target) != null)
                .map(Target::identity).distinct().count();
    }
    public void toggleMode() { enabled = !enabled; }

    public static UUID identity(BlockEntity be) {
        var data = be.getPersistentData();
        if (!data.hasUUID(ID)) {
            data.putUUID(ID, UUID.randomUUID());
            be.setChanged();
        }
        return data.getUUID(ID);
    }

    public static boolean matches(BlockEntity be, UUID identity) {
        return be.getPersistentData().hasUUID(ID) && be.getPersistentData().getUUID(ID).equals(identity);
    }

    public boolean inRange(BlockPos origin, BlockPos destination) {
        int range = range();
        return origin.distSqr(destination) <= (double) range * range;
    }

    /** Returns false only when adding would exceed the configured capacity. */
    public boolean toggle(BlockEntity destination, Direction face) {
        var pos = destination.getBlockPos();
        if (targets.removeIf(t -> t.pos().equals(pos) && t.face() == face)) return true;
        if (targets.size() >= UFOConfig.WIRELESS_MAX_LINKS.get()) return false;
        targets.add(new Target(pos.immutable(), face, identity(destination)));
        return true;
    }

    public Target next() {
        if (targets.isEmpty()) return null;
        cursor = Math.floorMod(cursor, targets.size());
        Target target = targets.get(cursor);
        cursor = (cursor + 1) % targets.size();
        return target;
    }

    public BlockEntity resolve(QuantumWirelessHost host, Target target) {
        var source = host.getBlockEntity();
        var level = source.getLevel();
        if (level == null || !level.hasChunkAt(target.pos()) || !inRange(source.getBlockPos(), target.pos())) return null;
        var be = level.getBlockEntity(target.pos());
        if (be == null || be == source || !matches(be, target.identity())) return null;
        var nodeHost = level.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST, target.pos(), null);
        if (nodeHost != null) {
            var node = nodeHost.getGridNode(target.face());
            var sourceNode = host.getMainNode().getNode();
            // An isolated AE device has a one-node grid even without a cable.
            if (node != null && node.getGrid().size() > 1
                    && (sourceNode == null || node.getGrid() != sourceNode.getGrid())) return null;
        }
        return be;
    }

    public void save(CompoundTag tag) {
        tag.putBoolean("ufoWirelessEnabled", enabled);
        tag.putInt("ufoWirelessRange", linkRange);
        var list = new ListTag();
        for (var target : targets) {
            var entry = new CompoundTag();
            entry.putLong("pos", target.pos().asLong());
            entry.putInt("face", target.face().ordinal());
            entry.putUUID("identity", target.identity());
            list.add(entry);
        }
        tag.put("ufoWirelessTargets", list);
    }

    public void load(CompoundTag tag) {
        enabled = tag.getBoolean("ufoWirelessEnabled");
        linkRange = tag.contains("ufoWirelessRange") ? Math.clamp(tag.getInt("ufoWirelessRange"), 1, 30_000_000) : 32;
        targets.clear();
        var list = tag.getList("ufoWirelessTargets", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(list.size(), 1024); i++) {
            var entry = list.getCompound(i);
            int face = entry.getInt("face");
            if (!entry.hasUUID("identity") || face < 0 || face >= Direction.values().length) continue;
            var target = new Target(BlockPos.of(entry.getLong("pos")), Direction.values()[face], entry.getUUID("identity"));
            if (!targets.contains(target)) targets.add(target);
        }
        cursor = 0;
    }
}

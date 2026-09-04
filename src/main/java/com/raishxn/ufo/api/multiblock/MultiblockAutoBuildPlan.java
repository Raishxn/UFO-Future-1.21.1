package com.raishxn.ufo.api.multiblock;

/* Adapted from AE2 Lightning Tech's MatrixAutoBuildPlan (LGPL-3.0). */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/** Pure, immutable preflight plan. It never proposes replacing an occupied mismatching slot. */
public record MultiblockAutoBuildPlan<T>(List<Placement<T>> placements, List<LocalPos> blocked) {
    public MultiblockAutoBuildPlan {
        placements = List.copyOf(placements);
        blocked = List.copyOf(blocked);
    }

    public static <T> MultiblockAutoBuildPlan<T> create(
            char[][][] template,
            char controllerSymbol,
            int controllerX,
            int controllerZ,
            Map<Character, T> targets,
            Predicate<T> isPlaceableTarget,
            SlotResolver<T> resolver) {
        Objects.requireNonNull(template);
        Objects.requireNonNull(targets);
        Objects.requireNonNull(isPlaceableTarget);
        Objects.requireNonNull(resolver);
        var placements = new ArrayList<Placement<T>>();
        var blocked = new ArrayList<LocalPos>();
        for (int y = 0; y < template.length; y++) for (int z = 0; z < template[y].length; z++) {
            for (int x = 0; x < template[y][z].length; x++) {
                char symbol = template[y][z][x];
                if (symbol == controllerSymbol) continue;
                T target = targets.get(symbol);
                if (target == null || !isPlaceableTarget.test(target)) continue;
                LocalPos local = new LocalPos(x, y, z);
                switch (resolver.resolve(local, symbol, target)) {
                    case MATCHING -> { }
                    case EMPTY -> placements.add(new Placement<>(local, symbol, target));
                    case BLOCKED -> blocked.add(local);
                }
            }
        }
        placements.sort(Comparator
                .comparingInt((Placement<T> p) -> p.localPos().y())
                .thenComparingInt(p -> Math.abs(p.localPos().x() - controllerX)
                        + Math.abs(p.localPos().z() - controllerZ))
                .thenComparingInt(p -> Math.abs(p.localPos().z() - controllerZ))
                .thenComparingInt(p -> p.localPos().x())
                .thenComparingInt(p -> p.localPos().z()));
        return new MultiblockAutoBuildPlan<>(placements, blocked);
    }

    public enum SlotState { MATCHING, EMPTY, BLOCKED }
    public record LocalPos(int x, int y, int z) { }
    public record Placement<T>(LocalPos localPos, char symbol, T target) { }
    @FunctionalInterface public interface SlotResolver<T> {
        SlotState resolve(LocalPos localPos, char symbol, T target);
    }
}

package com.raishxn.ufo.api.multiblock;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiblockAutoBuildPlanTest {
    private static final char[][][] TEMPLATE = {
            {{'A', 'C', 'A'}, {' ', 'B', ' '}},
            {{'A', 'A', 'A'}, {' ', 'B', ' '}}
    };
    private static final Map<Character, String> TARGETS = Map.of('A', "iron", 'B', "glass");

    @Test
    void plansOnlyMissingPlaceableSlotsAndSkipsControllerAndIgnoredAir() {
        var matching = new MultiblockAutoBuildPlan.LocalPos(0, 0, 0);
        var plan = MultiblockAutoBuildPlan.create(TEMPLATE, 'C', 1, 0, TARGETS, ignored -> true,
                (pos, symbol, target) -> pos.equals(matching)
                        ? MultiblockAutoBuildPlan.SlotState.MATCHING
                        : MultiblockAutoBuildPlan.SlotState.EMPTY);
        assertTrue(plan.blocked().isEmpty());
        assertEquals(6, plan.placements().size());
        assertTrue(plan.placements().stream().noneMatch(p -> p.localPos().equals(
                new MultiblockAutoBuildPlan.LocalPos(1, 0, 0))));
    }

    @Test
    void reportsOccupiedMismatchesWithoutAddingReplacementPlacements() {
        var blocked = new MultiblockAutoBuildPlan.LocalPos(1, 0, 1);
        var plan = MultiblockAutoBuildPlan.create(TEMPLATE, 'C', 1, 0, TARGETS, ignored -> true,
                (pos, symbol, target) -> pos.equals(blocked)
                        ? MultiblockAutoBuildPlan.SlotState.BLOCKED
                        : MultiblockAutoBuildPlan.SlotState.MATCHING);
        assertEquals(java.util.List.of(blocked), plan.blocked());
        assertTrue(plan.placements().isEmpty());
    }

    @Test
    void sortsBottomUpThenOutwardFromController() {
        var states = new HashMap<MultiblockAutoBuildPlan.LocalPos, MultiblockAutoBuildPlan.SlotState>();
        states.put(new MultiblockAutoBuildPlan.LocalPos(0, 1, 0), MultiblockAutoBuildPlan.SlotState.EMPTY);
        states.put(new MultiblockAutoBuildPlan.LocalPos(2, 0, 0), MultiblockAutoBuildPlan.SlotState.EMPTY);
        states.put(new MultiblockAutoBuildPlan.LocalPos(0, 0, 0), MultiblockAutoBuildPlan.SlotState.EMPTY);
        var plan = MultiblockAutoBuildPlan.create(TEMPLATE, 'C', 1, 0, TARGETS, ignored -> true,
                (pos, symbol, target) -> states.getOrDefault(pos, MultiblockAutoBuildPlan.SlotState.MATCHING));
        assertEquals(java.util.List.of(
                new MultiblockAutoBuildPlan.LocalPos(0, 0, 0),
                new MultiblockAutoBuildPlan.LocalPos(2, 0, 0),
                new MultiblockAutoBuildPlan.LocalPos(0, 1, 0)),
                plan.placements().stream().map(MultiblockAutoBuildPlan.Placement::localPos).toList());
    }
}

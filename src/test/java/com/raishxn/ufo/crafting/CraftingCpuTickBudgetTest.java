package com.raishxn.ufo.crafting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CraftingCpuTickBudgetTest {
    private static final int PAST_MINIMUM = CraftingCpuTickBudget.MIN_ATTEMPTS_PER_CPU;

    private final AtomicLong clock = new AtomicLong();
    private final CraftingCpuTickBudget budget = new CraftingCpuTickBudget(clock::get);

    @Test
    void attemptsAreUnlimitedBeforeTheDeadline() {
        budget.beginTick(1L, 100L);
        clock.set(99L);
        assertTrue(budget.mayAttempt(Integer.MAX_VALUE));
    }

    @Test
    void expiredBudgetStopsCpusPastTheirMinimum() {
        budget.beginTick(1L, 100L);
        clock.set(100L);
        assertFalse(budget.mayAttempt(PAST_MINIMUM));
    }

    @Test
    void lateCpuStillGetsItsMinimumAfterExpiry() {
        budget.beginTick(1L, 100L);
        clock.set(1_000L);
        assertTrue(budget.mayAttempt(PAST_MINIMUM - 1));
    }

    @Test
    void laterCpusShareTheDeadlineOfTheTick() {
        budget.beginTick(1L, 100L);
        clock.set(100L);
        budget.beginTick(1L, 100L);
        assertFalse(budget.mayAttempt(PAST_MINIMUM));
    }

    @Test
    void nextTickOpensAFreshBudget() {
        budget.beginTick(1L, 100L);
        clock.set(500L);
        budget.beginTick(2L, 100L);
        assertTrue(budget.mayAttempt(PAST_MINIMUM));
    }
}

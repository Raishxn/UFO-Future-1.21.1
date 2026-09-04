package com.raishxn.ufo.block.entity.processing;

import java.util.List;
import java.util.OptionalLong;

/** Computes an all-or-nothing reservation for recipes backed by a single fluid tank. */
public final class SingleTankFluidReservation {
    private SingleTankFluidReservation() {
    }

    public static OptionalLong reserve(long availableAmount, List<Demand> demands) {
        if (availableAmount < 0 || demands == null) {
            return OptionalLong.empty();
        }

        long requiredAmount = 0;
        for (Demand demand : demands) {
            if (demand == null || !demand.matchesStoredFluid() || demand.amount() <= 0) {
                return OptionalLong.empty();
            }

            try {
                requiredAmount = Math.addExact(requiredAmount, demand.amount());
            } catch (ArithmeticException ignored) {
                return OptionalLong.empty();
            }
        }

        return requiredAmount <= availableAmount
                ? OptionalLong.of(requiredAmount)
                : OptionalLong.empty();
    }

    public record Demand(boolean matchesStoredFluid, long amount) {
    }
}

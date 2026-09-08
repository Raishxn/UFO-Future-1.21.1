package com.raishxn.ufo.block.entity.processing;

import java.util.function.DoubleUnaryOperator;

/** Draws from the grid first, then completes the request from a private machine buffer. */
public record GridPoweredEnergySource(DoubleUnaryOperator grid, DoubleUnaryOperator buffer) {
    public double extract(double amount) {
        if (amount <= 0) {
            return 0;
        }
        double extracted = grid.applyAsDouble(amount);
        if (extracted < amount) {
            extracted += buffer.applyAsDouble(amount - extracted);
        }
        return extracted;
    }
}

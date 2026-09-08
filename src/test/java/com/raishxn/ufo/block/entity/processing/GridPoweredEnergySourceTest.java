package com.raishxn.ufo.block.entity.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridPoweredEnergySourceTest {
    @Test
    void consumesGridBeforePrivateBuffer() {
        var grid = new Battery(100);
        var buffer = new Battery(50);
        var source = new GridPoweredEnergySource(grid::extract, buffer::extract);
        assertEquals(60, source.extract(60));
        assertEquals(40, grid.stored);
        assertEquals(50, buffer.stored);
    }

    @Test
    void combinesSourcesWithoutConsumingDuringSimulation() {
        var grid = new Battery(30);
        var buffer = new Battery(50);
        var source = new GridPoweredEnergySource(grid::extract, buffer::extract);
        var simulation = new GridPoweredEnergySource(grid::simulate, buffer::simulate);
        assertEquals(60, simulation.extract(60));
        assertEquals(30, grid.stored);
        assertEquals(50, buffer.stored);
        assertEquals(60, source.extract(60));
        assertEquals(0, grid.stored);
        assertEquals(20, buffer.stored);
        assertEquals(20, source.extract(60));
        assertEquals(0, source.extract(60));
    }

    private static final class Battery {
        private double stored;

        private Battery(double stored) {
            this.stored = stored;
        }

        private double simulate(double amount) {
            return Math.min(stored, amount);
        }

        private double extract(double amount) {
            double extracted = simulate(amount);
            stored -= extracted;
            return extracted;
        }
    }
}

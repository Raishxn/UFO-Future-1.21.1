package com.raishxn.ufo.api.multiblock.port;

import com.raishxn.ufocore.api.port.CoolantTankState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CoolantTankStateTest {
    @Test
    void simulationDoesNotMutateFillOrExtraction() {
        CoolantTankState<String> tank = new CoolantTankState<>(1_000, id -> id.startsWith("ufo:"));

        assertEquals(700, tank.fill("ufo:stable", 700, true));
        assertEquals(0, tank.amount());
        assertEquals(700, tank.fill("ufo:stable", 700, false));
        assertEquals(500, tank.extract("ufo:stable", 500, true));
        assertEquals(700, tank.amount());
        assertEquals(500, tank.extract("ufo:stable", 500, false));
        assertEquals(200, tank.amount());
    }

    @Test
    void filtersWrongFluidAndPreventsMixing() {
        CoolantTankState<String> tank = new CoolantTankState<>(1_000, id -> id.startsWith("ufo:"));

        assertEquals(0, tank.fill("minecraft:water", 1_000, false));
        assertEquals(600, tank.fill("ufo:gelid", 600, false));
        assertEquals(0, tank.fill("ufo:temporal", 400, false));
        assertEquals(0, tank.extract("ufo:temporal", 600, false));
        assertEquals(600, tank.amount());
    }

    @Test
    void restoreClampsValidDataAndRejectsInvalidData() {
        CoolantTankState<String> tank = new CoolantTankState<>(1_000, id -> id.startsWith("ufo:"));

        tank.restore("ufo:temporal", 2_000);
        assertEquals("ufo:temporal", tank.coolant());
        assertEquals(1_000, tank.amount());
        tank.restore("minecraft:lava", 500);
        assertNull(tank.coolant());
        assertEquals(0, tank.amount());
    }

    @Test
    void exactExtractionEmptiesTankWithoutDuplication() {
        CoolantTankState<String> tank = new CoolantTankState<>(1_000, id -> true);
        tank.fill("ufo:gelid", 1_000, false);

        assertEquals(1_000, tank.extract("ufo:gelid", Long.MAX_VALUE, false));
        assertEquals(0, tank.amount());
        assertNull(tank.coolant());
        assertEquals(0, tank.extract("ufo:gelid", 1, false));
    }

    @Test
    void fillNeverExceedsCapacityOrChargesRejectedRemainder() {
        CoolantTankState<String> tank = new CoolantTankState<>(1_000, id -> true);

        assertEquals(1_000, tank.fill("ufo:stable", 1_500, false));
        assertEquals(1_000, tank.amount());
        assertEquals(0, tank.fill("ufo:stable", 1, false));
        assertEquals(1_000, tank.amount());
    }
}

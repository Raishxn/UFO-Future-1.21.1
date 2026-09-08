package com.raishxn.ufo.wireless;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WirelessBonusTest {
    @Test void idleWidgetKeepsAvailableBonusBetweenShortRecipes() {
        var available = new WirelessBonus(2, .8, .7);
        assertEquals(available, WirelessBonus.display(false, WirelessBonus.NONE, () -> available));
        assertEquals(WirelessBonus.NONE, WirelessBonus.display(true, WirelessBonus.NONE, () -> available));
        assertEquals(available, WirelessBonus.display(true, available, () -> WirelessBonus.NONE));
        assertEquals(WirelessBonus.NONE, WirelessBonus.display(false, available, () -> WirelessBonus.NONE));
    }
    @Test void configuredActiveDmaBonusChangesRecipeCostsNotJustDisplay() {
        var bonus = WirelessBonus.automatic(10, 5, 1, .2, .3);
        assertEquals(50, bonus.duration(100));
        assertEquals(40000, bonus.energyCost(50000));
        assertEquals(70, bonus.duration(100) * bonus.heatPerTick(100, bonus.duration(100)), 1e-12);
    }
    @Test void familyThresholdsGiveMultiblocksEarlierBonuses() {
        var twoMultiblocks = WirelessBonus.automatic(2,4,.2,.1,.15);
        assertEquals(1 + .2 / 3, twoMultiblocks.speed(), 1e-12);
        assertEquals(1 - .1 / 3, twoMultiblocks.energy(), 1e-12);
        assertEquals(.95, twoMultiblocks.heat(), 1e-12);
        assertEquals(new WirelessBonus(1.2,.9,.85), WirelessBonus.automatic(4,4,.2,.1,.15));
        assertEquals(new WirelessBonus(1.2,.9,.85), WirelessBonus.automatic(10,10,.2,.1,.15));
        assertTrue(WirelessBonus.automatic(4,10,.2,.1,.15).speed() < 1.2);
        assertEquals(WirelessBonus.NONE, WirelessBonus.automatic(1,4,.2,.1,.15));
    }
    @Test void oneMachineCannotFarmFactoryBonuses() {
        assertEquals(WirelessBonus.NONE, WirelessBonus.automatic(1,16,.2,.1,.15));
        assertEquals(WirelessBonus.NONE, WirelessBonus.automatic(0,16,.2,.1,.15));
    }
    @Test void factoryGrowthSaturatesWithAllThreeEffects() {
        var ten = WirelessBonus.automatic(10,16,.2,.1,.15);
        assertEquals(1.12, ten.speed(), 1e-12);
        assertEquals(.94, ten.energy(), 1e-12);
        assertEquals(.91, ten.heat(), 1e-12);
        assertEquals(WirelessBonus.automatic(16,16,.2,.1,.15),
                WirelessBonus.automatic(1024,16,.2,.1,.15));
    }
    @Test void speedDoesNotAlsoDiscountTotalEnergyOrHeat() {
        var speed = new WirelessBonus(1.2,1,1);
        int duration = speed.duration(200);
        assertEquals(10000, speed.energyCost(10000));
        assertEquals(200, duration * speed.heatPerTick(200,duration), 1e-12);
        assertTrue(10000.0 / duration > 10000.0 / 200);
    }
    @Test void automaticDiscountsApplyPerRecipeNotJustPerTick() {
        var mixed = WirelessBonus.automatic(16,16,.2,.1,.15);
        assertEquals(9000, mixed.energyCost(10000));
        int ticks = mixed.duration(200);
        assertEquals(200 * .85, ticks * mixed.heatPerTick(200,ticks), 1e-10);
    }
    @Test void oneTickRecipesAndFreeRecipesRemainBounded() {
        var bonus = new WirelessBonus(11,.05,.05);
        assertEquals(1, bonus.duration(1));
        assertEquals(0, bonus.energyCost(0));
        assertEquals(1, bonus.energyCost(1));
        assertEquals(.05, bonus.heatPerTick(1,1));
        assertThrows(IllegalArgumentException.class, () -> new WirelessBonus(Double.NaN,1,1));
    }
}

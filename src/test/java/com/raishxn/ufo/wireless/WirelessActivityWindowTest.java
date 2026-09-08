package com.raishxn.ufo.wireless;

import org.junit.jupiter.api.Test;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class WirelessActivityWindowTest {
    @Test void linkedButIdleMachinesDoNotCount() {
        var activity = new WirelessActivityWindow<Integer>();
        assertFalse(activity.active(1, 100));
        activity.mark(1, 100);
        assertTrue(activity.active(1, 100));
        assertTrue(activity.active(1, 119));
        assertFalse(activity.active(1, 120));
    }
    @Test void repeatedProgressCountsEachMachineOnceAndRefreshesActivity() {
        var activity = new WirelessActivityWindow<Integer>();
        for (int tick = 100; tick < 150; tick++) activity.mark(1, tick);
        assertTrue(activity.active(1, 168));
        assertFalse(activity.active(1, 169));
        assertFalse(activity.active(2, 149));
    }
    @Test void tenProductiveDmasReachConfiguredCapWithoutAnyPatternTicket() {
        var activity = new WirelessActivityWindow<Integer>();
        IntStream.range(0, 10).forEach(id -> activity.mark(id, 100));
        int count = (int) IntStream.range(0, 10).filter(id -> activity.active(id, 101)).count();
        assertEquals(10, count);
        assertEquals(new WirelessBonus(2, .8, .7), WirelessBonus.automatic(count, 5, 1, .2, .3));
        activity.prune(120);
        assertEquals(0, IntStream.range(0, 10).filter(id -> activity.active(id, 120)).count());
    }
    @Test void ReloadOrTimeRollbackDoesNotCreatePhantomActivity() {
        var activity = new WirelessActivityWindow<String>();
        activity.mark("dma", 100);
        assertFalse(activity.active("dma", 99));
        assertFalse(new WirelessActivityWindow<String>().active("dma", 101));
    }
}

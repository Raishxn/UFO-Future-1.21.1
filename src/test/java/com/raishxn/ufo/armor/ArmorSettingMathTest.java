package com.raishxn.ufo.armor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A setting value reaches the server as a {@code VAR_INT} from a client packet, so the clamp is the
 * boundary that decides what a player may actually run with. These tests pin its behaviour at the
 * extremes of the wire type, not only at plausible values.
 */
class ArmorSettingMathTest {
    /** Shapes taken from the real settings: unit steps, coarse steps and a wide range. */
    private static final int[][] SHAPES = {
        {0, 32, 1},      // reach distance, magnet range
        {25, 1000, 25},  // kinetic speed
        {0, 100, 10},    // flight inertia
        {16, 128, 16},   // cloaking range
    };

    @Test
    void valuesBelowTheRangeClampToTheMinimum() {
        for (int[] shape : SHAPES) {
            int min = shape[0];
            assertEquals(min, ArmorSettingMath.clamp(shape[0], shape[1], shape[2], min - 1),
                    () -> "min-1 for " + java.util.Arrays.toString(shape));
            assertEquals(min, ArmorSettingMath.clamp(shape[0], shape[1], shape[2], Integer.MIN_VALUE),
                    () -> "Integer.MIN_VALUE for " + java.util.Arrays.toString(shape));
            assertEquals(min, ArmorSettingMath.clamp(shape[0], shape[1], shape[2], Integer.MIN_VALUE + 1),
                    () -> "Integer.MIN_VALUE+1 for " + java.util.Arrays.toString(shape));
            assertEquals(min, ArmorSettingMath.clamp(shape[0], shape[1], shape[2], -Integer.MAX_VALUE),
                    () -> "-Integer.MAX_VALUE for " + java.util.Arrays.toString(shape));
        }
    }

    @Test
    void valuesAboveTheRangeClampToTheMaximum() {
        for (int[] shape : SHAPES) {
            int max = shape[1];
            assertEquals(max, ArmorSettingMath.clamp(shape[0], shape[1], shape[2], max + 1),
                    () -> "max+1 for " + java.util.Arrays.toString(shape));
            assertEquals(max, ArmorSettingMath.clamp(shape[0], shape[1], shape[2], Integer.MAX_VALUE),
                    () -> "Integer.MAX_VALUE for " + java.util.Arrays.toString(shape));
            assertEquals(max, ArmorSettingMath.clamp(shape[0], shape[1], shape[2], Integer.MAX_VALUE - 1),
                    () -> "Integer.MAX_VALUE-1 for " + java.util.Arrays.toString(shape));
        }
    }

    @Test
    void inRangeValuesSurviveAndOffGridValuesSnap() {
        for (int[] shape : SHAPES) {
            int min = shape[0], max = shape[1], step = shape[2];
            for (int value = min; value <= max; value += step) {
                final int expected = value;
                assertEquals(expected, ArmorSettingMath.clamp(min, max, step, expected),
                        () -> "on-grid value " + expected + " changed for " + java.util.Arrays.toString(shape));
            }
            if (step > 1 && max - min >= step) {
                assertEquals(min, ArmorSettingMath.clamp(min, max, step, min + 1),
                        () -> "off-grid value must snap down for " + java.util.Arrays.toString(shape));
                assertEquals(max, ArmorSettingMath.clamp(min, max, step, max - 1),
                        () -> "off-grid value must snap up for " + java.util.Arrays.toString(shape));
            }
        }
    }

    @Test
    void aDegenerateGridIsRefusedRatherThanGuessed() {
        assertThrows(IllegalArgumentException.class, () -> ArmorSettingMath.clamp(0, 10, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> ArmorSettingMath.clamp(0, 10, -1, 5));
        assertThrows(IllegalArgumentException.class, () -> ArmorSettingMath.clamp(10, 0, 1, 5));
        assertThrows(IllegalArgumentException.class, () -> ArmorSettingMath.clamp(0, 10, 3, 5));
    }
}

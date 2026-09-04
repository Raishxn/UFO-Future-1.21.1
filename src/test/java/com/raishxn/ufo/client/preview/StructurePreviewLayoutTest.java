package com.raishxn.ufo.client.preview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructurePreviewLayoutTest {
    @Test
    void shellDetectionKeepsInteriorCellsVisible() {
        assertTrue(StructurePreviewLayout.isShell(0, 2, 2, 5, 5, 5));
        assertTrue(StructurePreviewLayout.isShell(2, 4, 2, 5, 5, 5));
        assertFalse(StructurePreviewLayout.isShell(2, 2, 2, 5, 5, 5));
        assertFalse(StructurePreviewLayout.isShell(0, 0, 0, 0, 5, 5));
    }

    @Test
    void layerModesHaveDistinctVisibilityContracts() {
        assertTrue(StructurePreviewLayout.isVisible(4, 1, StructurePreviewLayout.LayerMode.FULL));
        assertTrue(StructurePreviewLayout.isVisible(1, 2, StructurePreviewLayout.LayerMode.UP_TO));
        assertFalse(StructurePreviewLayout.isVisible(3, 2, StructurePreviewLayout.LayerMode.UP_TO));
        assertTrue(StructurePreviewLayout.isVisible(2, 2, StructurePreviewLayout.LayerMode.SINGLE));
        assertFalse(StructurePreviewLayout.isVisible(1, 2, StructurePreviewLayout.LayerMode.SINGLE));
    }
}

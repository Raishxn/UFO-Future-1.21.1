package com.raishxn.ufo.client.ctm;

import org.junit.jupiter.api.Test;

import static com.raishxn.ufo.client.ctm.CtmFaceGeometry.DOWN;
import static com.raishxn.ufo.client.ctm.CtmFaceGeometry.LEFT;
import static com.raishxn.ufo.client.ctm.CtmFaceGeometry.RIGHT;
import static com.raishxn.ufo.client.ctm.CtmFaceGeometry.UP;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CtmTileSelectorTest {
    @Test
    void allEdgeMasksSelectTheDocumentedTileForEveryQuadrant() {
        for (CtmTileSelector.Quadrant quadrant : CtmTileSelector.Quadrant.values()) {
            int firstEdge = firstEdge(quadrant);
            int secondEdge = secondEdge(quadrant);
            for (int edgeMask = 0; edgeMask < 16; edgeMask++) {
                assertEquals(expected(quadrant, edgeMask, false),
                        CtmTileSelector.select(quadrant, edgeMask, 0),
                        quadrant + " edge mask " + edgeMask + " without diagonal");
                assertEquals(expected(quadrant, edgeMask, true),
                        CtmTileSelector.select(quadrant, edgeMask, 1 << quadrant.ordinal()),
                        quadrant + " edge mask " + edgeMask + " with diagonal");

                boolean first = has(edgeMask, firstEdge);
                boolean second = has(edgeMask, secondEdge);
                if (!(first && second)) {
                    assertEquals(
                            CtmTileSelector.select(quadrant, edgeMask, 0),
                            CtmTileSelector.select(quadrant, edgeMask, 1 << quadrant.ordinal()),
                            "A diagonal must matter only when both incident edges connect");
                }
            }
        }
    }

    @Test
    void faceQuarterCoordinatesMapToQuadrants() {
        assertEquals(CtmTileSelector.Quadrant.TOP_LEFT, CtmTileSelector.quadrant(0, 0));
        assertEquals(CtmTileSelector.Quadrant.TOP_RIGHT, CtmTileSelector.quadrant(1, 0));
        assertEquals(CtmTileSelector.Quadrant.BOTTOM_LEFT, CtmTileSelector.quadrant(0, 1));
        assertEquals(CtmTileSelector.Quadrant.BOTTOM_RIGHT, CtmTileSelector.quadrant(1, 1));
    }

    private static CtmTileSelector.Tile expected(CtmTileSelector.Quadrant quadrant,
            int edgeMask, boolean diagonal) {
        boolean first = has(edgeMask, firstEdge(quadrant));
        boolean second = has(edgeMask, secondEdge(quadrant));
        if (!first && !second) {
            return baseTile(quadrant);
        }
        if (first && second) {
            return diagonal ? interiorTile(quadrant) : missingDiagonalTile(quadrant);
        }
        return first ? firstOnlyTile(quadrant) : secondOnlyTile(quadrant);
    }

    private static int firstEdge(CtmTileSelector.Quadrant quadrant) {
        return switch (quadrant) {
            case TOP_LEFT, TOP_RIGHT -> UP;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> DOWN;
        };
    }

    private static int secondEdge(CtmTileSelector.Quadrant quadrant) {
        return switch (quadrant) {
            case TOP_LEFT, BOTTOM_LEFT -> LEFT;
            case TOP_RIGHT, BOTTOM_RIGHT -> RIGHT;
        };
    }

    private static CtmTileSelector.Tile baseTile(CtmTileSelector.Quadrant quadrant) {
        return switch (quadrant) {
            case TOP_LEFT -> tile(CtmTileSelector.Source.BASE, 0, 0);
            case TOP_RIGHT -> tile(CtmTileSelector.Source.BASE, 1, 0);
            case BOTTOM_RIGHT -> tile(CtmTileSelector.Source.BASE, 1, 1);
            case BOTTOM_LEFT -> tile(CtmTileSelector.Source.BASE, 0, 1);
        };
    }

    private static CtmTileSelector.Tile interiorTile(CtmTileSelector.Quadrant quadrant) {
        return switch (quadrant) {
            case TOP_LEFT -> tile(CtmTileSelector.Source.CTM, 0, 0);
            case TOP_RIGHT -> tile(CtmTileSelector.Source.CTM, 1, 0);
            case BOTTOM_RIGHT -> tile(CtmTileSelector.Source.CTM, 1, 1);
            case BOTTOM_LEFT -> tile(CtmTileSelector.Source.CTM, 0, 1);
        };
    }

    private static CtmTileSelector.Tile firstOnlyTile(CtmTileSelector.Quadrant quadrant) {
        return switch (quadrant) {
            case TOP_LEFT -> tile(CtmTileSelector.Source.CTM, 2, 0);
            case TOP_RIGHT -> tile(CtmTileSelector.Source.CTM, 3, 0);
            case BOTTOM_RIGHT -> tile(CtmTileSelector.Source.CTM, 3, 1);
            case BOTTOM_LEFT -> tile(CtmTileSelector.Source.CTM, 2, 1);
        };
    }

    private static CtmTileSelector.Tile secondOnlyTile(CtmTileSelector.Quadrant quadrant) {
        return switch (quadrant) {
            case TOP_LEFT -> tile(CtmTileSelector.Source.CTM, 0, 2);
            case TOP_RIGHT -> tile(CtmTileSelector.Source.CTM, 1, 2);
            case BOTTOM_RIGHT -> tile(CtmTileSelector.Source.CTM, 1, 3);
            case BOTTOM_LEFT -> tile(CtmTileSelector.Source.CTM, 0, 3);
        };
    }

    private static CtmTileSelector.Tile missingDiagonalTile(CtmTileSelector.Quadrant quadrant) {
        return switch (quadrant) {
            case TOP_LEFT -> tile(CtmTileSelector.Source.CTM, 2, 2);
            case TOP_RIGHT -> tile(CtmTileSelector.Source.CTM, 3, 2);
            case BOTTOM_RIGHT -> tile(CtmTileSelector.Source.CTM, 3, 3);
            case BOTTOM_LEFT -> tile(CtmTileSelector.Source.CTM, 2, 3);
        };
    }

    private static CtmTileSelector.Tile tile(CtmTileSelector.Source source, int x, int y) {
        return new CtmTileSelector.Tile(source, x, y);
    }

    private static boolean has(int mask, int edge) {
        return (mask & 1 << edge) != 0;
    }
}

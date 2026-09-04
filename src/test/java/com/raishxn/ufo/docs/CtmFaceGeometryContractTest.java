package com.raishxn.ufo.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CtmFaceGeometryContractTest {
    private static final Path GEOMETRY = Path.of(
            "src/main/java/com/raishxn/ufo/client/ctm/CtmFaceGeometry.java");

    @Test
    void allSixFaceSpaceOrientationTablesRemainExplicit() throws IOException {
        String source = Files.readString(GEOMETRY).replaceAll("\\s+", " ");
        for (String face : List.of("DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST")) {
            assertTrue(source.contains("result.put(Direction." + face + ", new Direction[]"),
                    "Missing face-space orientation for " + face);
        }
        assertTrue(source.contains("Direction.DOWN, new Direction[]{Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST}"));
        assertTrue(source.contains("Direction.UP, new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST}"));
        assertTrue(source.contains("Direction.NORTH, new Direction[]{Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST}"));
        assertTrue(source.contains("Direction.SOUTH, new Direction[]{Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST}"));
        assertTrue(source.contains("Direction.WEST, new Direction[]{Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH}"));
        assertTrue(source.contains("Direction.EAST, new Direction[]{Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH}"));
    }

    @Test
    void allFourFacePlaneDiagonalsRemainMappedToTheirIncidentEdges() throws IOException {
        String source = Files.readString(GEOMETRY).replaceAll("\\s+", " ");
        assertTrue(source.contains("case TOP_LEFT -> pos.relative(neighbourDirection(face, UP)).relative(neighbourDirection(face, LEFT))"));
        assertTrue(source.contains("case TOP_RIGHT -> pos.relative(neighbourDirection(face, UP)).relative(neighbourDirection(face, RIGHT))"));
        assertTrue(source.contains("case BOTTOM_RIGHT -> pos.relative(neighbourDirection(face, DOWN)).relative(neighbourDirection(face, RIGHT))"));
        assertTrue(source.contains("case BOTTOM_LEFT -> pos.relative(neighbourDirection(face, DOWN)).relative(neighbourDirection(face, LEFT))"));
    }

    @Test
    void customVerticesRemainOpaqueWhiteInsteadOfOneOfTwoHundredFiftyFive() throws IOException {
        String source = Files.readString(GEOMETRY).replaceAll("\\s+", " ");
        assertTrue(source.contains("builder.setColor(255, 255, 255, 255)"),
                "QuadBakingVertexConsumer uses integer 0..255 color channels");
    }
}

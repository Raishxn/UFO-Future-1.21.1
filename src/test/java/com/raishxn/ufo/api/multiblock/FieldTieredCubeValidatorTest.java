package com.raishxn.ufo.api.multiblock;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure coverage of the cube scan core: canonical origin selection, preferred-origin
 * confirmation, fail-fast read budget and tier/unloaded handling.
 */
class FieldTieredCubeValidatorTest {

    private static final int SHELL_CELLS = 343 - 125;

    /**
     * Builds a probe whose world contains valid cubes at exactly the given origins:
     * interior cells resolve to the tier field, shell cells to an accepting shell,
     * and every other loaded position is an invalid cell.
     */
    private static CountingProbe probeFor(Set<FieldTieredCubeValidator.CubePos> validOrigins, int tier) {
        return new CountingProbe(pos -> {
            for (FieldTieredCubeValidator.CubePos origin : validOrigins) {
                if (!FieldTieredCubeValidator.containsCell(origin, pos)) {
                    continue;
                }
                int dx = pos.x() - origin.x();
                int dy = pos.y() - origin.y();
                int dz = pos.z() - origin.z();
                boolean interior = dx >= 1 && dx < 6 && dy >= 1 && dy < 6 && dz >= 1 && dz < 6;
                return interior ? FieldTieredCubeValidator.Cell.field(tier) : FieldTieredCubeValidator.Cell.shell(true);
            }
            return FieldTieredCubeValidator.Cell.shell(false);
        });
    }

    private record CountingProbe(FieldTieredCubeValidator.CubeProbe delegate, AtomicInteger reads)
            implements FieldTieredCubeValidator.CubeProbe {
        private CountingProbe(FieldTieredCubeValidator.CubeProbe delegate) {
            this(delegate, new AtomicInteger());
        }

        @Override
        public FieldTieredCubeValidator.Cell at(FieldTieredCubeValidator.CubePos pos) {
            reads().incrementAndGet();
            return delegate().at(pos);
        }
    }

    @Test
    void fullCubeScanMaterializesShellInteriorAndTier() {
        var origin = new FieldTieredCubeValidator.CubePos(100, 50, -70);
        CountingProbe probe = probeFor(Set.of(origin), 2);

        var result = FieldTieredCubeValidator.scanAt(probe, origin, origin.offset(3, 3, 3));

        assertTrue(result.valid());
        assertEquals(2, result.machineTier());
        assertEquals(SHELL_CELLS, result.shellPositions().size());
        assertEquals(125, result.interiorPositions().size());
        assertTrue(result.shellPositions().contains(origin));
        assertFalse(result.shellPositions().contains(origin.offset(3, 3, 3)));
        assertTrue(result.interiorPositions().contains(origin.offset(3, 3, 3)));
        assertFalse(result.hasUnloadedPositions());
    }

    @Test
    void intactPreferredOriginConfirmsWithoutExhaustiveSearch() {
        var origin = new FieldTieredCubeValidator.CubePos(10, 10, 10);
        CountingProbe probe = probeFor(Set.of(origin), 1);
        var clicked = origin.offset(2, 5, 4);

        Optional<FieldTieredCubeValidator.CubePos> found =
                FieldTieredCubeValidator.findCanonicalOrigin(probe, clicked, origin);

        assertTrue(found.isPresent());
        assertEquals(origin, found.get());
        assertEquals(343, probe.reads().get(), "anchor confirmation must cost exactly one 7x7x7 pass");
    }

    @Test
    void stalePreferredOriginFallsBackToCanonicalSearch() {
        var realOrigin = new FieldTieredCubeValidator.CubePos(10, 10, 10);
        var staleAnchor = new FieldTieredCubeValidator.CubePos(40, 40, 40);
        CountingProbe probe = probeFor(Set.of(realOrigin), 3);
        var clicked = realOrigin.offset(2, 5, 4);

        Optional<FieldTieredCubeValidator.CubePos> found =
                FieldTieredCubeValidator.findCanonicalOrigin(probe, clicked, staleAnchor);

        assertTrue(found.isPresent());
        assertEquals(realOrigin, found.get());
    }

    @Test
    void canonicalOriginPrefersLowestYThenZThenX() {
        // Two overlapping valid cubes: A is first in X-outer scan order, B wins the
        // canonical (Y, Z, X) comparison because its Y is lower. The world is defined
        // analytically so the overlap band classifies consistently for both cubes.
        var a = new FieldTieredCubeValidator.CubePos(100, 101, 100);
        var b = new FieldTieredCubeValidator.CubePos(101, 100, 100);
        FieldTieredCubeValidator.CubeProbe probe = pos -> {
            boolean fieldA = pos.x() >= 101 && pos.x() <= 105 && pos.y() >= 102 && pos.y() <= 106
                    && pos.z() >= 101 && pos.z() <= 105;
            boolean fieldB = pos.x() >= 102 && pos.x() <= 106 && pos.y() >= 101 && pos.y() <= 105
                    && pos.z() >= 101 && pos.z() <= 105;
            if (fieldA || fieldB) {
                // Shared cells must satisfy both roles: interior tier for one cube and
                // an accepting shell for the neighbor cube.
                return new FieldTieredCubeValidator.Cell(true, 1, true);
            }
            boolean inA = pos.x() >= 100 && pos.x() <= 106 && pos.y() >= 101 && pos.y() <= 107
                    && pos.z() >= 100 && pos.z() <= 106;
            boolean inB = pos.x() >= 101 && pos.x() <= 107 && pos.y() >= 100 && pos.y() <= 106
                    && pos.z() >= 100 && pos.z() <= 106;
            if (inA || inB) {
                return FieldTieredCubeValidator.Cell.shell(true);
            }
            return FieldTieredCubeValidator.Cell.shell(false);
        };
        var clicked = new FieldTieredCubeValidator.CubePos(104, 104, 104);

        Optional<FieldTieredCubeValidator.CubePos> found =
                FieldTieredCubeValidator.findCanonicalOrigin(probe, clicked, null);

        assertTrue(found.isPresent());
        assertEquals(b, found.get());
    }

    @Test
    void failFastSearchReadsOneCellPerCandidateInEmptyWorld() {
        // Every loaded position rejects the shell predicate: each of the 343 candidate
        // origins must abort on its first cell instead of scanning 343 cells.
        var clicked = new FieldTieredCubeValidator.CubePos(0, 0, 0);
        CountingProbe probe = probeFor(Set.of(), 1);

        Optional<FieldTieredCubeValidator.CubePos> found =
                FieldTieredCubeValidator.findCanonicalOrigin(probe, clicked, null);

        assertTrue(found.isEmpty());
        assertEquals(343, probe.reads().get());
    }

    @Test
    void unloadedCellInvalidatesCubeAndIsReported() {
        var origin = new FieldTieredCubeValidator.CubePos(0, 0, 0);
        var unloadedCell = origin.offset(2, 2, 2);
        var origins = Set.of(origin);
        FieldTieredCubeValidator.CubeProbe probe = pos -> {
            if (pos.equals(unloadedCell)) {
                return FieldTieredCubeValidator.Cell.unloaded();
            }
            return probeFor(origins, 1).at(pos);
        };

        var result = FieldTieredCubeValidator.scanAt(probe, origin, origin.offset(3, 3, 3));

        assertFalse(result.valid());
        assertTrue(result.hasUnloadedPositions());
        assertTrue(FieldTieredCubeValidator.findCanonicalOrigin(probe, origin.offset(3, 3, 3), null).isEmpty());
    }

    @Test
    void mixedInteriorTiersInvalidateCube() {
        var origin = new FieldTieredCubeValidator.CubePos(0, 0, 0);
        FieldTieredCubeValidator.CubeProbe probe = pos -> {
            if (!FieldTieredCubeValidator.containsCell(origin, pos)) {
                return FieldTieredCubeValidator.Cell.shell(false);
            }
            int dy = pos.y() - origin.y();
            boolean interior = dy >= 1 && dy < 6
                    && pos.x() - origin.x() >= 1 && pos.x() - origin.x() < 6
                    && pos.z() - origin.z() >= 1 && pos.z() - origin.z() < 6;
            if (!interior) {
                return FieldTieredCubeValidator.Cell.shell(true);
            }
            return FieldTieredCubeValidator.Cell.field(dy >= 4 ? 2 : 1);
        };

        var result = FieldTieredCubeValidator.scanAt(probe, origin, origin.offset(3, 3, 3));

        assertFalse(result.valid());
    }

    @Test
    void missingInteriorFieldInvalidatesCube() {
        var origin = new FieldTieredCubeValidator.CubePos(0, 0, 0);
        var base = probeFor(Set.of(origin), 0);

        var result = FieldTieredCubeValidator.scanAt(base, origin, origin.offset(3, 3, 3));

        assertFalse(result.valid());
    }
}

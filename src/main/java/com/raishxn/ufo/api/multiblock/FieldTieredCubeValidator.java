package com.raishxn.ufo.api.multiblock;

import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Helper for controller-less cubic multiblocks whose real tier is defined by a fully
 * filled interior made of a single field tier.
 * <p>
 * The scan core is pure and operates on {@link CubeProbe} cells over {@link CubePos}
 * coordinates, so ordering, fail-fast and canonical-origin behavior are unit-testable
 * without a Minecraft classpath. The level-backed entry points adapt blocks and shell
 * predicates to cells.
 * <p>
 * Callers should always pass the structure's last known origin as
 * {@code preferredOrigin}: an intact cube is then confirmed with a single
 * 7x7x7 pass instead of testing all 343 candidate origins. The exhaustive
 * search remains as fallback, but each candidate now aborts at the first
 * invalid cell instead of running a full diagnostic.
 */
public final class FieldTieredCubeValidator {

    public static final int OUTER_SIZE = 7;
    public static final int INNER_SIZE = 5;
    public static final int OUTER_RADIUS = OUTER_SIZE - 1;
    public static final int INNER_OFFSET = 1;
    public static final int BLOCK_TESTS_PER_FULL_SEARCH = OUTER_SIZE * OUTER_SIZE * OUTER_SIZE
            * OUTER_SIZE * OUTER_SIZE * OUTER_SIZE;

    private FieldTieredCubeValidator() {
    }

    @FunctionalInterface
    public interface ShellPredicate {
        boolean test(BlockState state, Level level, BlockPos pos);
    }

    /**
     * Cell view used by the pure scan core. {@code fieldTier} is only consulted
     * for interior cells; {@code shellOk} only for shell cells.
     */
    public interface CubeProbe {
        Cell at(CubePos pos);
    }

    /**
     * @param loaded    false means the position is in an unloaded chunk
     * @param fieldTier 0 marks an invalid interior field block
     * @param shellOk   whether the shell predicate accepts the block
     */
    public record Cell(boolean loaded, int fieldTier, boolean shellOk) {
        public static Cell unloaded() {
            return new Cell(false, 0, false);
        }

        public static Cell field(int tier) {
            return new Cell(true, tier, false);
        }

        public static Cell shell(boolean ok) {
            return new Cell(true, 0, ok);
        }
    }

    /**
     * Minimal MC-free position used by the pure scan core.
     */
    public record CubePos(int x, int y, int z) {
        public static CubePos of(BlockPos pos) {
            return new CubePos(pos.getX(), pos.getY(), pos.getZ());
        }

        public BlockPos toBlockPos() {
            return new BlockPos(this.x, this.y, this.z);
        }

        public CubePos offset(int dx, int dy, int dz) {
            return new CubePos(this.x + dx, this.y + dy, this.z + dz);
        }
    }

    /**
     * Pure scan result; the level-backed API maps it to {@link ValidationResult}.
     */
    record CubeScanResult(
            boolean valid,
            CubePos origin,
            CubePos clickedPos,
            int machineTier,
            boolean hasUnloadedPositions,
            List<CubePos> shellPositions,
            List<CubePos> interiorPositions) {
    }

    public record ValidationResult(
            boolean valid,
            BlockPos origin,
            BlockPos minCorner,
            BlockPos maxCorner,
            BlockPos clickedPos,
            int machineTier,
            boolean hasUnloadedPositions,
            List<BlockPos> shellPositions,
            List<BlockPos> interiorPositions) {
    }

    /**
     * Finds a valid cube containing {@code clickedPos}. The canonical origin is the minimum corner
     * of the outer 7x7x7 cube, selected by lowest Y, then Z, then X.
     */
    public static Optional<ValidationResult> findMatchingCube(Level level, BlockPos clickedPos, ShellPredicate shellPredicate) {
        return findMatchingCube(level, clickedPos, shellPredicate, null);
    }

    /**
     * Finds a valid cube containing {@code clickedPos}, first confirming the cube at
     * {@code preferredOrigin} (the structure's cached anchor) when it contains the position.
     */
    public static Optional<ValidationResult> findMatchingCube(Level level, BlockPos clickedPos, ShellPredicate shellPredicate,
            @Nullable BlockPos preferredOrigin) {
        CubeProbe probe = levelProbe(level, shellPredicate);
        CubePos clicked = CubePos.of(clickedPos);
        CubePos preferred = preferredOrigin != null ? CubePos.of(preferredOrigin) : null;
        return findCanonicalOrigin(probe, clicked, preferred)
                .map(origin -> toValidationResult(scanAt(probe, origin, clicked)));
    }

    /**
     * Full diagnostic scan used for UI/scanner and for materializing the winning cube.
     * Collects every position and keeps scanning after failures.
     */
    public static ValidationResult validateAt(Level level, BlockPos origin, BlockPos clickedPos, ShellPredicate shellPredicate) {
        CubeProbe probe = levelProbe(level, shellPredicate);
        return toValidationResult(scanAt(probe, CubePos.of(origin), CubePos.of(clickedPos)));
    }

    public static boolean contains(BlockPos origin, BlockPos pos) {
        return pos.getX() >= origin.getX() && pos.getX() < origin.getX() + OUTER_SIZE
                && pos.getY() >= origin.getY() && pos.getY() < origin.getY() + OUTER_SIZE
                && pos.getZ() >= origin.getZ() && pos.getZ() < origin.getZ() + OUTER_SIZE;
    }

    public static boolean containsCell(CubePos origin, CubePos pos) {
        return pos.x() >= origin.x() && pos.x() < origin.x() + OUTER_SIZE
                && pos.y() >= origin.y() && pos.y() < origin.y() + OUTER_SIZE
                && pos.z() >= origin.z() && pos.z() < origin.z() + OUTER_SIZE;
    }

    public static int resolveFieldTier(BlockState state) {
        if (state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get())) {
            return MultiblockMachineTier.MK1.level();
        }
        if (state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get())) {
            return MultiblockMachineTier.MK2.level();
        }
        if (state.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get())) {
            return MultiblockMachineTier.MK3.level();
        }
        return 0;
    }

    private static CubeProbe levelProbe(Level level, ShellPredicate shellPredicate) {
        return pos -> {
            BlockPos blockPos = pos.toBlockPos();
            if (!level.isLoaded(blockPos)) {
                return Cell.unloaded();
            }
            BlockState state = level.getBlockState(blockPos);
            return new Cell(true, resolveFieldTier(state), shellPredicate.test(state, level, blockPos));
        };
    }

    /**
     * Pure origin search: confirms {@code preferredOrigin} first when it contains the
     * clicked position, otherwise tests every candidate origin with a fail-fast scan
     * and returns the canonical one (lowest Y, then Z, then X).
     */
    static Optional<CubePos> findCanonicalOrigin(CubeProbe probe, CubePos clickedPos, @Nullable CubePos preferredOrigin) {
        if (preferredOrigin != null && containsCell(preferredOrigin, clickedPos)
                && scansValid(probe, preferredOrigin)) {
            return Optional.of(preferredOrigin);
        }

        CubePos bestOrigin = null;
        for (int originX = clickedPos.x() - OUTER_RADIUS; originX <= clickedPos.x(); originX++) {
            for (int originY = clickedPos.y() - OUTER_RADIUS; originY <= clickedPos.y(); originY++) {
                for (int originZ = clickedPos.z() - OUTER_RADIUS; originZ <= clickedPos.z(); originZ++) {
                    CubePos origin = new CubePos(originX, originY, originZ);
                    if (!scansValid(probe, origin)) {
                        continue;
                    }
                    if (bestOrigin == null || compareOrigins(origin, bestOrigin) < 0) {
                        bestOrigin = origin;
                    }
                }
            }
        }
        return Optional.ofNullable(bestOrigin);
    }

    /**
     * Fail-fast validity check: stops at the first unloaded, invalid-field or
     * rejected-shell cell. Interior tier homogeneity is enforced along the way.
     */
    private static boolean scansValid(CubeProbe probe, CubePos origin) {
        Integer detectedTier = null;
        for (int x = 0; x < OUTER_SIZE; x++) {
            for (int y = 0; y < OUTER_SIZE; y++) {
                for (int z = 0; z < OUTER_SIZE; z++) {
                    Cell cell = probe.at(origin.offset(x, y, z));
                    if (!cell.loaded()) {
                        return false;
                    }
                    if (isInteriorCell(x, y, z)) {
                        if (cell.fieldTier() == 0) {
                            return false;
                        }
                        if (detectedTier == null) {
                            detectedTier = cell.fieldTier();
                        } else if (detectedTier != cell.fieldTier()) {
                            return false;
                        }
                    } else if (!cell.shellOk()) {
                        return false;
                    }
                }
            }
        }
        return detectedTier != null;
    }

    /**
     * Full diagnostic scan of a single cube. Result positions are always materialized;
     * {@code valid} aggregates every failure instead of aborting.
     */
    static CubeScanResult scanAt(CubeProbe probe, CubePos origin, CubePos clickedPos) {
        List<CubePos> shellPositions = new ArrayList<>(OUTER_SIZE * OUTER_SIZE * OUTER_SIZE - INNER_SIZE * INNER_SIZE * INNER_SIZE);
        List<CubePos> interiorPositions = new ArrayList<>(INNER_SIZE * INNER_SIZE * INNER_SIZE);
        boolean hasUnloadedPositions = false;

        Integer detectedTier = null;
        boolean valid = true;

        for (int x = 0; x < OUTER_SIZE; x++) {
            for (int y = 0; y < OUTER_SIZE; y++) {
                for (int z = 0; z < OUTER_SIZE; z++) {
                    CubePos currentPos = origin.offset(x, y, z);
                    boolean isInterior = isInteriorCell(x, y, z);

                    Cell cell = probe.at(currentPos);
                    if (!cell.loaded()) {
                        valid = false;
                        hasUnloadedPositions = true;
                        continue;
                    }

                    if (isInterior) {
                        interiorPositions.add(currentPos);
                        int fieldTier = cell.fieldTier();
                        if (fieldTier == 0) {
                            valid = false;
                            continue;
                        }

                        if (detectedTier == null) {
                            detectedTier = fieldTier;
                        } else if (detectedTier != fieldTier) {
                            valid = false;
                        }
                    } else {
                        shellPositions.add(currentPos);
                        if (!cell.shellOk()) {
                            valid = false;
                        }
                    }
                }
            }
        }

        if (detectedTier == null) {
            valid = false;
            detectedTier = MultiblockMachineTier.MK1.level();
        }

        return new CubeScanResult(
                valid,
                origin,
                clickedPos,
                detectedTier,
                hasUnloadedPositions,
                Collections.unmodifiableList(shellPositions),
                Collections.unmodifiableList(interiorPositions));
    }

    private static ValidationResult toValidationResult(CubeScanResult result) {
        BlockPos origin = result.origin().toBlockPos();
        List<BlockPos> shell = new ArrayList<>(result.shellPositions().size());
        for (CubePos pos : result.shellPositions()) {
            shell.add(pos.toBlockPos());
        }
        List<BlockPos> interior = new ArrayList<>(result.interiorPositions().size());
        for (CubePos pos : result.interiorPositions()) {
            interior.add(pos.toBlockPos());
        }
        return new ValidationResult(
                result.valid(),
                origin,
                origin,
                origin.offset(OUTER_SIZE - 1, OUTER_SIZE - 1, OUTER_SIZE - 1),
                result.clickedPos().toBlockPos(),
                result.machineTier(),
                result.hasUnloadedPositions(),
                Collections.unmodifiableList(shell),
                Collections.unmodifiableList(interior));
    }

    private static boolean isInteriorCell(int x, int y, int z) {
        return x >= INNER_OFFSET && x < OUTER_SIZE - INNER_OFFSET
                && y >= INNER_OFFSET && y < OUTER_SIZE - INNER_OFFSET
                && z >= INNER_OFFSET && z < OUTER_SIZE - INNER_OFFSET;
    }

    private static int compareOrigins(CubePos a, CubePos b) {
        if (a.y() != b.y()) {
            return Integer.compare(a.y(), b.y());
        }
        if (a.z() != b.z()) {
            return Integer.compare(a.z(), b.z());
        }
        return Integer.compare(a.x(), b.x());
    }
}

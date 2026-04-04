package com.raishxn.ufo.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/**
 * A reusable 3D pattern matcher for multiblock structures.
 * <p>
 * The pattern is defined as a 3D char array (layer × row × column) together with
 * a legend map that binds each char to a predicate (block or tag check).
 * The controller position within the pattern is marked by a special character
 * (default {@code 'C'}).
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * MultiblockPattern pattern = new MultiblockPattern.Builder()
 *     .layer(new String[]{
 *         "SSS",
 *         "SCS",
 *         "SSS"
 *     })
 *     .layer(new String[]{
 *         "SSS",
 *         "S S",
 *         "SSS"
 *     })
 *     .layer(new String[]{
 *         "SSS",
 *         "SSS",
 *         "SSS"
 *     })
 *     .where('S', block -> block instanceof MyCasingBlock)
 *     .where(' ', block -> true) // air / anything
 *     .build();
 * }</pre>
 */
public class MultiblockPattern {

    /** Functional interface used to test whether a block satisfies a pattern slot. */
    @FunctionalInterface
    public interface BlockPredicate {
        boolean test(BlockState state, Level level, BlockPos pos);
    }

    private final char[][][] pattern;      // [layer][row][col]
    private final Map<Character, BlockPredicate> legend;
    private final int controllerLayer;
    private final int controllerRow;
    private final int controllerCol;

    private MultiblockPattern(char[][][] pattern, Map<Character, BlockPredicate> legend, char controllerChar) {
        this.pattern = pattern;
        this.legend = legend;

        // Locate controller position in the pattern
        int cLayer = -1, cRow = -1, cCol = -1;
        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {
                    if (pattern[y][z][x] == controllerChar) {
                        cLayer = y;
                        cRow = z;
                        cCol = x;
                    }
                }
            }
        }
        if (cLayer == -1) {
            throw new IllegalArgumentException("Controller char '" + controllerChar + "' not found in pattern!");
        }
        this.controllerLayer = cLayer;
        this.controllerRow = cRow;
        this.controllerCol = cCol;
    }

    /**
     * Validates the multiblock structure by testing the world against the pattern,
     * centered on the given controller world position.
     *
     * @param level         the level to check
     * @param controllerPos the world position of the controller block
     * @return a {@link MatchResult} containing whether the structure matched and which positions are parts
     */
    public MatchResult match(Level level, BlockPos controllerPos) {
        List<BlockPos> partPositions = new ArrayList<>();
        boolean valid = true;

        for (int y = 0; y < pattern.length && valid; y++) {
            for (int z = 0; z < pattern[y].length && valid; z++) {
                for (int x = 0; x < pattern[y][z].length && valid; x++) {
                    char c = pattern[y][z][x];

                    // Calculate world offset from controller
                    int offsetX = x - controllerCol;
                    int offsetY = y - controllerLayer;
                    int offsetZ = z - controllerRow;

                    BlockPos worldPos = controllerPos.offset(offsetX, offsetY, offsetZ);

                    // Skip the controller position itself
                    if (worldPos.equals(controllerPos)) {
                        continue;
                    }

                    BlockPredicate predicate = legend.get(c);
                    if (predicate == null) {
                        // Unknown char in pattern ⇒ treat as "anything"
                        continue;
                    }

                    if (!level.isLoaded(worldPos)) {
                        valid = false;
                        break;
                    }

                    BlockState state = level.getBlockState(worldPos);
                    if (!predicate.test(state, level, worldPos)) {
                        valid = false;
                    } else {
                        partPositions.add(worldPos);
                    }
                }
            }
        }

        return new MatchResult(valid, valid ? Collections.unmodifiableList(partPositions) : Collections.emptyList());
    }

    /**
     * Result of a pattern match attempt.
     */
    public record MatchResult(boolean isValid, List<BlockPos> partPositions) {}

    // ──────────────────────── Builder ────────────────────────

    public static class Builder {
        private final List<String[]> layers = new ArrayList<>();
        private final Map<Character, BlockPredicate> legend = new HashMap<>();
        private char controllerChar = 'C';

        /**
         * Adds a horizontal layer to the pattern (bottom to top).
         * Each string represents a row (north to south); each char a column (west to east).
         */
        public Builder layer(String[] rows) {
            this.layers.add(rows);
            return this;
        }

        /**
         * Defines what block a character in the pattern maps to.
         */
        public Builder where(char c, BlockPredicate predicate) {
            this.legend.put(c, predicate);
            return this;
        }

        /**
         * Convenience: maps a char to a specific block class.
         */
        public Builder where(char c, Block block) {
            return where(c, (state, level, pos) -> state.is(block));
        }

        /**
         * Sets the character that represents the controller in the pattern.
         * Defaults to {@code 'C'}.
         */
        public Builder controllerChar(char c) {
            this.controllerChar = c;
            return this;
        }

        public MultiblockPattern build() {
            // Convert List<String[]> → char[][][]
            char[][][] patternArray = new char[layers.size()][][];
            for (int y = 0; y < layers.size(); y++) {
                String[] rows = layers.get(y);
                patternArray[y] = new char[rows.length][];
                for (int z = 0; z < rows.length; z++) {
                    patternArray[y][z] = rows[z].toCharArray();
                }
            }
            return new MultiblockPattern(patternArray, legend, controllerChar);
        }
    }
}

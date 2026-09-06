package com.raishxn.ufo.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
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

    /**
     * Marker predicate for cells that accept whatever the player puts there.
     * Symbols bound to it satisfy {@code strict()} validation but are never
     * scanned and never reported as structure parts, so the surrounding world
     * (cables, batteries, decoration) cannot unform the machine.
     */
    public static final BlockPredicate ANY = (state, level, pos) -> true;

    private final char[][][] pattern;      // [layer][row][col]
    private final Map<Character, BlockPredicate> legend;
    private final Map<Character, Component> legendNames;
    private final Map<Character, List<BlockState>> displayCandidates;
    private final Character controllerChar;
    private final int controllerLayer;
    private final int controllerRow;
    private final int controllerCol;
    private final int testedPositionCount;
    private final List<PatternCell> testedCells;
    private final Map<Character, List<LocalOffset>> offsetsBySymbol;

    public char[][][] getPattern() { return pattern; }
    public char getControllerChar() { return controllerChar != null ? controllerChar : 'C'; }
    public int getControllerLayer() { return controllerLayer; }
    public int getControllerRow() { return controllerRow; }
    public int getControllerCol() { return controllerCol; }
    public int getTestedPositionCount() { return testedPositionCount; }
    public Component getLegendName(char symbol) { return legendNames.getOrDefault(symbol, Component.literal("Unknown Block")); }
    public boolean matchesSlot(char symbol, BlockState state, Level level, BlockPos pos) {
        BlockPredicate predicate = legend.get(symbol);
        return predicate == null || predicate.test(state, level, pos);
    }
    public Set<Character> getSymbols() {
        Set<Character> symbols = new LinkedHashSet<>();
        for (char[][] layer : pattern) {
            for (char[] row : layer) {
                for (char symbol : row) symbols.add(symbol);
            }
        }
        return Collections.unmodifiableSet(symbols);
    }

    private MultiblockPattern(char[][][] pattern, Map<Character, BlockPredicate> legend, Map<Character, Component> legendNames,
                              Map<Character, List<BlockState>> displayCandidates, char controllerChar) {
        this.pattern = pattern;
        this.legend = Map.copyOf(legend);
        this.legendNames = Map.copyOf(legendNames);
        this.displayCandidates = Map.copyOf(displayCandidates);
        this.controllerChar = controllerChar;

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

        List<PatternCell> compiledCells = new ArrayList<>();
        Map<Character, List<LocalOffset>> compiledOffsets = new HashMap<>();
        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {
                    char symbol = pattern[y][z][x];
                    LocalOffset offset = new LocalOffset(x - cCol, y - cLayer, z - cRow);
                    compiledOffsets.computeIfAbsent(symbol, ignored -> new ArrayList<>()).add(offset);
                    BlockPredicate predicate = this.legend.get(symbol);
                    if (symbol != controllerChar && predicate != null && predicate != ANY) {
                        compiledCells.add(new PatternCell(
                                offset, predicate,
                                this.legendNames.getOrDefault(symbol, Component.literal("Expected part"))));
                    }
                }
            }
        }
        Map<Character, List<LocalOffset>> immutableOffsets = new HashMap<>();
        compiledOffsets.forEach((symbol, offsets) -> immutableOffsets.put(symbol, List.copyOf(offsets)));
        this.testedCells = List.copyOf(compiledCells);
        this.offsetsBySymbol = Map.copyOf(immutableOffsets);
        this.testedPositionCount = this.testedCells.size();
    }

    /**
     * Validates the multiblock structure by testing the world against the pattern,
     * centered on the given controller world position.
     *
     * @param level         the level to check
     * @param controllerPos the world position of the controller block
     * @return a {@link MatchResult} containing whether the structure matched and which positions are parts
     */
    public MatchResult match(Level level, BlockPos controllerPos, net.minecraft.core.Direction facing) {
        return match(level, controllerPos, facing, true);
    }

    /** Fast server-side scan that stops at the first mismatch. */
    public MatchResult matchFast(Level level, BlockPos controllerPos, net.minecraft.core.Direction facing) {
        return match(level, controllerPos, facing, false);
    }

    private MatchResult match(Level level, BlockPos controllerPos, net.minecraft.core.Direction facing, boolean diagnostic) {
        List<BlockPos> partPositions = new ArrayList<>(this.testedPositionCount);
        PatternError firstError = null;
        List<PatternError> allErrors = new ArrayList<>();
        boolean valid = true;
        boolean hasUnloadedPositions = false;

        for (PatternCell cell : this.testedCells) {
            LocalOffset offset = cell.offset();
            BlockPos worldPos = getRotatedPos(controllerPos, offset.x(), offset.y(), offset.z(), facing);

            if (!level.isLoaded(worldPos)) {
                valid = false;
                hasUnloadedPositions = true;
                PatternError err = new PatternError(worldPos, Component.literal("Chunk not loaded"));
                allErrors.add(err);
                if (firstError == null) firstError = err;
                if (!diagnostic) {
                    return new MatchResult(false, List.of(), Optional.of(err), List.of(err), true);
                }
                continue;
            }

            BlockState state = level.getBlockState(worldPos);
            if (!cell.predicate().test(state, level, worldPos)) {
                valid = false;
                PatternError err = new PatternError(worldPos, cell.expected());
                allErrors.add(err);
                if (firstError == null) firstError = err;
                if (!diagnostic) {
                    return new MatchResult(false, List.of(), Optional.of(err), List.of(err), false);
                }
            } else {
                partPositions.add(worldPos);
            }
        }

        return new MatchResult(
                valid,
                valid ? Collections.unmodifiableList(partPositions) : Collections.emptyList(),
                Optional.ofNullable(firstError),
                Collections.unmodifiableList(allErrors),
                hasUnloadedPositions);
    }

    /**
     * Translates local pattern offsets into world coordinates based on the controller's facing direction.
     * Assumes pattern is built such that z=0 is the front face looking SOUTH (+Z). 
     * If facing is NORTH, the machine goes $+Z$ backwards.
     */
    public static BlockPos getRotatedPos(BlockPos center, int localX, int localY, int localZ, net.minecraft.core.Direction facing) {
        MultiblockTemplateCompiler.HorizontalFacing horizontalFacing = switch (facing) {
            case SOUTH -> MultiblockTemplateCompiler.HorizontalFacing.SOUTH;
            case WEST -> MultiblockTemplateCompiler.HorizontalFacing.WEST;
            case EAST -> MultiblockTemplateCompiler.HorizontalFacing.EAST;
            default -> MultiblockTemplateCompiler.HorizontalFacing.NORTH;
        };
        MultiblockTemplateCompiler.Offset offset = MultiblockTemplateCompiler.rotate(
                localX, localY, localZ, horizontalFacing);
        return center.offset(offset.x(), offset.y(), offset.z());
    }

    /**
     * Instantly assembles the structure unconditionally, replacing non-matching blocks 
     * using the provided map of default states. Does not replace the controller.
     */
    public void assembleAsCreative(Level level, BlockPos controllerPos, net.minecraft.core.Direction facing, Map<Character, BlockState> defaultStates) {
        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {
                    char c = pattern[y][z][x];
                    
                    int offsetX = x - controllerCol;
                    int offsetY = y - controllerLayer;
                    int offsetZ = z - controllerRow;

                    BlockPos worldPos = getRotatedPos(controllerPos, offsetX, offsetY, offsetZ, facing);

                    if (worldPos.equals(controllerPos)) continue;

                    if (!level.isInWorldBounds(worldPos)) continue;
                    if (!level.hasChunkAt(worldPos)) continue;

                    BlockPredicate predicate = legend.get(c);
                    BlockState targetState = defaultStates.get(c);

                    if (predicate != null && targetState != null) {
                        BlockState currentState = level.getBlockState(worldPos);
                        if (!predicate.test(currentState, level, worldPos)) {
                            level.setBlockAndUpdate(worldPos, targetState);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the exact world positions for a specific character in the pattern.
     */
    public List<BlockPos> getExpectedPositions(BlockPos controllerPos, net.minecraft.core.Direction facing, char targetChar) {
        List<LocalOffset> offsets = this.offsetsBySymbol.getOrDefault(targetChar, List.of());
        List<BlockPos> positions = new ArrayList<>(offsets.size());
        for (LocalOffset offset : offsets) {
            positions.add(getRotatedPos(controllerPos, offset.x(), offset.y(), offset.z(), facing));
        }
        return positions;
    }

    private record LocalOffset(int x, int y, int z) {
    }

    private record PatternCell(LocalOffset offset, BlockPredicate predicate, Component expected) {
    }

    /** Positions whose changes can alter the match result, including required air cells. */
    public List<BlockPos> getTrackedPositions(BlockPos controllerPos, net.minecraft.core.Direction facing) {
        List<BlockPos> positions = new ArrayList<>(testedPositionCount);
        for (PatternCell cell : this.testedCells) {
            LocalOffset offset = cell.offset();
            positions.add(getRotatedPos(controllerPos, offset.x(), offset.y(), offset.z(), facing));
        }
        return Collections.unmodifiableList(positions);
    }

    public List<BlockState> getDisplayCandidates(char symbol) {
        return displayCandidates.getOrDefault(symbol, List.of());
    }

    public Optional<Character> getSymbolAt(BlockPos controllerPos, net.minecraft.core.Direction facing, BlockPos worldPos) {
        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {
                    int offsetX = x - controllerCol;
                    int offsetY = y - controllerLayer;
                    int offsetZ = z - controllerRow;
                    BlockPos expectedPos = getRotatedPos(controllerPos, offsetX, offsetY, offsetZ, facing);
                    if (expectedPos.equals(worldPos)) {
                        return Optional.of(pattern[y][z][x]);
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Represents a specific error in pattern matching.
     */
    public record PatternError(BlockPos pos, Component expected) {}

    /**
     * Result of a pattern match attempt.
     */
    public record MatchResult(
            boolean isValid,
            List<BlockPos> partPositions,
            Optional<PatternError> error,
            List<PatternError> allErrors,
            boolean hasUnloadedPositions) {}

    // ──────────────────────── Builder ────────────────────────

    public static class Builder {
        private final List<String[]> layers = new ArrayList<>();
        private final Map<Character, BlockPredicate> legend = new HashMap<>();
        private final Map<Character, Component> legendNames = new HashMap<>();
        private final Map<Character, List<BlockState>> displayCandidates = new HashMap<>();
        private char controllerChar = 'C';
        private boolean strict;

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
            return where(c, predicate, Component.literal("Unknown Block"));
        }

        public Builder where(char c, BlockPredicate predicate, Component expectedName) {
            this.legend.put(c, predicate);
            this.legendNames.put(c, expectedName);
            return this;
        }

        /**
         * Convenience: maps a char to a specific block class.
         */
        public Builder where(char c, Block block) {
            return where(c, (state, level, pos) -> state.is(block), block.getName());
        }

        public Builder candidates(char c, BlockState... states) {
            return candidates(c, Arrays.asList(states));
        }

        public Builder candidates(char c, List<BlockState> states) {
            List<BlockState> cleaned = states.stream()
                    .filter(Objects::nonNull)
                    .toList();
            if (!cleaned.isEmpty()) {
                this.displayCandidates.put(c, cleaned);
            }
            return this;
        }

        /**
         * Sets the character that represents the controller in the pattern.
         * Defaults to {@code 'C'}.
         */
        public Builder controllerChar(char c) {
            this.controllerChar = c;
            return this;
        }

        /** Enables fail-fast validation for compiled 3.0 definitions. */
        public Builder strict() {
            this.strict = true;
            return this;
        }

        public Builder serviceHatches(char casing, Block coolant, Block energy) {
            List<String[]> updated = ServiceHatchLayout.apply(this.layers, this.controllerChar, casing);
            this.layers.clear();
            this.layers.addAll(updated);
            // J/K select illustrative auto-build positions, not mandatory hatch locations.
            BlockPredicate casingRule = Objects.requireNonNull(this.legend.get(casing));
            Component name = this.legendNames.get(casing);
            List<BlockState> alternatives = this.displayCandidates.getOrDefault(casing, List.of());
            return where('J', casingRule, name).candidates('J', alternatives)
                    .where('K', casingRule, name).candidates('K', alternatives);
        }

        public MultiblockPattern build() {
            validateShape();
            // Convert List<String[]> → char[][][]
            char[][][] patternArray = new char[layers.size()][][];
            for (int y = 0; y < layers.size(); y++) {
                String[] rows = layers.get(y);
                patternArray[y] = new char[rows.length][];
                for (int z = 0; z < rows.length; z++) {
                    patternArray[y][z] = rows[z].toCharArray();
                }
            }
            return new MultiblockPattern(patternArray, legend, legendNames, new HashMap<>(displayCandidates), controllerChar);
        }

        private void validateShape() {
            MultiblockTemplateCompiler.validate(layers, controllerChar, legend.keySet(), strict);
        }
    }
}

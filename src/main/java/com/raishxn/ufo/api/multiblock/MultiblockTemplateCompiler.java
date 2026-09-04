package com.raishxn.ufo.api.multiblock;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Pure template validation and horizontal transform math, independent of Minecraft. */
public final class MultiblockTemplateCompiler {
    private MultiblockTemplateCompiler() {
    }

    public enum HorizontalFacing {
        NORTH, SOUTH, EAST, WEST
    }

    public record Offset(int x, int y, int z) {
    }

    public record Summary(int width, int height, int depth, int controllerX, int controllerY,
                          int controllerZ, Set<Character> symbols) {
    }

    public static Summary validate(List<String[]> layers, char controllerChar,
                                   Set<Character> declaredSymbols, boolean strict) {
        if (layers.isEmpty()) {
            throw new IllegalArgumentException("Multiblock pattern must contain at least one layer");
        }
        int expectedRows = -1;
        int expectedColumns = -1;
        int controllers = 0;
        int controllerX = -1;
        int controllerY = -1;
        int controllerZ = -1;
        Set<Character> usedSymbols = new LinkedHashSet<>();
        for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
            String[] rows = layers.get(layerIndex);
            if (rows == null || rows.length == 0) {
                throw new IllegalArgumentException("Layer " + layerIndex + " must contain at least one row");
            }
            if (expectedRows == -1) expectedRows = rows.length;
            if (rows.length != expectedRows) {
                throw new IllegalArgumentException("Layer " + layerIndex + " has " + rows.length
                        + " rows; expected " + expectedRows);
            }
            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                String row = rows[rowIndex];
                if (row == null || row.isEmpty()) {
                    throw new IllegalArgumentException("Layer " + layerIndex + " row " + rowIndex + " is empty");
                }
                if (expectedColumns == -1) expectedColumns = row.length();
                if (row.length() != expectedColumns) {
                    throw new IllegalArgumentException("Layer " + layerIndex + " row " + rowIndex
                            + " has width " + row.length() + "; expected " + expectedColumns);
                }
                for (int column = 0; column < row.length(); column++) {
                    char symbol = row.charAt(column);
                    usedSymbols.add(symbol);
                    if (symbol == controllerChar) {
                        controllers++;
                        controllerX = column;
                        controllerY = layerIndex;
                        controllerZ = rowIndex;
                    }
                }
            }
        }
        if (controllers != 1) {
            throw new IllegalArgumentException("Pattern must contain exactly one controller '"
                    + controllerChar + "'; found " + controllers);
        }
        if (strict) {
            Set<Character> missing = new LinkedHashSet<>(usedSymbols);
            missing.removeAll(declaredSymbols);
            if (!missing.isEmpty()) {
                throw new IllegalArgumentException("Pattern symbols without predicates: " + missing);
            }
        }
        return new Summary(expectedColumns, layers.size(), expectedRows,
                controllerX, controllerY, controllerZ, Set.copyOf(usedSymbols));
    }

    public static Offset rotate(int localX, int localY, int localZ, HorizontalFacing facing) {
        return switch (facing) {
            case SOUTH -> new Offset(-localX, localY, -localZ);
            case WEST -> new Offset(localZ, localY, -localX);
            case EAST -> new Offset(-localZ, localY, localX);
            case NORTH -> new Offset(localX, localY, localZ);
        };
    }
}

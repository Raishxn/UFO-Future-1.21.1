package com.raishxn.ufo.api.multiblock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Reserves nearest casing cells near the controller without resizing. */
public final class ServiceHatchLayout {
    private ServiceHatchLayout() {}
    private record Cell(int x, int y, int z) {}

    public static List<String[]> apply(List<String[]> original, char controller, char casing) {
        List<String[]> layers = original.stream().map(String[]::clone).toList();
        Cell origin = null;
        for (int y = 0; y < layers.size(); y++) for (int z = 0; z < layers.get(y).length; z++) {
            int x = layers.get(y)[z].indexOf(controller);
            if (x >= 0) origin = new Cell(x, y, z);
        }
        if (origin == null) throw new IllegalArgumentException("Controller missing");
        final Cell center = origin;
        List<Cell> candidates = new ArrayList<>();
        for (int y = 0; y < layers.size(); y++) {
            for (int z = 0; z < layers.get(y).length; z++) {
                String row = layers.get(y)[z];
                for (int x = 0; x < row.length(); x++) if (row.charAt(x) == casing) candidates.add(new Cell(x, y, z));
            }
        }
        candidates.sort(Comparator.comparingInt((Cell c) -> Math.abs(c.x() - center.x()) + Math.abs(c.y() - center.y()) + Math.abs(c.z() - center.z()))
                .thenComparingInt(Cell::z).thenComparingInt(Cell::y).thenComparingInt(Cell::x));
        if (candidates.size() < 2) throw new IllegalArgumentException("Structure needs two service casing cells");
        for (int i = 0; i < 2; i++) {
            Cell cell = candidates.get(i);
            char[] row = layers.get(cell.y())[cell.z()].toCharArray();
            row[cell.x()] = i == 0 ? 'J' : 'K';
            layers.get(cell.y())[cell.z()] = new String(row);
        }
        return layers;
    }
}

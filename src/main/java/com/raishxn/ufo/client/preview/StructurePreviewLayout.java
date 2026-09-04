package com.raishxn.ufo.client.preview;

/** Pure visibility math shared by structure preview renderers. */
public final class StructurePreviewLayout {
    private StructurePreviewLayout() {
    }

    public enum LayerMode {
        FULL,
        UP_TO,
        SINGLE
    }

    public static boolean isShell(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) return false;
        return x == 0 || y == 0 || z == 0 || x == sizeX - 1 || y == sizeY - 1 || z == sizeZ - 1;
    }

    public static boolean isVisible(int cellLayer, int selectedLayer, LayerMode mode) {
        return switch (mode) {
            case FULL -> true;
            case UP_TO -> cellLayer <= selectedLayer;
            case SINGLE -> cellLayer == selectedLayer;
        };
    }
}

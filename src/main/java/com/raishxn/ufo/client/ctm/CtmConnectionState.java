package com.raishxn.ufo.client.ctm;

import net.minecraft.core.Direction;

record CtmConnectionState(boolean[] faceCulled, int[] edgeConnect, int[] cornerConnect) {
    boolean culled(Direction face) {
        return faceCulled[face.get3DDataValue()];
    }

    int edges(Direction face) {
        return edgeConnect[face.get3DDataValue()];
    }

    int corners(Direction face) {
        return cornerConnect[face.get3DDataValue()];
    }
}

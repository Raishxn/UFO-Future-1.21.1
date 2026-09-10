package com.raishxn.ufo.api.ae;

import net.minecraft.core.BlockPos;

/** Controller contract used by a Quantum Grid Link without depending on a concrete multiblock. */
public interface QuantumGridLinkHost {
    /** Whether the controller currently owns a complete structure. */
    boolean isGridLinkFormed();

    /** Prevents a stale or copied link from exposing itself for the wrong controller. */
    boolean ownsGridLink(BlockPos linkPos);

    /** Called when the link changes power, channel or grid state. */
    void onGridLinkStateChanged();
}

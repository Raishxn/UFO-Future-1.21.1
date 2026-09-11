package com.raishxn.ufo.api.ae;

import net.minecraft.world.item.ItemStack;

/** Grid service used by pattern terminals to discover a writable fabrication matrix. */
public interface QuantumPatternMatrixHost extends appeng.api.networking.IGridNodeService {
    boolean insertEncodedPattern(ItemStack stack);

    int patternMatrixPriority();

    long patternMatrixSortKey();
}

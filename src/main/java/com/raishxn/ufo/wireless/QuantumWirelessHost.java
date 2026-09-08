package com.raishxn.ufo.wireless;

import appeng.api.networking.IManagedGridNode;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface QuantumWirelessHost {
    QuantumWirelessLinks wirelessLinks();
    IManagedGridNode getMainNode();
    BlockEntity getBlockEntity();
}

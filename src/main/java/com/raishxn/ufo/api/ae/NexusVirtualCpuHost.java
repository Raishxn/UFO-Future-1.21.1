package com.raishxn.ufo.api.ae;

import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Runtime services supplied to a virtual AE2 crafting CPU owned by the Nexus pool. */
public interface NexusVirtualCpuHost {
    @Nullable IGrid ufo$getCpuGrid();
    @Nullable IGridNode ufo$getCpuNode();
    Level ufo$getCpuLevel();
    IActionSource ufo$getCpuActionSource();
    boolean ufo$isCpuActive();
    void ufo$markCpuDirty();
    Component ufo$getCpuName();

    default int ufo$getCpuPriority() {
        return 0;
    }

    default CpuSelectionMode ufo$getCpuSelectionMode() {
        return CpuSelectionMode.ANY;
    }
}

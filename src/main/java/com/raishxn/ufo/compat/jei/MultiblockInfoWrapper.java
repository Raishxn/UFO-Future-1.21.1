package com.raishxn.ufo.compat.jei;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;

public class MultiblockInfoWrapper extends ModularWrapper<MultiblockPreviewWidget> {

    private final MultiblockControllerDefinitions.PreviewEntry entry;

    public MultiblockInfoWrapper(MultiblockControllerDefinitions.PreviewEntry entry) {
        super(new MultiblockPreviewWidget(entry));
        this.entry = entry;
    }

    public MultiblockControllerDefinitions.PreviewEntry entry() {
        return entry;
    }
}

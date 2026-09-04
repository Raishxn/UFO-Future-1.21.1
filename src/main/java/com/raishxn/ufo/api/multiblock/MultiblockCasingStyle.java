package com.raishxn.ufo.api.multiblock;

import net.minecraft.util.StringRepresentable;

/** Visual casing skin applied to a hatch only while a controller owns it. */
public enum MultiblockCasingStyle implements StringRepresentable {
    DEFAULT("default"),
    QUANTUM("quantum"),
    ENTROPY("entropy");

    private final String serializedName;

    MultiblockCasingStyle(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}

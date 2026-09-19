package com.raishxn.ufo.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Persistent per-scanner construction settings shared by the UI and server actions. */
public record StructureScannerSettings(Mode mode, boolean hatchMode) {
    private static final String MODE_KEY = "UfoStructureMode";
    private static final String HATCH_MODE_KEY = "UfoStructureHatchMode";
    public static final StructureScannerSettings DEFAULT = new StructureScannerSettings(Mode.SCAN, true);

    public enum Mode {
        SCAN,
        BUILD,
        REPLACE,
        DEMOLISH;

        public static Mode byOrdinal(int ordinal) {
            Mode[] values = values();
            return values[Math.floorMod(ordinal, values.length)];
        }
    }

    public static StructureScannerSettings read(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        Mode mode = tag.contains(MODE_KEY) ? Mode.byOrdinal(tag.getInt(MODE_KEY)) : DEFAULT.mode();
        boolean hatchMode = !tag.contains(HATCH_MODE_KEY) || tag.getBoolean(HATCH_MODE_KEY);
        return new StructureScannerSettings(mode, hatchMode);
    }

    public void write(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(MODE_KEY, mode.ordinal());
            tag.putBoolean(HATCH_MODE_KEY, hatchMode);
        });
    }
}

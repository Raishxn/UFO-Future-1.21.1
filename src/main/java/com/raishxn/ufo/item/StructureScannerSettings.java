package com.raishxn.ufo.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Persistent per-scanner construction settings shared by the UI and server actions. */
public record StructureScannerSettings(Mode mode, boolean hatchMode, int fieldTier, boolean useAeNetwork) {
    private static final String MODE_KEY = "UfoStructureMode";
    private static final String HATCH_MODE_KEY = "UfoStructureHatchMode";
    private static final String FIELD_TIER_KEY = "UfoStructureFieldTier";
    private static final String USE_AE_KEY = "UfoStructureUseAe";
    public static final StructureScannerSettings DEFAULT = new StructureScannerSettings(Mode.SCAN, true, 1, false);

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
        int fieldTier = tag.contains(FIELD_TIER_KEY) ? Math.clamp(tag.getInt(FIELD_TIER_KEY), 1, 3) : 1;
        boolean useAe = tag.contains(USE_AE_KEY) && tag.getBoolean(USE_AE_KEY);
        return new StructureScannerSettings(mode, hatchMode, fieldTier, useAe);
    }

    public void write(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(MODE_KEY, mode.ordinal());
            tag.putBoolean(HATCH_MODE_KEY, hatchMode);
            tag.putInt(FIELD_TIER_KEY, Math.clamp(fieldTier, 1, 3));
            tag.putBoolean(USE_AE_KEY, useAeNetwork);
        });
    }
}

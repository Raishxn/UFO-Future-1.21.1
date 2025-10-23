package com.raishxn.ufo.datagen;

import com.mojang.serialization.Codec;
import com.raishxn.ufo.UfoMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, UfoMod.MOD_ID);

    // Seus componentes existentes
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY =
            DATA_COMPONENT_TYPES.register("energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_MODE_INDEX =
            DATA_COMPONENT_TYPES.register("tool_mode_index", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FAST_MODE =
            DATA_COMPONENT_TYPES.register("fast_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BOW_FAST_MODE =
            DATA_COMPONENT_TYPES.register("bow_fast_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());

    // Novos componentes da Mochila-Jetpack
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> INVENTORY =
            DATA_COMPONENT_TYPES.register("inventory", () -> DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ENGINE_ON =
            DATA_COMPONENT_TYPES.register("engine_on", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> HOVERING =
            DATA_COMPONENT_TYPES.register("hovering", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());
}
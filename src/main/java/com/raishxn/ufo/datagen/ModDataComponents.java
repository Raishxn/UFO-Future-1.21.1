package com.raishxn.ufo.datagen;

import appeng.api.stacks.GenericStack;
import com.mojang.serialization.Codec;
import com.raishxn.ufo.UfoMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, UfoMod.MOD_ID);

    // --- Seus Componentes Originais (agora com BOW_FAST_MODE de volta) ---
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY =
            DATA_COMPONENTS.register("energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_MODE_INDEX =
            DATA_COMPONENTS.register("tool_mode_index", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FAST_MODE =
            DATA_COMPONENTS.register("fast_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BOW_FAST_MODE =
            DATA_COMPONENTS.register("bow_fast_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());


    // --- NOVOS COMPONENTES PARA A CÉLULA INFINITA (replicando OmniCells) ---
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CELL_BYTES_BIG =
            DATA_COMPONENTS.register("cell_bytes_big", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CELL_TYPES_USED =
            DATA_COMPONENTS.register("cell_types_used", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<GenericStack>>> CELL_TOOLTIP_STACKS =
            DATA_COMPONENTS.register("cell_tooltip_stacks", () -> DataComponentType.<List<GenericStack>>builder()
                    .persistent(GenericStack.CODEC.listOf())
                    .networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());


    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
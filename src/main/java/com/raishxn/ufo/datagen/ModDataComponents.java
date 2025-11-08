package com.raishxn.ufo.datagen;

import appeng.api.stacks.GenericStack;
import com.mojang.serialization.Codec;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.cell.BigIntegerCodec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs; // Importe este
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class ModDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, UfoMod.MOD_ID);

    // --- NOVOS COMPONENTES PARA AS CÉLULAS BIGINTEGER ---
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BigInteger>> CELL_BYTE_USAGE_BIG = register("cell_byte_usage_big", builder -> builder.persistent(BigIntegerCodec.INSTANCE).networkSynchronized(BigIntegerCodec.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> CELL_TYPES_USAGE =
            register("cell_types_usage", builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CELL_STATE = register("cell_state", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // LINHA CORRIGIDA ABAIXO
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<GenericStack>>> CELL_SHOW_TOOLTIP_STACKS = register("cell_show_tooltip_stacks", builder -> builder.persistent(GenericStack.CODEC.listOf()).networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list())));

    // --- Seus Componentes Existentes (sem alterações) ---
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY =
            DATA_COMPONENTS.register("energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_MODE_INDEX =
            DATA_COMPONENTS.register("tool_mode_index", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MODE =
            DATA_COMPONENTS.register("mode", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FAST_MODE =
            DATA_COMPONENTS.register("fast_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BOW_FAST_MODE =
            DATA_COMPONENTS.register("bow_fast_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());

    // --- Novo método helper para simplificar o registro ---
    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> operator) {
        return DATA_COMPONENTS.register(name, () -> operator.apply(DataComponentType.builder()).build());
    }
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> CELL_UUID = register("cell_uuid", builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));
    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
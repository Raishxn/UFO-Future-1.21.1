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

    // Componente de Energia (já deve existir)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY =
            DATA_COMPONENTS.register("energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    // --- ADICIONE ESTE NOVO COMPONENTE ---
    // Ele vai guardar o índice (a posição) da ferramenta atual no nosso ciclo de transformação.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_MODE_INDEX =
            DATA_COMPONENTS.register("tool_mode_index", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FAST_MODE =
            DATA_COMPONENTS.register("fast_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL) // Define que o dado é um Booleano e deve ser salvo
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BOW_FAST_MODE =
            DATA_COMPONENTS.register("bow_fast_mode", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL) // Define que o dado é um Booleano e deve ser salvo
                    .build());


    // --- NOVOS COMPONENTES PARA A CÉLULA INFINITA (replicando OmniCells) ---

    // Componente para guardar o número de bytes (como uma String, para suportar BigInteger)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CELL_BYTES_BIG =
            DATA_COMPONENTS.register("cell_bytes_big", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    // Componente para guardar o número de tipos de itens/fluidos
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CELL_TYPES_USED =
            DATA_COMPONENTS.register("cell_types_used", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    // Componente para guardar a lista de itens a mostrar na tooltip
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<GenericStack>>> CELL_TOOLTIP_STACKS =
            DATA_COMPONENTS.register("cell_tooltip_stacks", () -> DataComponentType.<List<GenericStack>>builder()
                    .persistent(GenericStack.CODEC.listOf())
                    .networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
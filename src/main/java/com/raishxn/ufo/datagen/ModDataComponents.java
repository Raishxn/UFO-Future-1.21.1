package com.raishxn.ufo.datagen;

import com.mojang.serialization.Codec;
import com.raishxn.ufo.UfoMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
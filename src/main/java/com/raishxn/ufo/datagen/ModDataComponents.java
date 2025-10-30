package com.raishxn.ufo.datagen;

import com.mojang.serialization.Codec;
import com.raishxn.ufo.UfoMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    // --- CORREÇÃO DEFINITIVA APLICADA AQUI ---
    // Alterado de 'public' para 'private' para impedir o registo automático pelo NeoForge.
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, UfoMod.MOD_ID);

    // Todos os seus componentes continuam públicos, então o resto do seu código não será afetado.
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

    // Este método continuará a funcionar, pois tem acesso ao campo 'private' dentro da mesma classe.
    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
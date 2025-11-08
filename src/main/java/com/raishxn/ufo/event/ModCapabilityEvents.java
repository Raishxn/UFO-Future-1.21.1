package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.util.UfoPersistentEnergyStorage;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = UfoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilityEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Registra as capabilities dos Itens (suas ferramentas e armaduras)
        registerItemCapabilities(event);

        // Registra as capabilities dos Blocos (suas máquinas)
        registerBlockCapabilities(event);
    }

    private static void registerItemCapabilities(RegisterCapabilitiesEvent event) {
        // --- Ferramentas e Armas ---
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_SWORD.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_PICKAXE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_AXE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_SHOVEL.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_HOE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_STAFF.get()
        );

        // --- Armaduras ---
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_HELMET.get()
        );
        // Peitoral com mais capacidade (exemplo mantido do seu código original)
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_CHESTPLATE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_LEGGINGS.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_BOOTS.get()
        );
    }

    private static void registerBlockCapabilities(RegisterCapabilitiesEvent event) {
        // === Dimensional Matter Assembler (DMA) ===

        // 1. Itens (Automação com funis/tubos)
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DMA_BE.get(),
                (be, side) -> {
                    // Se o acesso vier de BAIXO (Direction.DOWN), expõe a SAÍDA.
                    // Qualquer outro lado (Cima, Norte, Sul, etc.) expõe a ENTRADA.
                    if (side == Direction.DOWN) {
                        return be.itemOutputHandler;
                    }
                    return be.itemInputHandler;
                }
        );

        // 2. Energia (Cabos de energia)
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.DMA_BE.get(),
                (be, side) -> be.energyStorage // Permite conexão de energia por qualquer lado
        );

        // 3. Fluidos (Tubos de fluidos)
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.DMA_BE.get(),
                (be, side) -> {
                    // Acesso interno (sem lado específico) retorna o tanque principal por padrão
                    if (side == null) return be.fluidInputTank;

                    if (side == Direction.DOWN) {
                        return be.fluidOutputTank1; // Saída por BAIXO (apenas tanque 1 por enquanto)
                    } else if (side == Direction.UP) {
                        return be.fluidInputTank;   // Entrada principal por CIMA
                    } else {
                        return be.coolantInputTank; // Lados horizontais (N/S/L/O) para Coolant (água, etc.)
                    }
                }
        );
    }
}
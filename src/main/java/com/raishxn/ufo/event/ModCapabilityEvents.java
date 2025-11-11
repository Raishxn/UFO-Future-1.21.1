package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.custom.AetherContainmentCapsuleItem;
import com.raishxn.ufo.util.ConfigType;
import com.raishxn.ufo.util.IOMode;
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

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new AetherContainmentCapsuleItem.HazardousFluidHandler(stack, AetherContainmentCapsuleItem.CAPACITY),
                ModItems.AETHER_CONTAINMENT_CAPSULE.get()
        );
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
        // --- Dimensional Matter Assembler ---

        // 1. Itens (Usa SidedItemHandler para respeitar a configuração lateral e retorna null se NONE)
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DMA_BE.get(),
                (be, side) -> {
                    if (side == null) return be.itemInputHandler; // Acesso interno

                    // Se o modo for NONE, retorna null para o cabo desconectar
                    if (be.getSideConfig(ConfigType.ITEM, side) == IOMode.NONE) {
                        return null;
                    }
                    return be.new SidedItemHandler(side);
                }
        );

        // 2. Energia (Universal - Conecta em todos os lados, sem filtro)
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.DMA_BE.get(),
                (be, side) -> be.energyStorage
        );

        // 3. Fluidos (Usa SidedFluidHandler e retorna null se NONE)
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.DMA_BE.get(),
                (be, side) -> {
                    if (side == null) return be.fluidInputTank;

                    IOMode fluidMode = be.getSideConfig(ConfigType.FLUID, side);
                    IOMode coolantMode = be.getSideConfig(ConfigType.COOLANT, side);

                    // Se nem Fluido nem Coolant estão ativos neste lado, retorna null para desconectar
                    if (fluidMode == IOMode.NONE && coolantMode == IOMode.NONE) {
                        return null;
                    }
                    return be.new SidedFluidHandler(side);
                }
        );
    }
    }
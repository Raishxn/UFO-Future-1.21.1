package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.util.UfoPersistentEnergyStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = UfoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilityEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        // --- O LOOP 'FOR' FOI REMOVIDO DAQUI ---

        // O resto do seu código para registrar a energia deve permanecer.
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_SWORD.get()
        );
        // ... (e assim por diante para todas as outras ferramentas)
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_PICKAXE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_HAMMER.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_SHOVEL.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_GREATSWORD.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_FISHING_ROD.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_BOW.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_AXE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_HOE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_STAFF.get()

        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModItems.UFO_HELMET.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0), // Peitoral com mais capacidade
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
}
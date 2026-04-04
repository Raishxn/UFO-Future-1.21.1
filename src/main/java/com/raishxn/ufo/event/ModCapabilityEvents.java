package com.raishxn.ufo.event;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.ModArmor;
import com.raishxn.ufo.item.ModTools;
import com.raishxn.ufo.item.custom.AetherContainmentCapsuleItem;
import com.raishxn.ufo.util.ConfigType;
import com.raishxn.ufo.util.IOMode;
import com.raishxn.ufo.util.UfoPersistentEnergyStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;

@EventBusSubscriber(modid = UfoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilityEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Registra as capabilities dos Itens (suas ferramentas e armaduras)
        registerItemCapabilities(event);

        // Registra as capabilities dos Blocos (suas máquinas)
        event.registerBlockEntity(
                appeng.api.AECapabilities.IN_WORLD_GRID_NODE_HOST,
                ModBlockEntities.DIMENSIONAL_MATTER_ASSEMBLER_BE.get(),
                (be, context) -> (appeng.api.networking.IInWorldGridNodeHost) be
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DIMENSIONAL_MATTER_ASSEMBLER_BE.get(),
                appeng.blockentity.AEBaseInvBlockEntity::getExposedItemHandler
        );
        event.registerBlockEntity(
                appeng.api.AECapabilities.GENERIC_INTERNAL_INV,
                ModBlockEntities.DIMENSIONAL_MATTER_ASSEMBLER_BE.get(),
                (be, context) -> be.getTank()
        );
        // Expose FE/Forge Energy capability so external cables (Mekanism, etc.) can push energy in
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.DIMENSIONAL_MATTER_ASSEMBLER_BE.get(),
                AEBasePoweredBlockEntity::getEnergyStorage
        );
    }

    private static void registerItemCapabilities(RegisterCapabilitiesEvent event) {

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new AetherContainmentCapsuleItem.HazardousFluidHandler(stack, AetherContainmentCapsuleItem.CAPACITY),
                ModItems.AETHER_CONTAINMENT_CAPSULE.get()
        );
        // --- Ferramentas e Armas Já Existentes ---
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_SWORD.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_PICKAXE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_AXE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_SHOVEL.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_HOE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_STAFF.get()
        );

        // --- ADICIONE ESTES BLOCOS ABAIXO ---

        // Hammer
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_HAMMER.get()
        );

        // Greatsword
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_GREATSWORD.get()
        );

        // Fishing Rod
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_FISHING_ROD.get()
        );

        // Bow
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModTools.UFO_BOW.get()
        );

        // --- Armaduras ---
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModArmor.UFO_HELMET.get()
        );
        // Peitoral com mais capacidade (exemplo mantido do seu código original)
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModArmor.UFO_CHESTPLATE.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModArmor.UFO_LEGGINGS.get()
        );
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) ->
                        new UfoPersistentEnergyStorage(stack, ModDataComponents.ENERGY.get(), 1_000_000_000, 1000000, 0),
                ModArmor.UFO_BOOTS.get()
        );
    }
    }


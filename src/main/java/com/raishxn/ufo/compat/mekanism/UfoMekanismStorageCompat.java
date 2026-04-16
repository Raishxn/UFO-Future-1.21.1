package com.raishxn.ufo.compat.mekanism;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.parts.automation.StackWorldBehaviors;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class UfoMekanismStorageCompat {
    private UfoMekanismStorageCompat() {
    }

    public static void initialize(IEventBus modEventBus) {
        StackWorldBehaviors.registerImportStrategy(Holder.KEY_TYPE, UfoMekanismStackImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(Holder.KEY_TYPE, UfoMekanismStackExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(Holder.KEY_TYPE, UfoMekanismExternalStorageStrategy::new);
        ContainerItemStrategy.register(Holder.KEY_TYPE, UfoMekanismKey.class, new ChemicalContainerItemStrategy());
        modEventBus.addListener(UfoMekanismStorageCompat::onRegisterEvent);
    }

    public static void initializeClient(IEventBus modEventBus) {
        UfoChemicalStackRenderer.initialize(modEventBus);
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("mekanism");
    }

    public static AEKeyType getChemicalKeyType() {
        return Holder.KEY_TYPE;
    }

    public static boolean isChemicalBlacklisted(ItemStack cellItem, AEKey requestedAddition) {
        return !(requestedAddition instanceof UfoMekanismKey);
    }

    private static void onRegisterEvent(RegisterEvent event) {
        if (!event.getRegistryKey().equals(net.minecraft.core.registries.Registries.BLOCK)) {
            return;
        }
        AEKeyTypes.register(Holder.KEY_TYPE);
        GenericSlotCapacities.register(Holder.KEY_TYPE, GenericSlotCapacities.getMap().get(AEKeyType.fluids()));
    }

    private static final class Holder {
        private static final AEKeyType KEY_TYPE = UfoMekanismKeyType.TYPE;
    }
}

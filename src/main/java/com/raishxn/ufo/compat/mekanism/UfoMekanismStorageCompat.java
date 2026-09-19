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
        if (!isLoaded()) return;
        // Keep the legacy key type registered so worlds that once stored ufo:chemical keys can still
        // decode them. The live capability/container adapters, however, must have exactly one owner.
        // Applied Mekanistics already exposes Mekanism chemicals to AE2; registering our adapters too
        // makes interfaces that enumerate AE key types expose the same tank as two different keys.
        modEventBus.addListener(UfoMekanismStorageCompat::onRegisterEvent);
        if (!shouldRegisterNativeAdapters(true, usesAppliedMekanistics())) return;
        StackWorldBehaviors.registerImportStrategy(Holder.KEY_TYPE, UfoMekanismStackImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(Holder.KEY_TYPE, UfoMekanismStackExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(Holder.KEY_TYPE, UfoMekanismExternalStorageStrategy::new);
        ContainerItemStrategy.register(Holder.KEY_TYPE, UfoMekanismKey.class, new ChemicalContainerItemStrategy());
    }

    public static void initializeClient(IEventBus modEventBus) {
        if (!isLoaded()) return;
        UfoChemicalStackRenderer.initialize(modEventBus);
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("mekanism");
    }

    public static boolean usesAppliedMekanistics() {
        return isLoaded() && ModList.get().isLoaded("appmek");
    }

    static boolean shouldRegisterNativeAdapters(boolean mekanismLoaded, boolean appmekLoaded) {
        return mekanismLoaded && !appmekLoaded;
    }

    public static AEKeyType getChemicalKeyType() {
        // Keep registered cell IDs loadable without the integration. All additions
        // are rejected by isChemicalBlacklisted while Mekanism is absent.
        if (!isLoaded()) return AEKeyType.fluids();
        return usesAppliedMekanistics() ? AppliedMekanisticsCompat.keyType() : Holder.KEY_TYPE;
    }

    public static boolean isChemicalBlacklisted(ItemStack cellItem, AEKey requestedAddition) {
        return !isLoaded() || requestedAddition.getType() != getChemicalKeyType();
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

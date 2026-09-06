package com.raishxn.ufo.compat.appflux;

import com.glodblock.github.appflux.common.AFSingletons;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import com.raishxn.ufo.UfoMod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.energy.IEnergyStorage;


import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.upgrades.Upgrades;

/**
 * Bridge to Applied Flux FE storage inside the AE2 network. Any failure —
 * including linkage errors from an incompatible AppFlux build — is logged once
 * and permanently disables this bridge instead of being swallowed every tick.
 */
public class AppliedFluxPlugin {

    private static volatile boolean disabled = false;

    private static boolean unavailable() {
        return disabled || !ModList.get().isLoaded("appflux");
    }

    private static void disableAfterFailure(String action, Throwable failure) {
        disabled = true;
        UfoMod.LOGGER.error(
                "[UFO Mod] Applied Flux integration disabled after a failure in {}. Subsequent calls are skipped.",
                action, failure);
    }

    public static double rechargeAeStorageItem(
            IGrid grid, double neededPower, Player player, ItemStack stack, IAEItemPowerStorage aePowerStorage) {
        if (unavailable()) {
            return neededPower;
        }
        try {
            var storage = grid.getStorageService();

            var mult = PowerMultiplier.CONFIG;
            var neededFePower = mult.divide(neededPower);

            var extracted = mult.multiply(storage.getInventory()
                    .extract(
                            FluxKey.of(EnergyType.FE),
                            (long) neededFePower,
                            Actionable.MODULATE,
                            IActionSource.ofPlayer(player)));

            var remainder = aePowerStorage.injectAEPower(stack, extracted, Actionable.MODULATE);
            storage.getInventory()
                    .insert(
                            FluxKey.of(EnergyType.FE),
                            (long) mult.divide(remainder),
                            Actionable.MODULATE,
                            IActionSource.ofPlayer(player));

            neededPower -= extracted - remainder;
        } catch (Throwable failure) {
            disableAfterFailure("rechargeAeStorageItem", failure);
        }
        return neededPower;
    }

    public static void rechargeEnergyStorage(IGrid grid, int afRate, IActionSource source, IEnergyStorage cap) {
        if (unavailable()) {
            return;
        }
        try {
            var storage = grid.getStorageService();

            var extracted =
                    storage.getInventory().extract(FluxKey.of(EnergyType.FE), afRate, Actionable.MODULATE, source);
            var inserted = cap.receiveEnergy((int) extracted, false);
            storage.getInventory().insert(FluxKey.of(EnergyType.FE), extracted - inserted, Actionable.MODULATE, source);
        } catch (Throwable failure) {
            disableAfterFailure("rechargeEnergyStorage", failure);
        }
    }
}

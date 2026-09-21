package com.raishxn.ufo.compat.appflux;

import com.glodblock.github.appflux.common.AFSingletons;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.item.ModItems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.energy.IEnergyStorage;


import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
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

    /**
     * AppFlux only associates its induction card with AE2's own pattern providers, but its
     * mixin gives every PatternProviderLogic an upgrade inventory keyed by the host's
     * terminal icon item. Register the (card, machine) association for the Quantum Pattern
     * Hatch hosts so the card can be installed there too.
     */
    public static void registerInductionCardUpgrades() {
        if (unavailable()) {
            return;
        }
        try {
            Upgrades.add(AFSingletons.INDUCTION_CARD, MultiblockBlocks.QUANTUM_PATTERN_HATCH.get(), 1,
                    "group.pattern_provider.name");
            Upgrades.add(AFSingletons.INDUCTION_CARD, ModItems.QUANTUM_PATTERN_PROVIDER_PART.get(), 1,
                    "group.pattern_provider.name");
            Upgrades.add(AFSingletons.INDUCTION_CARD, ModItems.QUANTUM_INTERFACE.get(), 1,
                    "group.interface.name");
        } catch (Throwable failure) {
            disableAfterFailure("registerInductionCardUpgrades", failure);
        }
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

    /**
     * Consumes FE stored inside the ME network by Applied Flux and converts it to AE at
     * the standard FE-to-AE rate. This is a currency exchange, not generation, so the
     * multiblock supply hatches can burn network FE before touching AE energy cells.
     *
     * @return the AE produced, or 0 when Applied Flux is absent or the extraction failed.
     */
    public static double extractNetworkFeAsAe(IGrid grid, long maxFe, IActionSource source, boolean simulate) {
        if (maxFe <= 0L || unavailable()) {
            return 0.0D;
        }
        try {
            double aePerFe = PowerUnit.FE.convertTo(PowerUnit.AE, 1D);
            long extracted = grid.getStorageService().getInventory().extract(
                    FluxKey.of(EnergyType.FE), maxFe,
                    simulate ? Actionable.SIMULATE : Actionable.MODULATE, source);
            return extracted > 0L ? extracted * aePerFe : 0.0D;
        } catch (Throwable failure) {
            disableAfterFailure("extractNetworkFeAsAe", failure);
            return 0.0D;
        }
    }
}

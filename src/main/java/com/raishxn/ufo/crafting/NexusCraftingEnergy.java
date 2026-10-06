package com.raishxn.ufo.crafting;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyService;

/**
 * Energy view handed to Nexus job CPUs when {@code nexus.ignorePatternEnergy} is enabled.
 *
 * <p>AE2 charges every dispatched pattern through
 * {@link appeng.crafting.execution.CraftingCpuLogic#executeCrafting}. This view keeps every
 * read and every other energy operation on the real grid service, but reports pattern
 * extraction as fully satisfied without touching the stored buffer. The Nexus still needs the
 * Grid Link powered and other machines keep paying their own energy as usual.
 */
public final class NexusCraftingEnergy implements IEnergyService {
    private final IEnergyService delegate;

    public NexusCraftingEnergy(IEnergyService delegate) {
        this.delegate = delegate;
    }

    @Override
    public double extractAEPower(double amount, Actionable mode, PowerMultiplier usePowerMultiplier) {
        // The real service returns the requested amount after a fully successful extraction,
        // regardless of the configured multiplier and without keeping anything on MODULATE.
        return amount;
    }

    @Override
    public double getIdlePowerUsage() {
        return delegate.getIdlePowerUsage();
    }

    @Override
    public double getChannelPowerUsage() {
        return delegate.getChannelPowerUsage();
    }

    @Override
    public double getAvgPowerUsage() {
        return delegate.getAvgPowerUsage();
    }

    @Override
    public double getAvgPowerInjection() {
        return delegate.getAvgPowerInjection();
    }

    @Override
    public boolean isNetworkPowered() {
        return delegate.isNetworkPowered();
    }

    @Override
    public double injectPower(double amount, Actionable mode) {
        return delegate.injectPower(amount, mode);
    }

    @Override
    public double getStoredPower() {
        return delegate.getStoredPower();
    }

    @Override
    public double getMaxStoredPower() {
        return delegate.getMaxStoredPower();
    }

    @Override
    public double getEnergyDemand(double maxRequired) {
        return delegate.getEnergyDemand(maxRequired);
    }
}

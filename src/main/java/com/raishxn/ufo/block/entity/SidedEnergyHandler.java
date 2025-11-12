package com.raishxn.ufo.block.entity;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.energy.EnergyStorage;


/**
 * Implementação simples de energy storage.
 * Ajuste CAPACITY e os métodos canExtract/canReceive conforme necessário.
 */
public class SidedEnergyHandler implements IEnergyStorage {
    private final int capacity;
    private int energy;

    public SidedEnergyHandler(int capacity) {
        this.capacity = capacity;
        this.energy = 0;
    }

    public SidedEnergyHandler() {
        this(10000); // padrão
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) return 0;
        int energyReceived = Math.min(capacity - energy, maxReceive);
        if (!simulate) energy += energyReceived;
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) return 0;
        int energyExtracted = Math.min(energy, maxExtract);
        if (!simulate) energy -= energyExtracted;
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    // métodos utilitários
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, capacity));
    }
}

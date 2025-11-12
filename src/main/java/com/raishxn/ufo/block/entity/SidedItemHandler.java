package com.raishxn.ufo.block.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.core.Direction;

/**
 * Wrapper simples que delega para ItemStackHandler interno.
 * Recebe um lado no construtor caso queira comportamento por lado.
 */
public class SidedItemHandler implements IItemHandler {
    private final ItemStackHandler internal;
    private final Direction side; // pode ser usado para lógica specifica por lado

    public SidedItemHandler(int slots, Direction side) {
        this.internal = new ItemStackHandler(slots);
        this.side = side;
    }

    public SidedItemHandler(Direction side) {
        this(9, side);
    }

    public SidedItemHandler() {
        this(9, null);
    }

    // Delegando implementações para ItemStackHandler
    @Override
    public int getSlots() {
        return internal.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return internal.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        // Aqui você pode aplicar regras por side (ex.: impedir inserção de um lado)
        return internal.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        // Aqui você pode aplicar regras por side (ex.: somente extrair de certos lados)
        return internal.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return internal.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return internal.isItemValid(slot, stack);
    }

    // Expose internal if necessary
    public ItemStackHandler getInternal() {
        return internal;
    }
}

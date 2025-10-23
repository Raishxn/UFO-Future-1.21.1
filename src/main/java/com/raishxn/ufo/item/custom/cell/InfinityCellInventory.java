package com.raishxn.ufo.item.custom.cell;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import com.raishxn.ufo.UFOConfig; // <-- IMPORT CORRIGIDO!
import com.raishxn.ufo.item.custom.InfinityCell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class InfinityCellInventory implements StorageCell {

    public static final ICellHandler HANDLER = new Handler();
    private final ItemStack stack;
    private final AEKey record;

    public InfinityCellInventory(ItemStack stack) {
        this.stack = stack;
        this.record = ((InfinityCell) stack.getItem()).getRecord();
    }

    @Override
    public CellState getStatus() {
        return CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return UFOConfig.infCellCost; // <-- USO CORRIGIDO!
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return what.equals(this.record) ? amount : 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return what.equals(this.record) ? amount : 0;
    }

    @Override
    public void persist() { /* NO-OP */ }

    @Override
    public Component getDescription() {
        return this.stack.getHoverName();
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.record != null) {
            out.add(this.record, InfinityCell.getAsIntMax(this.record));
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what.equals(this.record);
    }

    private static class Handler implements ICellHandler {
        @Override
        public boolean isCell(ItemStack is) {
            return is != null && is.getItem() instanceof InfinityCell;
        }

        @Override
        public @Nullable StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
            if (is.getItem() instanceof InfinityCell cell && cell.getRecord() != null) {
                return new InfinityCellInventory(is);
            }
            return null;
        }
    }
}
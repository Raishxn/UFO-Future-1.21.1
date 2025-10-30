// Local: src/main/java/com/raishxn/ufo/item/custom/cell/InfinityModCellInventory.java
package com.raishxn.ufo.item.custom.cell;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfinityModCellInventory implements StorageCell {
    private final ItemStack stack;
    private final ISaveProvider saveProvider;
    private final Map<AEKey, BigInteger> storage;
    private final InfinityCellManager manager;

    public InfinityModCellInventory(ItemStack stack, @Nullable ISaveProvider saveProvider, Map<AEKey, BigInteger> storageMap, InfinityCellManager manager) {
        this.stack = stack;
        this.saveProvider = saveProvider;
        this.storage = storageMap;
        this.manager = manager;
    }

    @Override
    public CellState getStatus() {
        return storage.isEmpty() ? CellState.EMPTY : CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        if (stack.getItem() instanceof IInfinityCell cell) {
            return cell.getIdleDrain();
        }
        return 0;
    }

    private void updateItemStackTooltipData() {
        BigInteger usedBytes = BigInteger.ZERO;
        for (var entry : storage.entrySet()) {
            BigInteger amount = entry.getValue();
            if (amount != null && amount.signum() > 0) {
                long apb = Math.max(1, entry.getKey().getType().getAmountPerByte());
                usedBytes = usedBytes.add(amount.add(BigInteger.valueOf(apb - 1)).divide(BigInteger.valueOf(apb)));
            }
        }

        IInfinityCell.setUsedBytes(stack, usedBytes);
        IInfinityCell.setUsedTypes(stack, (int) storage.keySet().stream().filter(key -> storage.get(key).signum() > 0).count());

        List<GenericStack> show = new ArrayList<>(5);
        int count = 0;
        for (var e : storage.entrySet()) {
            BigInteger amount = e.getValue();
            if(amount != null && amount.signum() > 0) {
                long shownAmount = amount.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
                show.add(new GenericStack(e.getKey(), shownAmount));
                if (++count >= 5) break;
            }
        }
        IInfinityCell.setTooltipShowStacks(stack, show);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0) return 0;
        if (mode == Actionable.MODULATE) {
            storage.compute(what, (key, current) -> (current == null ? BigInteger.ZERO : current).add(BigInteger.valueOf(amount)));
            persist();
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        BigInteger current = storage.get(what);
        if (current == null || current.signum() <= 0) return 0;

        long toExtract = amount;
        if (current.compareTo(BigInteger.valueOf(toExtract)) < 0) {
            toExtract = current.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
        }
        if (toExtract <= 0) return 0;

        if (mode == Actionable.MODULATE) {
            BigInteger newAmount = current.subtract(BigInteger.valueOf(toExtract));
            if (newAmount.signum() <= 0) {
                storage.remove(what);
            } else {
                storage.put(what, newAmount);
            }
            persist();
        }
        return toExtract;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var entry : storage.entrySet()) {
            long amount = entry.getValue().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
            if (amount > 0) {
                out.add(entry.getKey(), amount);
            }
        }
    }

    @Override
    public Component getDescription() {
        return this.stack.getHoverName();
    }

    @Override
    public void persist() {
        updateItemStackTooltipData();
        manager.setDirty();
        if (saveProvider != null) {
            saveProvider.saveChanges();
        }
    }
}
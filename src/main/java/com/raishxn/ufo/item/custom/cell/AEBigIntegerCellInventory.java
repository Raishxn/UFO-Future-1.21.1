package com.raishxn.ufo.item.custom.cell;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.definitions.AEItems;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.IPartitionList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** BigInteger-backed AE2 cell with bounded external decoding and AE2-compatible capacity rules. */
public class AEBigIntegerCellInventory implements StorageCell
{

    private final @NotNull AEBigIntegerCellData cellData;
    private final @NotNull Object2ObjectMap<AEKey, BigInteger> storage;
    private final @NotNull ItemStack itemStack;
    private final @NotNull IAEBigIntegerCell cellType;
    private final @Nullable ISaveProvider saveContainer;
    private BigInteger totalAmountCached;
    private BigInteger usedBytesCached;
    private boolean isPersisted = false;

    public AEBigIntegerCellInventory(@NotNull AEBigIntegerCellData cellData,
                                     @NotNull ItemStack itemStack,
                                     @NotNull IAEBigIntegerCell cellType,
                                     @Nullable ISaveProvider saveProvider)
    {
        this.cellData = cellData;
        this.storage = cellData.getOriginalStorage();
        this.itemStack = itemStack;
        this.cellType = cellType;
        this.saveContainer = saveProvider;

        BigInteger totalAmount = BigInteger.ZERO;
        for (Object2ObjectMap.Entry<AEKey, BigInteger> e : storage.object2ObjectEntrySet())
        {
            BigInteger v = nonNegative(e.getValue());
            if (v.signum() <= 0) continue;
            totalAmount = totalAmount.add(v);
        }

        this.totalAmountCached = totalAmount;
        recalculateUsedBytes();
        updateItemTooltipState();
    }
    @Override
    public CellState getStatus()
    {
        if (storage.isEmpty()) return CellState.EMPTY;
        long maxBytes = cellType.getMaxBytes(itemStack);
        if (maxBytes == Long.MAX_VALUE) return CellState.NOT_EMPTY;

        int types = storage.size();
        long amountPerByte = Math.max(1, cellType.getKeyType().getAmountPerByte());
        int bytesPerType = Math.max(0, cellType.getBytesPerType(itemStack));
        BigInteger remainingExisting = BigCellCapacityMath.remainingAmount(
                totalAmountCached, types, maxBytes, amountPerByte, bytesPerType);
        if (remainingExisting.signum() <= 0) return CellState.FULL;

        boolean hasTypeSlot = types < cellType.getMaxTypes(itemStack);
        BigInteger remainingWithNewType = BigCellCapacityMath.remainingAmount(
                totalAmountCached, types + 1, maxBytes, amountPerByte, bytesPerType);
        return hasTypeSlot && remainingWithNewType.signum() > 0
                ? CellState.NOT_EMPTY
                : CellState.TYPES_FULL;
    }
    @Override
    public double getIdleDrain()
    {
        return cellType.getIdleDrain();
    }
    @Override
    public boolean canFitInsideCell()
    {
        return true;
    }
    @Override
    public void persist()
    {
        if (isPersisted) return;

        updateItemTooltipState();
        isPersisted = true;
    }
    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source)
    {
        if (amount <= 0) return 0;
        if (what.getType() != cellType.getKeyType()) return 0; // REJECT MISMATCHED KEY TYPES (e.g. Fluids in Item Cells)
        if (cellType.isBlackListed(itemStack, what)) return 0;
        if (!matchesPartitionAndUpgrades(what)) return 0;
        if (!canNestStorageCells(what)) return 0;

        final long apb = Math.max(1, what.getType().getAmountPerByte());
        final BigInteger current = nonNegative(storage.get(what));

        long maxBytesCap = cellType.getMaxBytes(itemStack);
        int maxTypesCap = cellType.getMaxTypes(itemStack);
        int overhead = Math.max(0, cellType.getBytesPerType(itemStack));
        amount = BigCellCapacityMath.acceptedInsert(
                totalAmountCached, storage.size(), current.signum() == 0,
                amount, maxBytesCap, apb, overhead, maxTypesCap);

        if (amount <= 0) return 0;

        if (mode == Actionable.MODULATE)
        {
            BigInteger inserted = BigInteger.valueOf(amount);
            totalAmountCached = totalAmountCached.add(inserted);
            storage.put(what, current.add(inserted));
            recalculateUsedBytes();
            markChanged();
        }
        return amount;
    }
    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source)
    {
        if (amount <= 0) return 0;

        final BigInteger current = nonNegative(storage.get(what));
        if (current.signum() <= 0) return 0;

        final long currentAsLongCap = BigCellCapacityMath.clampToLong(current);
        final long taken = Math.min(amount, currentAsLongCap);
        if (taken <= 0) return 0;

        if (mode == Actionable.MODULATE)
        {
            BigInteger next = current.subtract(BigInteger.valueOf(taken));
            if (next.signum() > 0)
            {
                storage.put(what, next);
            }
            else
            {
                storage.remove(what);
            }
            totalAmountCached = totalAmountCached.subtract(BigInteger.valueOf(taken));
            recalculateUsedBytes();
            markChanged();
        }
        return taken;
    }

    @Override
    public void getAvailableStacks(KeyCounter out)
    {
        for (Object2ObjectMap.Entry<AEKey, BigInteger> entry : storage.object2ObjectEntrySet())
        {
            BigInteger value = nonNegative(entry.getValue());
            if (value.signum() <= 0) continue;
            long existing = out.get(entry.getKey());
            long headroom = (existing <= 0) ? Long.MAX_VALUE : (Long.MAX_VALUE - existing);
            if (headroom <= 0) continue;

            long add = BigCellCapacityMath.clampToLong(value);

            if (add > headroom) add = headroom;

            if (add > 0) out.add(entry.getKey(), add);
        }
    }

    @Override
    public Component getDescription()
    {
        return this.itemStack.getHoverName();
    }
    private boolean canNestStorageCells(AEKey what)
    {
        if (what instanceof AEItemKey itemKey)
        {
            ItemStack s = itemKey.toStack();
            StorageCell nested = StorageCells.getCellInventory(s, null);
            return nested == null || nested.canFitInsideCell();
        }
        return true;
    }
    private boolean matchesPartitionAndUpgrades(AEKey what)
    {
        // 升级槽
        final IUpgradeInventory upgrades = cellType.getUpgrades(itemStack);
        final boolean hasInverter = upgrades.isInstalled(AEItems.INVERTER_CARD);
        final boolean hasFuzzy = upgrades.isInstalled(AEItems.FUZZY_CARD);

        // 分区配置
        ConfigInventory config = null;
        FuzzyMode fuzzyMode = FuzzyMode.IGNORE_ALL;
        if (cellType instanceof ICellWorkbenchItem cellWorkbenchItem)
        {
            config = cellWorkbenchItem.getConfigInventory(itemStack);
            if (hasFuzzy) fuzzyMode = cellWorkbenchItem.getFuzzyMode(itemStack);
        }
        IncludeExclude mode = hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST;
        IPartitionList.Builder partitionBuilder = IPartitionList.builder();
        if (hasFuzzy) partitionBuilder.fuzzyMode(fuzzyMode);
        if (config != null) partitionBuilder.addAll(config.keySet());
        return partitionBuilder.build().matchesFilter(what, mode);
    }
    private void markChanged()
    {
        cellData.setDirty();

        isPersisted = false;
        if (saveContainer != null)
            saveContainer.saveChanges();
        else
            persist();
    }
    private void updateItemTooltipState()
    {
        BigInteger used = usedBytesCached.signum() > 0 ? usedBytesCached : BigInteger.ZERO;

        IAEBigIntegerCell.setUsedBytes(itemStack, used);
        IAEBigIntegerCell.setUsedTypes(itemStack, storage.size());
        IAEBigIntegerCell.setCellState(itemStack, getStatus());
        List<GenericStack> show = new ArrayList<>(5);
        int count = 0;
        for (Object2ObjectMap.Entry<AEKey, BigInteger> e : storage.object2ObjectEntrySet())
        {
            BigInteger v = nonNegative(e.getValue());
            if (v.signum() <= 0) continue;
            show.add(new GenericStack(e.getKey(), BigCellCapacityMath.clampToLong(v)));
            if (++count >= 5) break;
        }
        IAEBigIntegerCell.setTooltipShowStacks(itemStack, show);
    }
    private void recalculateUsedBytes()
    {
        usedBytesCached = BigCellCapacityMath.usedBytes(
                totalAmountCached, storage.size(),
                Math.max(1, cellType.getKeyType().getAmountPerByte()),
                Math.max(0, cellType.getBytesPerType(itemStack)));
    }
    private static BigInteger nonNegative(BigInteger v)
    {
        if (v == null || v.signum() <= 0) return BigInteger.ZERO;
        return v;
    }
}

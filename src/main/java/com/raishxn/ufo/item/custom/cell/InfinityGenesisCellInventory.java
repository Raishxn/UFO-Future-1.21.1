package com.raishxn.ufo.item.custom.cell;

import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.definitions.AEItems;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.IPartitionList;
import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.InfinityCell;
import com.raishxn.ufo.item.InfinityGenesisCell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InfinityGenesisCellInventory implements StorageCell {
    public static final ICellHandler HANDLER = new Handler();

    private final ItemStack stack;
    private final @Nullable ISaveProvider saveProvider;
    private final IPartitionList partitionList;
    private final IncludeExclude partitionMode;

    public InfinityGenesisCellInventory(ItemStack stack, @Nullable ISaveProvider saveProvider) {
        this.stack = stack;
        this.saveProvider = saveProvider;
        InfinityGenesisCell cell = (InfinityGenesisCell) stack.getItem();
        IUpgradeInventory upgrades = cell.getUpgrades(stack);
        this.partitionMode = upgrades.isInstalled(AEItems.INVERTER_CARD)
                ? IncludeExclude.BLACKLIST
                : IncludeExclude.WHITELIST;

        ConfigInventory config = cell.getConfigInventory(stack);
        IPartitionList.Builder partition = IPartitionList.builder();
        if (upgrades.isInstalled(AEItems.FUZZY_CARD)) {
            partition.fuzzyMode(cell.getFuzzyMode(stack));
        }
        partition.addAll(config.keySet());
        this.partitionList = partition.build();
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0 || what == null || isStorageCellKey(what)
                || !matchesPartitionAndUpgrades(what)) {
            return 0;
        }

        if (mode == Actionable.MODULATE && !contains(what)) {
            var learned = new ArrayList<>(getLearnedStacks());
            learned.add(new GenericStack(what, 1));
            stack.set(ModDataComponents.INFINITY_GENESIS_CELL_KEYS.get(), learned);
            if (saveProvider != null) {
                saveProvider.saveChanges();
            }
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0 || what == null) {
            return 0;
        }
        return contains(what) ? amount : 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var stack : getLearnedStacks()) {
            out.add(stack.what(), InfinityCell.getAsIntMax(stack.what()));
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what != null && contains(what) && matchesPartitionAndUpgrades(what);
    }

    @Override
    public CellState getStatus() {
        return getLearnedStacks().isEmpty() ? CellState.EMPTY : CellState.TYPES_FULL;
    }

    @Override
    public double getIdleDrain() {
        return UFOConfig.infCellCost;
    }

    @Override
    public boolean canFitInsideCell() {
        return getLearnedStacks().isEmpty();
    }

    @Override
    public void persist() {
    }

    @Override
    public Component getDescription() {
        return stack.getHoverName();
    }

    private List<GenericStack> getLearnedStacks() {
        return stack.getOrDefault(ModDataComponents.INFINITY_GENESIS_CELL_KEYS.get(), List.of());
    }

    private boolean contains(AEKey what) {
        for (var learned : getLearnedStacks()) {
            if (learned.what().equals(what)) {
                return true;
            }
        }
        return false;
    }

    /** Mirrors AE2's configurable-cell partition semantics used by the Cell Workbench. */
    private boolean matchesPartitionAndUpgrades(AEKey what) {
        return partitionList.matchesFilter(what, partitionMode);
    }

    private static boolean isStorageCellKey(AEKey what) {
        if (what instanceof AEItemKey itemKey) {
            return StorageCells.getCellInventory(itemKey.toStack(), null) != null;
        }
        return false;
    }

    private static class Handler implements ICellHandler {
        @Override
        public boolean isCell(ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof InfinityGenesisCell && stack.getCount() == 1;
        }

        @Override
        public @Nullable StorageCell getCellInventory(ItemStack stack, @Nullable ISaveProvider host) {
            return isCell(stack) ? new InfinityGenesisCellInventory(stack, host) : null;
        }
    }
}

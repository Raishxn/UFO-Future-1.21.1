package com.raishxn.ufo.item.custom.cell;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class AEBigIntegerCellHandler implements ICellHandler
{
    public static final AEBigIntegerCellHandler INSTANCE = new AEBigIntegerCellHandler();

    private AEBigIntegerCellHandler() {}

    @Override
    public boolean isCell(ItemStack itemStack)
    {
        return itemStack.getItem() instanceof IAEBigIntegerCell && itemStack.getCount() == 1;
    }

    @Override
    public @Nullable StorageCell getCellInventory(ItemStack itemStack, @Nullable ISaveProvider iSaveProvider)
    {
        if(ServerLifecycleHooks.getCurrentServer() == null) return null;
        if(!(itemStack.getItem() instanceof IAEBigIntegerCell cellItem)) return null;
        if(itemStack.getCount() != 1) return null;

        AEBigIntegerCellData cellData = AEBigIntegerCellData.computeIfAbsentCellDataForItemStack(itemStack);
        if(cellData == null) return null;

        return new AEBigIntegerCellInventory(cellData, itemStack, cellItem, iSaveProvider);
    }
}
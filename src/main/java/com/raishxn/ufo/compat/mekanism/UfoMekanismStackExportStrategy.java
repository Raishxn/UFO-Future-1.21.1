package com.raishxn.ufo.compat.mekanism;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

public class UfoMekanismStackExportStrategy implements StackExportStrategy {
    private final BlockCapabilityCache<IChemicalHandler, Direction> cache;

    public UfoMekanismStackExportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.cache = BlockCapabilityCache.create(UfoMekCapabilities.CHEMICAL.block(), level, fromPos, fromSide);
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof UfoMekanismKey mekanismKey)) {
            return 0;
        }

        var storage = cache.getCapability();
        if (storage == null) {
            return 0;
        }

        var inv = context.getInternalStorage();
        var extracted = StorageHelper.poweredExtraction(context.getEnergySource(), inv.getInventory(), what, amount, context.getActionSource(), Actionable.SIMULATE);
        var wasInserted = extracted - storage.insertChemical(mekanismKey.withAmount(extracted), Action.SIMULATE).getAmount();

        if (wasInserted > 0) {
            extracted = StorageHelper.poweredExtraction(context.getEnergySource(), inv.getInventory(), what, wasInserted, context.getActionSource(), Actionable.MODULATE);
            wasInserted = extracted - storage.insertChemical(mekanismKey.withAmount(extracted), Action.EXECUTE).getAmount();

            if (wasInserted < extracted) {
                inv.getInventory().insert(what, extracted - wasInserted, Actionable.MODULATE, context.getActionSource());
            }
        }

        return wasInserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof UfoMekanismKey mekanismKey)) {
            return 0;
        }

        var storage = cache.getCapability();
        if (storage == null) {
            return 0;
        }

        return amount - storage.insertChemical(mekanismKey.withAmount(amount), Action.fromFluidAction(mode.getFluidAction())).getAmount();
    }
}

package com.raishxn.ufo.compat.mekanism;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

public class UfoMekanismStackImportStrategy implements StackImportStrategy {
    private final BlockCapabilityCache<IChemicalHandler, Direction> cache;

    public UfoMekanismStackImportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.cache = BlockCapabilityCache.create(UfoMekCapabilities.CHEMICAL.block(), level, fromPos, fromSide);
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(UfoMekanismKeyType.TYPE)) {
            return false;
        }

        var handler = cache.getCapability();
        if (handler == null) {
            return false;
        }

        long remainingTransferAmount = context.getOperationsRemaining() * (long) UfoMekanismKeyType.TYPE.getAmountPerOperation();
        var inv = context.getInternalStorage();

        for (var i = 0; i < handler.getChemicalTanks() && remainingTransferAmount > 0; i++) {
            var stack = handler.getChemicalInTank(i);
            var resource = UfoMekanismKey.of(stack);
            if (resource == null || context.isInFilter(resource) == context.isInverted()) {
                continue;
            }

            var amountForThisResource = inv.getInventory().insert(resource, remainingTransferAmount, Actionable.SIMULATE, context.getActionSource());
            var amount = handler.extractChemical(resource.withAmount(amountForThisResource), Action.EXECUTE).getAmount();
            if (amount > 0) {
                var inserted = inv.getInventory().insert(resource, amount, Actionable.MODULATE, context.getActionSource());
                if (inserted < amount) {
                    var leftover = amount - inserted;
                    handler.insertChemical(resource.withAmount(leftover), Action.EXECUTE);
                }

                var opsUsed = Math.max(1, inserted / UfoMekanismKeyType.TYPE.getAmountPerOperation());
                context.reduceOperationsRemaining(opsUsed);
                remainingTransferAmount -= inserted;
            }
        }

        return false;
    }
}

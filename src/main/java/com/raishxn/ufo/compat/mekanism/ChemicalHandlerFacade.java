package com.raishxn.ufo.compat.mekanism;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.network.chat.Component;

public record ChemicalHandlerFacade(IChemicalHandler handler, boolean extractableOnly, Runnable changeListener) implements MEStorage {
    private static final long MAX_REPORTED_AMOUNT = 1L << 42;

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof UfoMekanismKey key)) {
            return 0;
        }

        var inserted = amount - handler.insertChemical(key.withAmount(amount), Action.fromFluidAction(mode.getFluidAction())).getAmount();
        if (inserted > 0 && mode == Actionable.MODULATE) {
            changeListener.run();
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof UfoMekanismKey key)) {
            return 0;
        }

        var extracted = handler.extractChemical(key.withAmount(amount), Action.fromFluidAction(mode.getFluidAction())).getAmount();
        if (extracted > 0 && mode == Actionable.MODULATE) {
            changeListener.run();
        }
        return extracted;
    }

    @Override
    public Component getDescription() {
        return GuiText.ExternalStorage.text(net.minecraft.network.chat.Component.translatable("item.ufo.pulsar_chemical_cell"));
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var i = 0; i < handler.getChemicalTanks(); i++) {
            var stack = handler.getChemicalInTank(i);
            var key = UfoMekanismKey.of(stack);
            if (key == null) {
                continue;
            }
            if (extractableOnly && handler.extractChemical(stack, Action.SIMULATE).isEmpty()) {
                continue;
            }
            out.add(key, Math.min(stack.getAmount(), MAX_REPORTED_AMOUNT));
        }
    }
}

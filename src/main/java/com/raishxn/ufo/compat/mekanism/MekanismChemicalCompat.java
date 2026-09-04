package com.raishxn.ufo.compat.mekanism;

import appeng.api.stacks.AEKey;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class MekanismChemicalCompat {
    private MekanismChemicalCompat() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, net.minecraft.world.level.block.entity.BlockEntityType<? extends MekanismChemicalStorage> type) {
        event.registerBlockEntity(
                Capabilities.CHEMICAL.block(),
                type,
                (be, side) -> be.supportsChemicalIO() ? new HatchChemicalHandler(be) : null
        );
    }

    public static ChemicalStack createStack(ResourceLocation chemicalId, long amount) {
        if (amount <= 0L) {
            return ChemicalStack.EMPTY;
        }
        var chemical = MekanismAPI.CHEMICAL_REGISTRY.get(chemicalId);
        if (chemical == null || chemical.isEmptyType()) {
            return ChemicalStack.EMPTY;
        }
        return new ChemicalStack(chemical.getAsHolder(), amount);
    }

    public static ResourceLocation getChemicalId(ChemicalStack stack) {
        return ResourceLocation.parse(stack.getChemicalHolder().getRegisteredName());
    }

    public static @Nullable ResourceLocation getChemicalId(AEKey key) {
        if (key instanceof UfoMekanismKey ufoKey) {
            return ufoKey.getId();
        }
        return ModList.get().isLoaded("appmek") ? AppliedMekanisticsCompat.chemicalId(key) : null;
    }

    public static @Nullable AEKey createAeKey(ResourceLocation chemicalId, long amount) {
        ChemicalStack stack = createStack(chemicalId, amount);
        if (stack.isEmpty()) {
            return null;
        }
        return ModList.get().isLoaded("appmek")
                ? AppliedMekanisticsCompat.keyOf(stack)
                : UfoMekanismKey.of(stack);
    }

    private static final class HatchChemicalHandler implements IChemicalHandler {
        private final MekanismChemicalStorage storage;

        private HatchChemicalHandler(MekanismChemicalStorage storage) {
            this.storage = storage;
        }

        @Override
        public int getChemicalTanks() {
            return 1;
        }

        @Override
        public ChemicalStack getChemicalInTank(int tank) {
            if (tank != 0) {
                return ChemicalStack.EMPTY;
            }
            ResourceLocation chemicalId = this.storage.getStoredChemicalId();
            if (chemicalId == null) {
                return ChemicalStack.EMPTY;
            }
            return createStack(chemicalId, this.storage.getStoredChemicalAmount());
        }

        @Override
        public void setChemicalInTank(int tank, ChemicalStack stack) {
            if (tank != 0) {
                return;
            }
            if (stack.isEmpty()) {
                this.storage.setStoredChemical(null, 0L);
            } else {
                this.storage.setStoredChemical(getChemicalId(stack), stack.getAmount());
            }
        }

        @Override
        public long getChemicalTankCapacity(int tank) {
            return tank == 0 ? this.storage.getChemicalCapacity() : 0L;
        }

        @Override
        public boolean isValid(int tank, ChemicalStack stack) {
            return tank == 0 && !stack.isEmpty();
        }

        @Override
        public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
            if (tank != 0 || stack.isEmpty()) {
                return stack;
            }
            ResourceLocation incomingId = getChemicalId(stack);
            long inserted = this.storage.insertChemical(incomingId, stack.getAmount(), action.simulate());
            if (inserted <= 0L) {
                return stack;
            }
            return stack.getAmount() == inserted ? ChemicalStack.EMPTY : stack.copyWithAmount(stack.getAmount() - inserted);
        }

        @Override
        public ChemicalStack extractChemical(int tank, long amount, Action action) {
            if (tank != 0 || amount <= 0L) {
                return ChemicalStack.EMPTY;
            }
            ResourceLocation currentId = this.storage.getStoredChemicalId();
            if (currentId == null) {
                return ChemicalStack.EMPTY;
            }

            long extracted = this.storage.extractChemical(currentId, amount, action.simulate());
            ChemicalStack result = createStack(currentId, extracted);
            return result;
        }
    }
}

package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.block.custom.DimensionalMatterAssemblerBlock;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.menu.DimensionalMatterAssemblerMenu;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DimensionalMatterAssemblerBlockEntity extends BlockEntity implements MenuProvider {

    // === Inventários de Itens ===
    public final ItemStackHandler itemInputHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };
    public final ItemStackHandler itemOutputHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
    };

    // === Tanques de Fluido (16B cada) ===
    public final FluidTank fluidInputTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };
    public final FluidTank coolantInputTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };
    public final FluidTank fluidOutputTank1 = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };
    public final FluidTank fluidOutputTank2 = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // === Energia (1M FE) ===
    public final EnergyStorage energyStorage = new EnergyStorage(1000000, 50000, 0) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            setChanged();
            return super.receiveEnergy(maxReceive, simulate);
        }
    };

    // === Dados de Sincronização (ContainerData) ===
    // Índices: 0=Progress, 1=MaxProgress, 2=Energy, 3=MaxEnergy,
    // 4=CoolantID, 5=CoolantAmt, 6=InputID, 7=InputAmt,
    // 8=Out1ID, 9=Out1Amt, 10=Out2ID, 11=Out2Amt
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return switch (pIndex) {
                case 0 -> DimensionalMatterAssemblerBlockEntity.this.progress;
                case 1 -> DimensionalMatterAssemblerBlockEntity.this.maxProgress;
                case 2 -> DimensionalMatterAssemblerBlockEntity.this.energyStorage.getEnergyStored();
                case 3 -> DimensionalMatterAssemblerBlockEntity.this.energyStorage.getMaxEnergyStored();
                case 4 -> BuiltInRegistries.FLUID.getId(coolantInputTank.getFluid().getFluid());
                case 5 -> coolantInputTank.getFluidAmount();
                case 6 -> BuiltInRegistries.FLUID.getId(fluidInputTank.getFluid().getFluid());
                case 7 -> fluidInputTank.getFluidAmount();
                case 8 -> BuiltInRegistries.FLUID.getId(fluidOutputTank1.getFluid().getFluid());
                case 9 -> fluidOutputTank1.getFluidAmount();
                case 10 -> BuiltInRegistries.FLUID.getId(fluidOutputTank2.getFluid().getFluid());
                case 11 -> fluidOutputTank2.getFluidAmount();
                default -> 0;
            };
        }

        @Override
        public void set(int pIndex, int pValue) {
            switch (pIndex) {
                case 0 -> DimensionalMatterAssemblerBlockEntity.this.progress = pValue;
                case 1 -> DimensionalMatterAssemblerBlockEntity.this.maxProgress = pValue;
            }
        }

        @Override
        public int getCount() {
            return 12;
        }
    };

    private int progress = 0;
    private int maxProgress = 0;

    public DimensionalMatterAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DMA_BE.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ufo.dimensional_matter_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new DimensionalMatterAssemblerMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DimensionalMatterAssemblerBlockEntity be) {
        if (level.isClientSide) return;

        boolean isActive = false;
        Optional<DimensionalMatterAssemblerRecipe> recipeOptional = be.getRecipe();

        if (recipeOptional.isPresent()) {
            DimensionalMatterAssemblerRecipe recipe = recipeOptional.get();
            if (be.canProcess(recipe)) {
                isActive = true;
                be.maxProgress = recipe.getProcessTime();
                int energyPerTick = be.maxProgress > 0 ? recipe.getEnergy() / be.maxProgress : recipe.getEnergy();

                if (be.energyStorage.getEnergyStored() >= energyPerTick) {
                    be.energyStorage.extractEnergy(energyPerTick, false);
                    be.progress++;
                    if (be.progress >= be.maxProgress) {
                        be.craft(recipe);
                        be.progress = 0;
                    }
                } else {
                    isActive = false;
                }
            } else {
                be.progress = 0;
            }
        } else {
            be.progress = 0;
        }

        if (state.getValue(DimensionalMatterAssemblerBlock.ACTIVE) != isActive) {
            level.setBlock(pos, state.setValue(DimensionalMatterAssemblerBlock.ACTIVE, isActive), 3);
        }
        be.setChanged();
    }

    private Optional<DimensionalMatterAssemblerRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.DMA_TYPE.get()).stream()
                .map(RecipeHolder::value)
                .filter(this::matchesRecipe)
                .findFirst();
    }

    private boolean matchesRecipe(DimensionalMatterAssemblerRecipe recipe) {
        if (!recipe.getFluidInput().isEmpty()) {
            if (!recipe.getFluidInput().getIngredient().test(fluidInputTank.getFluid()) ||
                    fluidInputTank.getFluidAmount() < recipe.getFluidInput().getAmount()) return false;
        }
        if (!recipe.getCoolantInput().isEmpty()) {
            if (!recipe.getCoolantInput().getIngredient().test(coolantInputTank.getFluid()) ||
                    coolantInputTank.getFluidAmount() < recipe.getCoolantInput().getAmount()) return false;
        }
        for (var ingredientStack : recipe.getItemInputs()) {
            long requiredCount = ingredientStack.getAmount();
            long foundCount = 0;
            for (int i = 0; i < itemInputHandler.getSlots(); i++) {
                ItemStack stack = itemInputHandler.getStackInSlot(i);
                if (!stack.isEmpty() && ingredientStack.getIngredient().test(stack)) {
                    foundCount += stack.getCount();
                }
            }
            if (foundCount < requiredCount) return false;
        }
        return true;
    }

    private boolean canProcess(DimensionalMatterAssemblerRecipe recipe) {
        List<ItemStack> itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputs.size(); i++) {
            if (!itemOutputHandler.insertItem(i, itemOutputs.get(i), true).isEmpty()) return false;
        }
        List<FluidStack> fluidOutputs = recipe.getFluidOutputs();
        if (fluidOutputs.size() > 0) {
            if (fluidOutputTank1.fill(fluidOutputs.get(0), IFluidHandler.FluidAction.SIMULATE) != fluidOutputs.get(0).getAmount()) return false;
        }
        if (fluidOutputs.size() > 1) {
            if (fluidOutputTank2.fill(fluidOutputs.get(1), IFluidHandler.FluidAction.SIMULATE) != fluidOutputs.get(1).getAmount()) return false;
        }
        return true;
    }

    private void craft(DimensionalMatterAssemblerRecipe recipe) {
        if (!recipe.getFluidInput().isEmpty()) {
            fluidInputTank.drain((int)recipe.getFluidInput().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }
        if (!recipe.getCoolantInput().isEmpty()) {
            coolantInputTank.drain((int)recipe.getCoolantInput().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }
        for (var ingredientStack : recipe.getItemInputs()) {
            long remaining = ingredientStack.getAmount();
            for (int i = 0; i < itemInputHandler.getSlots(); i++) {
                if (remaining <= 0) break;
                ItemStack stack = itemInputHandler.getStackInSlot(i);
                if (!stack.isEmpty() && ingredientStack.getIngredient().test(stack)) {
                    int toExtract = (int) Math.min(stack.getCount(), remaining);
                    itemInputHandler.extractItem(i, toExtract, false);
                    remaining -= toExtract;
                }
            }
        }
        List<ItemStack> itemOutputs = recipe.getItemOutputs();
        for (int i = 0; i < itemOutputs.size(); i++) {
            itemOutputHandler.insertItem(i, itemOutputs.get(i).copy(), false);
        }
        List<FluidStack> fluidOutputs = recipe.getFluidOutputs();
        if (fluidOutputs.size() > 0) fluidOutputTank1.fill(fluidOutputs.get(0).copy(), IFluidHandler.FluidAction.EXECUTE);
        if (fluidOutputs.size() > 1) fluidOutputTank2.fill(fluidOutputs.get(1).copy(), IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("input_items", itemInputHandler.serializeNBT(registries));
        tag.put("output_items", itemOutputHandler.serializeNBT(registries));
        tag.put("input_fluid", fluidInputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("input_coolant", coolantInputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("output_fluid1", fluidOutputTank1.writeToNBT(registries, new CompoundTag()));
        tag.put("output_fluid2", fluidOutputTank2.writeToNBT(registries, new CompoundTag()));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemInputHandler.deserializeNBT(registries, tag.getCompound("input_items"));
        itemOutputHandler.deserializeNBT(registries, tag.getCompound("output_items"));
        fluidInputTank.readFromNBT(registries, tag.getCompound("input_fluid"));
        coolantInputTank.readFromNBT(registries, tag.getCompound("input_coolant"));
        fluidOutputTank1.readFromNBT(registries, tag.getCompound("output_fluid1"));
        fluidOutputTank2.readFromNBT(registries, tag.getCompound("output_fluid2"));
        if (tag.contains("energy")) {
            energyStorage.receiveEnergy(tag.getInt("energy") - energyStorage.getEnergyStored(), false);
        }
        progress = tag.getInt("progress");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
    }

    public IEnergyStorage getEnergyStorage() { return energyStorage; }
    public FluidTank getFluidInputTank() { return fluidInputTank; }
    public FluidTank getCoolantInputTank() { return coolantInputTank; }
    public FluidTank getFluidOutputTank1() { return fluidOutputTank1; }
    public FluidTank getFluidOutputTank2() { return fluidOutputTank2; }
}
package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.block.custom.DimensionalMatterAssemblerBlock;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.init.ModRecipes;
import com.raishxn.ufo.menu.DimensionalMatterAssemblerMenu;
import com.raishxn.ufo.recipe.DimensionalMatterAssemblerRecipe;
import com.raishxn.ufo.util.ConfigType;
import com.raishxn.ufo.util.IOMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DimensionalMatterAssemblerBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler itemInputHandler = new ItemStackHandler(9) { @Override protected void onContentsChanged(int slot) { setChanged(); } };
    public final ItemStackHandler itemOutputHandler = new ItemStackHandler(2) { @Override protected void onContentsChanged(int slot) { setChanged(); } @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; } };
    public final FluidTank fluidInputTank = new FluidTank(16000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final FluidTank coolantInputTank = new FluidTank(16000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final FluidTank fluidOutputTank1 = new FluidTank(16000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final FluidTank fluidOutputTank2 = new FluidTank(16000) { @Override protected void onContentsChanged() { setChanged(); } };
    public final EnergyStorage energyStorage = new EnergyStorage(1000000, 50000, 0) { @Override public int receiveEnergy(int maxReceive, boolean simulate) { setChanged(); return super.receiveEnergy(maxReceive, simulate); } };

    private final IOMode[][] sideConfigs = new IOMode[ConfigType.values().length][6];
    private final boolean[] autoEject = new boolean[ConfigType.values().length];
    private final boolean[] autoInput = new boolean[ConfigType.values().length];
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return switch (pIndex) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> energyStorage.getEnergyStored();
                case 3 -> energyStorage.getMaxEnergyStored();
                case 4 -> BuiltInRegistries.FLUID.getId(coolantInputTank.getFluid().getFluid());
                case 5 -> coolantInputTank.getFluidAmount();
                case 6 -> BuiltInRegistries.FLUID.getId(fluidInputTank.getFluid().getFluid());
                case 7 -> fluidInputTank.getFluidAmount();
                case 8 -> BuiltInRegistries.FLUID.getId(fluidOutputTank1.getFluid().getFluid());
                case 9 -> fluidOutputTank1.getFluidAmount();
                case 10 -> BuiltInRegistries.FLUID.getId(fluidOutputTank2.getFluid().getFluid());
                case 11 -> fluidOutputTank2.getFluidAmount();
                case 12 -> (autoEject[0] ? 1 : 0) | (autoEject[1] ? 2 : 0) | (autoEject[2] ? 4 : 0) | (autoEject[3] ? 8 : 0);
                default -> 0;
            };
        }
        @Override
        public void set(int pIndex, int pValue) {
            switch (pIndex) {
                case 0 -> progress = pValue;
                case 1 -> maxProgress = pValue;
                case 12 -> {
                    autoEject[0] = (pValue & 1) != 0;
                    autoEject[1] = (pValue & 2) != 0;
                    autoEject[2] = (pValue & 4) != 0;
                    autoEject[3] = (pValue & 8) != 0;
                }
            }
        }
        @Override public int getCount() { return 13; }
    };
    private int progress = 0;
    private int maxProgress = 0;

    public DimensionalMatterAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DMA_BE.get(), pos, state);
        for (ConfigType type : ConfigType.values()) {
            Arrays.fill(sideConfigs[type.ordinal()], IOMode.NONE);
            autoEject[type.ordinal()] = false;
        }
        sideConfigs[ConfigType.ITEM.ordinal()][Direction.DOWN.ordinal()] = IOMode.ITEM_OUT;
        sideConfigs[ConfigType.FLUID.ordinal()][Direction.UP.ordinal()] = IOMode.FLUID_IN;
        sideConfigs[ConfigType.COOLANT.ordinal()][Direction.NORTH.ordinal()] = IOMode.COOLANT_IN;
        sideConfigs[ConfigType.COOLANT.ordinal()][Direction.SOUTH.ordinal()] = IOMode.COOLANT_IN;
        sideConfigs[ConfigType.ENERGY.ordinal()][Direction.EAST.ordinal()] = IOMode.ENERGY;
        sideConfigs[ConfigType.ENERGY.ordinal()][Direction.WEST.ordinal()] = IOMode.ENERGY;
    }

    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.ufo.dimensional_matter_assembler"); }

    @Nullable @Override public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new DimensionalMatterAssemblerMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DimensionalMatterAssemblerBlockEntity be) {
        if (level.isClientSide) return;
        if (be.autoEject[ConfigType.ITEM.ordinal()]) be.tryEjectItems(level, pos);
        if (be.autoEject[ConfigType.FLUID.ordinal()]) be.tryEjectFluids(level, pos, ConfigType.FLUID);

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

    private void tryEjectItems(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (getSideConfig(ConfigType.ITEM, dir) == IOMode.ITEM_OUT) {
                IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(dir), dir.getOpposite());
                if (neighbor != null) {
                    for (int i = 0; i < itemOutputHandler.getSlots(); i++) {
                        ItemStack stack = itemOutputHandler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            ItemStack remaining = ItemHandlerHelper.insertItem(neighbor, stack.copy(), false);
                            itemOutputHandler.setStackInSlot(i, remaining);
                        }
                    }
                }
            }
        }
    }

    private void tryEjectFluids(Level level, BlockPos pos, ConfigType type) {
        for (Direction dir : Direction.values()) {
            IOMode mode = getSideConfig(type, dir);
            if (mode == IOMode.FLUID_OUT_1 || mode == IOMode.FLUID_OUT_2) {
                FluidTank tank = (mode == IOMode.FLUID_OUT_1) ? fluidOutputTank1 : fluidOutputTank2;
                if (tank.getFluidAmount() > 0) {
                    IFluidHandler neighbor = level.getCapability(Capabilities.FluidHandler.BLOCK, pos.relative(dir), dir.getOpposite());
                    if (neighbor != null) {
                        FluidStack drained = tank.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                        if (!drained.isEmpty()) {
                            int filled = neighbor.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
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
        if (!fluidOutputs.isEmpty()) {
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

    public IOMode getSideConfig(ConfigType type, Direction side) {
        return sideConfigs[type.ordinal()][side.ordinal()];
    }

    // --- MÉTODOS DE CONFIGURAÇÃO COM ATUALIZAÇÃO DE VIZINHOS ---
    public void setSideConfig(ConfigType type, Direction side, IOMode mode) {
        sideConfigs[type.ordinal()][side.ordinal()] = mode;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            level.updateNeighborsAt(worldPosition, state.getBlock()); // Importante para cabos!
        }
    }

    public boolean isAutoEject(ConfigType type) {
        return autoEject[type.ordinal()];
    }

    public void setAutoEject(ConfigType type, boolean active) {
        autoEject[type.ordinal()] = active;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            level.updateNeighborsAt(worldPosition, state.getBlock());
        }
    }

    public boolean isAutoInput(ConfigType type) {
        return autoInput[type.ordinal()];
    }

    public void setAutoInput(ConfigType type, boolean enabled) {
        autoInput[type.ordinal()] = enabled;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            level.updateNeighborsAt(worldPosition, state.getBlock());
        }
    }

    public void resetAllSideConfigs() {
        for(int i = 0; i < sideConfigs.length; i++) {
            Arrays.fill(sideConfigs[i], IOMode.NONE);
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            level.updateNeighborsAt(worldPosition, state.getBlock());
        }
    }
    // -----------------------------------------------------------

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

        for (ConfigType type : ConfigType.values()) {
            int idx = type.ordinal();
            tag.putBoolean("auto_eject_" + idx, autoEject[idx]);
            tag.putBoolean("auto_input_" + idx, autoInput[idx]);

            int[] sideOrdinals = new int[6];
            for (int i = 0; i < 6; i++) {
                sideOrdinals[i] = sideConfigs[idx][i].ordinal();
            }
            tag.putIntArray("side_config_" + idx, sideOrdinals);
        }
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

        for (ConfigType type : ConfigType.values()) {
            int idx = type.ordinal();
            autoEject[idx] = tag.getBoolean("auto_eject_" + idx);
            autoInput[idx] = tag.getBoolean("auto_input_" + idx);

            if (tag.contains("side_config_" + idx)) {
                int[] sideOrdinals = tag.getIntArray("side_config_" + idx);
                for (int i = 0; i < 6; i++) {
                    if (i < sideConfigs[idx].length) {
                        int safeOrdinal = Math.min(Math.max(sideOrdinals[i], 0), IOMode.values().length - 1);
                        sideConfigs[idx][i] = IOMode.values()[safeOrdinal];
                    }
                }
            }
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        if (level != null && level.isClientSide) {
            // Atualiza visualmente para o cliente
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }
    public IEnergyStorage getEnergyStorage() { return energyStorage; }
    public FluidTank getFluidInputTank() { return this.fluidInputTank; }
    public FluidTank getCoolantInputTank() { return this.coolantInputTank; }
    public FluidTank getFluidOutputTank1() { return this.fluidOutputTank1; }
    public FluidTank getFluidOutputTank2() { return this.fluidOutputTank2; }

    // =========================================================
    // Classes Internas para Controle de Acesso por Lado (Wrappers)
    // =========================================================

    public class SidedItemHandler implements IItemHandler {
        private final Direction side;

        public SidedItemHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return itemInputHandler.getSlots() + itemOutputHandler.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot < itemInputHandler.getSlots()) {
                return itemInputHandler.getStackInSlot(slot);
            }
            return itemOutputHandler.getStackInSlot(slot - itemInputHandler.getSlots());
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            IOMode mode = getSideConfig(ConfigType.ITEM, side);
            // Se não for modo de entrada, rejeita
            if (mode != IOMode.ITEM_IN && mode != IOMode.ITEM_IO && mode != IOMode.ITEM_IO2) {
                return stack;
            }
            // Só permite inserir nos slots de entrada
            if (slot < itemInputHandler.getSlots()) {
                return itemInputHandler.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            IOMode mode = getSideConfig(ConfigType.ITEM, side);
            // Se não for modo de saída, rejeita
            if (mode != IOMode.ITEM_OUT && mode != IOMode.ITEM_OUT2 && mode != IOMode.ITEM_IO && mode != IOMode.ITEM_IO2) {
                return ItemStack.EMPTY;
            }
            // Só permite extrair dos slots de saída
            if (slot >= itemInputHandler.getSlots()) {
                return itemOutputHandler.extractItem(slot - itemInputHandler.getSlots(), amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) { return 64; }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot < itemInputHandler.getSlots()) return itemInputHandler.isItemValid(slot, stack);
            return itemOutputHandler.isItemValid(slot - itemInputHandler.getSlots(), stack);
        }
    }

    public class SidedFluidHandler implements IFluidHandler {
        private final Direction side;

        public SidedFluidHandler(Direction side) {
            this.side = side;
        }

        @Override public int getTanks() { return 4; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> fluidInputTank.getFluid();
                case 1 -> coolantInputTank.getFluid();
                case 2 -> fluidOutputTank1.getFluid();
                case 3 -> fluidOutputTank2.getFluid();
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return switch (tank) {
                case 0 -> fluidInputTank.getCapacity();
                case 1 -> coolantInputTank.getCapacity();
                case 2 -> fluidOutputTank1.getCapacity();
                case 3 -> fluidOutputTank2.getCapacity();
                default -> 0;
            };
        }

        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return true; }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = 0;
            // Tenta Coolant se o modo permitir
            if (getSideConfig(ConfigType.COOLANT, side) == IOMode.COOLANT_IN) {
                filled = coolantInputTank.fill(resource, action);
            }
            // Se não encheu (ou se quiser permitir ambos), tenta Input se o modo permitir
            if (filled == 0) {
                IOMode fluidMode = getSideConfig(ConfigType.FLUID, side);
                if (fluidMode == IOMode.FLUID_IN || fluidMode == IOMode.FLUID_IO_1 || fluidMode == IOMode.FLUID_IO_2) {
                    filled = fluidInputTank.fill(resource, action);
                }
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            IOMode fluidMode = getSideConfig(ConfigType.FLUID, side);
            if ((fluidMode == IOMode.FLUID_OUT_1 || fluidMode == IOMode.FLUID_IO_1) && !fluidOutputTank1.isEmpty()) {
                return fluidOutputTank1.drain(resource, action);
            }
            if ((fluidMode == IOMode.FLUID_OUT_2 || fluidMode == IOMode.FLUID_IO_2) && !fluidOutputTank2.isEmpty()) {
                return fluidOutputTank2.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            IOMode fluidMode = getSideConfig(ConfigType.FLUID, side);
            if (fluidMode == IOMode.FLUID_OUT_1 || fluidMode == IOMode.FLUID_IO_1) {
                return fluidOutputTank1.drain(maxDrain, action);
            }
            if (fluidMode == IOMode.FLUID_OUT_2 || fluidMode == IOMode.FLUID_IO_2) {
                return fluidOutputTank2.drain(maxDrain, action);
            }
            return FluidStack.EMPTY;
        }
    }
}
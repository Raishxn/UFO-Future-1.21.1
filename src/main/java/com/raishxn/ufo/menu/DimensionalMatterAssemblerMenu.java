package com.raishxn.ufo.menu;

import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.custom.DimensionalMatterAssemblerBlock;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.init.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class DimensionalMatterAssemblerMenu extends AbstractContainerMenu {
    public final DimensionalMatterAssemblerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // Construtor do Cliente: ContainerData deve ter tamanho 12 agora!
    public DimensionalMatterAssemblerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(12));
    }

    public DimensionalMatterAssemblerMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.DMA_MENU.get(), pContainerId);
        checkContainerSize(inv, 11);
        this.blockEntity = ((DimensionalMatterAssemblerBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        ItemStackHandler itemInputHandler = this.blockEntity.itemInputHandler;
        ItemStackHandler itemOutputHandler = this.blockEntity.itemOutputHandler;

        int inputStartX = 43;
        int inputStartY = 20;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotItemHandler(itemInputHandler, col + row * 3,
                        inputStartX + col * 18, inputStartY + row * 18));
            }
        }

        this.addSlot(new SlotItemHandler(itemOutputHandler, 0, 130, 22));
        this.addSlot(new SlotItemHandler(itemOutputHandler, 1, 130, 50));

        addDataSlots(data);
    }

    public int getProgress() { return this.data.get(0); }
    public int getMaxProgress() { return this.data.get(1); }
    public int getEnergy() { return this.data.get(2); }
    public int getMaxEnergy() { return this.data.get(3); }

    // === Novos Getters de Fluido para o Cliente ===
    public FluidStack getCoolantStack() { return getFluidStackInternal(4, 5); }
    public FluidStack getInputFluidStack() { return getFluidStackInternal(6, 7); }
    public FluidStack getOutputFluid1Stack() { return getFluidStackInternal(8, 9); }
    public FluidStack getOutputFluid2Stack() { return getFluidStackInternal(10, 11); }

    private FluidStack getFluidStackInternal(int idSlot, int amountSlot) {
        int fluidId = this.data.get(idSlot);
        int amount = this.data.get(amountSlot);
        Fluid fluid = BuiltInRegistries.FLUID.byId(fluidId);
        if (fluid == null || amount <= 0) return FluidStack.EMPTY;
        return new FluidStack(fluid, amount);
    }

    public int getProgressPercent() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        return maxProgress > 0 && progress > 0 ? progress * 100 / maxProgress : 0;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < 36) {
            if (!moveItemStackTo(sourceStack, 36, 45, false)) return ItemStack.EMPTY;
        } else if (pIndex >= 36) {
            if (!moveItemStackTo(sourceStack, 0, 36, true)) return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) sourceSlot.set(ItemStack.EMPTY);
        else sourceSlot.setChanged();
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.DIMENSIONAL_MATTER_ASSEMBLER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
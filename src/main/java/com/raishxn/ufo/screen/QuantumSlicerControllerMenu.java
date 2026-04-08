package com.raishxn.ufo.screen;

import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.QuantumSlicerControllerBE;
import com.raishxn.ufo.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class QuantumSlicerControllerMenu extends AbstractContainerMenu {

    private final QuantumSlicerControllerBE blockEntity;
    private final ContainerLevelAccess levelAccess;
    private final ContainerData data;

    public QuantumSlicerControllerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public QuantumSlicerControllerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.QUANTUM_SLICER_CONTROLLER_MENU.get(), id);
        checkContainerSize(inv, 0);
        this.blockEntity = (QuantumSlicerControllerBE) entity;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());
        this.data = data;
        addDataSlots(this.data);
    }

    public boolean isAssembled() {
        return this.data.get(0) == 1;
    }

    public boolean isRunning() {
        return this.data.get(1) == 1;
    }

    public int getProgress() {
        return this.data.get(2);
    }

    public int getMaxProgress() {
        return this.data.get(3);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.levelAccess, player, MultiblockBlocks.QUANTUM_SLICER_CONTROLLER.get());
    }
}

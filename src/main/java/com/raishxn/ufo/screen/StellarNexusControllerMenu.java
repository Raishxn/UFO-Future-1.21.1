package com.raishxn.ufo.screen;

import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.block.MultiblockBlocks;
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

public class StellarNexusControllerMenu extends AbstractContainerMenu {

    private final StellarNexusControllerBE blockEntity;
    private final ContainerLevelAccess levelAccess;
    private final ContainerData data;

    public StellarNexusControllerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(9));
    }

    public StellarNexusControllerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.STELLAR_NEXUS_CONTROLLER_MENU.get(), id);
        checkContainerSize(inv, 0); // No items in controller
        this.blockEntity = (StellarNexusControllerBE) entity;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());
        this.data = data;
        
        // Data mapping:
        // 0 = progress, 1 = maxProgress, 2 = assembled
        // 3 = fieldLevel, 4 = fuelPercent, 5 = running
        // 6 = heatLevel (0-1000), 7 = safeMode, 8 = cooldownTimer
        addDataSlots(this.data);
    }

    public StellarNexusControllerBE getBlockEntity() {
        return this.blockEntity;
    }

    public boolean isAssembled() {
        return this.data.get(2) == 1;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getTotalTime() {
        return this.data.get(1);
    }

    public int getFieldLevel() {
        return this.data.get(3);
    }

    public int getFuelPercent() {
        return this.data.get(4);
    }

    public boolean isRunning() {
        return this.data.get(5) == 1;
    }

    /** Heat level 0-1000 (display as 0.0% to 100.0%) */
    public int getHeatLevel() {
        return this.data.get(6);
    }

    public boolean isSafeMode() {
        return this.data.get(7) == 1;
    }

    public int getCooldownTimer() {
        return this.data.get(8);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.levelAccess, player, MultiblockBlocks.STELLAR_NEXUS_CONTROLLER.get());
    }
}

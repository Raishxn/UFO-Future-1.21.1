package com.raishxn.ufo.screen;

import appeng.menu.AEBaseMenu;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.QuantumComputationNexusControllerBE;
import com.raishxn.ufo.init.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/** Read-only, server-synchronised diagnostics for the Quantum Computation Nexus. */
public final class QuantumComputationNexusMenu extends AEBaseMenu {
    public static final int DATA_COUNT = 16;

    private final QuantumComputationNexusControllerBE blockEntity;
    private final ContainerLevelAccess levelAccess;
    private final ContainerData data;

    public QuantumComputationNexusMenu(int containerId, Inventory inventory,
                                       QuantumComputationNexusControllerBE blockEntity) {
        super(ModMenus.QUANTUM_COMPUTATION_NEXUS_MENU.get(), containerId, inventory, blockEntity);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        // The controller exposes a read-only server view. Clientbound DataSlot updates need a
        // writable mirror; otherwise ContainerData#set silently discards every synchronized value.
        this.data = inventory.player.level().isClientSide
                ? new SimpleContainerData(DATA_COUNT)
                : blockEntity.getMenuData();
        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);
    }

    public QuantumComputationNexusControllerBE getBlockEntity() {
        return blockEntity;
    }

    public boolean isFormed() {
        return data.get(0) == 1;
    }

    public boolean isGridActive() {
        return data.get(1) == 1;
    }

    public boolean isCpuOnline() {
        return data.get(2) == 1;
    }

    public int getModuleCount() {
        return Math.max(0, data.get(3));
    }

    public int getStorageModuleCount() {
        return Math.max(0, data.get(4));
    }

    public int getCoProcessorModuleCount() {
        return Math.max(0, data.get(5));
    }

    public int getCpuPartitionCount() {
        return Math.max(0, data.get(14));
    }

    public boolean isInfiniteMode() {
        return data.get(15) == 1;
    }

    public BigInteger getStorageBytes() {
        return assembleAmount(6);
    }

    public BigInteger getParallelLanes() {
        return assembleAmount(10);
    }

    private BigInteger assembleAmount(int start) {
        BigInteger result = BigInteger.ZERO;
        for (int part = 0; part < 4; part++) {
            result = result.or(BigInteger.valueOf(data.get(start + part) & 0xFFFFL).shiftLeft(part * 16));
        }
        return result;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(levelAccess, player, MultiblockBlocks.QUANTUM_COMPUTATION_NEXUS_CONTROLLER.get());
    }
}

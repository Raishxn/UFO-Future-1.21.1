package com.raishxn.ufo.screen;

import appeng.menu.AEBaseMenu;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import com.raishxn.ufo.init.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Server-synchronised dashboard for the Quantum Pattern Fabrication Matrix. */
public final class QuantumPatternFabricationMatrixMenu extends AEBaseMenu {
    public static final int DATA_COUNT = 8;

    private final QuantumPatternFabricationMatrixControllerBE blockEntity;
    private final ContainerLevelAccess levelAccess;
    private final ContainerData data;

    public QuantumPatternFabricationMatrixMenu(int containerId, Inventory inventory,
                                                QuantumPatternFabricationMatrixControllerBE blockEntity) {
        super(ModMenus.QUANTUM_PATTERN_FABRICATION_MATRIX_MENU.get(), containerId, inventory, blockEntity);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        // The block entity's ContainerData is a read-only server view. On the client,
        // DataSlot updates must have writable storage or every synchronized value is
        // silently discarded (which made a formed Matrix render as unformed).
        this.data = inventory.player.level().isClientSide
                ? new SimpleContainerData(DATA_COUNT)
                : blockEntity.getMenuData();
        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);
    }

    public QuantumPatternFabricationMatrixControllerBE getBlockEntity() { return blockEntity; }
    public boolean isFormed() { return data.get(0) == 1; }
    public boolean isGridActive() { return data.get(1) == 1; }
    public int getFieldCount() { return Math.max(0, data.get(2)); }
    public int getStoredPatternCount() { return Math.max(0, data.get(3)); }
    public int getPatternCapacity() { return Math.max(0, data.get(4)); }
    public int getTier1Fields() { return Math.max(0, data.get(5)); }
    public int getTier2Fields() { return Math.max(0, data.get(6)); }
    public int getTier3Fields() { return Math.max(0, data.get(7)); }

    public void openPatternManagement() {
        if (isClientSide()) {
            com.raishxn.ufo.network.ModPackets.sendToServer(
                    new com.raishxn.ufo.network.packet.PacketOpenPatternMatrixPatterns(
                            blockEntity.getBlockPos()));
            return;
        }
        if (getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            blockEntity.openPatternManagement(serverPlayer);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(levelAccess, player,
                MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get());
    }
}

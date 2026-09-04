package com.raishxn.ufo.screen;

import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.fluid.ModFluids;
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
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class StellarNexusControllerMenu extends AbstractContainerMenu {

    private static final int DISPLAYED_REQUIREMENT_ROWS = 7;
    private static final int REQUIREMENT_DATA_START = 24;
    private static final int REQUIREMENT_DATA_PARTS = 4;
    private static final int DATA_COUNT = REQUIREMENT_DATA_START
            + DISPLAYED_REQUIREMENT_ROWS * REQUIREMENT_DATA_PARTS;

    private final StellarNexusControllerBE blockEntity;
    private final ContainerLevelAccess levelAccess;
    private final ContainerData data;

    public StellarNexusControllerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(DATA_COUNT));
    }

    public StellarNexusControllerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.STELLAR_NEXUS_CONTROLLER_MENU.get(), id);
        checkContainerSize(inv, 0);
        this.blockEntity = (StellarNexusControllerBE) entity;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());
        this.data = data;

        // Data mapping:
        // 0 = progress, 1 = maxProgress, 2 = assembled
        // 3 = fieldLevel, 4 = energyPercent, 5 = running
        // 6 = heatLevel (0-1000), 7 = safeMode, 8 = cooldownTimer
        // 9-12: energyBuffer (4 shorts for 64-bit long)
        // 13-16: energyCapacity (4 shorts for 64-bit long), 20: paused
        // 21: coolant kind, 22-23: buffered coolant amount
        // 24-51: available amount for each of the 7 visible resource rows
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

    /** Energy charge percentage 0-100 */
    public int getEnergyPercent() {
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

    /** Current AE energy stored (64-bit reassembled from 4 shorts) */
    public long getEnergyBuffer() {
        return (this.data.get(9) & 0xFFFFL) |
               ((this.data.get(10) & 0xFFFFL) << 16) |
               ((this.data.get(11) & 0xFFFFL) << 32) |
               ((this.data.get(12) & 0xFFFFL) << 48);
    }

    /** Maximum AE energy capacity (64-bit reassembled from 4 shorts) */
    public long getEnergyCapacity() {
        return (this.data.get(13) & 0xFFFFL) |
               ((this.data.get(14) & 0xFFFFL) << 16) |
               ((this.data.get(15) & 0xFFFFL) << 32) |
               ((this.data.get(16) & 0xFFFFL) << 48);
    }
    
    public boolean isAutoStart() {
        return this.data.get(17) == 1;
    }
    
    public boolean isSimulationLocked() {
        return this.data.get(18) == 1;
    }

    public boolean isOverclocked() {
        return this.data.get(19) == 1;
    }

    public boolean isPaused() {
        return this.data.get(20) == 1;
    }

    public FluidStack getBufferedCoolant() {
        int amount = (this.data.get(22) & 0xFFFF) | ((this.data.get(23) & 0xFFFF) << 16);
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        return switch (this.data.get(21)) {
            case 1 -> new FluidStack(ModFluids.SOURCE_GELID_CRYOTHEUM.get(), amount);
            case 2 -> new FluidStack(ModFluids.SOURCE_STABLE_COOLANT.get(), amount);
            case 3 -> new FluidStack(ModFluids.SOURCE_TEMPORAL_FLUID.get(), amount);
            default -> FluidStack.EMPTY;
        };
    }

    /** Current amount in the connected ME system for one displayed resource row. */
    public long getRequirementAvailable(int row) {
        if (row < 0 || row >= DISPLAYED_REQUIREMENT_ROWS) {
            return 0L;
        }
        int start = REQUIREMENT_DATA_START + row * REQUIREMENT_DATA_PARTS;
        return (this.data.get(start) & 0xFFFFL)
                | ((this.data.get(start + 1) & 0xFFFFL) << 16)
                | ((this.data.get(start + 2) & 0xFFFFL) << 32)
                | ((this.data.get(start + 3) & 0xFFFFL) << 48);
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

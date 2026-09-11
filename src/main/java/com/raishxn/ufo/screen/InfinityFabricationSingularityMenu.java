package com.raishxn.ufo.screen;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.InfinityFabricationSingularityControllerBE;
import com.raishxn.ufo.crafting.SingularityCraftingMode;
import com.raishxn.ufo.init.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Server-synchronised routing dashboard for the Infinity Fabrication Singularity. */
public final class InfinityFabricationSingularityMenu extends AEBaseMenu {
    public static final int DATA_COUNT = 14;
    public static final int PATTERN_SLOTS = 9;
    public static final int BUTTON_PAGE_BASE = 1_000;
    private static final int PAGE_DATA_COUNT = 3;

    private final InfinityFabricationSingularityControllerBE blockEntity;
    private final ContainerLevelAccess levelAccess;
    private final ContainerData data;
    private final ContainerData pageData;
    private final ContainerData enabledData;
    private int serverPage;

    public InfinityFabricationSingularityMenu(int containerId, Inventory inventory,
                                               InfinityFabricationSingularityControllerBE blockEntity) {
        super(ModMenus.INFINITY_FABRICATION_SINGULARITY_MENU.get(), containerId, inventory, blockEntity);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.data = inventory.player.level().isClientSide
                ? new SimpleContainerData(DATA_COUNT)
                : blockEntity.getMenuData();
        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);

        if (inventory.player.level().isClientSide) {
            pageData = new SimpleContainerData(PAGE_DATA_COUNT);
            pageData.set(1, 1);
        } else {
            pageData = new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case 0 -> serverPage;
                        case 1 -> InfinityFabricationSingularityMenu.this.blockEntity.getPageCount();
                        case 2 -> InfinityFabricationSingularityMenu.this.blockEntity.getManagementSlotCount();
                        default -> 0;
                    };
                }

                @Override public void set(int index, int value) { }
                @Override public int getCount() { return PAGE_DATA_COUNT; }
            };
        }
        addDataSlots(pageData);

        if (inventory.player.level().isClientSide) {
            enabledData = new SimpleContainerData(PATTERN_SLOTS);
        } else {
            enabledData = new ContainerData() {
                @Override
                public int get(int index) {
                    int physical = serverPage * PATTERN_SLOTS + index;
                    return InfinityFabricationSingularityMenu.this.blockEntity
                            .isPatternEnabled(physical) ? 1 : 0;
                }

                @Override public void set(int index, int value) { }
                @Override public int getCount() { return PATTERN_SLOTS; }
            };
        }
        addDataSlots(enabledData);
        registerClientAction("togglePattern", Integer.class, this::toggleVisiblePattern);

        InternalInventory pageInventory = inventory.player.level().isClientSide
                ? new AppEngInternalInventory(PATTERN_SLOTS)
                : new PageWindowInventory(blockEntity.getAccessiblePatternInventory());
        for (int slot = 0; slot < PATTERN_SLOTS; slot++) {
            PatternSlot patternSlot = new PatternSlot(pageInventory, slot);
            addSlot(patternSlot, SlotSemantics.MACHINE_INPUT);
        }
        createPlayerInventorySlots(inventory);
    }

    public InfinityFabricationSingularityControllerBE getBlockEntity() { return blockEntity; }
    public boolean isFormed() { return data.get(0) == 1; }
    public boolean isGridActive() { return data.get(1) == 1; }
    public boolean isOperational() { return data.get(2) == 1; }
    public int getComputeProviders() { return Math.max(0, data.get(3)); }
    public int getPatternProviders() { return Math.max(0, data.get(4)); }
    public int getPatternCount() { return Math.max(0, data.get(5)); }
    public int getActiveJobs() { return Math.max(0, data.get(6)); }
    public int getConflicts() { return Math.max(0, data.get(7)); }
    public int getQueuedRoutes() { return Math.max(0, data.get(8)); }
    public SingularityCraftingMode getCraftingMode() { return SingularityCraftingMode.byOrdinal(data.get(9)); }
    public int getRouteLimit() { return Math.max(0, data.get(10)); }
    public int getTier1Fields() { return Math.max(0, data.get(11)); }
    public int getTier2Fields() { return Math.max(0, data.get(12)); }
    public int getTier3Fields() { return Math.max(0, data.get(13)); }
    public int getCurrentPage() { return Math.max(0, pageData.get(0)); }
    public int getPageCount() { return Math.max(1, pageData.get(1)); }
    public int getPatternCapacity() { return Math.max(0, pageData.get(2)); }
    public boolean isVisiblePatternEnabled(int slot) {
        return slot >= 0 && slot < PATTERN_SLOTS && enabledData.get(slot) != 0;
    }
    public static int pageButton(int page) { return BUTTON_PAGE_BASE + Math.max(0, page); }

    public void toggleVisiblePattern(int slot) {
        if (slot < 0 || slot >= PATTERN_SLOTS) return;
        if (isClientSide()) {
            sendClientAction("togglePattern", slot);
            return;
        }
        blockEntity.togglePatternEnabled(serverPage * PATTERN_SLOTS + slot);
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (isClientSide() || id < BUTTON_PAGE_BASE) return false;
        int requested = id - BUTTON_PAGE_BASE;
        int clamped = Math.max(0, Math.min(requested, blockEntity.getPageCount() - 1));
        if (clamped == serverPage) return true;
        serverPage = clamped;
        broadcastFullState();
        return true;
    }

    @Override
    public void broadcastChanges() {
        if (!isClientSide()) {
            int clamped = Math.max(0, Math.min(serverPage, blockEntity.getPageCount() - 1));
            if (clamped != serverPage) {
                serverPage = clamped;
                broadcastFullState();
            }
        }
        super.broadcastChanges();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = source.getItem();
        ItemStack original = sourceStack.copy();
        boolean moved;
        if (index < PATTERN_SLOTS) {
            moved = moveItemStackTo(sourceStack, PATTERN_SLOTS, slots.size(), true);
        } else {
            if (!InfinityFabricationSingularityControllerBE.isSupportedPattern(sourceStack)) {
                return ItemStack.EMPTY;
            }
            moved = moveItemStackTo(sourceStack, 0, PATTERN_SLOTS, false);
        }
        if (!moved) return ItemStack.EMPTY;
        if (sourceStack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, sourceStack);
        return original;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(levelAccess, player,
                MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get());
    }

    private final class PageWindowInventory extends BaseInternalInventory {
        private final InternalInventory backing;

        private PageWindowInventory(InternalInventory backing) { this.backing = backing; }
        @Override public int size() { return PATTERN_SLOTS; }

        private int physicalSlot(int slot) {
            int physical = serverPage * PATTERN_SLOTS + slot;
            return slot >= 0 && slot < PATTERN_SLOTS && physical < backing.size() ? physical : -1;
        }

        @Override public ItemStack getStackInSlot(int slot) {
            int physical = physicalSlot(slot);
            return physical >= 0 ? backing.getStackInSlot(physical) : ItemStack.EMPTY;
        }

        @Override public void setItemDirect(int slot, ItemStack stack) {
            int physical = physicalSlot(slot);
            if (physical >= 0) backing.setItemDirect(physical, stack);
        }

        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int physical = physicalSlot(slot);
            return physical >= 0 ? backing.insertItem(physical, stack, simulate) : stack;
        }

        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int physical = physicalSlot(slot);
            return physical >= 0 ? backing.extractItem(physical, amount, simulate) : ItemStack.EMPTY;
        }

        @Override public int getSlotLimit(int slot) {
            int physical = physicalSlot(slot);
            return physical >= 0 ? backing.getSlotLimit(physical) : 0;
        }

        @Override public boolean isItemValid(int slot, ItemStack stack) {
            int physical = physicalSlot(slot);
            return physical >= 0 && backing.isItemValid(physical, stack);
        }
    }

    public static final class PatternSlot extends AppEngSlot {
        private PatternSlot(InternalInventory inventory, int index) { super(inventory, index); }

        @Override public boolean mayPlace(ItemStack stack) {
            return InfinityFabricationSingularityControllerBE.isSupportedPattern(stack)
                    && super.mayPlace(stack);
        }

        @Override public int getMaxStackSize() { return 1; }

        @Override
        public ItemStack getDisplayStack() {
            ItemStack pattern = super.getDisplayStack();
            if (!pattern.isEmpty() && pattern.getItem() instanceof EncodedPatternItem<?> encoded) {
                ItemStack output = encoded.getOutput(pattern);
                if (!output.isEmpty()) return output;
            }
            return pattern;
        }
    }
}

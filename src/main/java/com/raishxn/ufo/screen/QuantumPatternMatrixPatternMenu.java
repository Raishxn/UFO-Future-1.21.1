package com.raishxn.ufo.screen;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.raishxn.ufo.block.entity.QuantumPatternFabricationMatrixControllerBE;
import com.raishxn.ufo.init.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Server-paged pattern library: only the 54 visible slots are synchronized. */
public final class QuantumPatternMatrixPatternMenu extends AbstractContainerMenu {
    public static final int COLUMNS = 9;
    public static final int VISIBLE_ROWS = 6;
    public static final int WINDOW_SLOTS = COLUMNS * VISIBLE_ROWS;
    public static final int PATTERN_X = 13;
    public static final int PATTERN_Y = 22;
    public static final int PLAYER_INVENTORY_X = 13;
    public static final int PLAYER_INVENTORY_Y = 141;
    public static final int PLAYER_HOTBAR_Y = 199;
    public static final int BUTTON_PAGE_BASE = 1_000;
    private static final int DATA_COUNT = 3;
    private static final int SLOT_SPACING = 18;

    private final BlockPos blockPos;
    @Nullable private final QuantumPatternFabricationMatrixControllerBE host;
    private final List<PatternSlot> patternSlots = new ArrayList<>(WINDOW_SLOTS);
    private final ContainerData pageData;
    private final int initialTotalSlots;
    private int serverPage;
    private long patternContentRevision;

    public QuantumPatternMatrixPatternMenu(int id, Inventory playerInventory,
                                            QuantumPatternFabricationMatrixControllerBE host) {
        this(id, playerInventory, host.getBlockPos(), host, host.getManagementSlotCount());
    }

    private QuantumPatternMatrixPatternMenu(int id, Inventory playerInventory, BlockPos blockPos,
                                             @Nullable QuantumPatternFabricationMatrixControllerBE host,
                                             int totalSlots) {
        super(ModMenus.QUANTUM_PATTERN_MATRIX_PATTERN_MENU.get(), id);
        this.blockPos = blockPos;
        this.host = host;
        this.initialTotalSlots = Math.max(0, totalSlots);
        if (host == null) {
            SimpleContainerData clientData = new SimpleContainerData(DATA_COUNT);
            clientData.set(0, 0);
            clientData.set(1, pageCount(initialTotalSlots));
            clientData.set(2, initialTotalSlots);
            pageData = clientData;
        } else {
            pageData = new ContainerData() {
                @Override
                public int get(int index) {
                    int total = QuantumPatternMatrixPatternMenu.this.host.getManagementSlotCount();
                    return switch (index) {
                        case 0 -> serverPage;
                        case 1 -> pageCount(total);
                        case 2 -> total;
                        default -> 0;
                    };
                }

                @Override public void set(int index, int value) { }
                @Override public int getCount() { return DATA_COUNT; }
            };
        }
        addDataSlots(pageData);

        InternalInventory windowInventory = host == null
                ? new AppEngInternalInventory(WINDOW_SLOTS)
                : new PageWindowInventory(host.getAccessiblePatternInventory());
        for (int slot = 0; slot < WINDOW_SLOTS; slot++) {
            int row = slot / COLUMNS;
            int column = slot % COLUMNS;
            PatternSlot patternSlot = new PatternSlot(windowInventory, slot);
            patternSlot.x = PATTERN_X + column * SLOT_SPACING;
            patternSlot.y = PATTERN_Y + row * SLOT_SPACING;
            patternSlots.add(patternSlot);
            addSlot(patternSlot);
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                addSlot(new Slot(playerInventory, column + row * COLUMNS + COLUMNS,
                        PLAYER_INVENTORY_X + column * SLOT_SPACING,
                        PLAYER_INVENTORY_Y + row * SLOT_SPACING));
            }
        }
        for (int column = 0; column < COLUMNS; column++) {
            addSlot(new Slot(playerInventory, column,
                    PLAYER_INVENTORY_X + column * SLOT_SPACING, PLAYER_HOTBAR_Y));
        }
    }

    public static QuantumPatternMatrixPatternMenu clientCreate(
            int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int slotCount = Mth.clamp(buffer.readVarInt(), 0,
                QuantumPatternFabricationMatrixControllerBE.MAX_PATTERN_SLOTS);
        return new QuantumPatternMatrixPatternMenu(id, playerInventory, pos, null, slotCount);
    }

    public static void writeExtraData(FriendlyByteBuf buffer,
                                      QuantumPatternFabricationMatrixControllerBE host) {
        buffer.writeBlockPos(host.getBlockPos());
        buffer.writeVarInt(host.getManagementSlotCount());
    }

    @Override
    public boolean stillValid(Player player) {
        if (host != null) {
            if (host.isRemoved() || host.getLevel() == null || player.level() != host.getLevel()
                    || host.getLevel().getBlockEntity(blockPos) != host) return false;
        }
        return player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (host == null || id < BUTTON_PAGE_BASE) return false;
        int requested = id - BUTTON_PAGE_BASE;
        int clamped = Mth.clamp(requested, 0, Math.max(0, pageCount(host.getManagementSlotCount()) - 1));
        if (clamped == serverPage) return true;
        serverPage = clamped;
        broadcastFullState();
        return true;
    }

    @Override
    public void broadcastChanges() {
        if (host != null) {
            int clamped = Mth.clamp(serverPage, 0,
                    Math.max(0, pageCount(host.getManagementSlotCount()) - 1));
            if (clamped != serverPage) {
                serverPage = clamped;
                broadcastFullState();
            }
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = source.getItem();
        ItemStack original = sourceStack.copy();
        boolean moved;
        if (index < WINDOW_SLOTS) {
            moved = moveItemStackTo(sourceStack, WINDOW_SLOTS, slots.size(), true);
        } else {
            if (!QuantumPatternFabricationMatrixControllerBE.isSupportedPattern(sourceStack)) {
                return ItemStack.EMPTY;
            }
            moved = moveItemStackTo(sourceStack, 0, WINDOW_SLOTS, false);
        }
        if (!moved) return ItemStack.EMPTY;
        if (sourceStack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, sourceStack);
        return original;
    }

    public int getPatternSlotCount() { return WINDOW_SLOTS; }
    public int getTotalPatternSlots() { return Math.max(0, pageData.get(2)); }
    public int getCurrentPage() { return Math.max(0, pageData.get(0)); }
    public int getPageCount() { return Math.max(1, pageData.get(1)); }
    public long getPatternContentRevision() { return patternContentRevision; }
    public List<PatternSlot> getPatternSlots() { return Collections.unmodifiableList(patternSlots); }
    public static int pageButton(int page) { return BUTTON_PAGE_BASE + Math.max(0, page); }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        super.setItem(slotId, stateId, stack);
        if (slotId >= 0 && slotId < WINDOW_SLOTS) patternContentRevision++;
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        super.initializeContents(stateId, items, carried);
        patternContentRevision++;
    }

    private static int pageCount(int slots) {
        return Math.max(1, (Math.max(0, slots) + WINDOW_SLOTS - 1) / WINDOW_SLOTS);
    }

    private final class PageWindowInventory extends BaseInternalInventory {
        private final InternalInventory backing;

        private PageWindowInventory(InternalInventory backing) { this.backing = backing; }
        @Override public int size() { return WINDOW_SLOTS; }

        private int physicalSlot(int windowSlot) {
            int physical = serverPage * WINDOW_SLOTS + windowSlot;
            return windowSlot >= 0 && windowSlot < WINDOW_SLOTS && physical < backing.size()
                    ? physical : -1;
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
        private PatternSlot(InternalInventory inventory, int patternIndex) {
            super(inventory, patternIndex);
        }

        @Override public boolean mayPlace(ItemStack stack) {
            return QuantumPatternFabricationMatrixControllerBE.isSupportedPattern(stack)
                    && super.mayPlace(stack);
        }

        @Override public int getMaxStackSize() { return 1; }

        @Override
        public ItemStack getDisplayStack() {
            ItemStack pattern = super.getDisplayStack();
            if (!pattern.isEmpty() && pattern.getItem() instanceof EncodedPatternItem<?> encodedPattern) {
                ItemStack output = encodedPattern.getOutput(pattern);
                if (!output.isEmpty()) return output;
            }
            return pattern;
        }
    }
}

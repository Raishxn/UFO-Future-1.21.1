package com.raishxn.ufo.block.entity;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.helpers.IPriorityHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.raishxn.ufo.api.ae.QuantumGridLinkHost;
import com.raishxn.ufo.api.ae.QuantumPatternMatrixHost;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.StructureInvalidationTarget;
import com.raishxn.ufo.api.multiblock.StructureMembershipIndex;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.QuantumPatternFabricationMatrixControllerBlock;
import com.raishxn.ufo.block.entity.pattern.QuantumPatternFabricationMatrixPatternFactory;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.init.ModMenus;
import com.raishxn.ufo.screen.QuantumPatternFabricationMatrixMenu;
import com.raishxn.ufo.screen.QuantumPatternMatrixPatternMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/** Owns the field-scaled pattern library exposed through the Matrix Grid Link. */
public final class QuantumPatternFabricationMatrixControllerBE extends AENetworkedBlockEntity
        implements StructureInvalidationTarget, MenuProvider, IMultiblockController,
        QuantumGridLinkHost, QuantumPatternMatrixHost, IPriorityHost, InternalInventoryHost {
    public static final int SLOTS_PER_MK1_FIELD = 256;
    public static final int SLOTS_PER_MK2_FIELD = 512;
    public static final int SLOTS_PER_MK3_FIELD = 1024;
    public static final int FIELD_POSITION_COUNT = 22;
    public static final int MAX_PATTERN_SLOTS = FIELD_POSITION_COUNT * SLOTS_PER_MK3_FIELD;

    private static final int PERIODIC_RESCAN_TICKS = 20;
    private static final String TAG_PRIORITY = "PatternPriority";
    private static final String TAG_PATTERNS = "Patterns";

    private final AppEngInternalInventory patternInventory =
            new AppEngInternalInventory(this, MAX_PATTERN_SLOTS, 1) {
                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return stack.isEmpty() || isSupportedPattern(stack);
                }
            };
    private final InternalInventory accessiblePatternInventory = new AccessiblePatternInventory();
    private final InternalInventory terminalPatternInventory = new TerminalPatternInventory();
    private List<Integer> terminalPatternSlots = List.of();
    private boolean terminalPatternSlotsDirty = true;

    private boolean formed;
    private boolean structureDirty = true;
    private boolean patternUpdatePending;
    private boolean availablePatternsDirty = true;
    private List<IPatternDetails> availablePatterns = List.of();
    private boolean storedPatternCountDirty = true;
    private int storedPatternCount;
    private long nextPeriodicScan;
    @Nullable private BlockPos gridLinkPos;
    private int patternPriority;
    private int tier1Fields;
    private int tier2Fields;
    private int tier3Fields;
    private int patternCapacity;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> formed ? 1 : 0;
                case 1 -> isGridLinkActive() ? 1 : 0;
                case 2 -> tier1Fields + tier2Fields + tier3Fields;
                case 3 -> getStoredPatternCount();
                case 4 -> patternCapacity;
                case 5 -> tier1Fields;
                case 6 -> tier2Fields;
                case 7 -> tier3Fields;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) { }
        @Override public int getCount() { return QuantumPatternFabricationMatrixMenu.DATA_COUNT; }
    };

    public QuantumPatternFabricationMatrixControllerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER_BE.get(), pos, state);
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return super.createMainNode()
                .setTagName("quantum_pattern_fabrication_matrix")
                .setVisualRepresentation(MultiblockBlocks.QUANTUM_PATTERN_FABRICATION_MATRIX_CONTROLLER.get())
                .setIdlePowerUsage(0.0D);
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        long gameTime = level.getGameTime();
        if (structureDirty || gameTime >= nextPeriodicScan) {
            structureDirty = false;
            nextPeriodicScan = gameTime + PERIODIC_RESCAN_TICKS;
            refreshStructure(serverLevel);
        }
        if (patternUpdatePending) {
            patternUpdatePending = false;
            setChanged();
            QuantumGridLinkBE link = getGridLink();
            if (link != null) link.patternsChanged();
        }
        updateVisualState();
    }

    @Override public boolean isAssembled() { return formed; }

    @Override
    public void scanStructure(Level scanLevel) {
        if (scanLevel instanceof ServerLevel serverLevel && scanLevel == level) {
            structureDirty = false;
            nextPeriodicScan = scanLevel.getGameTime() + PERIODIC_RESCAN_TICKS;
            refreshStructure(serverLevel);
            updateVisualState();
        }
    }

    @Override public void addPart(BlockPos partPos) { markStructureDirty(); }
    @Override public void removePart(BlockPos partPos) { markStructureDirty(); }
    @Override public List<BlockPos> getParts() { return gridLinkPos == null ? List.of() : List.of(gridLinkPos); }
    @Override public BlockPos getControllerPos() { return worldPosition; }

    private void refreshStructure(ServerLevel serverLevel) {
        Direction structureFacing = getStructureFacing();
        MultiblockPattern pattern = getDefinition().pattern();
        indexCompleteFootprint(pattern, structureFacing);
        MultiblockPattern.MatchResult match = pattern.matchFast(serverLevel, worldPosition, structureFacing);
        if (match.hasUnloadedPositions()) return;
        if (!match.isValid()) {
            deform();
            return;
        }

        List<BlockPos> links = pattern.getExpectedPositions(worldPosition, structureFacing, 'L');
        if (links.size() != 1) {
            deform();
            return;
        }

        int mk1 = 0;
        int mk2 = 0;
        int mk3 = 0;
        for (BlockPos fieldPos : pattern.getExpectedPositions(worldPosition, structureFacing, 'F')) {
            BlockState fieldState = serverLevel.getBlockState(fieldPos);
            if (fieldState.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get())) mk1++;
            else if (fieldState.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get())) mk2++;
            else if (fieldState.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get())) mk3++;
        }

        BlockPos discoveredLink = links.getFirst().immutable();
        if (gridLinkPos != null && !gridLinkPos.equals(discoveredLink)) detachGridLink();
        gridLinkPos = discoveredLink;
        tier1Fields = mk1;
        tier2Fields = mk2;
        tier3Fields = mk3;
        patternCapacity = mk1 * SLOTS_PER_MK1_FIELD
                + mk2 * SLOTS_PER_MK2_FIELD
                + mk3 * SLOTS_PER_MK3_FIELD;
        terminalPatternSlotsDirty = true;

        // Ownership must be visible before the link refreshes its cable capability.
        formed = true;
        if (serverLevel.getBlockEntity(discoveredLink) instanceof QuantumGridLinkBE link) {
            link.linkToController(worldPosition);
            IGridNode controllerNode = getMainNode().getNode();
            link.synchronizeInternalNodes(controllerNode == null ? List.of() : List.of(controllerNode));
            link.refreshGridConnection();
        }
        setChanged();
    }

    private void deform() {
        boolean wasFormed = formed;
        detachGridLink();
        formed = false;
        tier1Fields = 0;
        tier2Fields = 0;
        tier3Fields = 0;
        patternCapacity = 0;
        terminalPatternSlotsDirty = true;
        if (wasFormed) onGridConnectableSidesChanged();
        setChanged();
    }

    private void indexCompleteFootprint(MultiblockPattern pattern, Direction facing) {
        if (level == null) return;
        List<Long> footprint = pattern.getSymbols().stream()
                .flatMap(symbol -> pattern.getExpectedPositions(worldPosition, facing, symbol).stream())
                .map(BlockPos::asLong)
                .toList();
        StructureMembershipIndex.INSTANCE.register(
                level.dimension().location().toString(), worldPosition.asLong(), footprint);
    }

    @Nullable
    private QuantumGridLinkBE getGridLink() {
        if (level == null || gridLinkPos == null || !level.hasChunkAt(gridLinkPos)) return null;
        return level.getBlockEntity(gridLinkPos) instanceof QuantumGridLinkBE link ? link : null;
    }

    private void detachGridLink() {
        QuantumGridLinkBE link = getGridLink();
        if (link != null && worldPosition.equals(link.getControllerPos())) link.unlinkFromController();
        gridLinkPos = null;
    }

    public void onControllerBroken() {
        if (level != null && !level.isClientSide()) {
            detachGridLink();
            StructureMembershipIndex.INSTANCE.unregister(
                    level.dimension().location().toString(), worldPosition.asLong());
        }
    }

    @Override public void markStructureDirty() { structureDirty = true; }
    @Override public Set<Direction> getGridConnectableSides(BlockOrientation orientation) { return Set.of(); }
    @Override public AECableType getCableConnectionType(Direction direction) { return AECableType.NONE; }
    @Override public boolean isGridLinkFormed() { return formed; }
    @Override public boolean ownsGridLink(BlockPos linkPos) { return formed && linkPos.equals(gridLinkPos); }
    @Override public void onGridLinkStateChanged() { updateVisualState(); }

    private boolean isGridLinkActive() {
        QuantumGridLinkBE link = getGridLink();
        return link != null && link.isNetworkReady();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) updateVisualState();
    }

    private void updateVisualState() {
        if (level == null || level.isClientSide() || isRemoved()) return;
        BlockState current = level.getBlockState(worldPosition);
        if (!(current.getBlock() instanceof QuantumPatternFabricationMatrixControllerBlock)) return;
        boolean powered = formed && isGridLinkActive();
        BlockState updated = current
                .setValue(QuantumPatternFabricationMatrixControllerBlock.FORMED, formed)
                .setValue(QuantumPatternFabricationMatrixControllerBlock.POWERED, powered);
        if (updated != current) level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
    }

    public int getStoredPatternCount() {
        if (storedPatternCountDirty) {
            int total = 0;
            for (int slot = 0; slot < patternInventory.size(); slot++) {
                if (!patternInventory.getStackInSlot(slot).isEmpty()) total++;
            }
            storedPatternCount = total;
            storedPatternCountDirty = false;
        }
        return storedPatternCount;
    }

    public int getPatternCapacity() { return patternCapacity; }

    /** Includes overflow after a field downgrade so stored patterns never become inaccessible. */
    public int getManagementSlotCount() {
        int highestUsed = 0;
        for (int slot = patternInventory.size() - 1; slot >= 0; slot--) {
            if (!patternInventory.getStackInSlot(slot).isEmpty()) {
                highestUsed = slot + 1;
                break;
            }
        }
        return Math.max(patternCapacity, highestUsed);
    }

    public InternalInventory getAccessiblePatternInventory() { return accessiblePatternInventory; }

    /**
     * Compact view used by AE2's Pattern Access Terminal. It contains every occupied
     * physical slot plus one writable empty slot, so AE2 never polls 22,528 empty
     * entries every tick merely because the Matrix has a large maximum capacity.
     */
    public InternalInventory getTerminalPatternInventory() { return terminalPatternInventory; }

    public List<IPatternDetails> getAvailablePatterns() {
        if (!formed || level == null) return List.of();
        if (availablePatternsDirty) {
            List<IPatternDetails> decoded = new ArrayList<>(getStoredPatternCount());
            int slots = getManagementSlotCount();
            for (int slot = 0; slot < slots; slot++) {
                ItemStack stack = patternInventory.getStackInSlot(slot);
                if (!isSupportedPattern(stack)) continue;
                try {
                    IPatternDetails details = PatternDetailsHelper.decodePattern(stack, level);
                    if (details != null) decoded.add(details);
                } catch (RuntimeException ignored) {
                    // A broken third-party pattern must not hide the remaining catalog.
                }
            }
            availablePatterns = List.copyOf(decoded);
            availablePatternsDirty = false;
        }
        return availablePatterns;
    }

    @Override
    public boolean insertEncodedPattern(ItemStack stack) {
        if (!formed || !isGridLinkActive() || !isSupportedPattern(stack)) return false;
        ItemStack one = stack.copyWithCount(1);
        for (int slot = 0; slot < patternCapacity; slot++) {
            if (patternInventory.getStackInSlot(slot).isEmpty()) {
                patternInventory.setItemDirect(slot, one);
                return true;
            }
        }
        return false;
    }

    /** The Matrix is a virtual assembler, never a generic processing-pattern machine. */
    public static boolean isSupportedPattern(ItemStack stack) {
        if (stack.isEmpty() || !PatternDetailsHelper.isEncodedPattern(stack)) return false;
        return stack.is(AEItems.CRAFTING_PATTERN.asItem())
                || stack.is(AEItems.SMITHING_TABLE_PATTERN.asItem())
                || stack.is(AEItems.STONECUTTING_PATTERN.asItem());
    }

    @Override public int patternMatrixPriority() { return patternPriority; }
    @Override public long patternMatrixSortKey() { return worldPosition.asLong(); }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        terminalPatternSlotsDirty = true;
        storedPatternCountDirty = true;
        availablePatternsDirty = true;
        patternUpdatePending = true;
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        setChanged();
    }

    @Override
    public boolean isClientSide() {
        return level == null || level.isClientSide();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_PRIORITY, patternPriority);
        patternInventory.writeToNBT(tag, TAG_PATTERNS, registries);
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        patternPriority = tag.getInt(TAG_PRIORITY);
        patternInventory.readFromNBT(tag, TAG_PATTERNS, registries);
        storedPatternCountDirty = true;
        terminalPatternSlotsDirty = true;
        availablePatternsDirty = true;
    }

    public ContainerData getMenuData() { return menuData; }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ufo.quantum_pattern_fabrication_matrix_controller");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new QuantumPatternFabricationMatrixMenu(id, inventory, this);
    }

    public void openPatternManagement(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> new QuantumPatternMatrixPatternMenu(id, inventory, this),
                Component.translatable("gui.ufo.quantum_pattern_fabrication_matrix.pattern_management")),
                buffer -> QuantumPatternMatrixPatternMenu.writeExtraData(buffer, this));
    }

    @Override public int getPriority() { return patternPriority; }

    @Override
    public void setPriority(int priority) {
        if (patternPriority == priority) return;
        patternPriority = priority;
        setChanged();
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(ModMenus.QUANTUM_PATTERN_FABRICATION_MATRIX_MENU.get(), player, subMenu.getLocator());
    }

    @Override public ItemStack getMainMenuIcon() { return new ItemStack(getBlockState().getBlock()); }
    public MultiblockDefinition getDefinition() { return QuantumPatternFabricationMatrixPatternFactory.getDefinition(); }

    public Direction getControllerFacing() {
        return getBlockState().hasProperty(QuantumPatternFabricationMatrixControllerBlock.FACING)
                ? getBlockState().getValue(QuantumPatternFabricationMatrixControllerBlock.FACING)
                : Direction.NORTH;
    }

    /** The schema's local west side is its front, hence the quarter-turn from the visible face. */
    public Direction getStructureFacing() { return getControllerFacing().getClockWise(); }

    private final class AccessiblePatternInventory extends BaseInternalInventory {
        @Override public int size() { return getManagementSlotCount(); }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return slotIndex >= 0 && slotIndex < size()
                    ? patternInventory.getStackInSlot(slotIndex) : ItemStack.EMPTY;
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            if (slotIndex < 0 || slotIndex >= size()) return;
            if (!stack.isEmpty() && !isItemValid(slotIndex, stack)) return;
            patternInventory.setItemDirect(slotIndex, stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= patternCapacity || !isItemValid(slot, stack)) return stack;
            return patternInventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= 0 && slot < size()
                    ? patternInventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override public int getSlotLimit(int slot) { return slot >= 0 && slot < patternCapacity ? 1 : 0; }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < patternCapacity && isSupportedPattern(stack);
        }
    }

    private List<Integer> getTerminalPatternSlots() {
        if (!terminalPatternSlotsDirty) return terminalPatternSlots;
        List<Integer> slots = new ArrayList<>(Math.min(getStoredPatternCount() + 1, patternCapacity));
        int firstEmpty = -1;
        int visibleSize = getManagementSlotCount();
        for (int slot = 0; slot < visibleSize; slot++) {
            if (patternInventory.getStackInSlot(slot).isEmpty()) {
                if (firstEmpty < 0 && slot < patternCapacity) firstEmpty = slot;
            } else {
                slots.add(slot);
            }
        }
        if (firstEmpty >= 0) slots.add(firstEmpty);
        terminalPatternSlots = List.copyOf(slots);
        terminalPatternSlotsDirty = false;
        return terminalPatternSlots;
    }

    private final class TerminalPatternInventory extends BaseInternalInventory {
        @Override public int size() { return getTerminalPatternSlots().size(); }

        private int physicalSlot(int terminalSlot) {
            List<Integer> slots = getTerminalPatternSlots();
            return terminalSlot >= 0 && terminalSlot < slots.size() ? slots.get(terminalSlot) : -1;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            int physical = physicalSlot(slotIndex);
            return physical >= 0 ? patternInventory.getStackInSlot(physical) : ItemStack.EMPTY;
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            int physical = physicalSlot(slotIndex);
            if (physical < 0 || (!stack.isEmpty() && !isItemValid(slotIndex, stack))) return;
            patternInventory.setItemDirect(physical, stack);
        }

        @Override
        public ItemStack insertItem(int slotIndex, ItemStack stack, boolean simulate) {
            int physical = physicalSlot(slotIndex);
            if (physical < 0 || physical >= patternCapacity || !isItemValid(slotIndex, stack)) return stack;
            return patternInventory.insertItem(physical, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slotIndex, int amount, boolean simulate) {
            int physical = physicalSlot(slotIndex);
            return physical >= 0
                    ? patternInventory.extractItem(physical, amount, simulate) : ItemStack.EMPTY;
        }

        @Override public int getSlotLimit(int slotIndex) { return physicalSlot(slotIndex) >= 0 ? 1 : 0; }

        @Override
        public boolean isItemValid(int slotIndex, ItemStack stack) {
            int physical = physicalSlot(slotIndex);
            return physical >= 0 && physical < patternCapacity
                    && isSupportedPattern(stack);
        }
    }
}

package com.raishxn.ufo.block.entity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.raishxn.ufo.api.ae.QuantumGridLinkHost;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.StructureInvalidationTarget;
import com.raishxn.ufo.api.multiblock.StructureMembershipIndex;
import com.raishxn.ufo.block.InfinityFabricationSingularityControllerBlock;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.pattern.InfinityFabricationSingularityPatternFactory;
import com.raishxn.ufo.crafting.SingularityCraftingMode;
import com.raishxn.ufo.crafting.SingularityPatternCapacity;
import com.raishxn.ufo.diagnostic.MachineMetricKey;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.screen.InfinityFabricationSingularityMenu;
import com.raishxn.ufo.util.LoadedBlockEntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hosts the field-scaled crafting-pattern library and repeats enabled patterns as
 * aggregate stock-production routes executed by its Quantum Grid Link. Patterns stay
 * internal: they are never published to AE2's crafting service, so terminal crafting
 * keeps flowing through the player's normal autocrafting chain.
 */
public final class InfinityFabricationSingularityControllerBE extends AENetworkedBlockEntity
        implements StructureInvalidationTarget, MenuProvider, IMultiblockController, QuantumGridLinkHost,
        InternalInventoryHost {
    public static final int PATTERNS_PER_PAGE = SingularityPatternCapacity.PATTERNS_PER_PAGE;
    public static final int MAX_PATTERN_SLOTS = SingularityPatternCapacity.MAX_PATTERN_SLOTS;

    private static final int ROUTING_REFRESH_TICKS = 10;
    private static final String TAG_MODE = "CraftingMode";
    private static final String TAG_PATTERNS = "Patterns";
    private static final String TAG_DISABLED_PATTERNS = "DisabledPatterns";

    private final AppEngInternalInventory patternInventory =
            new AppEngInternalInventory(this, MAX_PATTERN_SLOTS, 1) {
                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return stack.isEmpty() || isSupportedPattern(stack);
                }
            };
    private final InternalInventory accessiblePatternInventory = new AccessiblePatternInventory();

    private boolean formed;
    private boolean structureDirty = true;
    private boolean routingDirty = true;
    private long nextRoutingRefresh;
    private long nextAutomaticCraft;
    @Nullable private BlockPos gridLinkPos;
    private List<BlockPos> energyHatchPositions = List.of();
    private SingularityCraftingMode craftingMode = SingularityCraftingMode.BALANCED;
    private List<IPatternDetails> routedPatterns = List.of();
    private int patternProviderCount;
    private int computeProviderCount;
    private int activeJobCount;
    private int conflictCount;
    private int tier1Fields;
    private int tier2Fields;
    private int tier3Fields;
    private int patternCapacity;
    private int autoCraftPatternOffset;
    private boolean storedPatternCountDirty = true;
    private int storedPatternCount;
    private final Set<Integer> disabledPatternSlots = new HashSet<>();
    private final boolean[] occupiedPatternSlots = new boolean[MAX_PATTERN_SLOTS];
    @Nullable private MachineMetricKey performanceMetricKey;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> formed ? 1 : 0;
                case 1 -> isGridLinkActive() ? 1 : 0;
                case 2 -> isOperational() ? 1 : 0;
                case 3 -> computeProviderCount;
                case 4 -> patternProviderCount;
                case 5 -> getStoredPatternCount();
                case 6 -> activeJobCount;
                case 7 -> conflictCount;
                case 8 -> getQueuedRouteCount();
                case 9 -> craftingMode.ordinal();
                case 10 -> getRouteLimit();
                case 11 -> tier1Fields;
                case 12 -> tier2Fields;
                case 13 -> tier3Fields;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) { }
        @Override public int getCount() { return InfinityFabricationSingularityMenu.DATA_COUNT; }
    };

    public InfinityFabricationSingularityControllerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFINITY_FABRICATION_SINGULARITY_CONTROLLER_BE.get(), pos, state);
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return super.createMainNode()
                .setTagName("infinity_fabrication_singularity")
                .setVisualRepresentation(MultiblockBlocks.INFINITY_FABRICATION_SINGULARITY_CONTROLLER.get())
                .setIdlePowerUsage(0.0D);
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        long gameTime = level.getGameTime();
        if (structureDirty) {
            structureDirty = false;
            refreshStructure(serverLevel);
        }
        if (routingDirty || gameTime >= nextRoutingRefresh) {
            routingDirty = false;
            nextRoutingRefresh = gameTime + ROUTING_REFRESH_TICKS;
            refreshRoutingState();
        }
        runAutomaticCrafting(gameTime);
        updateVisualState();
    }

    @Override public boolean isAssembled() { return formed; }

    @Override
    public void scanStructure(Level scanLevel) {
        if (scanLevel instanceof ServerLevel serverLevel && scanLevel == level) {
            structureDirty = false;
            refreshStructure(serverLevel);
            refreshRoutingState();
            updateVisualState();
        }
    }

    @Override
    public void onReady() {
        super.onReady();
        markStructureDirty();
    }

    @Override public void addPart(BlockPos partPos) { markStructureDirty(); }
    @Override public void removePart(BlockPos partPos) { markStructureDirty(); }
    @Override public List<BlockPos> getParts() {
        List<BlockPos> parts = new ArrayList<>(energyHatchPositions);
        if (gridLinkPos != null) parts.add(gridLinkPos);
        return List.copyOf(parts);
    }
    @Override public BlockPos getControllerPos() { return worldPosition; }

    private void refreshStructure(ServerLevel serverLevel) {
        MultiblockPattern pattern = getDefinition().pattern();
        long startedAt = System.nanoTime();
        try {
            Direction structureFacing = getStructureFacing();
            indexRelevantFootprint(pattern, structureFacing);
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

            List<BlockPos> discoveredEnergyHatches = match.partPositions().stream()
                    .filter(pos -> LoadedBlockEntityLookup.get(serverLevel, pos) instanceof MassiveOutputHatchBE hatch
                            && hatch.supportsEnergyInput())
                    .map(BlockPos::immutable).toList();
            if (discoveredEnergyHatches.isEmpty()) {
                deform();
                return;
            }
            detachEnergyHatchesNotIn(discoveredEnergyHatches);
            energyHatchPositions = discoveredEnergyHatches;
            for (BlockPos pos : energyHatchPositions) {
                if (LoadedBlockEntityLookup.get(serverLevel, pos) instanceof MassiveOutputHatchBE hatch
                        && !worldPosition.equals(hatch.getControllerPos())) hatch.linkToController(worldPosition);
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
            applyCraftingMode(SingularityCraftingMode.forFieldTiers(mk1, mk2, mk3));
            patternCapacity = calculatePatternCapacity(mk1, mk2, mk3);
            formed = true;
            if (serverLevel.getBlockEntity(discoveredLink) instanceof QuantumGridLinkBE link) {
                link.linkToController(worldPosition);
                IGridNode controllerNode = getMainNode().getNode();
                link.synchronizeInternalNodes(controllerNode == null ? List.of() : List.of(controllerNode));
                link.refreshGridConnection();
            }
            routingDirty = true;
            setChanged();
        } finally {
            MachinePerformanceRegistry.INSTANCE.recordScan(performanceMetricKey(),
                    System.nanoTime() - startedAt, pattern.getTestedPositionCount(), serverLevel.getGameTime());
        }
    }

    private MachineMetricKey performanceMetricKey() {
        if (performanceMetricKey == null) {
            performanceMetricKey = new MachineMetricKey(serverDimension(), worldPosition.asLong(),
                    getClass().getSimpleName());
        }
        return performanceMetricKey;
    }

    private String serverDimension() {
        return level == null ? "unknown" : level.dimension().location().toString();
    }

    private void refreshRoutingState() {
        QuantumGridLinkBE ownLink = getGridLink();
        IGrid grid = ownLink == null ? null : ownLink.getGrid();
        if (!formed || grid == null) {
            replaceRoutingState(List.of(), 0, 0, 0, 0);
            return;
        }

        Set<IPatternDetails> patterns = new LinkedHashSet<>();
        int slots = getManagementSlotCount();
        for (int slot = 0; slot < slots; slot++) {
            if (!isPatternEnabled(slot)) continue;
            ItemStack stack = patternInventory.getStackInSlot(slot);
            if (!isSupportedPattern(stack)) continue;
            try {
                IPatternDetails details = PatternDetailsHelper.decodePattern(stack, level);
                if (details != null) patterns.add(details);
            } catch (RuntimeException ignored) {
                // A malformed third-party pattern must not hide the rest of the library.
            }
        }
        int queuedRoutes = ownLink.getPendingCraftingRouteCount();
        replaceRoutingState(List.copyOf(patterns), patterns.isEmpty() ? 0 : 1, 1, queuedRoutes, 0);
    }

    /**
     * Quantum-Crafter-compatible stockless mode: every enabled pattern repeats while its
     * ingredients are present. The Grid Link executes all currently available copies as one
     * aggregate route, so throughput does not introduce a per-item server loop.
     */
    private void runAutomaticCrafting(long gameTime) {
        if (!isOperational()) return;
        if (gameTime < nextAutomaticCraft) return;
        nextAutomaticCraft = gameTime + craftingMode.autoCraftIntervalTicks();
        QuantumGridLinkBE link = getGridLink();
        if (link == null) return;
        QuantumGridLinkBE.AutoCraftResult result = link.autoCraftPatterns(
                routedPatterns, autoCraftPatternOffset, craftingMode.routesPerTick());
        autoCraftPatternOffset = result.nextOffset();
        activeJobCount = link.getPendingCraftingRouteCount();
    }

    public static boolean isSupportedPattern(ItemStack stack) {
        return QuantumPatternFabricationMatrixControllerBE.isSupportedPattern(stack);
    }

    private void replaceRoutingState(List<IPatternDetails> patterns, int patternProviders, int computeProviders,
                                     int activeJobs, int conflicts) {
        boolean patternsChanged = !routedPatterns.equals(patterns);
        routedPatterns = patterns;
        patternProviderCount = patternProviders;
        computeProviderCount = computeProviders;
        activeJobCount = activeJobs;
        conflictCount = conflicts;
        if (patternsChanged) {
            QuantumGridLinkBE link = getGridLink();
            if (link != null) link.patternsChanged();
        }
    }

    private void deform() {
        boolean wasFormed = formed;
        detachGridLink();
        detachEnergyHatchesNotIn(List.of());
        energyHatchPositions = List.of();
        formed = false;
        tier1Fields = 0;
        tier2Fields = 0;
        tier3Fields = 0;
        patternCapacity = 0;
        replaceRoutingState(List.of(), 0, 0, 0, 0);
        if (wasFormed) onGridConnectableSidesChanged();
        setChanged();
    }

    private void indexRelevantFootprint(MultiblockPattern pattern, Direction facing) {
        if (level == null) return;
        List<Long> footprint = java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(worldPosition),
                        pattern.getTrackedPositions(worldPosition, facing).stream())
                .map(BlockPos::asLong).toList();
        StructureMembershipIndex.INSTANCE.register(
                level.dimension().location().toString(), worldPosition.asLong(), footprint);
    }

    @Nullable
    private QuantumGridLinkBE getGridLink() {
        if (level == null || gridLinkPos == null) return null;
        return LoadedBlockEntityLookup.get(level, gridLinkPos) instanceof QuantumGridLinkBE link ? link : null;
    }

    private void detachGridLink() {
        QuantumGridLinkBE link = getGridLink();
        if (link != null && worldPosition.equals(link.getControllerPos())) link.unlinkFromController();
        gridLinkPos = null;
    }

    private void detachEnergyHatchesNotIn(List<BlockPos> retained) {
        if (level == null) return;
        for (BlockPos pos : energyHatchPositions) {
            if (!retained.contains(pos) && LoadedBlockEntityLookup.get(level, pos) instanceof MassiveOutputHatchBE hatch
                    && worldPosition.equals(hatch.getControllerPos())) hatch.unlinkFromController();
        }
    }

    /** FE fuel is independent of ME grid power; no hatch or empty hatches means no paid craft. */
    public double extractCraftingEnergy(double requested, boolean simulate) {
        if (!formed || level == null || !Double.isFinite(requested) || requested <= 0D) return 0D;
        double extracted = 0D;
        for (BlockPos pos : energyHatchPositions) {
            if (extracted >= requested) break;
            if (LoadedBlockEntityLookup.get(level, pos) instanceof MassiveOutputHatchBE hatch
                    && worldPosition.equals(hatch.getControllerPos())) {
                extracted += hatch.extractBufferedExternalEnergyAE(requested - extracted, simulate);
            }
        }
        return Math.min(requested, extracted);
    }

    public void onControllerBroken() {
        if (level != null && !level.isClientSide()) {
            detachGridLink();
            detachEnergyHatchesNotIn(List.of());
            energyHatchPositions = List.of();
            StructureMembershipIndex.INSTANCE.unregister(
                    level.dimension().location().toString(), worldPosition.asLong());
        }
    }

    @Override public void markStructureDirty() { structureDirty = true; }
    @Override public Set<Direction> getGridConnectableSides(BlockOrientation orientation) { return Set.of(); }
    @Override public AECableType getCableConnectionType(Direction direction) { return AECableType.NONE; }
    @Override public boolean isGridLinkFormed() { return formed; }
    @Override public boolean ownsGridLink(BlockPos linkPos) { return formed && linkPos.equals(gridLinkPos); }
    @Override public void onGridLinkStateChanged() { routingDirty = true; updateVisualState(); }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            routingDirty = true;
            updateVisualState();
        }
    }

    private boolean isGridLinkActive() {
        QuantumGridLinkBE link = getGridLink();
        return link != null && link.isNetworkReady();
    }

    public boolean isOperational() {
        return formed && isGridLinkActive() && !routedPatterns.isEmpty();
    }

    private void updateVisualState() {
        if (level == null || level.isClientSide() || isRemoved()) return;
        if (LoadedBlockEntityLookup.get(level, worldPosition) != this) return;
        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof InfinityFabricationSingularityControllerBlock)) return;
        BlockState updated = current
                .setValue(InfinityFabricationSingularityControllerBlock.FORMED, formed)
                .setValue(InfinityFabricationSingularityControllerBlock.POWERED, isOperational());
        if (updated != current) level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
    }

    public List<IPatternDetails> getRoutedPatterns() { return isOperational() ? routedPatterns : List.of(); }
    public InternalInventory getAccessiblePatternInventory() { return accessiblePatternInventory; }
    public int getPatternCapacity() { return patternCapacity; }

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

    public static int calculatePatternCapacity(int mk1Fields, int mk2Fields, int mk3Fields) {
        return SingularityPatternCapacity.calculate(mk1Fields, mk2Fields, mk3Fields);
    }

    /** Keeps overflow visible after a field downgrade so no encoded pattern is lost. */
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

    public int getPageCount() {
        return Math.max(1, (getManagementSlotCount() + PATTERNS_PER_PAGE - 1) / PATTERNS_PER_PAGE);
    }

    public boolean isPatternEnabled(int slot) {
        return slot >= 0 && slot < MAX_PATTERN_SLOTS && !disabledPatternSlots.contains(slot);
    }

    public void togglePatternEnabled(int slot) {
        if (slot < 0 || slot >= getManagementSlotCount()) return;
        if (!disabledPatternSlots.add(slot)) disabledPatternSlots.remove(slot);
        routingDirty = true;
        setChanged();
        QuantumGridLinkBE link = getGridLink();
        if (link != null) link.patternsChanged();
    }
    public SingularityCraftingMode getCraftingMode() { return craftingMode; }
    public int getRouteLimit() { return craftingMode.parallelJobLimit(); }

    public int getQueuedRouteCount() {
        QuantumGridLinkBE link = getGridLink();
        return link == null ? 0 : link.getPendingCraftingRouteCount();
    }

    public void cycleCraftingMode() {
        applyCraftingMode(SingularityCraftingMode.forFieldTiers(tier1Fields, tier2Fields, tier3Fields));
    }

    public void setCraftingMode(SingularityCraftingMode mode) {
        SingularityCraftingMode fieldMode = SingularityCraftingMode.forFieldTiers(
                tier1Fields, tier2Fields, tier3Fields);
        if (mode != fieldMode) return;
        applyCraftingMode(fieldMode);
    }

    private void applyCraftingMode(SingularityCraftingMode mode) {
        if (craftingMode == mode) return;
        craftingMode = mode;
        nextAutomaticCraft = 0L;
        setChanged();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == patternInventory && slot >= 0 && slot < occupiedPatternSlots.length) {
            boolean occupied = !patternInventory.getStackInSlot(slot).isEmpty();
            if (occupied && !occupiedPatternSlots[slot]) {
                // Match the Quantum Crafter's safe activation semantics: a newly inserted
                // recipe waits for an explicit click before it starts consuming the network.
                disabledPatternSlots.add(slot);
            } else if (!occupied) {
                disabledPatternSlots.remove(slot);
            }
            occupiedPatternSlots[slot] = occupied;
        }
        storedPatternCountDirty = true;
        routingDirty = true;
        setChanged();
        QuantumGridLinkBE link = getGridLink();
        if (link != null) link.patternsChanged();
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
        tag.putString(TAG_MODE, craftingMode.name());
        patternInventory.writeToNBT(tag, TAG_PATTERNS, registries);
        tag.putIntArray(TAG_DISABLED_PATTERNS,
                disabledPatternSlots.stream().mapToInt(Integer::intValue).sorted().toArray());
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        try {
            craftingMode = SingularityCraftingMode.valueOf(tag.getString(TAG_MODE));
        } catch (IllegalArgumentException ignored) {
            craftingMode = SingularityCraftingMode.BALANCED;
        }
        patternInventory.readFromNBT(tag, TAG_PATTERNS, registries);
        disabledPatternSlots.clear();
        for (int slot : tag.getIntArray(TAG_DISABLED_PATTERNS)) {
            if (slot >= 0 && slot < MAX_PATTERN_SLOTS) disabledPatternSlots.add(slot);
        }
        for (int slot = 0; slot < occupiedPatternSlots.length; slot++) {
            occupiedPatternSlots[slot] = !patternInventory.getStackInSlot(slot).isEmpty();
        }
        storedPatternCountDirty = true;
        routingDirty = true;
    }

    public ContainerData getMenuData() { return menuData; }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ufo.infinity_fabrication_singularity_controller");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new InfinityFabricationSingularityMenu(id, inventory, this);
    }

    public MultiblockDefinition getDefinition() {
        return InfinityFabricationSingularityPatternFactory.getDefinition();
    }

    public Direction getControllerFacing() {
        return getBlockState().hasProperty(InfinityFabricationSingularityControllerBlock.FACING)
                ? getBlockState().getValue(InfinityFabricationSingularityControllerBlock.FACING)
                : Direction.NORTH;
    }

    /** The schema's local west side is its front. */
    public Direction getStructureFacing() { return getControllerFacing().getClockWise(); }

    private static int saturatingAdd(int left, int right) {
        if (right <= 0) return Math.max(0, left);
        return left >= Integer.MAX_VALUE - right ? Integer.MAX_VALUE : left + right;
    }

    private final class AccessiblePatternInventory extends BaseInternalInventory {
        @Override public int size() { return getManagementSlotCount(); }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < size() ? patternInventory.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public void setItemDirect(int slot, ItemStack stack) {
            if (slot < 0 || slot >= size() || !stack.isEmpty() && !isItemValid(slot, stack)) return;
            patternInventory.setItemDirect(slot, stack);
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
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < patternCapacity && isSupportedPattern(stack);
        }
    }
}

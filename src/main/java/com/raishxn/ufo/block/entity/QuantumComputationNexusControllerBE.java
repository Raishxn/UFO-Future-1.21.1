package com.raishxn.ufo.block.entity;

import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.helpers.IPriorityHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.me.helpers.MachineSource;
import com.raishxn.ufo.api.ae.NexusCraftingUnitOwnership;
import com.raishxn.ufo.api.ae.NexusVirtualCpuHost;
import com.raishxn.ufo.api.ae.QuantumGridLinkHost;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.StructureInvalidationTarget;
import com.raishxn.ufo.api.multiblock.StructureMembershipIndex;
import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.block.QuantumComputationNexusControllerBlock;
import com.raishxn.ufo.block.entity.pattern.QuantumComputationNexusPatternFactory;
import com.raishxn.ufo.crafting.NexusSharedCraftingCpuPool;
import com.raishxn.ufo.core.MegaCoProcessorTier;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import com.raishxn.ufo.diagnostic.MachineMetricKey;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.init.ModMenus;
import com.raishxn.ufo.screen.QuantumComputationNexusMenu;
import com.raishxn.ufo.util.LoadedBlockEntityLookup;
import com.raishxn.ufocore.api.crafting.CraftingComputeCapacity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

/** Controller and persistent host for the Nexus shared crafting CPU pool. */
public final class QuantumComputationNexusControllerBE extends AENetworkedBlockEntity
        implements StructureInvalidationTarget, MenuProvider, IMultiblockController,
        QuantumGridLinkHost, NexusVirtualCpuHost, IPriorityHost {
    public static final int INFINITE_MODE_MODULE_THRESHOLD = 25;
    private static final String TAG_CPU_POOL = "NexusCpuPool";
    private static final String TAG_CPU_PRIORITY = "CpuPriority";

    private final IActionSource cpuActionSource = new MachineSource(this);
    private final NexusSharedCraftingCpuPool cpuPool = new NexusSharedCraftingCpuPool(this);
    private boolean formed;
    private boolean structureDirty = true;
    private List<BlockPos> modulePositions = List.of();
    private Set<BlockPos> moduleSpace = Set.of();
    @Nullable private BlockPos gridLinkPos;
    private CraftingComputeCapacity exactCapacity = CraftingComputeCapacity.ZERO;
    private int storageModuleCount;
    private int coProcessorModuleCount;
    private boolean infiniteMode;
    private int cpuPriority;
    @Nullable private MachineMetricKey performanceMetricKey;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> formed ? 1 : 0;
                case 1 -> isGridLinkActive() ? 1 : 0;
                case 2 -> cpuPool.isActive() ? 1 : 0;
                case 3 -> modulePositions.size();
                case 4 -> storageModuleCount;
                case 5 -> coProcessorModuleCount;
                case 6, 7, 8, 9 -> amountPart(exactCapacity.storageBytes().asBigInteger(), index - 6);
                case 10, 11, 12, 13 -> amountPart(exactCapacity.parallelLanes().asBigInteger(), index - 10);
                case 14 -> cpuPool.getActiveJobCount();
                case 15 -> infiniteMode ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) { }
        @Override public int getCount() { return QuantumComputationNexusMenu.DATA_COUNT; }
    };

    public QuantumComputationNexusControllerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_COMPUTATION_NEXUS_CONTROLLER_BE.get(), pos, state);
        getMainNode().setFlags().setIdlePowerUsage(0.0D);
        onGridConnectableSidesChanged();
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        long startedAt = System.nanoTime();
        try {
            if (structureDirty) {
                structureDirty = false;
                refreshStructure(serverLevel);
            }
            if (!cpuPool.hasPersistentState() && formed) configurePool();
            updateVisualState();
        } finally {
            MachinePerformanceRegistry.INSTANCE.recordTick(performanceMetricKey(),
                    System.nanoTime() - startedAt, serverLevel.getGameTime());
        }
    }

    @Override public boolean isAssembled() { return formed; }
    @Override public void scanStructure(Level scanLevel) {
        if (scanLevel instanceof ServerLevel serverLevel && scanLevel == level) {
            structureDirty = false;
            refreshStructure(serverLevel);
            updateVisualState();
        }
    }
    @Override public void onReady() { super.onReady(); markStructureDirty(); }
    @Override public void addPart(BlockPos partPos) { markStructureDirty(); }
    @Override public void removePart(BlockPos partPos) { markStructureDirty(); }
    @Override public List<BlockPos> getParts() {
        ArrayList<BlockPos> parts = new ArrayList<>(modulePositions);
        if (gridLinkPos != null) parts.add(gridLinkPos);
        return List.copyOf(parts);
    }
    @Override public BlockPos getControllerPos() { return worldPosition; }

    private void refreshStructure(ServerLevel serverLevel) {
        MultiblockPattern pattern = getDefinition().pattern();
        long startedAt = System.nanoTime();
        try {
            Direction facing = getFacing();
            indexRelevantFootprint(pattern, facing);
            MultiblockPattern.MatchResult match = pattern.matchFast(serverLevel, worldPosition, facing);
            if (match.hasUnloadedPositions()) return;
            if (!match.isValid()) { deform(); return; }

            List<BlockPos> links = pattern.getExpectedPositions(worldPosition, facing, 'L');
            if (links.size() != 1 || !(serverLevel.getBlockEntity(links.getFirst()) instanceof QuantumGridLinkBE link)) {
                deform();
                return;
            }
            BlockPos discoveredLink = links.getFirst().immutable();
            if (gridLinkPos != null && !gridLinkPos.equals(discoveredLink)) detachGridLink();
            gridLinkPos = discoveredLink;

            List<BlockPos> spaces = pattern.getExpectedPositions(worldPosition, facing, 'I');
            moduleSpace = Set.copyOf(spaces);
            List<CraftingBlockEntity> modules = new ArrayList<>();
            CraftingComputeCapacity capacity = CraftingComputeCapacity.ZERO;
            int storages = 0;
            int coProcessors = 0;
            int ultimateStorages = 0;
            int ultimateCoProcessors = 0;
            for (BlockPos pos : spaces) {
                if (!serverLevel.isLoaded(pos)) return;
                BlockState state = serverLevel.getBlockState(pos);
                CraftingComputeCapacity contribution = contributionOf(state);
                if (!contribution.storageBytes().isZero()) storages++;
                if (!contribution.parallelLanes().isZero()) coProcessors++;
                if (state.is(ModBlocks.CRAFTING_STORAGE_BLOCKS.get(MegaCraftingStorageTier.STORAGE_1QD).get())) {
                    ultimateStorages++;
                }
                if (state.is(ModBlocks.CO_PROCESSOR_BLOCKS.get(MegaCoProcessorTier.COPROCESSOR_2B).get())) {
                    ultimateCoProcessors++;
                }
                capacity = capacity.add(contribution);
                if (serverLevel.getBlockEntity(pos) instanceof CraftingBlockEntity module) modules.add(module);
            }

            clearModuleOwnershipNotIn(modules);
            for (CraftingBlockEntity module : modules) {
                if (module.getCluster() != null) module.breakCluster();
                ((NexusCraftingUnitOwnership) module).ufo$setNexusController(worldPosition);
            }
            modulePositions = modules.stream().map(BlockEntity::getBlockPos).map(BlockPos::immutable).toList();
            exactCapacity = capacity;
            storageModuleCount = storages;
            coProcessorModuleCount = coProcessors;
            infiniteMode = ultimateStorages >= INFINITE_MODE_MODULE_THRESHOLD
                    && ultimateCoProcessors >= INFINITE_MODE_MODULE_THRESHOLD;
            // Formation describes the shell. Compute readiness is a separate state derived from the
            // installed storage capacity, allowing an empty Nexus to connect and report exactly what
            // it is missing instead of contradicting the structure scanner.
            formed = true;
            link.linkToController(worldPosition);
            link.synchronizeInternalNodes(List.of());
            configurePool();
            onGridConnectableSidesChanged();
            setChanged();
        } finally {
            MachinePerformanceRegistry.INSTANCE.recordScan(performanceMetricKey(),
                    System.nanoTime() - startedAt, pattern.getTestedPositionCount(), serverLevel.getGameTime());
        }
    }

    private MachineMetricKey performanceMetricKey() {
        if (performanceMetricKey == null) {
            performanceMetricKey = new MachineMetricKey(
                    level == null ? "unknown" : level.dimension().location().toString(),
                    worldPosition.asLong(), getClass().getSimpleName());
        }
        return performanceMetricKey;
    }

    private void configurePool() {
        long storage = exactCapacity.storageBytes().asBigInteger()
                .min(java.math.BigInteger.valueOf(Long.MAX_VALUE)).longValue();
        int coProcessors = exactCapacity.parallelLanes().asBigInteger()
                // Integer.MAX_VALUE - 1 is reserved as the explicit infinity marker in the AE2 terminal.
                .min(java.math.BigInteger.valueOf(Integer.MAX_VALUE - 2L)).intValue();
        cpuPool.reconfigure(storage, coProcessors, infiniteMode);
    }

    private void clearModuleOwnershipNotIn(List<CraftingBlockEntity> retained) {
        Set<BlockPos> keep = retained.stream().map(BlockEntity::getBlockPos)
                .collect(java.util.stream.Collectors.toSet());
        if (level == null) return;
        for (BlockPos pos : modulePositions) {
            if (!keep.contains(pos) && level.getBlockEntity(pos) instanceof CraftingBlockEntity module) {
                ((NexusCraftingUnitOwnership) module).ufo$setNexusController(null);
            }
        }
    }

    private void deform() {
        boolean wasFormed = formed;
        clearAllModuleOwnership();
        detachGridLink();
        formed = false;
        modulePositions = List.of();
        moduleSpace = Set.of();
        exactCapacity = CraftingComputeCapacity.ZERO;
        storageModuleCount = 0;
        coProcessorModuleCount = 0;
        infiniteMode = false;
        if (wasFormed) onGridConnectableSidesChanged();
        setChanged();
    }

    private void clearAllModuleOwnership() {
        if (level == null) return;
        for (BlockPos pos : modulePositions) {
            if (LoadedBlockEntityLookup.get(level, pos) instanceof CraftingBlockEntity module) {
                ((NexusCraftingUnitOwnership) module).ufo$setNexusController(null);
            }
        }
    }

    private void indexRelevantFootprint(MultiblockPattern pattern, Direction facing) {
        List<Long> footprint = java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(worldPosition),
                        java.util.stream.Stream.concat(
                                pattern.getTrackedPositions(worldPosition, facing).stream(),
                                pattern.getExpectedPositions(worldPosition, facing, 'I').stream()))
                .map(BlockPos::asLong).toList();
        StructureMembershipIndex.INSTANCE.register(
                level.dimension().location().toString(), worldPosition.asLong(), footprint);
    }

    private CraftingComputeCapacity contributionOf(BlockState state) {
        for (var entry : ModBlocks.CRAFTING_STORAGE_BLOCKS.entrySet()) {
            if (state.is(entry.getValue().get())) return entry.getKey().quantumComputeContribution();
        }
        for (var entry : ModBlocks.CO_PROCESSOR_BLOCKS.entrySet()) {
            if (state.is(entry.getValue().get())) return entry.getKey().quantumComputeContribution();
        }
        return CraftingComputeCapacity.ZERO;
    }

    @Nullable private QuantumGridLinkBE getGridLink() {
        if (level == null || gridLinkPos == null) return null;
        return LoadedBlockEntityLookup.get(level, gridLinkPos) instanceof QuantumGridLinkBE link ? link : null;
    }
    @Nullable private IGridNode getGridLinkNode() {
        QuantumGridLinkBE link = getGridLink();
        return link == null ? null : link.getGridLinkNode();
    }
    private void detachGridLink() {
        QuantumGridLinkBE link = getGridLink();
        if (link != null && worldPosition.equals(link.getControllerPos())) link.unlinkFromController();
        gridLinkPos = null;
    }

    public boolean canOwnModuleAt(BlockPos pos) {
        return moduleSpace.contains(pos)
                || getDefinition().pattern().getExpectedPositions(worldPosition, getFacing(), 'I').contains(pos);
    }
    public void onControllerBroken() {
        if (level != null && !level.isClientSide()) {
            cpuPool.cancelAllJobs();
            clearAllModuleOwnership();
            detachGridLink();
            StructureMembershipIndex.INSTANCE.unregister(level.dimension().location().toString(), worldPosition.asLong());
        }
    }

    @Override public void markStructureDirty() { structureDirty = true; }
    @Override public Set<Direction> getGridConnectableSides(BlockOrientation orientation) { return Set.of(); }
    @Override public AECableType getCableConnectionType(Direction direction) { return AECableType.NONE; }
    @Override public boolean isGridLinkFormed() { return formed; }
    @Override public boolean ownsGridLink(BlockPos linkPos) { return formed && linkPos.equals(gridLinkPos); }
    @Override public void onGridLinkStateChanged() { updateVisualState(); ufo$markCpuDirty(); }

    private boolean isGridLinkActive() {
        QuantumGridLinkBE link = getGridLink();
        return link != null && link.isNetworkReady();
    }
    @Override public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) updateVisualState();
    }
    private void updateVisualState() {
        if (level == null || level.isClientSide() || isRemoved()) return;
        if (LoadedBlockEntityLookup.get(level, worldPosition) != this) return;
        BlockState current = getBlockState();
        if (!(current.getBlock() instanceof QuantumComputationNexusControllerBlock)) return;
        boolean powered = formed && isGridLinkActive();
        BlockState updated = current.setValue(QuantumComputationNexusControllerBlock.FORMED, formed)
                .setValue(QuantumComputationNexusControllerBlock.POWERED, powered);
        if (updated != current) level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
    }

    @Override public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag poolTag = new CompoundTag();
        cpuPool.writeToNBT(poolTag, registries);
        tag.put(TAG_CPU_POOL, poolTag);
        tag.putInt(TAG_CPU_PRIORITY, cpuPriority);
    }
    @Override public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains(TAG_CPU_POOL, TagTypes.COMPOUND)) cpuPool.readFromNBT(tag.getCompound(TAG_CPU_POOL), registries);
        cpuPriority = tag.getInt(TAG_CPU_PRIORITY);
    }

    @Override public @Nullable IGrid ufo$getCpuGrid() {
        IGridNode node = getGridLinkNode();
        return node == null ? null : node.getGrid();
    }
    @Override public @Nullable IGridNode ufo$getCpuNode() { return getGridLinkNode(); }
    @Override public Level ufo$getCpuLevel() { return java.util.Objects.requireNonNull(level); }
    @Override public IActionSource ufo$getCpuActionSource() { return cpuActionSource; }
    @Override public boolean ufo$isCpuActive() { return formed && isGridLinkActive(); }
    @Override public Component ufo$getCpuName() { return getDisplayName(); }
    @Override public CpuSelectionMode ufo$getCpuSelectionMode() { return CpuSelectionMode.ANY; }
    @Override public int ufo$getCpuPriority() { return cpuPriority; }
    @Override public void ufo$markCpuDirty() {
        setChanged();
        IGridNode node = getGridLinkNode();
        if (node != null && node.getGrid() != null) node.getGrid().postEvent(new GridCraftingCpuChange(node));
    }

    public NexusSharedCraftingCpuPool getCpuPool() { return cpuPool; }
    private static int amountPart(java.math.BigInteger amount, int part) {
        return amount.shiftRight(part * 16).intValue() & 0xFFFF;
    }
    public ContainerData getMenuData() { return menuData; }
    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("block.ufo.quantum_computation_nexus_controller");
    }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory,
                                                                 @NotNull Player player) {
        return new QuantumComputationNexusMenu(id, inventory, this);
    }
    @Override public int getPriority() { return cpuPriority; }
    @Override public void setPriority(int priority) {
        if (cpuPriority == priority) return;
        cpuPriority = priority;
        ufo$markCpuDirty();
    }
    @Override public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(ModMenus.QUANTUM_COMPUTATION_NEXUS_MENU.get(), player, subMenu.getLocator());
    }
    @Override public ItemStack getMainMenuIcon() {
        return new ItemStack(getBlockState().getBlock());
    }
    public MultiblockDefinition getDefinition() { return QuantumComputationNexusPatternFactory.getDefinition(); }
    private Direction getFacing() {
        return getBlockState().hasProperty(QuantumComputationNexusControllerBlock.FACING)
                ? getBlockState().getValue(QuantumComputationNexusControllerBlock.FACING) : Direction.NORTH;
    }
    public int getCpuPartitionCount() { return cpuPool.getActiveJobCount(); }

    private static final class TagTypes {
        private static final int COMPOUND = 10;
    }
}

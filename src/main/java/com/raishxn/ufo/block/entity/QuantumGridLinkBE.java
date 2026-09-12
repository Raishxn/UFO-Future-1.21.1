package com.raishxn.ufo.block.entity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.inv.ICraftingInventory;
import appeng.me.helpers.MachineSource;
import com.raishxn.ufo.api.ae.QuantumGridLinkHost;
import com.raishxn.ufo.api.ae.QuantumPatternMatrixHost;
import com.raishxn.ufo.api.ae.QuantumPatternMatrixContainerService;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockCasingStyle;
import com.raishxn.ufo.block.QuantumGridLinkBlock;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPool;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPoolProvider;
import com.raishxn.ufo.api.crafting.IAggregateCraftingProvider;
import com.raishxn.ufo.crafting.AggregateRecipeBatch;
import com.raishxn.ufo.util.LoadedBlockEntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Cable endpoint that joins a multiblock's internal AE nodes to one authoritative grid. */
public final class QuantumGridLinkBE extends AENetworkedBlockEntity
        implements IMultiblockPart, SharedCraftingCpuPoolProvider,
        QuantumPatternMatrixHost, QuantumPatternMatrixContainerService, IAggregateCraftingProvider, IGridTickable {
    private static final String TAG_PENDING_CRAFTING_OUTPUTS = "pendingCraftingOutputs";
    private static final String TAG_PENDING_CRAFTING_ROUTES = "pendingCraftingRoutes";
    private static final String TAG_ROUTE_OUTPUTS = "outputs";

    @Nullable
    private BlockPos controllerPos;
    private final Set<IGridNode> internalNodes = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<IGridNode, IGridConnection> ownedConnections = new IdentityHashMap<>();
    private final List<Map<AEKey, Long>> pendingCraftingRoutes = new ArrayList<>();
    private final IActionSource actionSource = new MachineSource(this);
    private boolean deferRestoredCraftingOutputs;

    public QuantumGridLinkBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(32.0D)
                .addService(IGridMultiblock.class, this::getMultiblockNodes)
                .addService(SharedCraftingCpuPoolProvider.class, this)
                .addService(QuantumPatternMatrixHost.class, this)
                .addService(QuantumPatternMatrixContainerService.class, this)
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this);
        getMainNode().setExposedOnSides(Set.of());
    }

    @Override
    public void onReady() {
        super.onReady();
        refreshGridConnection();
    }

    @Override
    public void linkToController(BlockPos controllerPos) {
        BlockPos normalized = controllerPos.immutable();
        if (!normalized.equals(this.controllerPos)) {
            destroyOwnedConnections();
            this.controllerPos = normalized;
        }
        updateFacingAwayFromController(normalized);
        updateCasingStyle(casingStyleFor(normalized));
        refreshGridConnection();
        patternsChanged();
        setChanged();
    }

    @Override
    public void unlinkFromController() {
        if (controllerPos == null) return;
        destroyOwnedConnections();
        controllerPos = null;
        updateCasingStyle(MultiblockCasingStyle.DEFAULT);
        refreshGridConnection();
        patternsChanged();
        setChanged();
    }

    public void unlinkForRemoval() {
        destroyOwnedConnections();
        controllerPos = null;
        setChanged();
    }

    @Nullable
    @Override
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public boolean isLinked() {
        return controllerPos != null;
    }

    public boolean isNetworkReady() {
        IGridNode node = getMainNode().getNode();
        return isOwnedByFormedController() && node != null && node.isActive();
    }

    @Nullable
    public IGridNode getGridLinkNode() {
        return getMainNode().getNode();
    }

    @Nullable
    private QuantumPatternFabricationMatrixControllerBE getPatternMatrixController() {
        if (!isOwnedByFormedController() || level == null || controllerPos == null) return null;
        return LoadedBlockEntityLookup.get(level, controllerPos) instanceof QuantumPatternFabricationMatrixControllerBE matrix
                ? matrix : null;
    }

    @Nullable
    private InfinityFabricationSingularityControllerBE getSingularityController() {
        if (!isOwnedByFormedController() || level == null || controllerPos == null) return null;
        return LoadedBlockEntityLookup.get(level, controllerPos) instanceof InfinityFabricationSingularityControllerBE singularity
                ? singularity : null;
    }

    public boolean isSingularityProvider() {
        return getSingularityController() != null;
    }

    @Override
    public boolean insertEncodedPattern(net.minecraft.world.item.ItemStack stack) {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        return matrix != null && matrix.insertEncodedPattern(stack);
    }

    @Override
    public int patternMatrixPriority() {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        return matrix != null ? matrix.patternMatrixPriority() : Integer.MIN_VALUE;
    }

    @Override
    public long patternMatrixSortKey() {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        return matrix != null ? matrix.patternMatrixSortKey() : worldPosition.asLong();
    }

    @Override
    public @Nullable IGrid getGrid() {
        IGridNode node = getMainNode().getNode();
        return node != null && node.isActive() ? node.getGrid() : null;
    }

    @Override
    public boolean isVisibleInTerminal() {
        return getPatternMatrixController() != null;
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        return matrix != null ? matrix.getTerminalPatternInventory() : InternalInventory.empty();
    }

    @Override
    public long getTerminalSortOrder() {
        return patternMatrixSortKey();
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        if (matrix == null) return PatternContainerGroup.nothing();
        return new PatternContainerGroup(
                AEItemKey.of(matrix.getBlockState().getBlock()),
                matrix.getDisplayName().copy().append(" (" + matrix.getStoredPatternCount()
                        + "/" + matrix.getPatternCapacity() + ")"),
                List.of());
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        if (matrix != null) return matrix.getAvailablePatterns();
        // Singularity patterns only drive the autonomous stock machine. Publishing them
        // to the crafting service would list every stored recipe as craftable in the
        // terminal and steal planning from the player's normal autocrafting chain.
        return List.of();
    }

    @Override
    public int getPatternPriority() {
        return getSingularityController() != null ? Integer.MAX_VALUE / 2 : patternMatrixPriority();
    }

    @Override
    public int getAggregatePriority() {
        return getSingularityController() != null ? 1_000 : 0;
    }

    @Override
    public int getAggregateOperationCost() {
        InfinityFabricationSingularityControllerBE singularity = getSingularityController();
        return singularity != null ? singularity.getCraftingMode().operationCost() : 1;
    }

    @Override
    public double getAggregateEnergyMultiplier() {
        InfinityFabricationSingularityControllerBE singularity = getSingularityController();
        return singularity != null ? singularity.getCraftingMode().energyMultiplier() : 1.0D;
    }

    @Override
    public long getAggregateCapacity(IPatternDetails details) {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        if (matrix != null) {
            return getGrid() != null && pendingCraftingRoutes.isEmpty()
                    && matrix.getAvailablePatterns().contains(details) ? Long.MAX_VALUE : 0L;
        }
        InfinityFabricationSingularityControllerBE singularity = getSingularityController();
        return singularity != null && singularity.isOperational() && getGrid() != null
                && pendingCraftingRoutes.size() < singularity.getRouteLimit()
                && singularity.getRoutedPatterns().contains(details) ? Long.MAX_VALUE : 0L;
    }

    @Override
    public long pushAggregate(IPatternDetails details, KeyCounter[] inputHolder, long maxCraft) {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        InfinityFabricationSingularityControllerBE singularity = getSingularityController();
        IGrid grid = getGrid();
        boolean matrixReady = matrix != null && pendingCraftingRoutes.isEmpty()
                && matrix.getAvailablePatterns().contains(details);
        boolean singularityReady = singularity != null && singularity.isOperational()
                && pendingCraftingRoutes.size() < singularity.getRouteLimit()
                && singularity.getRoutedPatterns().contains(details);
        if (maxCraft <= 0 || grid == null || !matrixReady && !singularityReady) {
            return maxCraft;
        }

        Map<AEKey, Long> products = new LinkedHashMap<>();
        for (var output : details.getOutputs()) {
            if (output != null && output.what() != null && output.amount() > 0) {
                products.merge(output.what(), saturatingMultiply(output.amount(), maxCraft),
                        QuantumGridLinkBE::saturatingAdd);
            }
        }
        var inputs = details.getInputs();
        for (int slot = 0; slot < inputs.length && slot < inputHolder.length; slot++) {
            KeyCounter selected = inputHolder[slot];
            if (selected == null) continue;
            AEKey selectedKey = null;
            for (var entry : selected) {
                if (entry.getKey() != null && entry.getLongValue() > 0) {
                    selectedKey = entry.getKey();
                    break;
                }
            }
            if (selectedKey == null) continue;
            AEKey remainder = inputs[slot].getRemainingKey(selectedKey);
            if (remainder != null) {
                products.merge(remainder,
                        saturatingMultiply(Math.max(1L, inputs[slot].getMultiplier()), maxCraft),
                        QuantumGridLinkBE::saturatingAdd);
            }
        }
        if (products.isEmpty()) return maxCraft;

        pendingCraftingRoutes.add(products);
        setChanged();
        IGridNode node = getMainNode().getNode();
        if (node != null) {
            grid.getTickManager().alertDevice(node);
        }
        return 0L;
    }

    @Override
    public boolean isBusy() {
        QuantumPatternFabricationMatrixControllerBE matrix = getPatternMatrixController();
        if (matrix != null) return getGrid() == null || !pendingCraftingRoutes.isEmpty();
        InfinityFabricationSingularityControllerBE singularity = getSingularityController();
        return singularity == null || !singularity.isOperational() || getGrid() == null
                || pendingCraftingRoutes.size() >= singularity.getRouteLimit();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, pendingCraftingRoutes.isEmpty());
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (pendingCraftingRoutes.isEmpty()) {
            return TickRateModulation.SLEEP;
        }

        // Restored crafting CPUs and their waiting-output lists may mount after block entities.
        // Give them one complete grid tick before replaying persisted results.
        if (deferRestoredCraftingOutputs) {
            deferRestoredCraftingOutputs = false;
            return TickRateModulation.URGENT;
        }

        IGrid grid = getGrid();
        if (grid == null) return TickRateModulation.SLOWER;

        var storage = grid.getStorageService().getInventory();
        boolean movedAnything = false;
        InfinityFabricationSingularityControllerBE singularity = getSingularityController();
        int routeBudget = Math.min(pendingCraftingRoutes.size(),
                singularity == null ? 1 : singularity.getCraftingMode().routesPerTick());
        Iterator<Map<AEKey, Long>> routes = pendingCraftingRoutes.iterator();
        while (routes.hasNext() && routeBudget-- > 0) {
            Map<AEKey, Long> route = routes.next();
            Iterator<Map.Entry<AEKey, Long>> outputs = route.entrySet().iterator();
            while (outputs.hasNext()) {
                Map.Entry<AEKey, Long> output = outputs.next();
                long inserted = storage.insert(output.getKey(), output.getValue(), Actionable.MODULATE, actionSource);
                if (inserted <= 0) continue;
                movedAnything = true;
                if (inserted >= output.getValue()) {
                    outputs.remove();
                } else {
                    output.setValue(output.getValue() - inserted);
                }
            }
            if (route.isEmpty()) routes.remove();
        }
        if (movedAnything) setChanged();
        if (pendingCraftingRoutes.isEmpty()) {
            return TickRateModulation.SLEEP;
        }
        return movedAnything ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    public void patternsChanged() {
        if (getMainNode().isReady() && getMainNode().getNode() != null) {
            ICraftingProvider.requestUpdate(getMainNode());
        }
    }

    public int getPendingCraftingRouteCount() {
        return pendingCraftingRoutes.size();
    }

    /**
     * Repeats enabled Singularity patterns directly from ME storage. One accepted pattern route
     * consumes every currently craftable copy in aggregate, independently of the copy count.
     *
     * @return the fair-scheduling offset for the next controller tick
     */
    public AutoCraftResult autoCraftPatterns(List<IPatternDetails> patterns, int startOffset, int routeBudget) {
        InfinityFabricationSingularityControllerBE singularity = getSingularityController();
        IGrid grid = getGrid();
        if (singularity == null || grid == null || level == null || patterns.isEmpty() || routeBudget <= 0) {
            return new AutoCraftResult(0, 0);
        }

        int patternCount = patterns.size();
        int offset = Math.floorMod(startOffset, patternCount);
        int probes = Math.min(patternCount, routeBudget);
        int availableRoutes = Math.min(routeBudget,
                Math.max(0, singularity.getRouteLimit() - pendingCraftingRoutes.size()));
        if (availableRoutes <= 0) return new AutoCraftResult(offset, 0);

        MEStorage storage = grid.getStorageService().getInventory();
        // Building the network-wide fuzzy-key snapshot is the expensive part. Reuse one snapshot
        // for the complete pass instead of rebuilding it once for every encoded pattern.
        NetworkCraftingInventory networkInventory = new NetworkCraftingInventory(storage);
        int acceptedRoutes = 0;
        int probedPatterns = 0;
        while (probedPatterns < probes && acceptedRoutes < availableRoutes) {
            IPatternDetails details = patterns.get((offset + probedPatterns) % patternCount);
            probedPatterns++;
            if (tryAutoCraftRoute(details, storage, networkInventory, singularity)) {
                acceptedRoutes++;
            }
        }
        if (acceptedRoutes > 0) {
            setChanged();
            IGridNode node = getMainNode().getNode();
            if (node != null) grid.getTickManager().alertDevice(node);
        }
        return new AutoCraftResult((offset + probedPatterns) % patternCount, acceptedRoutes);
    }

    private boolean tryAutoCraftRoute(IPatternDetails details, MEStorage storage,
                                      NetworkCraftingInventory networkInventory,
                                      InfinityFabricationSingularityControllerBE singularity) {
        KeyCounter expectedOutputs = new KeyCounter();
        KeyCounter expectedContainers = new KeyCounter();
        KeyCounter[] oneCopyInputs;
        try {
            oneCopyInputs = CraftingCpuHelper.extractPatternInputs(
                    details, networkInventory, level, expectedOutputs, expectedContainers);
        } catch (RuntimeException ignored) {
            return false;
        }
        if (oneCopyInputs == null) return false;

        Map<AEKey, Long> requirements = aggregateRequirements(oneCopyInputs);
        Map<AEKey, Long> outputs = new LinkedHashMap<>();
        Map<AEKey, Long> containers = new LinkedHashMap<>();
        addProducts(outputs, expectedOutputs, 1L);
        addProducts(containers, expectedContainers, 1L);
        if (requirements.isEmpty() || outputs.isEmpty()) {
            CraftingCpuHelper.reinjectPatternInputs(networkInventory, oneCopyInputs);
            return false;
        }

        AggregateRecipeBatch<AEKey> batch = AggregateRecipeBatch.of(requirements, outputs, containers);
        long copies = batch.maximumCopies(key -> storage.extract(
                key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource));

        double powerPerCopy = CraftingCpuHelper.calculatePatternPower(oneCopyInputs)
                * singularity.getCraftingMode().energyMultiplier();
        if (!Double.isFinite(powerPerCopy) || powerPerCopy < 0D) {
            CraftingCpuHelper.reinjectPatternInputs(networkInventory, oneCopyInputs);
            return false;
        }
        if (powerPerCopy > 0.0D) {
            double requestedPower = Math.min(Double.MAX_VALUE, powerPerCopy * copies);
            double availablePower = singularity.extractCraftingEnergy(requestedPower, true);
            if (availablePower < requestedPower - 0.01D) {
                copies = Math.min(copies, floorPositive(availablePower / powerPerCopy));
            }
        }
        if (copies <= 0L) {
            CraftingCpuHelper.reinjectPatternInputs(networkInventory, oneCopyInputs);
            return false;
        }

        Map<AEKey, Long> additionallyExtracted = new LinkedHashMap<>();
        if (!extractAdditionalCopies(storage, batch.consumedInputs(), copies - 1L, additionallyExtracted)) {
            reinject(storage, additionallyExtracted);
            CraftingCpuHelper.reinjectPatternInputs(networkInventory, oneCopyInputs);
            return false;
        }

        Map<AEKey, Long> products = batch.products(copies);

        double cost = powerPerCopy * copies;
        if (cost > 0D && singularity.extractCraftingEnergy(cost, true) < cost - 0.000001D) {
            reinject(storage, additionallyExtracted);
            CraftingCpuHelper.reinjectPatternInputs(networkInventory, oneCopyInputs);
            return false;
        }
        if (cost > 0D) singularity.extractCraftingEnergy(cost, false);
        pendingCraftingRoutes.add(products);
        return true;
    }

    private static Map<AEKey, Long> aggregateRequirements(KeyCounter[] oneCopyInputs) {
        Map<AEKey, Long> requirements = new LinkedHashMap<>();
        for (KeyCounter input : oneCopyInputs) {
            if (input == null) continue;
            for (var entry : input) {
                if (entry.getKey() != null && entry.getLongValue() > 0L) {
                    requirements.merge(entry.getKey(), entry.getLongValue(), QuantumGridLinkBE::saturatingAdd);
                }
            }
        }
        return requirements;
    }

    private boolean extractAdditionalCopies(MEStorage storage, Map<AEKey, Long> requirements,
                                             long additionalCopies, Map<AEKey, Long> extracted) {
        if (additionalCopies <= 0L) return true;
        for (Map.Entry<AEKey, Long> requirement : requirements.entrySet()) {
            long requested = saturatingMultiply(requirement.getValue(), additionalCopies);
            long taken = storage.extract(requirement.getKey(), requested, Actionable.MODULATE, actionSource);
            if (taken > 0L) extracted.put(requirement.getKey(), taken);
            if (taken != requested) return false;
        }
        return true;
    }

    private void reinject(MEStorage storage, Map<AEKey, Long> stacks) {
        for (Map.Entry<AEKey, Long> stack : stacks.entrySet()) {
            storage.insert(stack.getKey(), stack.getValue(), Actionable.MODULATE, actionSource);
        }
    }

    private static void addProducts(Map<AEKey, Long> products, KeyCounter source, long copies) {
        for (var entry : source) {
            if (entry.getKey() != null && entry.getLongValue() > 0L) {
                products.merge(entry.getKey(), saturatingMultiply(entry.getLongValue(), copies),
                        QuantumGridLinkBE::saturatingAdd);
            }
        }
    }

    private static long floorPositive(double value) {
        if (!(value > 0.0D)) return 0L;
        return value >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) Math.floor(value);
    }

    private final class NetworkCraftingInventory implements ICraftingInventory {
        private final MEStorage storage;
        private final KeyCounter availableStacks;

        private NetworkCraftingInventory(MEStorage storage) {
            this.storage = storage;
            this.availableStacks = storage.getAvailableStacks();
        }

        @Override
        public void insert(AEKey what, long amount, Actionable mode) {
            storage.insert(what, amount, mode, actionSource);
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode) {
            return storage.extract(what, amount, mode, actionSource);
        }

        @Override
        public Iterable<AEKey> findFuzzyTemplates(AEKey what) {
            List<AEKey> result = new ArrayList<>();
            for (var entry : availableStacks.findFuzzy(what, FuzzyMode.IGNORE_ALL)) {
                result.add(entry.getKey());
            }
            return result;
        }
    }

    private static long saturatingAdd(long left, long right) {
        if (left <= 0) return Math.max(0L, right);
        if (right <= 0) return left;
        return left >= Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left <= 0 || right <= 0) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    public record AutoCraftResult(int nextOffset, int acceptedRoutes) {
    }

    @Nullable
    @Override
    public SharedCraftingCpuPool getSharedCraftingCpuPool() {
        if (!isOwnedByFormedController() || level == null || controllerPos == null) return null;
        return LoadedBlockEntityLookup.get(level, controllerPos) instanceof QuantumComputationNexusControllerBE controller
                ? controller.getCpuPool() : null;
    }

    /** Reconciles only the virtual connections created by this link. */
    public boolean synchronizeInternalNodes(Collection<IGridNode> desiredNodes) {
        if (level == null || level.isClientSide()) return false;

        Set<IGridNode> desired = Collections.newSetFromMap(new IdentityHashMap<>());
        for (IGridNode node : desiredNodes) if (node != null) desired.add(node);
        boolean changed = internalNodes.size() != desired.size() || !internalNodes.containsAll(desired);

        Iterator<Map.Entry<IGridNode, IGridConnection>> iterator = ownedConnections.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IGridNode, IGridConnection> entry = iterator.next();
            if (!desired.contains(entry.getKey())) {
                entry.getValue().destroy();
                iterator.remove();
                changed = true;
            }
        }

        internalNodes.clear();
        internalNodes.addAll(desired);
        IGridNode linkNode = getMainNode().getNode();
        if (!getMainNode().isReady() || linkNode == null || !isOwnedByFormedController()) return changed;

        for (IGridNode target : desired) {
            if (target == linkNode || hasConnection(linkNode, target)) continue;
            try {
                ownedConnections.put(target, GridHelper.createConnection(linkNode, target));
                changed = true;
            } catch (IllegalStateException ignored) {
                // Nodes can be reconnecting during a grid split; the controller retries on its next scan.
            }
        }
        return changed;
    }

    private static boolean hasConnection(IGridNode source, IGridNode target) {
        for (IGridConnection connection : source.getConnections()) {
            if (connection.getOtherSide(source) == target) return true;
        }
        return false;
    }

    private Iterator<IGridNode> getMultiblockNodes() {
        ArrayList<IGridNode> nodes = new ArrayList<>(internalNodes.size() + 1);
        IGridNode ownNode = getMainNode().getNode();
        if (ownNode != null) nodes.add(ownNode);
        nodes.addAll(internalNodes);
        return nodes.iterator();
    }

    private void destroyOwnedConnections() {
        for (IGridConnection connection : ownedConnections.values()) connection.destroy();
        ownedConnections.clear();
        internalNodes.clear();
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        if (!isOwnedByFormedController()) return Set.of();
        Direction facing = getBlockState().hasProperty(DirectionalBlock.FACING)
                ? getBlockState().getValue(DirectionalBlock.FACING)
                : Direction.NORTH;
        return EnumSet.of(facing);
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return isOwnedByFormedController() ? AECableType.DENSE_SMART : AECableType.NONE;
    }

    public void refreshGridConnection() {
        if (level == null || level.isClientSide()) return;
        getMainNode().setExposedOnSides(getGridConnectableSides(getOrientation()));
        onGridConnectableSidesChanged();
    }

    private boolean isOwnedByFormedController() {
        if (level == null || controllerPos == null) return false;
        BlockEntity entity = LoadedBlockEntityLookup.get(level, controllerPos);
        return entity instanceof QuantumGridLinkHost host
                && host.isGridLinkFormed()
                && host.ownsGridLink(worldPosition);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason == IGridNodeListener.State.GRID_BOOT || level == null || controllerPos == null) return;
        if (LoadedBlockEntityLookup.get(level, controllerPos) instanceof QuantumGridLinkHost host) {
            host.onGridLinkStateChanged();
        }
    }

    @Override
    public void setRemoved() {
        destroyOwnedConnections();
        super.setRemoved();
    }

    private MultiblockCasingStyle casingStyleFor(BlockPos controllerPos) {
        return level != null && LoadedBlockEntityLookup.get(level, controllerPos) instanceof StellarNexusControllerBE
                ? MultiblockCasingStyle.ENTROPY
                : MultiblockCasingStyle.QUANTUM;
    }

    private void updateFacingAwayFromController(BlockPos controllerPos) {
        if (level == null || level.isClientSide() || !getBlockState().hasProperty(DirectionalBlock.FACING)) return;
        int dx = worldPosition.getX() - controllerPos.getX();
        int dy = worldPosition.getY() - controllerPos.getY();
        int dz = worldPosition.getZ() - controllerPos.getZ();
        Direction outward;
        if (Math.abs(dy) > Math.max(Math.abs(dx), Math.abs(dz))) {
            outward = dy >= 0 ? Direction.UP : Direction.DOWN;
        } else if (Math.abs(dx) >= Math.abs(dz)) {
            outward = dx >= 0 ? Direction.EAST : Direction.WEST;
        } else {
            outward = dz >= 0 ? Direction.SOUTH : Direction.NORTH;
        }
        BlockState current = getBlockState();
        if (current.getValue(DirectionalBlock.FACING) != outward) {
            level.setBlock(worldPosition, current.setValue(DirectionalBlock.FACING, outward), Block.UPDATE_CLIENTS);
        }
    }

    private void updateCasingStyle(MultiblockCasingStyle style) {
        BlockState state = getBlockState();
        if (level == null || level.isClientSide() || !state.hasProperty(QuantumGridLinkBlock.CASING_STYLE)
                || state.getValue(QuantumGridLinkBlock.CASING_STYLE) == style) return;
        level.setBlock(worldPosition, state.setValue(QuantumGridLinkBlock.CASING_STYLE, style), Block.UPDATE_CLIENTS);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) tag.put("controllerPos", NbtUtils.writeBlockPos(controllerPos));
        if (!pendingCraftingRoutes.isEmpty()) {
            ListTag routes = new ListTag();
            for (Map<AEKey, Long> route : pendingCraftingRoutes) {
                CompoundTag routeTag = new CompoundTag();
                ListTag outputs = new ListTag();
                for (var output : route.entrySet()) {
                    outputs.add(GenericStack.writeTag(
                            registries, new GenericStack(output.getKey(), output.getValue())));
                }
                routeTag.put(TAG_ROUTE_OUTPUTS, outputs);
                routes.add(routeTag);
            }
            tag.put(TAG_PENDING_CRAFTING_ROUTES, routes);
        }
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        controllerPos = tag.contains("controllerPos")
                ? NbtUtils.readBlockPos(tag.getCompound("controllerPos"), "").orElse(null)
                : null;
        pendingCraftingRoutes.clear();
        ListTag routes = tag.getList(TAG_PENDING_CRAFTING_ROUTES, Tag.TAG_COMPOUND);
        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            Map<AEKey, Long> route = readRoute(registries,
                    routes.getCompound(routeIndex).getList(TAG_ROUTE_OUTPUTS, Tag.TAG_COMPOUND));
            if (!route.isEmpty()) pendingCraftingRoutes.add(route);
        }
        // Save compatibility for Matrix output batches written before routes were introduced.
        ListTag outputs = tag.getList(TAG_PENDING_CRAFTING_OUTPUTS, Tag.TAG_COMPOUND);
        Map<AEKey, Long> legacyRoute = readRoute(registries, outputs);
        if (!legacyRoute.isEmpty()) pendingCraftingRoutes.add(legacyRoute);
        deferRestoredCraftingOutputs = !pendingCraftingRoutes.isEmpty();
    }

    private static Map<AEKey, Long> readRoute(HolderLookup.Provider registries, ListTag outputs) {
        Map<AEKey, Long> route = new LinkedHashMap<>();
        for (int index = 0; index < outputs.size(); index++) {
            GenericStack output = GenericStack.readTag(registries, outputs.getCompound(index));
            if (output != null && output.what() != null && output.amount() > 0) {
                route.merge(output.what(), output.amount(), QuantumGridLinkBE::saturatingAdd);
            }
        }
        return route;
    }
}

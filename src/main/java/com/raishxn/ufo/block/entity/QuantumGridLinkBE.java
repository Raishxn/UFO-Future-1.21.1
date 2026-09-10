package com.raishxn.ufo.block.entity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import com.raishxn.ufo.api.ae.QuantumGridLinkHost;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockCasingStyle;
import com.raishxn.ufo.block.QuantumGridLinkBlock;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPool;
import com.raishxn.ufocore.api.crafting.SharedCraftingCpuPoolProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import java.util.Map;
import java.util.Set;

/** Cable endpoint that joins a multiblock's internal AE nodes to one authoritative grid. */
public final class QuantumGridLinkBE extends AENetworkedBlockEntity
        implements IMultiblockPart, SharedCraftingCpuPoolProvider {
    @Nullable
    private BlockPos controllerPos;
    private final Set<IGridNode> internalNodes = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<IGridNode, IGridConnection> ownedConnections = new IdentityHashMap<>();

    public QuantumGridLinkBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(32.0D)
                .addService(IGridMultiblock.class, this::getMultiblockNodes)
                .addService(SharedCraftingCpuPoolProvider.class, this);
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
        setChanged();
    }

    @Override
    public void unlinkFromController() {
        if (controllerPos == null) return;
        destroyOwnedConnections();
        controllerPos = null;
        updateCasingStyle(MultiblockCasingStyle.DEFAULT);
        refreshGridConnection();
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
    @Override
    public SharedCraftingCpuPool getSharedCraftingCpuPool() {
        if (!isOwnedByFormedController() || level == null || controllerPos == null) return null;
        return level.getBlockEntity(controllerPos) instanceof QuantumComputationNexusControllerBE controller
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
        if (level == null || controllerPos == null || !level.hasChunkAt(controllerPos)) return false;
        BlockEntity entity = level.getBlockEntity(controllerPos);
        return entity instanceof QuantumGridLinkHost host
                && host.isGridLinkFormed()
                && host.ownsGridLink(worldPosition);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason == IGridNodeListener.State.GRID_BOOT || level == null || controllerPos == null
                || !level.hasChunkAt(controllerPos)) return;
        if (level.getBlockEntity(controllerPos) instanceof QuantumGridLinkHost host) {
            host.onGridLinkStateChanged();
        }
    }

    @Override
    public void setRemoved() {
        destroyOwnedConnections();
        super.setRemoved();
    }

    private MultiblockCasingStyle casingStyleFor(BlockPos controllerPos) {
        return level != null && level.getBlockEntity(controllerPos) instanceof StellarNexusControllerBE
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
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        controllerPos = tag.contains("controllerPos")
                ? NbtUtils.readBlockPos(tag.getCompound("controllerPos"), "").orElse(null)
                : null;
    }
}

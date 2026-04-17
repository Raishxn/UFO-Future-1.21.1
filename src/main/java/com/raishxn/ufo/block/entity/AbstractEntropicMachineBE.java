package com.raishxn.ufo.block.entity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import com.raishxn.ufo.api.multiblock.EntropicMachineLocator;
import com.raishxn.ufo.api.multiblock.FieldTieredCubeValidator;
import com.raishxn.ufo.api.multiblock.IEntropicMachineController;
import com.raishxn.ufo.api.multiblock.MultiblockMachineTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class AbstractEntropicMachineBE extends AENetworkedBlockEntity
        implements IEntropicMachineController, IUniversalMultiblockController, IUpgradeableObject, MenuProvider {
    protected boolean assembled;
    protected final List<BlockPos> parts = new ArrayList<>();
    protected final Set<BlockPos> partSet = new HashSet<>();
    protected int machineTier = MultiblockMachineTier.MK1.level();
    protected boolean running;
    protected int progress;
    protected int maxProgress;
    protected int temperature;
    protected int maxTemperature = 10_000;
    protected long storedEnergy;
    protected long maxStoredEnergy;
    protected boolean safeMode = true;
    protected boolean overclocked;
    protected final List<UniversalDisplayedRecipe> displayedRecipes = new ArrayList<>();
    protected final IUpgradeInventory upgrades;
    @Nullable
    protected BlockPos anchorPos;

    protected AbstractEntropicMachineBE(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.upgrades = UpgradeInventories.forMachine(blockState.getBlock().asItem(), 4, this::saveChanges);
        this.getMainNode()
                .setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL)
                .addService(IGridMultiblock.class, this::getMultiblockNodes)
                .setIdlePowerUsage(0);
        onGridConnectableSidesChanged();
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        if (!isPrimaryMachine()) {
            return;
        }

        tickMachine();
    }

    protected abstract void tickMachine();

    protected abstract FieldTieredCubeValidator.ShellPredicate getShellPredicate();

    protected abstract String getMachineNameKey();

    @Override
    public void scanStructure(Level level) {
        EntropicMachineLocator.markNearbyDirty(level, this.worldPosition);
    }

    public @Nullable FieldTieredCubeValidator.ValidationResult findStructure(Level level) {
        return FieldTieredCubeValidator.findMatchingCube(level, this.worldPosition, getShellPredicate()).orElse(null);
    }

    public void applyStructure(FieldTieredCubeValidator.ValidationResult result) {
        boolean wasAssembled = this.assembled;
        BlockPos previousAnchor = this.anchorPos;

        this.assembled = result.valid() && result.shellPositions().contains(this.worldPosition);
        this.machineTier = result.machineTier();
        this.anchorPos = result.origin();
        this.parts.clear();
        this.partSet.clear();

        for (BlockPos pos : result.shellPositions()) {
            if (!pos.equals(this.worldPosition)) {
                BlockPos immutable = pos.immutable();
                this.parts.add(immutable);
                this.partSet.add(immutable);
            }
        }

        for (BlockPos pos : result.interiorPositions()) {
            BlockPos immutable = pos.immutable();
            this.parts.add(immutable);
            this.partSet.add(immutable);
        }

        setChanged();
        updateVisualState();
        if (this.level != null && (wasAssembled != this.assembled || !java.util.Objects.equals(previousAnchor, this.anchorPos))) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void clearStructureState() {
        boolean wasAssembled = this.assembled;
        BlockPos previousAnchor = this.anchorPos;
        this.assembled = false;
        this.running = false;
        this.progress = 0;
        this.maxProgress = 0;
        this.temperature = 0;
        this.storedEnergy = 0L;
        this.maxStoredEnergy = 0L;
        this.machineTier = MultiblockMachineTier.MK1.level();
        this.parts.clear();
        this.partSet.clear();
        this.displayedRecipes.clear();
        this.anchorPos = null;
        updateVisualState();
        setChanged();
        if (this.level != null && (wasAssembled || previousAnchor != null)) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public boolean isAssembled() {
        return this.assembled;
    }

    @Override
    public boolean canProxyInteract(BlockPos pos) {
        return isPrimaryMachine() && (pos.equals(this.worldPosition) || this.partSet.contains(pos));
    }

    @Override
    public boolean isNetworkConnected() {
        IGridNode node = this.getActionableNode();
        return node != null && node.getGrid() != null && node.isActive();
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return this.assembled ? EnumSet.allOf(Direction.class) : EnumSet.noneOf(Direction.class);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            updateVisualState();
        }
    }

    @Override
    public void markStructureDirty() {
        if (this.level != null && !this.level.isClientSide()) {
            EntropicMachineLocator.markNearbyDirty(this.level, this.worldPosition);
        }
    }

    @Override
    public void addPart(BlockPos partPos) {
        BlockPos immutable = partPos.immutable();
        if (this.partSet.add(immutable)) {
            this.parts.add(immutable);
        }
    }

    @Override
    public void removePart(BlockPos partPos) {
        this.partSet.remove(partPos);
        this.parts.remove(partPos);
        markStructureDirty();
    }

    @Override
    public List<BlockPos> getParts() {
        return Collections.unmodifiableList(this.parts);
    }

    @Override
    public BlockPos getControllerPos() {
        return this.anchorPos != null ? this.anchorPos : this.worldPosition;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getMachineNameKey());
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public boolean isGuiAssembled() {
        return this.assembled;
    }

    @Override
    public boolean isGuiRunning() {
        return this.running;
    }

    @Override
    public int getGuiProgress() {
        return this.progress;
    }

    @Override
    public int getGuiMaxProgress() {
        return this.maxProgress;
    }

    @Override
    public int getGuiTemperature() {
        return this.temperature;
    }

    @Override
    public int getGuiMaxTemperature() {
        return this.maxTemperature;
    }

    @Override
    public int getGuiMachineTier() {
        return this.machineTier;
    }

    @Override
    public long getGuiStoredEnergy() {
        return this.storedEnergy;
    }

    @Override
    public long getGuiMaxEnergy() {
        return this.maxStoredEnergy;
    }

    @Override
    public int getGuiActiveParallels() {
        return 0;
    }

    @Override
    public int getGuiMaxParallels() {
        return 1;
    }

    @Override
    public boolean isGuiSafeMode() {
        return this.safeMode;
    }

    @Override
    public boolean isGuiOverclocked() {
        return this.overclocked;
    }

    @Override
    public void toggleSafeMode() {
        this.safeMode = !this.safeMode;
        saveChanges();
    }

    @Override
    public void toggleOverclock() {
        this.overclocked = !this.overclocked;
        saveChanges();
    }

    @Override
    public List<UniversalDisplayedRecipe> getDisplayedRecipes() {
        return Collections.unmodifiableList(this.displayedRecipes);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Assembled", this.assembled);
        tag.putInt("MachineTier", this.machineTier);
        tag.putBoolean("SafeMode", this.safeMode);
        tag.putBoolean("Overclocked", this.overclocked);
        if (this.anchorPos != null) {
            tag.put("EntropicAnchor", NbtUtils.writeBlockPos(this.anchorPos));
        }

        ListTag partsTag = new ListTag();
        for (BlockPos partPos : this.parts) {
            partsTag.add(NbtUtils.writeBlockPos(partPos));
        }
        tag.put("EntropicParts", partsTag);
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        this.assembled = tag.getBoolean("Assembled");
        this.machineTier = Math.max(MultiblockMachineTier.MK1.level(), tag.getInt("MachineTier"));
        this.safeMode = !tag.contains("SafeMode") || tag.getBoolean("SafeMode");
        this.overclocked = tag.getBoolean("Overclocked");
        this.anchorPos = null;
        if (tag.contains("EntropicAnchor")) {
            NbtUtils.readBlockPos(tag.getCompound("EntropicAnchor"), "").ifPresent(pos -> this.anchorPos = pos.immutable());
        }

        this.parts.clear();
        this.partSet.clear();
        ListTag partsTag = tag.getList("EntropicParts", Tag.TAG_COMPOUND);
        for (Tag partTag : partsTag) {
            NbtUtils.readBlockPos((CompoundTag) partTag, "").ifPresent(pos -> {
                BlockPos immutable = pos.immutable();
                this.parts.add(immutable);
                this.partSet.add(immutable);
            });
        }
        updateVisualState();
    }

    protected void updateVisualState() {
        onGridConnectableSidesChanged();
        if (this.level == null || this.level.isClientSide() || this.isRemoved()) {
            return;
        }

        BlockState current = this.level.getBlockState(this.worldPosition);
        if (current.getBlock() instanceof com.raishxn.ufo.block.AbstractEntropicMachineBlock<?> block) {
            BlockState updated = current
                    .setValue(com.raishxn.ufo.block.AbstractEntropicMachineBlock.FORMED, this.assembled)
                    .setValue(com.raishxn.ufo.block.AbstractEntropicMachineBlock.POWERED, this.assembled && this.getMainNode().isOnline());
            if (updated != current) {
                this.level.setBlock(this.worldPosition, updated, 2);
            }
        }
    }

    private Iterator<IGridNode> getMultiblockNodes() {
        List<IGridNode> nodes = new ArrayList<>(this.parts.size() + 1);

        if (this.level == null) {
            addNode(nodes, this);
            return nodes.iterator();
        }

        if (!this.assembled) {
            addNode(nodes, this);
            return nodes.iterator();
        }

        for (BlockPos partPos : this.parts) {
            var part = this.level.getBlockEntity(partPos);
            if (part instanceof AbstractEntropicMachineBE machine
                    && machine.isAssembled()
                    && machine.getClass() == this.getClass()
                    && java.util.Objects.equals(machine.anchorPos, this.anchorPos)) {
                addNode(nodes, machine);
            }
        }

        addNode(nodes, this);
        return nodes.iterator();
    }

    private static void addNode(List<IGridNode> nodes, AbstractEntropicMachineBE machine) {
        IGridNode node = machine.getActionableNode();
        if (node != null) {
            nodes.add(node);
        }
    }

    public boolean isPrimaryMachine() {
        return this.assembled && this.anchorPos != null && this.worldPosition.equals(this.anchorPos);
    }
}

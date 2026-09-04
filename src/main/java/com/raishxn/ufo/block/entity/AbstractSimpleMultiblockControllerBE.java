package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockMachineTier;
import com.raishxn.ufo.api.multiblock.MultiblockDefinition;
import com.raishxn.ufo.api.multiblock.MultiblockScanMode;
import com.raishxn.ufo.api.multiblock.MultiblockRuntimeState;
import com.raishxn.ufo.api.multiblock.StructureMembershipIndex;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.diagnostic.MachineMetricKey;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import com.raishxn.ufo.block.entity.processing.ParallelViewerSnapshotBudget;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractSimpleMultiblockControllerBE extends BlockEntity implements IMultiblockController, MenuProvider, IUniversalMultiblockController, IUpgradeableObject {
    protected boolean assembled = false;
    protected boolean structureDirty = true;
    protected final List<BlockPos> parts = new ArrayList<>();
    protected boolean running = false;
    protected int progress = 0;
    protected int maxProgress = 0;
    protected int temperature = 0;
    protected int maxTemperature = 10000;
    protected int machineTier = MultiblockMachineTier.MK1.level();
    protected long storedEnergy = 0L;
    protected long maxStoredEnergy = 0L;
    protected boolean safeMode = true;
    protected boolean overclocked = false;
    protected MultiblockRuntimeState runtimeState = MultiblockRuntimeState.UNFORMED;
    protected final List<UniversalDisplayedRecipe> displayedRecipes = new ArrayList<>();
    protected final IUpgradeInventory upgrades;
    private MachineMetricKey performanceMetricKey;
    @Nullable
    private Direction indexedFootprintFacing;
    private List<Long> indexedFootprint = List.of();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AbstractSimpleMultiblockControllerBE.this.assembled ? 1 : 0;
                case 1 -> AbstractSimpleMultiblockControllerBE.this.running ? 1 : 0;
                case 2 -> AbstractSimpleMultiblockControllerBE.this.progress;
                case 3 -> AbstractSimpleMultiblockControllerBE.this.maxProgress;
                case 4 -> AbstractSimpleMultiblockControllerBE.this.temperature;
                case 5 -> AbstractSimpleMultiblockControllerBE.this.maxTemperature;
                case 6 -> AbstractSimpleMultiblockControllerBE.this.safeMode ? 1 : 0;
                case 7 -> AbstractSimpleMultiblockControllerBE.this.overclocked ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> AbstractSimpleMultiblockControllerBE.this.assembled = value == 1;
                case 1 -> AbstractSimpleMultiblockControllerBE.this.running = value == 1;
                case 2 -> AbstractSimpleMultiblockControllerBE.this.progress = value;
                case 3 -> AbstractSimpleMultiblockControllerBE.this.maxProgress = value;
                case 4 -> AbstractSimpleMultiblockControllerBE.this.temperature = value;
                case 5 -> AbstractSimpleMultiblockControllerBE.this.maxTemperature = value;
                case 6 -> AbstractSimpleMultiblockControllerBE.this.safeMode = value == 1;
                case 7 -> AbstractSimpleMultiblockControllerBE.this.overclocked = value == 1;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    protected AbstractSimpleMultiblockControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.upgrades = UpgradeInventories.forMachine(state.getBlock().asItem(), 4, this::saveChanges);
    }

    protected abstract MultiblockPattern getControllerPattern();

    /** Migrated controllers override this to opt into the compiled 3.0 scanner. */
    protected MultiblockDefinition getMultiblockDefinition() {
        return null;
    }

    public MultiblockRuntimeState getRuntimeState() {
        return this.runtimeState;
    }

    public boolean usesCompiledDefinition() {
        return getMultiblockDefinition() != null;
    }

    protected final void setRuntimeState(MultiblockRuntimeState state) {
        if (getMultiblockDefinition() != null) this.runtimeState = state;
    }

    protected abstract String getControllerTranslationKey();

    public ContainerData getContainerData() {
        return this.data;
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        long startedAt = System.nanoTime();
        try {
            if (this.structureDirty) {
                scanStructure(this.level);
                this.structureDirty = false;
            }

            machineTick();
        } finally {
            MachinePerformanceRegistry.INSTANCE.recordTick(performanceMetricKey(), System.nanoTime() - startedAt, this.level.getGameTime());
        }
    }

    protected void machineTick() {
        tickThermals();
    }

    protected void tickThermals() {
        rebuildDisplayedRecipes();
        if (this.running) {
            this.temperature = Math.min(this.maxTemperature, this.temperature + (this.overclocked ? 8 : 2));
            if (this.safeMode && this.temperature >= this.maxTemperature) {
                this.running = false;
                this.progress = 0;
            }
        } else if (this.temperature > 0) {
            this.temperature = Math.max(0, this.temperature - 2);
        }
    }

    @Override
    public boolean isAssembled() {
        return this.assembled;
    }

    @Override
    public void scanStructure(Level level) {
        long startedAt = System.nanoTime();
        MultiblockPattern pattern = getControllerPattern();
        try {
            boolean wasAssembled = this.assembled;
            boolean wasRunning = this.running;
            int previousProgress = this.progress;
            int previousMaxProgress = this.maxProgress;
            int previousTemperature = this.temperature;
            int previousMachineTier = this.machineTier;
            MultiblockRuntimeState previousRuntimeState = this.runtimeState;
            BlockState state = level.getBlockState(this.worldPosition);
            Direction facing = MultiblockControllerDefinitions.getPatternFacing(this, state);

            MultiblockDefinition definition = getMultiblockDefinition();
            if (definition != null) {
                setRuntimeState(MultiblockRuntimeState.FORMING);
                if (this.indexedFootprintFacing != facing || this.indexedFootprint.isEmpty()) {
                    List<BlockPos> trackedPositions = pattern.getTrackedPositions(this.worldPosition, facing);
                    List<Long> packedPositions = new ArrayList<>(trackedPositions.size());
                    for (BlockPos trackedPos : trackedPositions) packedPositions.add(trackedPos.asLong());
                    this.indexedFootprint = List.copyOf(packedPositions);
                    this.indexedFootprintFacing = facing;
                }
                StructureMembershipIndex.INSTANCE.register(
                        level.dimension().location().toString(),
                        this.worldPosition.asLong(),
                        this.indexedFootprint);
            }
            MultiblockPattern.MatchResult result = definition == null
                    ? pattern.match(level, this.worldPosition, facing)
                    : definition.scan(level, this.worldPosition, facing, MultiblockScanMode.FAST);
            this.assembled = result.isValid() && validateMatchedStructure(level, result, facing);
            if (definition != null) {
                setRuntimeState(this.assembled ? MultiblockRuntimeState.IDLE : MultiblockRuntimeState.UNFORMED);
            }

        List<BlockPos> matchedParts = result.partPositions();
        boolean layoutChanged = !this.parts.equals(matchedParts);
        if (layoutChanged && !this.parts.isEmpty()) {
            Set<BlockPos> matchedPartSet = new HashSet<>(matchedParts);
            for (BlockPos existingPart : this.parts) {
                if (!matchedPartSet.contains(existingPart)
                        && level.getBlockEntity(existingPart) instanceof IMultiblockPart part) {
                    part.unlinkFromController();
                }
            }
        }

        if (this.assembled) {
            this.machineTier = resolveMachineTier(result);
            if (layoutChanged) {
                this.parts.clear();
                this.parts.addAll(matchedParts);
            }
            for (BlockPos partPos : matchedParts) {
                if (level.getBlockEntity(partPos) instanceof IMultiblockPart part
                        && !this.worldPosition.equals(part.getControllerPos())) {
                    part.linkToController(this.worldPosition);
                }
            }
        } else {
            if (layoutChanged) this.parts.clear();
            this.running = false;
            this.progress = 0;
            this.maxProgress = 0;
            this.temperature = 0;
            this.machineTier = MultiblockMachineTier.MK1.level();
        }

        updateControllerBlockState(level.getBlockState(this.worldPosition), wasAssembled);
        if (layoutChanged
                || wasAssembled != this.assembled
                || wasRunning != this.running
                || previousProgress != this.progress
                || previousMaxProgress != this.maxProgress
                || previousTemperature != this.temperature
                || previousMachineTier != this.machineTier
                || previousRuntimeState != this.runtimeState) {
            this.setChanged();
        }
        } finally {
            MachinePerformanceRegistry.INSTANCE.recordScan(
                    performanceMetricKey(), System.nanoTime() - startedAt,
                    pattern.getTestedPositionCount(), level.getGameTime());
        }
    }

    protected final MachineMetricKey performanceMetricKey() {
        if (this.performanceMetricKey == null && this.level != null) {
            this.performanceMetricKey = new MachineMetricKey(
                    this.level.dimension().location().toString(),
                    this.worldPosition.asLong(),
                    this.getClass().getSimpleName());
        }
        return this.performanceMetricKey;
    }

    private void updateControllerBlockState(BlockState currentState, boolean wasAssembled) {
        if (this.level == null || wasAssembled == this.assembled) {
            return;
        }

        for (var property : currentState.getProperties()) {
            if (property instanceof BooleanProperty booleanProperty && "active".equals(booleanProperty.getName())) {
                this.level.setBlock(this.worldPosition, currentState.setValue(booleanProperty, this.assembled), Block.UPDATE_CLIENTS);
                return;
            }
        }

        this.level.sendBlockUpdated(this.worldPosition, currentState, currentState, Block.UPDATE_ALL);
    }

    public void markStructureDirty() {
        this.structureDirty = true;
    }

    protected int resolveMachineTier(MultiblockPattern.MatchResult result) {
        return MultiblockMachineTier.MK1.level();
    }

    protected boolean validateMatchedStructure(Level level, MultiblockPattern.MatchResult result, Direction facing) {
        return true;
    }

    protected boolean hasOngoingWork() {
        return this.running || this.progress > 0 || this.maxProgress > 0;
    }

    public void onControllerBroken() {
        if (this.level == null) {
            return;
        }

        if (getMultiblockDefinition() != null) {
            StructureMembershipIndex.INSTANCE.unregister(
                    this.level.dimension().location().toString(), this.worldPosition.asLong());
        }
        this.indexedFootprint = List.of();
        this.indexedFootprintFacing = null;

        for (BlockPos partPos : this.parts) {
            if (this.level.getBlockEntity(partPos) instanceof IMultiblockPart part) {
                part.unlinkFromController();
            }
        }

        this.parts.clear();
        this.assembled = false;
        this.running = false;
        this.progress = 0;
        this.maxProgress = 0;
        this.temperature = 0;
        this.machineTier = MultiblockMachineTier.MK1.level();
        setRuntimeState(MultiblockRuntimeState.UNFORMED);
        this.setChanged();
    }

    @Override
    public void addPart(BlockPos partPos) {
        if (!this.parts.contains(partPos)) {
            this.parts.add(partPos);
        }
    }

    @Override
    public void removePart(BlockPos partPos) {
        this.parts.remove(partPos);
        markStructureDirty();
    }

    @Override
    public List<BlockPos> getParts() {
        return Collections.unmodifiableList(this.parts);
    }

    @Override
    public BlockPos getControllerPos() {
        return this.worldPosition;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(getControllerTranslationKey());
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("assembled", this.assembled);
        tag.putBoolean("running", this.running);
        tag.putInt("progress", this.progress);
        tag.putInt("maxProgress", this.maxProgress);
        tag.putInt("temperature", this.temperature);
        tag.putInt("maxTemperature", this.maxTemperature);
        tag.putInt("machineTier", this.machineTier);
        tag.putLong("storedEnergy", this.storedEnergy);
        tag.putLong("maxStoredEnergy", this.maxStoredEnergy);
        tag.putBoolean("safeMode", this.safeMode);
        tag.putBoolean("overclocked", this.overclocked);
        if (getMultiblockDefinition() != null) tag.putString("runtimeState", this.runtimeState.name());
        this.upgrades.writeToNBT(tag, "upgrades", registries);

        ListTag partsList = new ListTag();
        for (BlockPos pos : this.parts) {
            partsList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("parts", partsList);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        if (tag.contains("viewerState", Tag.TAG_COMPOUND)) {
            loadViewerState(tag.getCompound("viewerState"));
            return;
        }
        super.loadAdditional(tag, registries);
        this.assembled = tag.getBoolean("assembled");
        this.running = tag.getBoolean("running");
        this.progress = tag.getInt("progress");
        this.maxProgress = tag.getInt("maxProgress");
        this.temperature = tag.getInt("temperature");
        if (tag.contains("maxTemperature")) {
            this.maxTemperature = tag.getInt("maxTemperature");
        }
        if (tag.contains("machineTier")) {
            this.machineTier = Math.max(MultiblockMachineTier.MK1.level(), tag.getInt("machineTier"));
        }
        this.storedEnergy = tag.getLong("storedEnergy");
        this.maxStoredEnergy = tag.getLong("maxStoredEnergy");
        this.safeMode = !tag.contains("safeMode") || tag.getBoolean("safeMode");
        this.overclocked = tag.getBoolean("overclocked");
        if (getMultiblockDefinition() != null && tag.contains("runtimeState", Tag.TAG_STRING)) {
            try {
                this.runtimeState = MultiblockRuntimeState.valueOf(tag.getString("runtimeState"));
            } catch (IllegalArgumentException ignored) {
                this.runtimeState = this.assembled ? MultiblockRuntimeState.IDLE : MultiblockRuntimeState.UNFORMED;
            }
        }

        this.parts.clear();
        if (tag.contains("parts", Tag.TAG_LIST)) {
            ListTag partsList = tag.getList("parts", Tag.TAG_COMPOUND);
            for (int i = 0; i < partsList.size(); i++) {
                NbtUtils.readBlockPos(partsList.getCompound(i), "").ifPresent(this.parts::add);
            }
        }
        this.upgrades.readFromNBT(tag, "upgrades", registries);
        loadDisplayedRecipes(tag);

        this.structureDirty = true;
        this.indexedFootprint = List.of();
        this.indexedFootprintFacing = null;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.put("viewerState", saveViewerState());
        if (this.level != null && !this.level.isClientSide()) {
            MachinePerformanceRegistry.INSTANCE.recordSync(performanceMetricKey(), tag.sizeInBytes(), this.level.getGameTime());
        }
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void saveChanges() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
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
        return this.running ? 1 : 0;
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
        if (hasOngoingWork()) {
            return;
        }
        this.safeMode = !this.safeMode;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void toggleOverclock() {
        if (hasOngoingWork()) {
            return;
        }
        this.overclocked = !this.overclocked;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public List<UniversalDisplayedRecipe> getDisplayedRecipes() {
        return List.copyOf(this.displayedRecipes);
    }

    protected void rebuildDisplayedRecipes() {
        this.displayedRecipes.clear();
        if (this.running || this.progress > 0 || this.maxProgress > 0) {
            String label = Component.translatable(getControllerTranslationKey()).getString();
            if (label.endsWith(" Controller")) {
                label = label.substring(0, label.length() - " Controller".length());
            }
            this.displayedRecipes.add(new UniversalDisplayedRecipe(
                    ItemStack.EMPTY,
                    net.neoforged.neoforge.fluids.FluidStack.EMPTY,
                    Component.literal(label),
                    1,
                    this.progress,
                    this.maxProgress));
        }
    }

    private ListTag saveDisplayedRecipes() {
        ListTag recipeList = new ListTag();
        int recipeCount = ParallelViewerSnapshotBudget.rowCount(this.displayedRecipes.size());
        for (int recipeIndex = 0; recipeIndex < recipeCount; recipeIndex++) {
            UniversalDisplayedRecipe recipe = this.displayedRecipes.get(recipeIndex);
            CompoundTag recipeTag = new CompoundTag();
            if (!recipe.itemIcon().isEmpty()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(recipe.itemIcon().getItem());
                if (itemId != null) {
                    recipeTag.putString("itemIcon", itemId.toString());
                }
            }
            if (!recipe.fluidIcon().isEmpty()) {
                ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(recipe.fluidIcon().getFluid());
                if (fluidId != null) {
                    recipeTag.putString("fluidIcon", fluidId.toString());
                }
            }
            recipeTag.putString("label", recipe.label().getString());
            recipeTag.putLong("outputAmount", recipe.outputAmount());
            recipeTag.putInt("progress", recipe.progress());
            recipeTag.putInt("maxProgress", recipe.maxProgress());
            recipeTag.putInt("processIndex", recipe.processIndex());
            recipeTag.putBoolean("paused", recipe.paused());
            recipeList.add(recipeTag);
        }
        return recipeList;
    }

    /**
     * Client-only presentation data. It deliberately excludes persistent process
     * ledgers, upgrades and structure membership, which are only needed by the
     * server/save path.
     */
    private CompoundTag saveViewerState() {
        CompoundTag viewerState = new CompoundTag();
        viewerState.putBoolean("assembled", this.assembled);
        viewerState.putBoolean("running", this.running);
        viewerState.putInt("progress", this.progress);
        viewerState.putInt("maxProgress", this.maxProgress);
        viewerState.putInt("temperature", this.temperature);
        viewerState.putInt("maxTemperature", this.maxTemperature);
        viewerState.putInt("machineTier", this.machineTier);
        viewerState.putLong("storedEnergy", this.storedEnergy);
        viewerState.putLong("maxStoredEnergy", this.maxStoredEnergy);
        viewerState.putBoolean("safeMode", this.safeMode);
        viewerState.putBoolean("overclocked", this.overclocked);
        viewerState.put("displayedRecipes", saveDisplayedRecipes());
        return viewerState;
    }

    private void loadViewerState(CompoundTag viewerState) {
        this.assembled = viewerState.getBoolean("assembled");
        this.running = viewerState.getBoolean("running");
        this.progress = viewerState.getInt("progress");
        this.maxProgress = viewerState.getInt("maxProgress");
        this.temperature = viewerState.getInt("temperature");
        this.maxTemperature = Math.max(1, viewerState.getInt("maxTemperature"));
        this.machineTier = Math.max(MultiblockMachineTier.MK1.level(), viewerState.getInt("machineTier"));
        this.storedEnergy = viewerState.getLong("storedEnergy");
        this.maxStoredEnergy = viewerState.getLong("maxStoredEnergy");
        this.safeMode = viewerState.getBoolean("safeMode");
        this.overclocked = viewerState.getBoolean("overclocked");
        loadDisplayedRecipes(viewerState);
    }

    private void loadDisplayedRecipes(CompoundTag tag) {
        this.displayedRecipes.clear();
        if (!tag.contains("displayedRecipes", Tag.TAG_LIST)) {
            return;
        }

        ListTag recipeList = tag.getList("displayedRecipes", Tag.TAG_COMPOUND);
        for (int i = 0; i < recipeList.size(); i++) {
            CompoundTag recipeTag = recipeList.getCompound(i);
            ItemStack itemIcon = ItemStack.EMPTY;
            net.neoforged.neoforge.fluids.FluidStack fluidIcon = net.neoforged.neoforge.fluids.FluidStack.EMPTY;

            if (recipeTag.contains("itemIcon", Tag.TAG_STRING)) {
                ResourceLocation itemId = ResourceLocation.tryParse(recipeTag.getString("itemIcon"));
                if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
                    itemIcon = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
                }
            }

            if (recipeTag.contains("fluidIcon", Tag.TAG_STRING)) {
                ResourceLocation fluidId = ResourceLocation.tryParse(recipeTag.getString("fluidIcon"));
                if (fluidId != null && BuiltInRegistries.FLUID.containsKey(fluidId)) {
                    fluidIcon = new net.neoforged.neoforge.fluids.FluidStack(BuiltInRegistries.FLUID.get(fluidId), 1);
                }
            }

            this.displayedRecipes.add(new UniversalDisplayedRecipe(
                    itemIcon,
                    fluidIcon,
                    Component.literal(recipeTag.getString("label")),
                    recipeTag.getLong("outputAmount"),
                    recipeTag.getInt("progress"),
                    recipeTag.getInt("maxProgress"),
                    recipeTag.contains("processIndex") ? recipeTag.getInt("processIndex") : -1,
                    recipeTag.getBoolean("paused")));
        }
    }
}

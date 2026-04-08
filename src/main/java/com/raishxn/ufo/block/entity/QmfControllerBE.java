package com.raishxn.ufo.block.entity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import net.minecraft.world.inventory.ContainerData;
import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.pattern.QmfPatternFactory;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.recipe.QMFRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class QmfControllerBE extends BlockEntity implements net.minecraft.world.MenuProvider, IMultiblockController, IUniversalMultiblockController, IUpgradeableObject {

    private boolean assembled = false;
    private boolean structureDirty = true;
    private int scanCooldown = 0;
    private final List<BlockPos> parts = new ArrayList<>();

    // Processing State
    private ResourceLocation activeRecipeId = null;
    private QMFRecipe cachedRecipe = null;
    private boolean running = false;
    private int progress = 0;
    private int maxProgress = 0;
    private int temperature = 0;
    private int maxTemperature = 1000;
    private boolean safeMode = true;
    private boolean overclocked = false;
    private final List<UniversalDisplayedRecipe> displayedRecipes = new ArrayList<>();
    private final IUpgradeInventory upgrades;

    // Buffer states
    private long energyBuffer = 0;
    private long[] itemBuffers = new long[0];
    private long[] fluidBuffers = new long[0];

    // Container data for Menu
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> assembled ? 1 : 0;
                case 1 -> running ? 1 : 0;
                case 2 -> progress;
                case 3 -> maxProgress;
                case 4 -> temperature;
                case 5 -> maxTemperature;
                case 6 -> safeMode ? 1 : 0;
                case 7 -> overclocked ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Read-only on client
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    private static MultiblockPattern PATTERN;

    public QmfControllerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QMF_CONTROLLER.get(), pos, state);
        this.upgrades = UpgradeInventories.forMachine(state.getBlock().asItem(), 2, this::saveChanges);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.ufo.quantum_matter_fabricator_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new com.raishxn.ufo.screen.QmfControllerMenu(id, playerInventory, this);
    }

    private static MultiblockPattern getPattern() {
        if (PATTERN == null) PATTERN = QmfPatternFactory.getPattern();
        return PATTERN;
    }

    public void serverTick() {
        this.scanCooldown--;
        if (this.structureDirty || this.scanCooldown <= 0) {
            scanStructure();
            this.scanCooldown = 40;
            this.structureDirty = false;
        }

        rebuildDisplayedRecipes();

        if (this.assembled && this.running && this.cachedRecipe != null) {
            processTick();
        } else if (this.temperature > 0) {
            this.temperature = Math.max(0, this.temperature - 2);
        }
    }

    private void processTick() {
        // Find ME Network
        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null || nodeBE.getActionableNode() == null) return;

        IGridNode node = nodeBE.getActionableNode();
        IGrid grid = node.getGrid();
        if (grid == null) return;

        IEnergyService energyService = grid.getEnergyService();
        IStorageService storageService = grid.getStorageService();
        MEStorage inventory = storageService.getInventory();
        IActionSource src = IActionSource.ofMachine(nodeBE);

        // 1. Charge Energy limit
        if (this.energyBuffer < this.cachedRecipe.getEnergy()) {
            long needed = this.cachedRecipe.getEnergy() - this.energyBuffer;
            long chargeRate = 5_000_000; // Fast charge rate
            long toCharge = Math.min(needed, chargeRate);
            double extracted = energyService.extractAEPower(toCharge, Actionable.MODULATE, PowerMultiplier.CONFIG);
            this.energyBuffer += (long) extracted;
        }

        // 2. Extract Items/Fluids
        boolean materialsFulfilled = true;

        for (int i = 0; i < this.cachedRecipe.getItemInputs().size(); i++) {
            var req = this.cachedRecipe.getItemInputs().get(i);
            if (this.itemBuffers[i] < req.amount()) {
                materialsFulfilled = false;
                long needed = req.amount() - this.itemBuffers[i];
                long extractLimit = 100_000; // Extract up to 100k items per tick
                long toExtract = Math.min(needed, extractLimit);

                // Simple implementation: try matching first item in ingredient
                for (ItemStack match : req.ingredient().getItems()) {
                    long ext = inventory.extract(AEItemKey.of(match), toExtract, Actionable.MODULATE, src);
                    this.itemBuffers[i] += ext;
                    toExtract -= ext;
                    if (toExtract <= 0) break;
                }
            }
        }

        for (int i = 0; i < this.cachedRecipe.getFluidInputs().size(); i++) {
            var req = this.cachedRecipe.getFluidInputs().get(i);
            if (this.fluidBuffers[i] < req.amount()) {
                materialsFulfilled = false;
                long needed = req.amount() - this.fluidBuffers[i];
                long extractLimit = 1_000_000; // 1000 buckets per tick
                long toExtract = Math.min(needed, extractLimit);

                FluidStack fluidStack = req.fluid();
                long ext = inventory.extract(AEFluidKey.of(fluidStack.getFluid()), toExtract, Actionable.MODULATE, src);
                this.fluidBuffers[i] += ext;
            }
        }

        // 3. Process if fulfilled
        if (materialsFulfilled && this.energyBuffer >= this.cachedRecipe.getEnergy()) {
            this.temperature = Math.min(this.maxTemperature, this.temperature + (this.overclocked ? 10 : 3));
            if (this.safeMode && this.temperature >= this.maxTemperature) {
                this.running = false;
                this.progress = 0;
                this.setChanged();
                return;
            }

            this.progress += this.overclocked ? 5 : 1;
            if (this.progress >= this.maxProgress) {
                // Done!
                ItemStack out = this.cachedRecipe.getResultItem(this.level.registryAccess());
                inventory.insert(AEItemKey.of(out), out.getCount(), Actionable.MODULATE, src);
                
                // Reset loop
                this.progress = 0;
                for (int i = 0; i < this.itemBuffers.length; i++) this.itemBuffers[i] = 0;
                for (int i = 0; i < this.fluidBuffers.length; i++) this.fluidBuffers[i] = 0;
                this.energyBuffer = 0;
                this.temperature = Math.max(0, this.temperature - 25);
            }
        }

        this.setChanged();
    }

    public void scanStructure() {
        scanStructure(this.level);
    }

    @Override
    public void scanStructure(net.minecraft.world.level.Level level) {
        if (this.level == null || this.level.isClientSide()) return;

        var pattern = getPattern();
        net.minecraft.core.Direction facing = net.minecraft.core.Direction.NORTH;
        BlockState currentState = this.level.getBlockState(this.worldPosition);
        if (currentState.hasProperty(DirectionalBlock.FACING)) {
            facing = currentState.getValue(DirectionalBlock.FACING);
        }
        var match = pattern.match(this.level, this.worldPosition, facing);
        boolean wasAssembled = this.assembled;

        if (match.isValid()) {
            this.assembled = true;

            for (BlockPos p : this.parts) {
                if (!match.partPositions().contains(p)) {
                    if (this.level.getBlockEntity(p) instanceof IMultiblockPart part) {
                        part.unlinkFromController();
                    }
                }
            }
            this.parts.clear();
            for (BlockPos p : match.partPositions()) {
                if (!p.equals(this.worldPosition)) {
                    this.parts.add(p);
                    if (this.level.getBlockEntity(p) instanceof IMultiblockPart part) {
                        part.linkToController(this.worldPosition);
                    }
                }
            }
        } else {
            onControllerBroken();
        }

        updateActiveState(currentState, wasAssembled);
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    private void updateActiveState(BlockState currentState, boolean wasAssembled) {
        if (this.level == null || wasAssembled == this.assembled) {
            return;
        }

        for (var property : currentState.getProperties()) {
            if (property instanceof BooleanProperty booleanProperty && "active".equals(booleanProperty.getName())) {
                this.level.setBlock(this.worldPosition, currentState.setValue(booleanProperty, this.assembled), Block.UPDATE_CLIENTS);
                return;
            }
        }
    }

    public void markStructureDirty() {
        this.structureDirty = true;
        this.scanCooldown = 0;
    }

    public void onControllerBroken() {
        if (this.level == null) return;
        for (BlockPos partPos : this.parts) {
            if (this.level.getBlockEntity(partPos) instanceof IMultiblockPart part) {
                part.unlinkFromController();
            }
        }
        this.parts.clear();
        this.assembled = false;
        this.running = false;
        this.progress = 0;
        this.temperature = 0;
    }

    @Override
    public boolean isAssembled() {
        return this.assembled;
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

    private AENetworkedBlockEntity getConnectedNetworkNode() {
        if (this.level == null) return null;
        for (BlockPos p : this.parts) {
            if (this.level.getBlockEntity(p) instanceof AENetworkedBlockEntity nodeBE) {
                if (nodeBE.getActionableNode() != null && nodeBE.getActionableNode().getGrid() != null) {
                    return nodeBE;
                }
            }
        }
        return null;
    }

    // NBT and serialization
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("assembled", this.assembled);
        tag.putBoolean("running", this.running);
        tag.putInt("progress", this.progress);
        tag.putInt("maxProgress", this.maxProgress);
        tag.putInt("temperature", this.temperature);
        tag.putInt("maxTemperature", this.maxTemperature);
        tag.putBoolean("safeMode", this.safeMode);
        tag.putBoolean("overclocked", this.overclocked);
        this.upgrades.writeToNBT(tag, "upgrades", registries);
        tag.putLong("energyBuffer", this.energyBuffer);

        ListTag partsList = new ListTag();
        for (BlockPos pos : this.parts) {
            partsList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("parts", partsList);

        if (this.activeRecipeId != null) {
            tag.putString("activeRecipeId", this.activeRecipeId.toString());
        }

        tag.putLongArray("itemBuffers", this.itemBuffers);
        tag.putLongArray("fluidBuffers", this.fluidBuffers);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.assembled = tag.getBoolean("assembled");
        this.running = tag.getBoolean("running");
        this.progress = tag.getInt("progress");
        this.maxProgress = tag.getInt("maxProgress");
        this.temperature = tag.getInt("temperature");
        if (tag.contains("maxTemperature")) {
            this.maxTemperature = tag.getInt("maxTemperature");
        }
        this.safeMode = !tag.contains("safeMode") || tag.getBoolean("safeMode");
        this.overclocked = tag.getBoolean("overclocked");
        this.energyBuffer = tag.getLong("energyBuffer");

        this.parts.clear();
        if (tag.contains("parts", Tag.TAG_LIST)) {
            ListTag partsList = tag.getList("parts", Tag.TAG_COMPOUND);
            for (int i = 0; i < partsList.size(); i++) {
                NbtUtils.readBlockPos(partsList.getCompound(i), "").ifPresent(this.parts::add);
            }
        }

        if (tag.contains("activeRecipeId", Tag.TAG_STRING)) {
            this.activeRecipeId = ResourceLocation.parse(tag.getString("activeRecipeId"));
            // Try to resolve recipe
            if (this.level != null) {
                Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> recipeInfo = this.level.getRecipeManager().byKey(this.activeRecipeId);
                if (recipeInfo.isPresent() && recipeInfo.get().value() instanceof QMFRecipe r) {
                    this.cachedRecipe = r;
                }
            }
        }

        this.itemBuffers = tag.getLongArray("itemBuffers");
        this.fluidBuffers = tag.getLongArray("fluidBuffers");
        this.upgrades.readFromNBT(tag, "upgrades", registries);
        this.structureDirty = true;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
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
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void toggleOverclock() {
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

    private void rebuildDisplayedRecipes() {
        this.displayedRecipes.clear();
        if (this.cachedRecipe == null || this.level == null) {
            return;
        }

        ItemStack out = this.cachedRecipe.getResultItem(this.level.registryAccess());
        this.displayedRecipes.add(new UniversalDisplayedRecipe(
                out,
                FluidStack.EMPTY,
                out.getHoverName(),
                out.getCount(),
                this.progress,
                this.maxProgress));
    }
}

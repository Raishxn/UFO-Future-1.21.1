package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory;
import com.raishxn.ufo.block.StellarNexusControllerBlock;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;
import net.pedroksl.ae2addonlib.recipes.IngredientStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.blockentity.grid.AENetworkedBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Block Entity for the Stellar Nexus Controller.
 * <p>
 * Implements {@link IMultiblockController} from our internal API to scan and
 * validate the multiblock structure using {@link MultiblockPattern}.
 * <p>
 * Phase 1 (Foundation) — only structure scanning; no recipes or GUI yet.
 */
import com.raishxn.ufo.screen.StellarNexusControllerMenu;

public class StellarNexusControllerBE extends BlockEntity implements IMultiblockController, MenuProvider {

    private boolean assembled = false;
    private boolean structureDirty = true;
    private int scanCooldown = 0;
    private final List<BlockPos> parts = new ArrayList<>();

    // Processing state
    private ResourceLocation activeRecipeId = null;
    private int progress = 0;
    private int maxProgress = 0; // Cached total time
    private final ContainerData data;

    // The multiblock pattern will be initialized lazily
    private static MultiblockPattern PATTERN;

    public StellarNexusControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> StellarNexusControllerBE.this.progress;
                    case 1 -> StellarNexusControllerBE.this.maxProgress;
                    case 2 -> StellarNexusControllerBE.this.assembled ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> StellarNexusControllerBE.this.progress = pValue;
                    case 1 -> StellarNexusControllerBE.this.maxProgress = pValue;
                    case 2 -> StellarNexusControllerBE.this.assembled = pValue == 1;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    // ──────────────────── IMultiblockController ────────────────────

    @Override
    public boolean isAssembled() {
        return this.assembled;
    }

    @Override
    public void scanStructure(Level level) {
        MultiblockPattern pattern = getPattern();
        BlockState currentState = level.getBlockState(this.worldPosition);
        net.minecraft.core.Direction facing = net.minecraft.core.Direction.NORTH;
        if (currentState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            facing = currentState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        }
        MultiblockPattern.MatchResult result = pattern.match(level, this.worldPosition, facing);

        boolean wasAssembled = this.assembled;
        this.assembled = result.isValid();

        // Global Hatch Validation
        if (this.assembled) {
            int itemOutputs = 0;
            int fluidOutputs = 0;
            for (BlockPos partPos : result.partPositions()) {
                Block block = level.getBlockState(partPos).getBlock();
                if (block == com.raishxn.ufo.block.MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get()) itemOutputs++;
                if (block == com.raishxn.ufo.block.MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get()) fluidOutputs++;
            }

            if (itemOutputs != 1 || fluidOutputs != 1) {
                this.assembled = false;
                // A structure scanner click will just say "invalid" at the controller if we don't pass an error,
                // but at least it successfully prevents formation.
            }
        }

        // Update tracked parts
        this.parts.clear();
        if (this.assembled) {
            this.parts.addAll(result.partPositions());

            // Link all parts to this controller
            for (BlockPos partPos : this.parts) {
                if (level.getBlockEntity(partPos) instanceof IMultiblockPart part) {
                    part.linkToController(this.worldPosition);
                }
            }
        }

        // Update block state visual
        if (wasAssembled != this.assembled) {
            BlockState finalState = level.getBlockState(this.worldPosition);
            if (finalState.getBlock() instanceof StellarNexusControllerBlock) {
                level.setBlock(this.worldPosition,
                        finalState.setValue(StellarNexusControllerBlock.ASSEMBLED, this.assembled),
                        Block.UPDATE_CLIENTS);
            }
            this.setChanged();
        }
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
        // If a part is removed, flag for re-scan
        this.structureDirty = true;
    }

    @Override
    public List<BlockPos> getParts() {
        return Collections.unmodifiableList(this.parts);
    }

    @Override
    public BlockPos getControllerPos() {
        return this.worldPosition;
    }

    // ──────────────────── Server Tick ────────────────────

    /**
     * Called every server tick by the block's ticker.
     * Throttled structure scanning to avoid performance overhead.
     */
    public void serverTick() {
        if (this.level == null || this.level.isClientSide()) return;

        // Only re-scan when dirty, throttled to every 20 ticks (1 second)
        if (this.structureDirty) {
            this.scanCooldown++;
            if (this.scanCooldown >= 20) {
                this.scanCooldown = 0;
                this.structureDirty = false;
                scanStructure(this.level);
            }
        }

        if (this.assembled) {
            processMachineTick();
        } else {
            this.progress = 0; // reset if broken
        }
    }

    private void processMachineTick() {
        // Set default recipe for testing until GUI exists
        if (this.activeRecipeId == null) {
            this.activeRecipeId = UfoMod.id("stellar_simulation/red_giant_collapse");
            this.setChanged();
        }

        var recipeOpt = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof StellarSimulationRecipe recipe)) {
            return;
        }

        // Cache the time for UI representation
        this.maxProgress = recipe.getTime();

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null || nodeBE.getActionableNode() == null) return;
        
        IGridNode node = nodeBE.getActionableNode();
        if (node.getGrid() == null) return;

        IGrid grid = node.getGrid();
        IActionSource src = IActionSource.ofMachine(nodeBE);
        MEStorage storage = grid.getStorageService().getInventory();
        IEnergyService energy = grid.getEnergyService();

        // 1. Check structural requirements (mocked for now, future phase will calculate)
        int currentCoolingLevel = 3; 
        int currentFieldTier = getHighestFieldTier();
        if (currentCoolingLevel < recipe.getCoolingLevel() || currentFieldTier < recipe.getFieldTier()) {
            return; // Structure does not meet requirements
        }

        // 2. Initial input extraction
        if (this.progress == 0) {
            if (!extractInputs(recipe, storage, src)) {
                return; // Missing ingredients
            }
        }

        // 3. Consume energy per tick
        double extracted = energy.extractAEPower(recipe.getEnergy(), Actionable.MODULATE, PowerMultiplier.CONFIG);
        if (extracted < recipe.getEnergy() - 0.01) {
            return; // Not enough power, pause processing
        }

        // 4. Progress and completion
        this.progress++;
        if (this.progress >= recipe.getTime()) {
            injectOutputs(recipe, storage, src);
            this.progress = 0; // Restart cycle
        }
        this.setChanged();
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

    private int getHighestFieldTier() {
        int highest = 0;
        if (this.level == null) return 0;
        for (BlockPos p : this.parts) {
            Block block = this.level.getBlockState(p).getBlock();
            if (block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get()) return 3;
            if (block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get()) highest = Math.max(highest, 2);
            if (block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get()) highest = Math.max(highest, 1);
        }
        return highest;
    }

    public ResourceLocation getActiveRecipeId() {
        return this.activeRecipeId;
    }

    public void setActiveRecipe(ResourceLocation activeRecipeId) {
        this.activeRecipeId = activeRecipeId;
        this.progress = 0; // Changing recipe restarts progress
        this.setChanged();
        
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.getBlockPos());
            this.level.sendBlockUpdated(this.getBlockPos(), state, state, 3);
        }
    }

    // ──────────────────── Inventory Operations ────────────────────

    private boolean extractInputs(StellarSimulationRecipe recipe, MEStorage storage, IActionSource src) {
        // SIMULATE pass guarantees all inputs exist
        for (var req : recipe.getItemInputs()) {
            if (!req.isEmpty() && simulateExtractItem(req, storage, src) < req.getAmount()) return false;
        }
        for (var req : recipe.getFluidInputs()) {
            if (!req.isEmpty() && simulateExtractFluid(req, storage, src) < req.getAmount()) return false;
        }
        
        // MODULATE pass actually consumes them
        for (var req : recipe.getItemInputs()) {
            if (!req.isEmpty()) modulateExtractItem(req, storage, src);
        }
        for (var req : recipe.getFluidInputs()) {
            if (!req.isEmpty()) modulateExtractFluid(req, storage, src);
        }
        return true;
    }

    private long simulateExtractItem(IngredientStack.Item req, MEStorage storage, IActionSource src) {
        long extracted = 0;
        long needed = req.getAmount();
        for (ItemStack match : req.getIngredient().getItems()) {
            long ext = storage.extract(AEItemKey.of(match), needed, Actionable.SIMULATE, src);
            extracted += ext;
            needed -= ext;
            if (needed <= 0) break;
        }
        return extracted;
    }

    private void modulateExtractItem(IngredientStack.Item req, MEStorage storage, IActionSource src) {
        long needed = req.getAmount();
        for (ItemStack match : req.getIngredient().getItems()) {
            long ext = storage.extract(AEItemKey.of(match), needed, Actionable.MODULATE, src);
            needed -= ext;
            if (needed <= 0) break;
        }
    }

    private long simulateExtractFluid(IngredientStack.Fluid req, MEStorage storage, IActionSource src) {
        long extracted = 0;
        long needed = req.getAmount();
        for (FluidStack match : req.getIngredient().getStacks()) {
            long ext = storage.extract(AEFluidKey.of(match.getFluid()), needed, Actionable.SIMULATE, src);
            extracted += ext;
            needed -= ext;
            if (needed <= 0) break;
        }
        return extracted;
    }

    private void modulateExtractFluid(IngredientStack.Fluid req, MEStorage storage, IActionSource src) {
        long needed = req.getAmount();
        for (FluidStack match : req.getIngredient().getStacks()) {
            long ext = storage.extract(AEFluidKey.of(match.getFluid()), needed, Actionable.MODULATE, src);
            needed -= ext;
            if (needed <= 0) break;
        }
    }

    private void injectOutputs(StellarSimulationRecipe recipe, MEStorage storage, IActionSource src) {
        for (GenericStack out : recipe.getItemOutputs()) {
            storage.insert(out.what(), out.amount(), Actionable.MODULATE, src);
        }
        for (GenericStack out : recipe.getFluidOutputs()) {
            storage.insert(out.what(), out.amount(), Actionable.MODULATE, src);
        }
    }

    /**
     * Marks the structure as needing a re-scan (e.g. neighbour change).
     */
    public void markStructureDirty() {
        this.structureDirty = true;
        this.scanCooldown = 0;
    }

    /**
     * Called when the controller block is broken.
     * Unlinks all connected parts so they revert to standalone mode.
     */
    public void onControllerBroken() {
        if (this.level == null) return;
        for (BlockPos partPos : this.parts) {
            if (this.level.getBlockEntity(partPos) instanceof IMultiblockPart part) {
                part.unlinkFromController();
            }
        }
        this.parts.clear();
        this.assembled = false;
    }

    // ──────────────────── Pattern Definition ────────────────────

    /**
     * Retrieves the structural pattern from the external factory.
     */
    private static MultiblockPattern getPattern() {
        if (PATTERN == null) {
            PATTERN = StellarNexusPatternFactory.getPattern();
        }
        return PATTERN;
    }

    // ──────────────────── Menu Provider ────────────────────

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ufo.stellar_nexus_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
        return new StellarNexusControllerMenu(id, playerInventory, this, this.data);
    }

    // ──────────────────── NBT Persistence ────────────────────

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("assembled", this.assembled);
        
        tag.putInt("progress", this.progress);
        if (this.activeRecipeId != null) {
            tag.putString("activeRecipeId", this.activeRecipeId.toString());
        }

        ListTag partsList = new ListTag();
        for (BlockPos pos : this.parts) {
            partsList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("parts", partsList);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.assembled = tag.getBoolean("assembled");
        
        this.progress = tag.getInt("progress");
        if (tag.contains("activeRecipeId", Tag.TAG_STRING)) {
            this.activeRecipeId = ResourceLocation.parse(tag.getString("activeRecipeId"));
        } else {
            this.activeRecipeId = null;
        }

        this.parts.clear();
        if (tag.contains("parts", Tag.TAG_LIST)) {
            ListTag partsList = tag.getList("parts", Tag.TAG_COMPOUND);
            for (int i = 0; i < partsList.size(); i++) {
                NbtUtils.readBlockPos(partsList.getCompound(i), "").ifPresent(this.parts::add);
            }
        }

        // After loading, schedule a re-scan to validate
        this.structureDirty = true;
    }
}

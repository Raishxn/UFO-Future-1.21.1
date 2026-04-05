package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory;
import com.raishxn.ufo.block.StellarNexusControllerBlock;
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
    private boolean running = false;
    private final ContainerData data;

    // Fuel buffer — charged passively from AE2 network via Energy Input Hatch
    private long fuelBuffer = 0;
    private long fuelCapacity = 0; // Set from recipe's fuel cost when recipe is selected

    // Thermal system
    private int heatLevel = 0;        // 0-1000 (displayed as 0.0% - 100.0%)
    private static final int MAX_HEAT = 1000;
    private boolean safeMode = true;  // Default ON: auto-shutdown at 100% heat
    private int cooldownTimer = 0;    // Ticks remaining for 30-min cooldown after overheat
    private static final int COOLDOWN_DURATION = 36000; // 30 minutes = 36000 ticks

    // Cached field tier
    private int fieldLevel = 0;

    // The multiblock pattern will be initialized lazily
    private static MultiblockPattern PATTERN;

    // Fuel charge rates per Field Generator tier (AE per tick)
    private static final long[] FUEL_RATE_BY_TIER = {0, 500_000, 1_000_000, 2_000_000};

    public StellarNexusControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // ContainerData layout:
        // 0 = progress
        // 1 = maxProgress
        // 2 = assembled (0 or 1)
        // 3 = fieldLevel (1-3)
        // 4 = fuelPercent (0-100)
        // 5 = running (0 or 1)
        // 6 = heatPercent (0-1000, displayed as 0.0%-100.0%)
        // 7 = safeMode (0 or 1)
        // 8 = cooldownTimer (ticks remaining)
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> StellarNexusControllerBE.this.progress;
                    case 1 -> StellarNexusControllerBE.this.maxProgress;
                    case 2 -> StellarNexusControllerBE.this.assembled ? 1 : 0;
                    case 3 -> StellarNexusControllerBE.this.fieldLevel;
                    case 4 -> StellarNexusControllerBE.this.fuelCapacity > 0
                            ? (int) (StellarNexusControllerBE.this.fuelBuffer * 100 / StellarNexusControllerBE.this.fuelCapacity)
                            : 0;
                    case 5 -> StellarNexusControllerBE.this.running ? 1 : 0;
                    case 6 -> StellarNexusControllerBE.this.heatLevel;
                    case 7 -> StellarNexusControllerBE.this.safeMode ? 1 : 0;
                    case 8 -> StellarNexusControllerBE.this.cooldownTimer;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> StellarNexusControllerBE.this.progress = pValue;
                    case 1 -> StellarNexusControllerBE.this.maxProgress = pValue;
                    case 2 -> StellarNexusControllerBE.this.assembled = pValue == 1;
                    case 3 -> StellarNexusControllerBE.this.fieldLevel = pValue;
                    case 5 -> StellarNexusControllerBE.this.running = pValue == 1;
                    case 6 -> StellarNexusControllerBE.this.heatLevel = pValue;
                    case 7 -> StellarNexusControllerBE.this.safeMode = pValue == 1;
                    case 8 -> StellarNexusControllerBE.this.cooldownTimer = pValue;
                }
            }

            @Override
            public int getCount() {
                return 9;
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

        // Update cached field level
        this.fieldLevel = getHighestFieldTier();

        // Handle cooldown after overheat
        if (this.cooldownTimer > 0) {
            this.cooldownTimer--;
            if (this.cooldownTimer == 0) {
                this.heatLevel = 0; // Fully cooled
            }
            this.setChanged();
            return; // Machine is in cooldown, don't process
        }

        if (this.assembled) {
            processMachineTick();
        } else {
            this.progress = 0;
            this.running = false;
        }
    }

    // ──────────────────── Start Operation ────────────────────

    public boolean isActive() {
        return this.running;
    }

    /**
     * Called from the network packet when the player clicks "Start Operation".
     * Performs pre-flight checks and begins the simulation cycle.
     */
    public void startOperation() {
        if (!this.assembled || this.running || this.cooldownTimer > 0) return;
        if (this.level == null || this.level.isClientSide()) return;

        if (this.activeRecipeId == null) return;

        var recipeOpt = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof StellarSimulationRecipe recipe)) {
            return;
        }

        // Check field tier
        if (this.fieldLevel < recipe.getFieldTier()) return;

        // Check fuel buffer
        if (this.fuelBuffer < recipe.getFuelCost()) return;

        // Check inputs exist
        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null || nodeBE.getActionableNode() == null) return;
        IGridNode node = nodeBE.getActionableNode();
        if (node.getGrid() == null) return;

        IGrid grid = node.getGrid();
        IActionSource src = IActionSource.ofMachine(nodeBE);
        MEStorage storage = grid.getStorageService().getInventory();

        if (!extractInputs(recipe, storage, src)) return;

        // All good — consume fuel and start
        this.fuelBuffer -= recipe.getFuelCost();
        this.maxProgress = recipe.getTime();
        this.progress = 0;
        this.running = true;
        this.setChanged();
    }

    public void toggleSafeMode() {
        this.safeMode = !this.safeMode;
        this.setChanged();
    }

    private void processMachineTick() {
        if (this.activeRecipeId == null) return;

        var recipeOpt = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof StellarSimulationRecipe recipe)) {
            return;
        }

        // Cache the time for UI representation
        this.maxProgress = recipe.getTime();
        this.fuelCapacity = recipe.getFuelCost();

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();

        // ── Passive fuel charging (when NOT running) ──
        if (!this.running) {
            if (nodeBE != null && nodeBE.getActionableNode() != null) {
                IGridNode node = nodeBE.getActionableNode();
                if (node.getGrid() != null) {
                    IEnergyService energy = node.getGrid().getEnergyService();
                    long chargeRate = this.fieldLevel >= 1 && this.fieldLevel <= 3
                            ? FUEL_RATE_BY_TIER[this.fieldLevel] : 0;
                    long spaceLeft = this.fuelCapacity - this.fuelBuffer;
                    long toCharge = Math.min(chargeRate, spaceLeft);
                    if (toCharge > 0) {
                        double extracted = energy.extractAEPower(toCharge, Actionable.MODULATE, PowerMultiplier.CONFIG);
                        this.fuelBuffer += (long) extracted;
                    }
                }
            }
            this.setChanged();
            return;
        }

        // ── Active processing ──
        if (nodeBE == null || nodeBE.getActionableNode() == null) return;
        IGridNode node = nodeBE.getActionableNode();
        if (node.getGrid() == null) return;

        IGrid grid = node.getGrid();
        IActionSource src = IActionSource.ofMachine(nodeBE);
        MEStorage storage = grid.getStorageService().getInventory();

        // Heat generation — increases based on recipe Efficiency requirement
        int heatPerTick = recipe.getCoolingLevel() + 1; // 1-4 heat per tick
        this.heatLevel = Math.min(MAX_HEAT, this.heatLevel + heatPerTick);

        // Coolant consumption from Stellar Fuel Hatch — reduces heat
        int EfficiencyApplied = consumeFuel(grid);
        this.heatLevel = Math.max(0, this.heatLevel - EfficiencyApplied);

        // Overheat check
        if (this.heatLevel >= MAX_HEAT) {
            if (this.safeMode) {
                // Safe shutdown
                this.running = false;
                this.progress = 0;
                this.cooldownTimer = COOLDOWN_DURATION;
                this.setChanged();
                // Broadcast warning
                if (this.level != null) {
                    BlockPos pos = this.worldPosition;
                    this.level.players().forEach(p -> p.displayClientMessage(
                            Component.literal("§c§l[STELLAR NEXUS] §eSafe Mode activated at " + pos.toShortString() + " — 30 minute cooldown initiated."),
                            false));
                }
                return;
            } else {
                // MASSIVE EXPLOSION — tier-dependent
                triggerStellarExplosion();
                return;
            }
        }

        // Progress and completion
        this.progress++;
        if (this.progress >= recipe.getTime()) {
            injectOutputs(recipe, storage, src);
            this.progress = 0;
            this.running = false;
            // Gradually cool down after completion
        }
        this.setChanged();
    }

    private void triggerStellarExplosion() {
        if (this.level == null) return;
        BlockPos pos = this.worldPosition;

        // Broadcast explosion warning
        this.level.players().forEach(p -> p.displayClientMessage(
                Component.literal("§4§l[STELLAR NEXUS] §c§lCRITICAL THERMAL FAILURE at " + pos.toShortString() + "! CATASTROPHIC EXPLOSION!"),
                false));

        // Explosion radius based on field tier (bigger tier = bigger boom)
        float radius = 15.0f + (this.fieldLevel * 10.0f); // T1=25, T2=35, T3=45 blocks

        // Create the explosion
        this.level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                radius, Level.ExplosionInteraction.BLOCK);

        // Reset everything
        this.running = false;
        this.progress = 0;
        this.heatLevel = 0;
        this.fuelBuffer = 0;
        this.assembled = false;
        onControllerBroken();
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

    // ──────────────────── Fuel System ────────────────────

    /** Amount of fluid (in mB, where 1 bucket = 1000) consumed per tick for Efficiency. */
    private static final long FUEL_CONSUMPTION_PER_TICK = 100; // 100 mB/tick = 5 buckets/second

    /**
     * Tries to extract coolant from the AE2 network via the Stellar Fuel Hatch.
     * Returns the Efficiency power applied this tick (heat units to subtract).
     * <p>
     * Coolant effectiveness:
     * <ul>
     *   <li>Water: 2 Efficiency per mB consumed</li>
     *   <li>Lava (for exotic builds): 0 Efficiency (it's hot!)</li>
     *   <li>Any other fluid: 3 Efficiency per mB (generic coolant tier)</li>
     * </ul>
     */
    private int consumeFuel(IGrid grid) {
        AENetworkedBlockEntity coolantHatch = getFuelHatch();
        if (coolantHatch == null) return 0;
        
        IActionSource src = IActionSource.ofMachine(coolantHatch);
        MEStorage storage = grid.getStorageService().getInventory();
        
        // Try to extract water first (most common coolant)
        AEFluidKey waterKey = AEFluidKey.of(net.minecraft.world.level.material.Fluids.WATER);
        
        long extracted = storage.extract(waterKey, FUEL_CONSUMPTION_PER_TICK, Actionable.MODULATE, src);
        if (extracted > 0) {
            // Water: 2 Efficiency per mB
            return (int) (extracted * 2 / FUEL_CONSUMPTION_PER_TICK) * (recipe_EfficiencyBonus() + 1);
        }
        
        // Try any available fluid in the network as generic coolant
        // Iterate over what's available and try to extract the first fluid
        for (var entry : storage.getAvailableStacks()) {
            if (entry.getKey() instanceof AEFluidKey fluidKey) {
                extracted = storage.extract(fluidKey, FUEL_CONSUMPTION_PER_TICK, Actionable.MODULATE, src);
                if (extracted > 0) {
                    // Generic fluid: 3 Efficiency per mB
                    return (int) (extracted * 3 / FUEL_CONSUMPTION_PER_TICK) * (recipe_EfficiencyBonus() + 1);
                }
            }
        }
        
        return 0; // No coolant available — heat will continue to rise!
    }

    /** 
     * Bonus Efficiency multiplier based on the recipe's Efficiency level.
     * Higher Efficiency requirements → more Efficiency needed, but better coolant infrastructure
     * also provides better Efficiency.
     */
    private int recipe_EfficiencyBonus() {
        return this.fieldLevel; // T1=1x, T2=2x, T3=3x multiplier
    }

    /**
     * Find the Stellar Fuel Hatch block entity among the multiblock parts.
     */
    @Nullable
    private AENetworkedBlockEntity getFuelHatch() {
        if (this.level == null) return null;
        for (BlockPos p : this.parts) {
            BlockState state = this.level.getBlockState(p);
            if (state.is(MultiblockBlocks.STELLAR_FUEL_HATCH.get())) {
                if (this.level.getBlockEntity(p) instanceof AENetworkedBlockEntity hatchBE) {
                    return hatchBE;
                }
            }
        }
        return null;
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
        tag.putBoolean("running", this.running);
        tag.putBoolean("safeMode", this.safeMode);
        
        tag.putInt("progress", this.progress);
        tag.putInt("heatLevel", this.heatLevel);
        tag.putInt("cooldownTimer", this.cooldownTimer);
        tag.putLong("fuelBuffer", this.fuelBuffer);
        tag.putLong("fuelCapacity", this.fuelCapacity);

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
        this.running = tag.getBoolean("running");
        this.safeMode = tag.getBoolean("safeMode");
        
        this.progress = tag.getInt("progress");
        this.heatLevel = tag.getInt("heatLevel");
        this.cooldownTimer = tag.getInt("cooldownTimer");
        this.fuelBuffer = tag.getLong("fuelBuffer");
        this.fuelCapacity = tag.getLong("fuelCapacity");

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

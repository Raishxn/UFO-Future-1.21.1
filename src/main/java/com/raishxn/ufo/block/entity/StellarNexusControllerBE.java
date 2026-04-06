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
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Block Entity for the Stellar Nexus Controller.
 * <p>
 * Manages the multiblock structure, AE energy charging, fuel/coolant
 * extraction from the ME network, thermal system, and simulation processing.
 * <p>
 * <b>Terminology:</b>
 * <ul>
 *   <li><b>Energy</b> (energyBuffer) = AE power charged passively from the AE grid</li>
 *   <li><b>Fuel</b> = liquid combustible extracted from ME storage on start (e.g., Hydrogen)</li>
 *   <li><b>Coolant</b> = liquid refrigerant consumed during operation (e.g., Gelid Cryotheum)</li>
 * </ul>
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

    // Energy buffer — AE power charged passively from AE2 network via Energy Input Hatch
    private long energyBuffer = 0;
    private long energyCapacity = 0; // Set from recipe's energy cost when recipe is selected

    // Thermal system
    private int heatLevel = 0;        // 0-1000 (displayed as 0.0% - 100.0%)
    private static final int MAX_HEAT = 1000;
    private boolean safeMode = true;  // Default ON: auto-shutdown at 100% heat
    private int cooldownTimer = 0;    // Ticks remaining for 30-min cooldown after overheat
    private static final int COOLDOWN_DURATION = 36000; // 30 minutes = 36000 ticks

    // Safe mode penalty — consumes 2.5x more resources
    private static final double SAFE_MODE_MULTIPLIER = 2.5;

    // Catastrophic explosion system
    private boolean exploding = false;
    private int explosionTick = 0;
    private static final int EXPLOSION_RADIUS = 50;
    private static final int EXPLOSION_DURATION_TICKS = 60; // Spread across 3 seconds

    // Cached field tier
    private int fieldLevel = 0;

    // The multiblock pattern will be initialized lazily
    private static MultiblockPattern PATTERN;

    // Energy charge rates per Field Generator tier (AE per tick)
    private static final long[] ENERGY_RATE_BY_TIER = {0, 500_000, 1_000_000, 2_000_000};

    public StellarNexusControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // ContainerData layout:
        // 0 = progress
        // 1 = maxProgress
        // 2 = assembled (0 or 1)
        // 3 = fieldLevel (1-3)
        // 4 = energyPercent (0-100)
        // 5 = running (0 or 1)
        // 6 = heatPercent (0-1000, displayed as 0.0%-100.0%)
        // 7 = safeMode (0 or 1)
        // 8 = cooldownTimer (ticks remaining)
        // 9-12 = energyBuffer as 4 shorts
        // 13-16 = energyCapacity as 4 shorts
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> StellarNexusControllerBE.this.progress;
                    case 1 -> StellarNexusControllerBE.this.maxProgress;
                    case 2 -> StellarNexusControllerBE.this.assembled ? 1 : 0;
                    case 3 -> StellarNexusControllerBE.this.fieldLevel;
                    case 4 -> StellarNexusControllerBE.this.energyCapacity > 0
                            ? (int) (StellarNexusControllerBE.this.energyBuffer * 100 / StellarNexusControllerBE.this.energyCapacity)
                            : 0;
                    case 5 -> StellarNexusControllerBE.this.running ? 1 : 0;
                    case 6 -> StellarNexusControllerBE.this.heatLevel;
                    case 7 -> StellarNexusControllerBE.this.safeMode ? 1 : 0;
                    case 8 -> StellarNexusControllerBE.this.cooldownTimer;
                    case 9 -> (int) (StellarNexusControllerBE.this.energyBuffer & 0xFFFF);
                    case 10 -> (int) ((StellarNexusControllerBE.this.energyBuffer >> 16) & 0xFFFF);
                    case 11 -> (int) ((StellarNexusControllerBE.this.energyBuffer >> 32) & 0xFFFF);
                    case 12 -> (int) ((StellarNexusControllerBE.this.energyBuffer >> 48) & 0xFFFF);
                    case 13 -> (int) (StellarNexusControllerBE.this.energyCapacity & 0xFFFF);
                    case 14 -> (int) ((StellarNexusControllerBE.this.energyCapacity >> 16) & 0xFFFF);
                    case 15 -> (int) ((StellarNexusControllerBE.this.energyCapacity >> 32) & 0xFFFF);
                    case 16 -> (int) ((StellarNexusControllerBE.this.energyCapacity >> 48) & 0xFFFF);
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
                return 17;
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
        if (currentState.hasProperty(net.minecraft.world.level.block.DirectionalBlock.FACING)) {
            facing = currentState.getValue(net.minecraft.world.level.block.DirectionalBlock.FACING);
        }
        MultiblockPattern.MatchResult result = pattern.match(level, this.worldPosition, facing);

        boolean wasAssembled = this.assembled;
        this.assembled = result.isValid();

        // Global Hatch Validation — no longer requires Fuel Hatch
        if (this.assembled) {
            int itemOutputs = 0;
            int fluidOutputs = 0;
            int itemInputs = 0;
            int energyInputs = 0;

            for (BlockPos partPos : result.partPositions()) {
                Block block = level.getBlockState(partPos).getBlock();
                if (block == MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get()) itemOutputs++;
                else if (block == MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get()) fluidOutputs++;
                else if (block == MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get()) itemInputs++;
                else if (block == MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get()) energyInputs++;
            }

            // Require exactly 1 of each hatch (no fuel hatch anymore)
            if (itemOutputs != 1 || fluidOutputs != 1 || itemInputs != 1 || energyInputs != 1) {
                this.assembled = false;
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

    public void serverTick() {
        if (this.level == null || this.level.isClientSide()) return;

        // Handle ongoing catastrophic explosion (spread across ticks)
        if (this.exploding) {
            processExplosionTick();
            return;
        }

        // Only re-scan when dirty, throttled to every 20 ticks
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
                this.heatLevel = 0;
            }
            this.setChanged();
            return;
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
     * Validates all requirements for starting a simulation and returns a list
     * of error messages. An empty list means the simulation can start.
     */
    public List<Component> getStartErrors() {
        List<Component> errors = new ArrayList<>();

        if (!this.assembled) {
            errors.add(Component.literal("§c✗ Structure not assembled"));
        }
        if (this.running) {
            errors.add(Component.literal("§c✗ Already in operation"));
        }
        if (this.cooldownTimer > 0) {
            int secLeft = this.cooldownTimer / 20;
            errors.add(Component.literal("§c✗ Cooling down: " + secLeft + "s remaining"));
        }
        if (this.activeRecipeId == null) {
            errors.add(Component.literal("§c✗ No simulation program selected"));
            return errors;
        }

        if (this.level == null) return errors;

        var recipeOpt = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof StellarSimulationRecipe recipe)) {
            errors.add(Component.literal("§c✗ Invalid simulation program"));
            return errors;
        }

        // Field tier check
        if (this.fieldLevel < recipe.getFieldTier()) {
            errors.add(Component.literal("§c✗ Field Generator too low: Mk." + toRoman(this.fieldLevel) + " (need Mk." + toRoman(recipe.getFieldTier()) + ")"));
        }

        // Compute effective costs (safe mode = 2.5x)
        double multiplier = this.safeMode ? SAFE_MODE_MULTIPLIER : 1.0;
        long effectiveEnergyCost = (long)(recipe.getEnergyCost() * multiplier);
        long effectiveFuelAmount = (long)(recipe.getFuelAmount() * multiplier);

        // Energy check
        if (this.energyBuffer < effectiveEnergyCost) {
            int pct = this.energyCapacity > 0 ? (int)(this.energyBuffer * 100 / this.energyCapacity) : 0;
            String safeNote = this.safeMode ? " §7(2.5x Safe Mode)" : "";
            errors.add(Component.literal("§c✗ Energy: " + formatAmount(this.energyBuffer) + " / " + formatAmount(effectiveEnergyCost) + " AE" + safeNote));
        }

        // Fuel liquid check (from ME storage)
        if (!recipe.getFuelFluid().isEmpty() && recipe.getFuelAmount() > 0) {
            AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
            if (nodeBE == null || nodeBE.getActionableNode() == null || nodeBE.getActionableNode().getGrid() == null) {
                errors.add(Component.literal("§c✗ No ME network connection"));
            } else {
                ResourceLocation fuelRL = ResourceLocation.parse(recipe.getFuelFluid());
                Fluid fuelFluid = BuiltInRegistries.FLUID.get(fuelRL);
                if (fuelFluid == null || fuelFluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                    errors.add(Component.literal("§c✗ Invalid fuel fluid type: " + fuelRL));
                } else {
                    AEFluidKey fuelKey = AEFluidKey.of(fuelFluid);
                    MEStorage storage = nodeBE.getActionableNode().getGrid().getStorageService().getInventory();
                    IActionSource src = IActionSource.ofMachine(nodeBE);
                    long available = storage.extract(fuelKey, effectiveFuelAmount, Actionable.SIMULATE, src);
                    if (available < effectiveFuelAmount) {
                        String fluidName = formatFluidName(fuelRL.getPath());
                        String safeNote = this.safeMode ? " §7(2.5x Safe Mode)" : "";
                        errors.add(Component.literal("§c✗ Fuel: " + formatAmount(available) + " / " + formatAmount(effectiveFuelAmount) + " mB §f" + fluidName + safeNote));
                    }
                }
            }
        }

        // Item/Fluid inputs check — show specific names
        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE != null && nodeBE.getActionableNode() != null && nodeBE.getActionableNode().getGrid() != null) {
            MEStorage storage = nodeBE.getActionableNode().getGrid().getStorageService().getInventory();
            IActionSource src = IActionSource.ofMachine(nodeBE);

            for (var req : recipe.getItemInputs()) {
                if (!req.isEmpty()) {
                    long available = simulateExtractItem(req, storage, src);
                    if (available < req.getAmount()) {
                        // Get the display name of the first matching item
                        String itemName = "Unknown Item";
                        ItemStack[] matches = req.getIngredient().getItems();
                        if (matches.length > 0) {
                            itemName = matches[0].getHoverName().getString();
                        }
                        errors.add(Component.literal("§c✗ Missing: " + formatAmount(available) + " / " + formatAmount(req.getAmount()) + "x §f" + itemName));
                    }
                }
            }
            for (var req : recipe.getFluidInputs()) {
                if (!req.isEmpty()) {
                    long available = simulateExtractFluid(req, storage, src);
                    if (available < req.getAmount()) {
                        // Get the fluid name
                        String fluidName = "Unknown Fluid";
                        var fluidStacks = req.getIngredient().getStacks();
                        if (fluidStacks.length > 0) {
                            ResourceLocation fluidRL = BuiltInRegistries.FLUID.getKey(fluidStacks[0].getFluid());
                            fluidName = formatFluidName(fluidRL.getPath());
                        }
                        errors.add(Component.literal("§c✗ Missing: " + formatAmount(available) + " / " + formatAmount(req.getAmount()) + " mB §f" + fluidName));
                    }
                }
            }
        }

        return errors;
    }

    /**
     * Called from the network packet when the player clicks "Start Operation".
     * Returns a list of error messages (empty = success).
     */
    public List<Component> startOperation() {
        List<Component> errors = getStartErrors();
        if (!errors.isEmpty()) return errors;

        if (this.level == null || this.level.isClientSide()) return List.of(Component.literal("§c✗ Internal error"));

        var recipeOpt = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof StellarSimulationRecipe recipe)) {
            return List.of(Component.literal("§c✗ Invalid recipe"));
        }

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null || nodeBE.getActionableNode() == null) return List.of(Component.literal("§c✗ No network"));
        IGridNode node = nodeBE.getActionableNode();
        if (node.getGrid() == null) return List.of(Component.literal("§c✗ No grid"));

        IGrid grid = node.getGrid();
        IActionSource src = IActionSource.ofMachine(nodeBE);
        MEStorage storage = grid.getStorageService().getInventory();

        // Compute effective costs (safe mode = 2.5x)
        double multiplier = this.safeMode ? SAFE_MODE_MULTIPLIER : 1.0;
        long effectiveEnergyCost = (long)(recipe.getEnergyCost() * multiplier);
        long effectiveFuelAmount = (long)(recipe.getFuelAmount() * multiplier);

        // Extract fuel liquid from ME storage (if required)
        if (!recipe.getFuelFluid().isEmpty() && recipe.getFuelAmount() > 0) {
            ResourceLocation fuelRL = ResourceLocation.parse(recipe.getFuelFluid());
            Fluid fuelFluid = BuiltInRegistries.FLUID.get(fuelRL);
            if (fuelFluid == null || fuelFluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                return List.of(Component.literal("§c✗ Invalid fuel fluid type: " + fuelRL));
            }
            AEFluidKey fuelKey = AEFluidKey.of(fuelFluid);
            long extracted = storage.extract(fuelKey, effectiveFuelAmount, Actionable.MODULATE, src);
            if (extracted < effectiveFuelAmount) {
                if (extracted > 0) {
                    storage.insert(fuelKey, extracted, Actionable.MODULATE, src);
                }
                String fluidName = formatFluidName(fuelRL.getPath());
                return List.of(Component.literal("§c✗ Failed to extract fuel: " + fluidName));
            }
        }

        // Extract item/fluid inputs
        if (!extractInputs(recipe, storage, src)) {
            return List.of(Component.literal("§c✗ Failed to extract inputs"));
        }

        // Consume AE energy (with safe mode multiplier)
        this.energyBuffer -= effectiveEnergyCost;
        this.maxProgress = recipe.getTime();
        this.progress = 0;
        this.running = true;
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
        return List.of(); // Success
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

        // Cache the time and energy capacity for UI
        this.maxProgress = recipe.getTime();
        this.energyCapacity = recipe.getEnergyCost();

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();

        // ── Passive AE energy charging (when NOT running) ──
        if (!this.running) {
            if (nodeBE != null && nodeBE.getActionableNode() != null) {
                IGridNode node = nodeBE.getActionableNode();
                if (node.getGrid() != null) {
                    IEnergyService energy = node.getGrid().getEnergyService();
                    long chargeRate = this.fieldLevel >= 1 && this.fieldLevel <= 3
                            ? ENERGY_RATE_BY_TIER[this.fieldLevel] : 0;
                    long spaceLeft = this.energyCapacity - this.energyBuffer;
                    long toCharge = Math.min(chargeRate, spaceLeft);
                    if (toCharge > 0) {
                        double extracted = energy.extractAEPower(toCharge, Actionable.MODULATE, PowerMultiplier.CONFIG);
                        this.energyBuffer += (long) extracted;
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

        // Heat generation — increases based on recipe cooling requirement
        int heatPerTick = recipe.getCoolingLevel() + 1;
        this.heatLevel = Math.min(MAX_HEAT, this.heatLevel + heatPerTick);

        // Coolant consumption from ME network — reduces heat (2.5x in safe mode)
        int coolingApplied = consumeCoolant(recipe, grid);
        this.heatLevel = Math.max(0, this.heatLevel - coolingApplied);

        // Overheat check
        if (this.heatLevel >= MAX_HEAT) {
            if (this.safeMode) {
                this.running = false;
                this.progress = 0;
                this.cooldownTimer = COOLDOWN_DURATION;
                this.setChanged();
                if (this.level != null) {
                    BlockPos pos = this.worldPosition;
                    this.level.players().forEach(p -> p.displayClientMessage(
                            Component.literal("§c§l[STELLAR NEXUS] §eSafe Mode activated at " + pos.toShortString() + " — 30 minute cooldown initiated."),
                            false));
                    
                    BlockState state = this.level.getBlockState(this.worldPosition);
                    this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
                }
                return;
            } else {
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
            
            if (this.level != null) {
                BlockState state = this.level.getBlockState(this.worldPosition);
                this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
            }
        }
        this.setChanged();
    }

    private void triggerStellarExplosion() {
        if (this.level == null) return;
        BlockPos pos = this.worldPosition;

        this.level.players().forEach(p -> p.displayClientMessage(
                Component.literal("§4§l[STELLAR NEXUS] §c§lCRITICAL THERMAL FAILURE at " + pos.toShortString() + "! CATASTROPHIC EXPLOSION!"),
                false));

        // Initial vanilla explosion at center
        this.level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                30.0f, Level.ExplosionInteraction.BLOCK);

        // Start the multi-tick catastrophic explosion
        this.exploding = true;
        this.explosionTick = 0;

        this.running = false;
        this.progress = 0;
        this.heatLevel = 0;
        this.energyBuffer = 0;
        this.assembled = false;

        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }

        onControllerBroken();
    }

    /**
     * Multi-tick catastrophic explosion processor.
     * Spreads lava in the inner core and fire in the outer ring.
     * Processes a shell of blocks each tick to avoid lag spikes.
     */
    private void processExplosionTick() {
        if (this.level == null) {
            this.exploding = false;
            return;
        }

        BlockPos center = this.worldPosition;
        this.explosionTick++;

        // Calculate which radial shell to process this tick
        int currentRadius = (int)((float) this.explosionTick / EXPLOSION_DURATION_TICKS * EXPLOSION_RADIUS);
        int prevRadius = (int)((float) (this.explosionTick - 1) / EXPLOSION_DURATION_TICKS * EXPLOSION_RADIUS);

        if (currentRadius > EXPLOSION_RADIUS) {
            this.exploding = false;
            this.explosionTick = 0;

            // Damage all nearby entities at the end
            var entities = this.level.getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    new net.minecraft.world.phys.AABB(
                            center.getX() - EXPLOSION_RADIUS, center.getY() - EXPLOSION_RADIUS, center.getZ() - EXPLOSION_RADIUS,
                            center.getX() + EXPLOSION_RADIUS, center.getY() + EXPLOSION_RADIUS, center.getZ() + EXPLOSION_RADIUS
                    ));
            for (var entity : entities) {
                double dist = entity.distanceToSqr(center.getX(), center.getY(), center.getZ());
                if (dist < EXPLOSION_RADIUS * EXPLOSION_RADIUS) {
                    float damage = (float)(200.0 * (1.0 - Math.sqrt(dist) / EXPLOSION_RADIUS));
                    entity.hurt(this.level.damageSources().explosion(null), damage);
                    entity.setRemainingFireTicks(600); // 30 seconds of fire
                }
            }
            return;
        }

        // Process blocks in the current shell (between prevRadius and currentRadius)
        int rMin = Math.max(prevRadius, 0);
        int rMax = currentRadius;

        // Inner zone (r < 20): replace with lava
        // Outer zone (20 <= r < EXPLOSION_RADIUS): place fire, destroy blocks
        int innerLavaRadius = 20;

        for (int rr = rMin; rr <= rMax; rr++) {
            // Sample points on the shell surface
            int pointsOnShell = Math.max(1, (int)(4 * Math.PI * rr * rr / 16)); // ~1 point per 4 blocks
            var random = this.level.getRandom();

            for (int p = 0; p < pointsOnShell && p < 2000; p++) {
                // Random point on sphere of radius rr
                double theta = random.nextDouble() * 2 * Math.PI;
                double phi = Math.acos(2 * random.nextDouble() - 1);
                int dx = (int)(rr * Math.sin(phi) * Math.cos(theta));
                int dy = (int)(rr * Math.cos(phi));
                int dz = (int)(rr * Math.sin(phi) * Math.sin(theta));

                BlockPos target = center.offset(dx, dy, dz);
                if (!this.level.isLoaded(target)) continue;

                BlockState targetState = this.level.getBlockState(target);
                if (targetState.isAir()) continue;

                // Don't destroy bedrock
                if (targetState.getDestroySpeed(this.level, target) < 0) continue;

                if (rr < innerLavaRadius) {
                    // Inner zone: replace with lava
                    this.level.setBlock(target, net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState(), Block.UPDATE_CLIENTS);
                } else {
                    // Outer zone: destroy block and place fire on top
                    this.level.setBlock(target, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    BlockPos above = target.above();
                    BlockState aboveState = this.level.getBlockState(above);
                    if (aboveState.isAir()) {
                        this.level.setBlock(above, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        // Periodic sub-explosions for visual/audio effect
        if (this.explosionTick % 5 == 0) {
            var random = this.level.getRandom();
            double ox = center.getX() + (random.nextDouble() - 0.5) * currentRadius;
            double oy = center.getY() + (random.nextDouble() - 0.5) * currentRadius * 0.5;
            double oz = center.getZ() + (random.nextDouble() - 0.5) * currentRadius;
            this.level.explode(null, ox, oy, oz, 8.0f, Level.ExplosionInteraction.BLOCK);
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

    // ──────────────────── Coolant System ────────────────────

    /** Amount of coolant fluid (mB) consumed per tick during active operation. */
    private static final long COOLANT_CONSUMPTION_PER_TICK = 100;

    /**
     * Tries to extract the recipe's specific coolant fluid from the ME network.
     * Returns the cooling power applied this tick (heat units to subtract).
     * <p>
     * If the recipe specifies a coolant fluid, only that fluid is used.
     * Otherwise, tries water as a generic coolant.
     * <p>
     * Coolant effectiveness per tier:
     * <ul>
     *   <li>Gelid Cryotheum (T1): 3 cooling/mB</li>
     *   <li>Stable Coolant (T2): 5 cooling/mB</li>
     *   <li>Temporal Fluid (T3): 8 cooling/mB</li>
     *   <li>Water (fallback): 1 cooling/mB</li>
     * </ul>
     */
    private int consumeCoolant(StellarSimulationRecipe recipe, IGrid grid) {
        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null) return 0;

        IActionSource src = IActionSource.ofMachine(nodeBE);
        MEStorage storage = grid.getStorageService().getInventory();

        // Determine which coolant to use
        AEFluidKey coolantKey;
        int coolingEfficiency;

        if (!recipe.getCoolantFluid().isEmpty()) {
            // Use the recipe's specified coolant
            ResourceLocation coolantRL = ResourceLocation.parse(recipe.getCoolantFluid());
            Fluid coolantFluid = BuiltInRegistries.FLUID.get(coolantRL);
            if (coolantFluid == null || coolantFluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                return 0; // Return 0 cooling if invalid fluid
            }
            coolantKey = AEFluidKey.of(coolantFluid);

            // Determine efficiency based on the coolant type
            String coolantPath = coolantRL.getPath();
            if (coolantPath.contains("temporal_fluid")) {
                coolingEfficiency = 8; // T3
            } else if (coolantPath.contains("stable_coolant")) {
                coolingEfficiency = 5; // T2
            } else if (coolantPath.contains("gelid_cryotheum")) {
                coolingEfficiency = 3; // T1
            } else {
                coolingEfficiency = 2; // generic
            }
        } else {
            // Fallback to water
            coolantKey = AEFluidKey.of(net.minecraft.world.level.material.Fluids.WATER);
            coolingEfficiency = 1;
        }

        // Safe mode consumes 2.5x more coolant per tick
        long effectiveCoolantPerTick = this.safeMode
                ? (long)(COOLANT_CONSUMPTION_PER_TICK * SAFE_MODE_MULTIPLIER)
                : COOLANT_CONSUMPTION_PER_TICK;

        long extracted = storage.extract(coolantKey, effectiveCoolantPerTick, Actionable.MODULATE, src);
        if (extracted > 0) {
            return (int) (extracted * coolingEfficiency / effectiveCoolantPerTick) * (coolingTierBonus() + 1);
        }

        return 0; // No coolant available — heat will continue to rise!
    }

    /**
     * Bonus cooling multiplier based on field generator tier.
     */
    private int coolingTierBonus() {
        return this.fieldLevel;
    }

    public ResourceLocation getActiveRecipeId() {
        return this.activeRecipeId;
    }

    public void setActiveRecipe(ResourceLocation activeRecipeId) {
        this.activeRecipeId = activeRecipeId;
        this.progress = 0;
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
    }

    // ──────────────────── Utility ────────────────────

    private static String formatAmount(long amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000.0);
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000.0);
        return String.valueOf(amount);
    }

    private static String toRoman(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(tier);
        };
    }

    /**
     * Converts a fluid registry path to a human-readable name.
     * e.g., "source_gelid_cryotheum" → "Gelid Cryotheum"
     */
    private static String formatFluidName(String path) {
        if (path.startsWith("source_")) path = path.substring(7);
        if (path.startsWith("flowing_")) path = path.substring(8);
        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    // ──────────────────── Pattern Definition ────────────────────

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
        tag.putLong("energyBuffer", this.energyBuffer);
        tag.putLong("energyCapacity", this.energyCapacity);

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

        // Backward compat: read old "fuelBuffer"/"fuelCapacity" tags as energy
        if (tag.contains("energyBuffer")) {
            this.energyBuffer = tag.getLong("energyBuffer");
        } else if (tag.contains("fuelBuffer")) {
            this.energyBuffer = tag.getLong("fuelBuffer");
        }
        if (tag.contains("energyCapacity")) {
            this.energyCapacity = tag.getLong("energyCapacity");
        } else if (tag.contains("fuelCapacity")) {
            this.energyCapacity = tag.getLong("fuelCapacity");
        }

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
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}

package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.Ae2NodeAvailability;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufocore.api.port.EnergyInputPort;
import com.raishxn.ufocore.api.port.EnergyPortGroup;
import com.raishxn.ufocore.api.port.FluidInputPort;
import com.raishxn.ufocore.api.port.FluidPortGroup;
import com.raishxn.ufocore.api.port.ItemPort;
import com.raishxn.ufocore.api.port.ItemPortGroup;
import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.pattern.StellarNexusPatternFactory;
import com.raishxn.ufo.block.StellarNexusControllerBlock;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.diagnostic.MachineMetricKey;
import com.raishxn.ufo.diagnostic.MachinePerformanceRegistry;
import com.raishxn.ufo.block.entity.processing.PendingOutputBuffer;
import com.raishxn.ufo.block.entity.processing.StellarCoolantMath;
import com.raishxn.ufo.block.entity.processing.StellarEnergyMath;
import com.raishxn.ufo.block.entity.processing.StellarExplosionPolicy;
import com.raishxn.ufo.block.entity.processing.ThermalSystem;
import com.raishxn.ufo.block.entity.processing.TransactionalAmountLedger;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;
import net.pedroksl.ae2addonlib.recipes.IngredientStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
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
import net.minecraft.world.Containers;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

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
import com.raishxn.ufo.block.MultiblockBlocks;

public class StellarNexusControllerBE extends BlockEntity implements IMultiblockController, MenuProvider {

    private boolean assembled = false;
    private boolean structureDirty = true;
    private int scanCooldown = 0;
    private final List<BlockPos> parts = new ArrayList<>();
    private List<BlockPos> networkNodeCandidates = List.of();
    private MachineMetricKey performanceMetricKey;

    // Processing state
    private ResourceLocation activeRecipeId = null;
    private int progress = 0;
    private int maxProgress = 0; // Cached total time
    private boolean running = false;
    /** A player-requested pause preserves the paid inputs and current progress. */
    private boolean paused = false;
    private final PendingOutputBuffer<AEKey> pendingOutputs = new PendingOutputBuffer<>();
    private final ContainerData data;
    private static final int DISPLAYED_REQUIREMENT_ROWS = 7;
    private static final int REQUIREMENT_DATA_START = 24;
    private static final int REQUIREMENT_DATA_PARTS = 4;
    private static final int CONTAINER_DATA_COUNT = REQUIREMENT_DATA_START
            + DISPLAYED_REQUIREMENT_ROWS * REQUIREMENT_DATA_PARTS;
    private final long[] displayedRequirementAvailability = new long[DISPLAYED_REQUIREMENT_ROWS];
    private long requirementAvailabilityRefreshBucket = Long.MIN_VALUE;

    // Energy buffer — AE power charged passively from AE2 network via Energy Input
    // Hatch
    private long energyBuffer = 0;
    private static final long GLOBAL_ENERGY_CAPACITY = 200_000_000_000L; // 200 Billion AE global buffer
    private long energyCapacity = GLOBAL_ENERGY_CAPACITY;
    private EnergyPortGroup energyPorts = EnergyPortGroup.empty();
    private FluidPortGroup<AEFluidKey> coolantPorts = FluidPortGroup.empty();
    private List<MassiveOutputHatchBE> coolantHatches = List.of();
    private ItemPortGroup<AEItemKey> itemInputPorts = ItemPortGroup.empty();
    private ItemPortGroup<AEItemKey> itemOutputPorts = ItemPortGroup.empty();
    private long lastEnergyRequested = 0L;
    private long lastEnergyAccepted = 0L;
    private long lastCoolantRequested = 0L;
    private long lastCoolantAccepted = 0L;

    // Thermal system
    private int heatLevel = 0; // 0-1000 (displayed as 0.0% - 100.0%)
    private static final int MAX_HEAT = 1000;
    private boolean safeMode = true; // Default ON: auto-shutdown at 100% heat
    private int cooldownTimer = 0; // Ticks remaining for 30-min cooldown after overheat
    private static final int COOLDOWN_DURATION = 36000; // 30 minutes = 36000 ticks
    private static final int COOLDOWN_SAVE_INTERVAL = 20;

    // Safe mode penalty — consumes 2x more resources
    private static final double SAFE_MODE_MULTIPLIER = 2.0;
    // UI Toggles
    private boolean autoStart = false;
    private boolean simulationLocked = false;
    private boolean isOverclocked = false;

    // Catastrophic explosion system
    private boolean exploding = false;
    private int explosionTick = 0;
    private int explosionRadius = 50;
    private int explosionShellRadius = 0;
    private int explosionCursorX = 0;
    private int explosionCursorY = 0;
    private int explosionCursorZ = 0;
    private int explosionBlockChanges = 0;
    private StellarExplosionPolicy explosionPolicy = StellarExplosionPolicy.resolve(
            false, false, 1, 1, 1, 1, 100_000L, false, false);

    // Cached field tier
    private int fieldLevel = 0;
    private CoolantDefinition[] coolantDefinitions;

    // The multiblock pattern will be initialized lazily
    private static MultiblockPattern PATTERN;

    // Energy charge rates per Field Generator tier (AE per tick)
    private static final long[] ENERGY_RATE_BY_TIER = { 0, 500_000, 1_000_000, 2_000_000 };

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
        // 20 = paused (0 or 1)
        // 21 = buffered coolant kind (0 empty, 1 gelid, 2 stable, 3 temporal)
        // 22-23 = buffered coolant amount as 2 unsigned shorts
        // 24-51 = available ME amount for 7 displayed requirements, 4 shorts each
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> StellarNexusControllerBE.this.progress;
                    case 1 -> StellarNexusControllerBE.this.maxProgress;
                    case 2 -> StellarNexusControllerBE.this.assembled ? 1 : 0;
                    case 3 -> StellarNexusControllerBE.this.fieldLevel;
                    case 4 -> StellarNexusControllerBE.this.energyCapacity > 0
                            ? (int) (StellarNexusControllerBE.this.energyBuffer * 100
                                    / StellarNexusControllerBE.this.energyCapacity)
                            : 0;
                    case 5 -> StellarNexusControllerBE.this.running ? 1 : 0;
                    case 6 -> StellarNexusControllerBE.this.heatLevel;
                    case 7 -> StellarNexusControllerBE.this.safeMode ? 1 : 0;
                    case 8 -> StellarNexusControllerBE.this.cooldownTimer;
                    case 9 -> (int) (StellarNexusControllerBE.this.energyBuffer & 0xFFFF);
                    case 10 -> (int) ((StellarNexusControllerBE.this.energyBuffer >> 16) & 0xFFFF);
                    case 11 -> (int) ((StellarNexusControllerBE.this.energyBuffer >> 32) & 0xFFFF);
                    case 12 -> (int) ((StellarNexusControllerBE.this.energyBuffer >> 48) & 0xFFFF);
                    case 13 -> (int) (GLOBAL_ENERGY_CAPACITY & 0xFFFF);
                    case 14 -> (int) ((GLOBAL_ENERGY_CAPACITY >> 16) & 0xFFFF);
                    case 15 -> (int) ((GLOBAL_ENERGY_CAPACITY >> 32) & 0xFFFF);
                    case 16 -> (int) ((GLOBAL_ENERGY_CAPACITY >> 48) & 0xFFFF);
                    case 17 -> StellarNexusControllerBE.this.autoStart ? 1 : 0;
                    case 18 -> StellarNexusControllerBE.this.simulationLocked ? 1 : 0;
                    case 19 -> StellarNexusControllerBE.this.isOverclocked ? 1 : 0;
                    case 20 -> StellarNexusControllerBE.this.paused ? 1 : 0;
                    case 21 -> StellarNexusControllerBE.this.getDisplayedCoolantKind();
                    case 22 -> StellarNexusControllerBE.this.getDisplayedCoolantAmount() & 0xFFFF;
                    case 23 -> (StellarNexusControllerBE.this.getDisplayedCoolantAmount() >> 16) & 0xFFFF;
                    default -> {
                        int relativeIndex = pIndex - REQUIREMENT_DATA_START;
                        if (relativeIndex >= 0
                                && relativeIndex < DISPLAYED_REQUIREMENT_ROWS * REQUIREMENT_DATA_PARTS) {
                            StellarNexusControllerBE.this.ensureDisplayedRequirementAvailabilityFresh();
                            int row = relativeIndex / REQUIREMENT_DATA_PARTS;
                            int part = relativeIndex % REQUIREMENT_DATA_PARTS;
                            yield (int) ((StellarNexusControllerBE.this.displayedRequirementAvailability[row]
                                    >>> (part * 16)) & 0xFFFFL);
                        }
                        yield 0;
                    }
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
                    case 17 -> StellarNexusControllerBE.this.autoStart = pValue == 1;
                    case 18 -> StellarNexusControllerBE.this.simulationLocked = pValue == 1;
                    case 19 -> StellarNexusControllerBE.this.isOverclocked = pValue == 1;
                    case 20 -> StellarNexusControllerBE.this.paused = pValue == 1;
                }
            }

            @Override
            public int getCount() {
                return CONTAINER_DATA_COUNT;
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
        scanStructure(level, null);
    }

    public void scanStructure(Level level, @Nullable Player player) {
        MultiblockPattern pattern = getPattern();
        long startedAt = System.nanoTime();
        try {
        BlockState currentState = level.getBlockState(this.worldPosition);
        net.minecraft.core.Direction facing = net.minecraft.core.Direction.NORTH;
        if (currentState.hasProperty(net.minecraft.world.level.block.DirectionalBlock.FACING)) {
            facing = currentState.getValue(net.minecraft.world.level.block.DirectionalBlock.FACING);
        }
        MultiblockPattern.MatchResult result = pattern.match(level, this.worldPosition, facing);
        if (player != null && !result.isValid()) {
            for (var error : result.allErrors().stream().limit(10).toList()) {
                BlockPos missing = error.pos();
                player.displayClientMessage(Component.literal("[" + missing.getX() + ", " + missing.getY()
                        + ", " + missing.getZ() + "] Expected: ").append(error.expected()), false);
            }
            if (result.allErrors().size() > 10) player.displayClientMessage(
                    Component.literal("... and " + (result.allErrors().size() - 10) + " more blocks."), false);
        }
        List<BlockPos> expectedE = pattern.getExpectedPositions(this.worldPosition, facing, 'E');
        boolean hasUnloadedFieldPositions = false;
        int tier1 = 0, tier2 = 0, tier3 = 0;
        for (BlockPos ePos : expectedE) {
            if (!level.isLoaded(ePos)) {
                hasUnloadedFieldPositions = true;
                continue;
            }

            Block block = level.getBlockState(ePos).getBlock();
            if (block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get()) {
                tier1++;
            } else if (block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get()) {
                tier2++;
            } else if (block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get()) {
                tier3++;
            }
        }

        boolean waitingForChunks = result.hasUnloadedPositions() || hasUnloadedFieldPositions;
        if (waitingForChunks && player == null) {
            this.scanCooldown = 0;
            this.structureDirty = true;
            this.energyPorts = EnergyPortGroup.empty();
            this.coolantPorts = FluidPortGroup.empty();
            this.itemInputPorts = ItemPortGroup.empty();
            this.itemOutputPorts = ItemPortGroup.empty();
            this.networkNodeCandidates = List.of();
            return;
        }

        boolean wasAssembled = this.assembled;
        this.structureDirty = false;
        this.scanCooldown = 0;
        this.assembled = result.isValid();

        // Global Hatch Validation — no longer requires Fuel Hatch
        if (this.assembled) {
            int itemOutputs = 0;
            int fluidOutputs = 0;
            int itemInputs = 0;
            int energyInputs = 0;

            for (BlockPos partPos : result.partPositions()) {
                Block block = level.getBlockState(partPos).getBlock();
                if (block == MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get())
                    itemOutputs++;
                else if (block == MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get())
                    fluidOutputs++;
                else if (block == MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get())
                    itemInputs++;
                else if (block == MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get())
                    energyInputs++;
            }

            // Require at least one of each service hatch; locations are interchangeable.
            if (itemOutputs < 1 || fluidOutputs < 1 || itemInputs < 1 || energyInputs < 1) {
                this.assembled = false;
                if (player != null) {
                    player.displayClientMessage(Component.literal("[Stellar Nexus] Hatches (minimum 1 each): item input="
                            + itemInputs + ", item output=" + itemOutputs + ", coolant=" + fluidOutputs
                            + ", energy=" + energyInputs), false);
                }
            }
        }

        int totalFound = tier1 + tier2 + tier3;
        int targetFields = expectedE.size();

        // Field Tier Validation
        if (this.assembled) {
            int typesCount = (tier1 > 0 ? 1 : 0) + (tier2 > 0 ? 1 : 0) + (tier3 > 0 ? 1 : 0);

            if (typesCount > 1 || totalFound < targetFields) {
                // Mixed tiers or missing fields. Structure is invalid.
                this.assembled = false;
                this.fieldLevel = 0;
            } else if (typesCount == 1) {
                // Structure is homogeneous and valid
                if (tier3 > 0) this.fieldLevel = 3;
                else if (tier2 > 0) this.fieldLevel = 2;
                else this.fieldLevel = 1;
            } else {
                this.fieldLevel = 0;
            }
        } else {
            this.fieldLevel = 0;
        }

        // --- Player Feedback Logic ---
        if (player != null && waitingForChunks) {
            player.displayClientMessage(Component.literal("§e§l[STELLAR NEXUS] §7Ainda aguardando chunks da estrutura carregarem. Tente novamente em alguns segundos."), false);
        } else if (player != null && !this.assembled) {
            if (targetFields > 0 && (totalFound < targetFields || (tier1 > 0 && tier2 > 0) || (tier2 > 0 && tier3 > 0) || (tier1 > 0 && tier3 > 0))) {
                player.displayClientMessage(Component.literal("§c§l[STELLAR NEXUS] §eIncomplete or Mixed Field Generators detected:"), false);
                
                if (tier1 > 0 || totalFound == 0) {
                    player.displayClientMessage(Component.literal("  §7- Missing §c" + (targetFields - tier1) + "§7 blocks for §fTier 1§7 equivalence"), false);
                }
                if (tier2 > 0) {
                    player.displayClientMessage(Component.literal("  §7- Missing §c" + (targetFields - tier2) + "§7 blocks for §bTier 2§7 equivalence"), false);
                }
                if (tier3 > 0) {
                    player.displayClientMessage(Component.literal("  §7- Missing §c" + (targetFields - tier3) + "§7 blocks for §dTier 3§7 equivalence"), false);
                }
            } else {
                // Fields are fine, something else failed
                if (totalFound == targetFields) {
                     player.displayClientMessage(Component.literal("§c§l[STELLAR NEXUS] §eStructure incomplete. Check casings, condensation matrix, or hatches."), false);
                } else {
                     player.displayClientMessage(Component.literal("§c§l[STELLAR NEXUS] §eStructure match failed."), false);
                }
            }
        }

        // Update tracked parts
        this.parts.clear();
        if (this.assembled) {
            this.parts.addAll(result.partPositions());
            List<EnergyInputPort> detectedEnergyPorts = new ArrayList<>();
            List<FluidInputPort<AEFluidKey>> detectedCoolantPorts = new ArrayList<>();
            List<MassiveOutputHatchBE> detectedCoolantHatches = new ArrayList<>();
            List<ItemPort<AEItemKey>> detectedItemInputPorts = new ArrayList<>();
            List<ItemPort<AEItemKey>> detectedItemOutputPorts = new ArrayList<>();
            BlockPos itemInputNode = null;
            BlockPos fluidNode = null;
            BlockPos energyNode = null;
            BlockPos outputNode = null;

            // Link all parts to this controller
            for (BlockPos partPos : this.parts) {
                BlockState partState = level.getBlockState(partPos);
                if (level.getBlockEntity(partPos) instanceof IMultiblockPart part) {
                    part.linkToController(this.worldPosition);
                    if (part instanceof MassiveOutputHatchBE hatch) {
                        hatch.refreshGridConnection();
                    }
                }
                if (partState.is(MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get())
                        && level.getBlockEntity(partPos) instanceof EnergyInputPort energyPort) {
                    detectedEnergyPorts.add(energyPort);
                }
                if (partState.is(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get())
                    && level.getBlockEntity(partPos) instanceof MassiveOutputHatchBE fluidPort
                    && fluidPort.supportsFluidInput()) {
                    detectedCoolantPorts.add(fluidPort);
                    detectedCoolantHatches.add(fluidPort);
                }
                if (level.getBlockEntity(partPos) instanceof MassiveOutputHatchBE itemPort) {
                    if (partState.is(MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get())
                            && itemPort.supportsItemInput()) {
                        detectedItemInputPorts.add(itemPort);
                    } else if (partState.is(MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get())
                            && itemPort.supportsItemOutput()) {
                        detectedItemOutputPorts.add(itemPort);
                    }
                }
                if (partState.is(MultiblockBlocks.ME_MASSIVE_INPUT_HATCH.get())) {
                    itemInputNode = partPos.immutable();
                } else if (partState.is(MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get())) {
                    fluidNode = partPos.immutable();
                } else if (partState.is(MultiblockBlocks.AE_ENERGY_INPUT_HATCH.get())) {
                    energyNode = partPos.immutable();
                } else if (partState.is(MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get())) {
                    outputNode = partPos.immutable();
                }
            }
            this.energyPorts = new EnergyPortGroup(detectedEnergyPorts);
            this.coolantPorts = new FluidPortGroup<>(detectedCoolantPorts);
            this.coolantHatches = List.copyOf(detectedCoolantHatches);
            this.itemInputPorts = new ItemPortGroup<>(detectedItemInputPorts);
            this.itemOutputPorts = new ItemPortGroup<>(detectedItemOutputPorts);
            List<BlockPos> candidates = new ArrayList<>(4);
            addCandidate(candidates, itemInputNode);
            addCandidate(candidates, fluidNode);
            addCandidate(candidates, energyNode);
            addCandidate(candidates, outputNode);
            this.networkNodeCandidates = List.copyOf(candidates);
        } else {
            this.energyPorts = EnergyPortGroup.empty();
            this.coolantPorts = FluidPortGroup.empty();
            this.coolantHatches = List.of();
            this.itemInputPorts = ItemPortGroup.empty();
            this.itemOutputPorts = ItemPortGroup.empty();
            this.networkNodeCandidates = List.of();
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
        } finally {
            MachinePerformanceRegistry.INSTANCE.recordScan(
                    performanceMetricKey(), System.nanoTime() - startedAt,
                    pattern.getTestedPositionCount(), level.getGameTime());
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
        this.energyPorts = EnergyPortGroup.empty();
        this.coolantPorts = FluidPortGroup.empty();
        this.coolantHatches = List.of();
        this.itemInputPorts = ItemPortGroup.empty();
        this.itemOutputPorts = ItemPortGroup.empty();
        this.networkNodeCandidates = List.of();
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
        if (this.level == null || this.level.isClientSide())
            return;

        long startedAt = System.nanoTime();
        try {

        // Handle ongoing catastrophic explosion (spread across ticks)
        if (this.exploding) {
            processExplosionTick();
            return;
        }

        // Only re-scan when dirty, throttled to every 20 ticks
        if (this.structureDirty) {
            this.scanCooldown++;
            if (this.scanCooldown >= 20) {
                scanStructure(this.level);
            }
        }

        // Cached field level is now updated inside scanStructure()



        // Handle cooldown after overheat
        if (this.cooldownTimer > 0) {
            this.cooldownTimer--;
            if (this.cooldownTimer == 0) {
                this.heatLevel = 0;
                this.setChanged();
            } else if (this.cooldownTimer % COOLDOWN_SAVE_INTERVAL == 0) {
                this.setChanged();
            }
            return;
        }

        if ((!this.running || this.paused) && this.heatLevel > 0 && this.level.getGameTime() % 20L == 0L) {
            int passiveRate = UFOConfig.STELLAR_PASSIVE_DISSIPATION_PER_SECOND.get();
            int remainingHeat = (int) ThermalSystem.dissipate(this.heatLevel, passiveRate);
            if (remainingHeat != this.heatLevel) {
                this.heatLevel = remainingHeat;
                this.setChanged();
            }
        }

        if (this.assembled) {
            processMachineTick();

            // Auto-Restart logic
            if (!this.running && !this.pendingOutputs.hasPending()
                    && this.activeRecipeId != null && this.cooldownTimer == 0) {
                if (this.autoStart) {
                    startOperation();
                }
            }
        } else {
            this.progress = 0;
            this.running = false;
            this.paused = false;
        }
        } finally {
            MachinePerformanceRegistry.INSTANCE.recordTick(
                    performanceMetricKey(), System.nanoTime() - startedAt, this.level.getGameTime());
        }
    }

    private MachineMetricKey performanceMetricKey() {
        if (this.performanceMetricKey == null && this.level != null) {
            this.performanceMetricKey = new MachineMetricKey(
                    this.level.dimension().location().toString(),
                    this.worldPosition.asLong(),
                    this.getClass().getSimpleName());
        }
        return this.performanceMetricKey;
    }

    private long instrumentedExtract(MEStorage storage, appeng.api.stacks.AEKey key, long amount,
                                     Actionable mode, IActionSource source) {
        MachinePerformanceRegistry.INSTANCE.recordStorageOperation(performanceMetricKey(), this.level.getGameTime());
        return storage.extract(key, amount, mode, source);
    }

    private long instrumentedInsert(MEStorage storage, appeng.api.stacks.AEKey key, long amount,
                                    IActionSource source) {
        MachinePerformanceRegistry.INSTANCE.recordStorageOperation(performanceMetricKey(), this.level.getGameTime());
        return storage.insert(key, amount, Actionable.MODULATE, source);
    }

    // ──────────────────── Start Operation ────────────────────

    public boolean isActive() {
        return this.running;
    }

    public boolean isPaused() {
        return this.paused;
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
        if (this.pendingOutputs.hasPending()) {
            errors.add(Component.literal("§c✗ Output storage blocked"));
        }
        if (this.cooldownTimer > 0) {
            int secLeft = this.cooldownTimer / 20;
            errors.add(Component.literal("§c✗ Cooling down: " + secLeft + "s remaining"));
        }
        if (this.activeRecipeId == null) {
            errors.add(Component.literal("§c✗ No simulation program selected"));
            return errors;
        }

        if (this.level == null)
            return errors;

        var recipeOpt = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof StellarSimulationRecipe recipe)) {
            errors.add(Component.literal("§c✗ Invalid simulation program"));
            return errors;
        }

        // Field tier check
        if (this.fieldLevel < recipe.getFieldTier()) {
            errors.add(Component.literal("§c✗ Field Generator too low: Mk." + toRoman(this.fieldLevel) + " (need Mk."
                    + toRoman(recipe.getFieldTier()) + ")"));
        }

        // Compute effective costs (safe mode = 2x, overclock = 8x)
        long effectiveEnergyCost = StellarEnergyMath.effectiveCost(
                recipe.getEnergyCost(), this.safeMode, this.isOverclocked);

        double fuelMultiplier = this.safeMode ? SAFE_MODE_MULTIPLIER : 1.0;
        if (this.isOverclocked) fuelMultiplier *= 5.0;
        long effectiveFuelAmount = (long) (recipe.getFuelAmount() * fuelMultiplier);

        // Energy check
        if (this.energyBuffer < effectiveEnergyCost) {
            int pct = this.energyCapacity > 0 ? (int) (this.energyBuffer * 100 / this.energyCapacity) : 0;
            String safeNote = this.safeMode ? " §7(2x Safe Mode)" : "";
            errors.add(Component.literal("§c✗ Energy: " + formatAmount(this.energyBuffer) + " / "
                    + formatAmount(effectiveEnergyCost) + " AE" + safeNote));
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
                    long available = instrumentedExtract(storage, fuelKey, effectiveFuelAmount, Actionable.SIMULATE, src);
                    if (available < effectiveFuelAmount) {
                        String fluidName = formatFluidName(fuelRL.getPath());
                        String safeNote = this.safeMode ? " §7(2x Safe Mode)" : "";
                        errors.add(Component.literal("§c✗ Fuel: " + formatAmount(available) + " / "
                                + formatAmount(effectiveFuelAmount) + " mB §f" + fluidName + safeNote));
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
                    long available = simulateExtractItem(req);
                    if (available < req.getAmount()) {
                        // Get the display name of the first matching item
                        String itemName = "Unknown Item";
                        ItemStack[] matches = req.getIngredient().getItems();
                        if (matches.length > 0) {
                            itemName = matches[0].getHoverName().getString();
                        }
                        errors.add(Component.literal("§c✗ Missing: " + formatAmount(available) + " / "
                                + formatAmount(req.getAmount()) + "x §f" + itemName));
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
                        errors.add(Component.literal("§c✗ Missing: " + formatAmount(available) + " / "
                                + formatAmount(req.getAmount()) + " mB §f" + fluidName));
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
        if (!errors.isEmpty())
            return errors;

        if (this.level == null || this.level.isClientSide())
            return List.of(Component.literal("§c✗ Internal error"));

        var recipeOpt = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof StellarSimulationRecipe recipe)) {
            return List.of(Component.literal("§c✗ Invalid recipe"));
        }

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null || nodeBE.getActionableNode() == null)
            return List.of(Component.literal("§c✗ No network"));
        IGridNode node = nodeBE.getActionableNode();
        if (node.getGrid() == null)
            return List.of(Component.literal("§c✗ No grid"));

        IGrid grid = node.getGrid();
        IActionSource src = IActionSource.ofMachine(nodeBE);
        MEStorage storage = grid.getStorageService().getInventory();

        // Compute effective costs (safe mode = 2x, overclock = 8x)
        long effectiveEnergyCost = StellarEnergyMath.effectiveCost(
                recipe.getEnergyCost(), this.safeMode, this.isOverclocked);

        double fuelMultiplier = this.safeMode ? SAFE_MODE_MULTIPLIER : 1.0;
        if (this.isOverclocked) fuelMultiplier *= 5.0;
        long effectiveFuelAmount = (long) (recipe.getFuelAmount() * fuelMultiplier);

        ResourceReservation reservation = reserveStartResources(recipe, storage, src, effectiveFuelAmount);
        if (reservation == null) {
            return List.of(Component.literal("§c✗ Failed to extract inputs"));
        }
        if (!extractReservation(reservation, storage, src)) {
            return List.of(Component.literal("§c✗ Item input changed during reservation; committed items were refunded"));
        }

        // Consume AE energy (with safe mode multiplier)
        this.energyBuffer -= effectiveEnergyCost;
        this.maxProgress = recipe.getTime();
        this.progress = 0;
        this.running = true;
        this.paused = false;
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
        return List.of(); // Success
    }

    public void toggleSafeMode() {
        if (!this.running) {
            this.safeMode = !this.safeMode;
            invalidateDisplayedRequirementAvailability();
            this.setChanged();
        }
    }

    public void toggleAutoStart() {
        this.autoStart = !this.autoStart;
        this.setChanged();
    }

    public void toggleSimulationLock() {
        this.simulationLocked = !this.simulationLocked;
        this.setChanged();
    }

    public void toggleOverclock() {
        if (!this.running) {
            this.isOverclocked = !this.isOverclocked;
            invalidateDisplayedRequirementAvailability();
            this.setChanged();
        }
    }

    /** Pauses or resumes a running simulation without discarding its paid inputs or progress. */
    public void togglePause() {
        if (!this.running) {
            return;
        }
        this.paused = !this.paused;
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    private void processMachineTick() {
        boolean changed = false;
        if (this.energyCapacity != GLOBAL_ENERGY_CAPACITY) {
            this.energyCapacity = GLOBAL_ENERGY_CAPACITY;
            changed = true;
        }

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        changed |= chargeEnergyFromPorts();
        changed |= coolWhileIdle();

        if (this.pendingOutputs.hasPending()) {
            changed |= flushPendingOutputs(nodeBE);
            if (changed) {
                this.setChanged();
            }
            return;
        }

        if (this.activeRecipeId == null) {
            if (changed) {
                this.setChanged();
            }
            return;
        }

        var recipeOpt = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeOpt.isEmpty() || !(recipeOpt.get().value() instanceof StellarSimulationRecipe recipe)) {
            if (changed) {
                this.setChanged();
            }
            return;
        }

        // Cache the time for UI
        this.maxProgress = recipe.getTime();
        if (!this.running || this.paused) {
            if (changed) {
                this.setChanged();
            }
            return;
        }

        // ── Active processing ──
        if (nodeBE == null || nodeBE.getActionableNode() == null)
            return;
        IGridNode node = nodeBE.getActionableNode();
        if (node.getGrid() == null)
            return;

        IGrid grid = node.getGrid();
        IActionSource src = IActionSource.ofMachine(nodeBE);
        MEStorage storage = grid.getStorageService().getInventory();

        // Heat generation — increases based on recipe cooling requirement
        int heatPerTick = recipe.getCoolingLevel() + 1;
        if (this.isOverclocked) heatPerTick *= 5;
        this.heatLevel = Math.min(MAX_HEAT, this.heatLevel + heatPerTick);

        // Coolant consumption through the explicit fluid-port snapshot.
        int coolingApplied = consumeCoolant(true);
        this.heatLevel = Math.max(0, this.heatLevel - coolingApplied);

        // Overheat check
        if (this.heatLevel >= MAX_HEAT) {
            if (this.safeMode) {
                this.running = false;
                this.paused = false;
                this.progress = 0;
                this.cooldownTimer = this.isOverclocked ? 144000 : COOLDOWN_DURATION;
                this.setChanged();
                if (this.level != null) {
                    BlockPos pos = this.worldPosition;
                    int cooldownMinutes = this.cooldownTimer / 1200;
                    this.level.players().forEach(p -> p.displayClientMessage(
                            Component.literal("§c§l[STELLAR NEXUS] §eSafe Mode activated at " + pos.toShortString()
                                    + " - " + cooldownMinutes + " minute cooldown initiated."),
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
        this.progress += this.isOverclocked ? 5 : 1;
        if (this.progress >= recipe.getTime()) {
            prepareOutputs(recipe);
            flushPendingOutputs(storage, src);
            this.progress = 0;
            this.running = false;
            this.paused = false;

            if (this.level != null) {
                BlockState state = this.level.getBlockState(this.worldPosition);
                this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
            }
        }
        this.setChanged();
    }

    private boolean chargeEnergyFromPorts() {
        if (this.fieldLevel < 1 || this.fieldLevel > 3 || this.energyPorts.size() == 0) {
            return false;
        }

        long toCharge = StellarEnergyMath.chargeRequest(
                this.energyBuffer, this.energyCapacity, ENERGY_RATE_BY_TIER[this.fieldLevel]);
        if (toCharge <= 0L) {
            return false;
        }

        long accepted = this.energyPorts.extract(toCharge, false);
        this.lastEnergyRequested = toCharge;
        this.lastEnergyAccepted = accepted;
        MachinePerformanceRegistry.INSTANCE.recordEnergyTransfer(
                performanceMetricKey(), toCharge, accepted, this.level.getGameTime());
        if (accepted <= 0L) {
            return false;
        }

        this.energyBuffer = Math.min(this.energyCapacity, this.energyBuffer + accepted);
        return true;
    }

    private void triggerStellarExplosion() {
        if (this.level == null)
            return;
        BlockPos pos = this.worldPosition;

        int requestedRadius = this.fieldLevel == 3 ? 100 : (this.fieldLevel == 2 ? 50 : 30);
        boolean dimensionAllowed = UFOConfig.STELLAR_EXPLOSION_ALLOWED_DIMENSIONS.get().stream()
                .anyMatch(this.level.dimension().location().toString()::equals);
        this.explosionPolicy = StellarExplosionPolicy.resolve(
                UFOConfig.STELLAR_EXPLOSION_BLOCK_GRIEF.get(),
                dimensionAllowed,
                requestedRadius,
                UFOConfig.STELLAR_EXPLOSION_MAX_RADIUS.get(),
                UFOConfig.STELLAR_EXPLOSION_MAX_BLOCKS_PER_TICK.get(),
                UFOConfig.STELLAR_EXPLOSION_MAX_TOTAL_BLOCKS.get(),
                UFOConfig.STELLAR_EXPLOSION_MAX_NANOS_PER_TICK.get(),
                UFOConfig.STELLAR_EXPLOSION_CREATE_LAVA.get(),
                UFOConfig.STELLAR_EXPLOSION_SECONDARY_EXPLOSIONS.get());
        this.explosionRadius = this.explosionPolicy.radius();
        UfoMod.LOGGER.warn(
                "Stellar Nexus thermal failure at {} in {} resolved to {} radius={} perTick={} total={} nanos={}",
                pos, this.level.dimension().location(), this.explosionPolicy.mode(),
                this.explosionPolicy.radius(), this.explosionPolicy.maxBlockChangesPerTick(),
                this.explosionPolicy.maxTotalBlockChanges(), this.explosionPolicy.maxNanosPerTick());

        this.level.players().forEach(p -> p.displayClientMessage(
                Component.literal("§4§l[STELLAR NEXUS] §c§lCRITICAL THERMAL FAILURE at " + pos.toShortString()
                        + (this.explosionPolicy.allowsBlockGrief()
                        ? "! BOUNDED DESTRUCTIVE WAVE!"
                        : "! LOCAL CONTAINMENT FAILURE!")),
                false));

        // The initial blast damages locally but never bypasses the configured block budget.
        this.level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                8.0F, Level.ExplosionInteraction.NONE);

        this.exploding = this.explosionPolicy.allowsBlockGrief();
        this.explosionTick = 0;
        this.explosionShellRadius = 0;
        this.explosionBlockChanges = 0;
        resetExplosionCursor();

        this.running = false;
        this.paused = false;
        this.progress = 0;
        this.heatLevel = 0;
        this.energyBuffer = 0;
        this.assembled = false;

        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }

        onControllerBroken();
        if (!this.exploding) {
            removeControllerBlockAfterExplosion();
        }
    }

    private void processExplosionTick() {
        if (this.level == null) {
            this.exploding = false;
            return;
        }

        this.explosionTick++;
        int changedThisTick = 0;
        long deadline = System.nanoTime() + this.explosionPolicy.maxNanosPerTick();
        while (changedThisTick < this.explosionPolicy.maxBlockChangesPerTick()
                && this.explosionBlockChanges < this.explosionPolicy.maxTotalBlockChanges()
                && this.exploding
                && System.nanoTime() < deadline) {
            if (this.explosionShellRadius > this.explosionRadius) {
                finishExplosionWave();
                break;
            }

            int radius = this.explosionShellRadius;
            int remainingChanges = Math.min(
                    this.explosionPolicy.maxBlockChangesPerTick() - changedThisTick,
                    this.explosionPolicy.maxTotalBlockChanges() - this.explosionBlockChanges);
            int stepResult = processExplosionCursor(radius, remainingChanges);
            if (stepResult >= 0) {
                changedThisTick += stepResult;
                this.explosionBlockChanges += stepResult;
            } else if (stepResult == -2) {
                spawnExplosionPulse(radius);
                this.explosionShellRadius++;
                resetExplosionCursor();
            }
        }

        if (this.explosionBlockChanges >= this.explosionPolicy.maxTotalBlockChanges()) {
            finishExplosionWave();
        }

        this.setChanged();
    }

    private int processExplosionCursor(int radius, int remainingChanges) {
        if (radius == 0) {
            return -2;
        }

        if (this.explosionCursorY > radius) {
            return -2;
        }

        int dx = this.explosionCursorX;
        int dy = this.explosionCursorY;
        int dz = this.explosionCursorZ;

        advanceExplosionCursor(radius);

        int distSq = dx * dx + dy * dy + dz * dz;
        int outerSq = radius * radius;
        int innerSq = (radius - 1) * (radius - 1);
        if (distSq > outerSq || distSq <= innerSq) {
            return -1;
        }

        return processExplosionBlock(this.worldPosition.offset(dx, dy, dz), radius, remainingChanges);
    }

    private void advanceExplosionCursor(int radius) {
        this.explosionCursorX++;
        if (this.explosionCursorX > radius) {
            this.explosionCursorX = -radius;
            this.explosionCursorZ++;
            if (this.explosionCursorZ > radius) {
                this.explosionCursorZ = -radius;
                this.explosionCursorY++;
            }
        }
    }

    private void resetExplosionCursor() {
        this.explosionCursorX = -this.explosionShellRadius;
        this.explosionCursorY = -this.explosionShellRadius;
        this.explosionCursorZ = -this.explosionShellRadius;
    }

    private int processExplosionBlock(BlockPos target, int radius, int remainingChanges) {
        if (remainingChanges <= 0 || target.equals(this.worldPosition)) {
            return 0;
        }
        if (this.level == null || !this.level.isLoaded(target) || !this.level.isInWorldBounds(target)) {
            return 0;
        }

        BlockState targetState = this.level.getBlockState(target);
        if (targetState.isAir() || targetState.getDestroySpeed(this.level, target) < 0) {
            return 0;
        }

        int lavaRadius = Math.max(3, (int) (this.explosionRadius * 0.28));
        BlockState replacement = this.explosionPolicy.createLava() && radius <= lavaRadius
                ? net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState()
                : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        int changes = this.level.setBlock(target, replacement, Block.UPDATE_CLIENTS) ? 1 : 0;

        BlockPos above = target.above();
        if (changes < remainingChanges && replacement.isAir()
                && this.level.isLoaded(above) && this.level.getBlockState(above).isAir()
                && this.level.setBlock(above, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), Block.UPDATE_CLIENTS)) {
            changes++;
        }
        return changes;
    }

    private void spawnExplosionPulse(int radius) {
        if (this.level == null || radius <= 0) {
            return;
        }

        if (this.explosionPolicy.secondaryExplosions()
                && (radius == 1 || radius == this.explosionRadius || radius % 4 == 0)) {
            var random = this.level.getRandom();
            double offsetScale = Math.max(2.0, radius * 0.35);
            double ox = this.worldPosition.getX() + 0.5 + (random.nextDouble() - 0.5) * offsetScale;
            double oy = this.worldPosition.getY() + 0.5 + (random.nextDouble() - 0.5) * offsetScale;
            double oz = this.worldPosition.getZ() + 0.5 + (random.nextDouble() - 0.5) * offsetScale;
            float power = Math.min(18.0f, 4.0f + radius * 0.12f);
            this.level.explode(null, ox, oy, oz, power, Level.ExplosionInteraction.NONE);
        }
    }

    private void finishExplosionWave() {
        this.exploding = false;
        this.explosionTick = 0;
        this.explosionShellRadius = 0;
        this.explosionBlockChanges = 0;
        resetExplosionCursor();
        removeControllerBlockAfterExplosion();
    }

    private void removeControllerBlockAfterExplosion() {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return;
        }

        this.level.removeBlock(this.worldPosition, false);
    }

    private AENetworkedBlockEntity getConnectedNetworkNode() {
        if (this.level == null)
            return null;

        for (BlockPos position : this.networkNodeCandidates) {
            if (this.level.getBlockEntity(position) instanceof AENetworkedBlockEntity nodeBE) {
                IGridNode node = nodeBE.getActionableNode();
                if (Ae2NodeAvailability.isUsable(
                        node != null,
                        node != null && node.getGrid() != null,
                        node != null && node.isActive(),
                        node != null && node.isPowered())) {
                    return nodeBE;
                }
            }
        }
        return null;
    }

    private static void addCandidate(List<BlockPos> candidates, @Nullable BlockPos position) {
        if (position != null) {
            candidates.add(position);
        }
    }



    // ──────────────────── Coolant System ────────────────────

    /** Amount of coolant fluid (mB) consumed per tick during active operation. */
    private static final long COOLANT_CONSUMPTION_PER_TICK = 100;

    /**
     * Tries to extract coolant fluid from the ME network.
     * Returns the cooling power applied this tick (heat units to subtract).
     * <p>
     * Prioritizes the intended coolant ladder without falling back to water.
     * <p>
     * Coolant effectiveness per tier:
     * <ul>
     * <li>Gelid Cryotheum (T1): 1 cooling/mB</li>
     * <li>Stable Coolant (T2): 4 cooling/mB</li>
     * <li>Temporal Fluid (T3): 8 cooling/mB</li>
     * </ul>
     */
    private boolean coolWhileIdle() {
        if ((this.running && !this.paused) || this.heatLevel <= 0) {
            return false;
        }

        int interval = UFOConfig.STELLAR_IDLE_COOLANT_INTERVAL_TICKS.get();
        if (interval <= 0 || this.level.getGameTime() % interval != 0L) {
            return false;
        }

        int coolingApplied = consumeCoolant(false);
        if (coolingApplied <= 0) {
            return false;
        }
        this.heatLevel = Math.max(0, this.heatLevel - coolingApplied);
        return true;
    }

    private int consumeCoolant(boolean activeOperation) {
        if (this.coolantPorts.size() == 0 || this.heatLevel <= 0
                || this.fieldLevel < 1 || this.fieldLevel > 3)
            return 0;

        long effectiveCoolantPerTick = StellarCoolantMath.targetFlow(
                COOLANT_CONSUMPTION_PER_TICK,
                activeOperation,
                this.safeMode,
                this.isOverclocked);

        int tierMultiplier = this.fieldLevel + 1;
        CoolantDefinition[] definitions = getCoolantDefinitions();
        int[] priority = coolantPriority(definitions);
        for (int definitionIndex : priority) {
            CoolantDefinition coolant = definitions[definitionIndex];
            long required = StellarCoolantMath.amountRequiredForHeat(
                    this.heatLevel, coolant.efficiency(), tierMultiplier, effectiveCoolantPerTick);
            this.lastCoolantRequested = required;
            this.lastCoolantAccepted = 0L;
            long available = extractCoolantFromPorts(coolant.key(), required, true);
            long amountToExtract = Math.min(required, available);
            if (StellarCoolantMath.coolingFromExtracted(
                    amountToExtract, coolant.efficiency(), tierMultiplier, effectiveCoolantPerTick) <= 0L) {
                continue;
            }

            long extracted = extractCoolantFromPorts(coolant.key(), amountToExtract, false);
            this.lastCoolantRequested = amountToExtract;
            this.lastCoolantAccepted = extracted;
            long cooling = StellarCoolantMath.coolingFromExtracted(
                    extracted, coolant.efficiency(), tierMultiplier, effectiveCoolantPerTick);
            return (int) Math.min(this.heatLevel, cooling);
        }

        return 0; // No coolant available — heat will continue to rise!
    }

    private long extractCoolantFromPorts(AEFluidKey coolant, long amount, boolean simulate) {
        if (amount <= 0L) {
            return 0L;
        }
        long extracted = 0L;
        for (MassiveOutputHatchBE hatch : this.coolantHatches) {
            long remaining = amount - extracted;
            if (remaining <= 0L) {
                break;
            }
            long accepted = hatch.extractBufferedCoolant(coolant, remaining, simulate);
            extracted += Math.max(0L, Math.min(remaining, accepted));
        }
        return extracted;
    }

    /** Keeps every linked hatch supplied from ME storage, best coolant first. */
    private static int[] coolantPriority(CoolantDefinition[] definitions) {
        long[] efficiencies = new long[definitions.length];
        for (int index = 0; index < definitions.length; index++) {
            efficiencies[index] = definitions[index].efficiency();
        }
        return StellarCoolantMath.priorityByEfficiency(efficiencies);
    }

    private int getDisplayedCoolantKind() {
        FluidStack coolant = getDisplayedCoolant();
        if (coolant.isEmpty()) return 0;
        if (coolant.getFluid() == ModFluids.SOURCE_GELID_CRYOTHEUM.get()) return 1;
        if (coolant.getFluid() == ModFluids.SOURCE_STABLE_COOLANT.get()) return 2;
        if (coolant.getFluid() == ModFluids.SOURCE_TEMPORAL_FLUID.get()) return 3;
        return 0;
    }

    private int getDisplayedCoolantAmount() {
        return getDisplayedCoolant().getAmount();
    }

    private FluidStack getDisplayedCoolant() {
        for (MassiveOutputHatchBE hatch : this.coolantHatches) {
            FluidStack coolant = hatch.getStoredCoolant();
            if (!coolant.isEmpty()) {
                return coolant;
            }
        }
        return FluidStack.EMPTY;
    }

    private CoolantDefinition[] getCoolantDefinitions() {
        if (this.coolantDefinitions == null) {
            this.coolantDefinitions = new CoolantDefinition[]{
                    new CoolantDefinition(
                            AEFluidKey.of(ModFluids.SOURCE_TEMPORAL_FLUID.get()),
                            UFOConfig.STELLAR_COOLANT_TEMPORAL_EFFICIENCY::get),
                    new CoolantDefinition(
                            AEFluidKey.of(ModFluids.SOURCE_STABLE_COOLANT.get()),
                            UFOConfig.STELLAR_COOLANT_STABLE_EFFICIENCY::get),
                    new CoolantDefinition(
                            AEFluidKey.of(ModFluids.SOURCE_GELID_CRYOTHEUM.get()),
                            UFOConfig.STELLAR_COOLANT_GELID_EFFICIENCY::get)
            };
        }
        return this.coolantDefinitions;
    }

    private record CoolantDefinition(AEFluidKey key, LongSupplier efficiencySupplier) {
        private long efficiency() {
            return Math.max(0L, this.efficiencySupplier.getAsLong());
        }
    }

    public ResourceLocation getActiveRecipeId() {
        return this.activeRecipeId;
    }

    public void setActiveRecipe(ResourceLocation activeRecipeId) {
        this.activeRecipeId = activeRecipeId;
        this.progress = 0;
        invalidateDisplayedRequirementAvailability();
        this.setChanged();

        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.getBlockPos());
            this.level.sendBlockUpdated(this.getBlockPos(), state, state, 3);
        }
    }

    // ──────────────────── Inventory Operations ────────────────────

    private void ensureDisplayedRequirementAvailabilityFresh() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        long refreshBucket = this.level.getGameTime() / 10L;
        if (this.requirementAvailabilityRefreshBucket == refreshBucket) {
            return;
        }
        this.requirementAvailabilityRefreshBucket = refreshBucket;
        refreshDisplayedRequirementAvailability();
    }

    private void invalidateDisplayedRequirementAvailability() {
        this.requirementAvailabilityRefreshBucket = Long.MIN_VALUE;
    }

    /** Refreshes the seven quantities shown by the attached resource widget. */
    private void refreshDisplayedRequirementAvailability() {
        Arrays.fill(this.displayedRequirementAvailability, 0L);
        if (this.level == null || this.level.isClientSide() || this.activeRecipeId == null) {
            return;
        }

        var recipeHolder = this.level.getRecipeManager().byKey(this.activeRecipeId);
        if (recipeHolder.isEmpty() || !(recipeHolder.get().value() instanceof StellarSimulationRecipe recipe)) {
            return;
        }

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null || nodeBE.getActionableNode() == null
                || nodeBE.getActionableNode().getGrid() == null) {
            return;
        }

        var availableStacks = nodeBE.getActionableNode().getGrid()
                .getStorageService().getInventory().getAvailableStacks();
        Map<String, Set<AEKey>> rows = new LinkedHashMap<>();

        for (var input : recipe.getItemInputs()) {
            if (input.isEmpty()) continue;
            ItemStack[] matches = input.getIngredient().getItems();
            if (matches.length == 0 || matches[0].isEmpty()) continue;
            String rowKey = "item:" + BuiltInRegistries.ITEM.getKey(matches[0].getItem());
            Set<AEKey> acceptedKeys = rows.computeIfAbsent(rowKey, ignored -> new LinkedHashSet<>());
            for (ItemStack match : matches) {
                if (!match.isEmpty()) acceptedKeys.add(AEItemKey.of(match));
            }
        }

        for (var input : recipe.getFluidInputs()) {
            if (input.isEmpty()) continue;
            FluidStack[] matches = input.getIngredient().getStacks();
            if (matches.length == 0 || matches[0].isEmpty()) continue;
            String rowKey = "fluid:" + BuiltInRegistries.FLUID.getKey(matches[0].getFluid());
            Set<AEKey> acceptedKeys = rows.computeIfAbsent(rowKey, ignored -> new LinkedHashSet<>());
            for (FluidStack match : matches) {
                if (!match.isEmpty()) acceptedKeys.add(AEFluidKey.of(match.getFluid()));
            }
        }

        recipe.getFuelFluidRL().ifPresent(id -> {
            Fluid fuel = BuiltInRegistries.FLUID.get(id);
            if (fuel != null && fuel != net.minecraft.world.level.material.Fluids.EMPTY
                    && recipe.getFuelAmount() > 0L) {
                rows.computeIfAbsent("fluid:" + id, ignored -> new LinkedHashSet<>())
                        .add(AEFluidKey.of(fuel));
            }
        });

        int rowIndex = 0;
        for (Set<AEKey> acceptedKeys : rows.values()) {
            if (rowIndex >= DISPLAYED_REQUIREMENT_ROWS) break;
            long available = 0L;
            for (AEKey key : acceptedKeys) {
                long amount = Math.max(0L, availableStacks.get(key));
                available = saturatedAdd(available, amount);
            }
            this.displayedRequirementAvailability[rowIndex++] = available;
        }
    }

    @Nullable
    private ResourceReservation reserveStartResources(StellarSimulationRecipe recipe, MEStorage storage, IActionSource src, long effectiveFuelAmount) {
        Map<AEItemKey, Long> itemReservations = new HashMap<>();
        Map<AEFluidKey, Long> fluidReservations = new HashMap<>();

        if (!recipe.getFuelFluid().isEmpty() && recipe.getFuelAmount() > 0) {
            ResourceLocation fuelRL = ResourceLocation.parse(recipe.getFuelFluid());
            Fluid fuelFluid = BuiltInRegistries.FLUID.get(fuelRL);
            if (fuelFluid == null || fuelFluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                return null;
            }
            if (!reserveFluid(AEFluidKey.of(fuelFluid), effectiveFuelAmount, fluidReservations, storage, src)) {
                return null;
            }
        }

        for (var req : recipe.getItemInputs()) {
            if (!req.isEmpty() && !reserveItem(req, itemReservations)) {
                return null;
            }
        }
        for (var req : recipe.getFluidInputs()) {
            if (!req.isEmpty() && !reserveFluid(req, fluidReservations, storage, src)) {
                return null;
            }
        }

        return new ResourceReservation(itemReservations, fluidReservations);
    }

    private boolean reserveItem(IngredientStack.Item req, Map<AEItemKey, Long> reservations) {
        long amount = req.getAmount();
        for (ItemStack match : req.getIngredient().getItems()) {
            AEItemKey key = AEItemKey.of(match);
            long reserved = reservations.getOrDefault(key, 0L);
            long neededWithReservation = saturatedAdd(reserved, amount);
            long available = this.itemInputPorts.extract(key, neededWithReservation, true);
            if (available >= neededWithReservation) {
                reservations.put(key, neededWithReservation);
                return true;
            }
        }
        return false;
    }

    private boolean reserveFluid(IngredientStack.Fluid req, Map<AEFluidKey, Long> reservations, MEStorage storage, IActionSource src) {
        long amount = req.getAmount();
        for (FluidStack match : req.getIngredient().getStacks()) {
            if (reserveFluid(AEFluidKey.of(match.getFluid()), amount, reservations, storage, src)) {
                return true;
            }
        }
        return false;
    }

    private boolean reserveFluid(AEFluidKey key, long amount, Map<AEFluidKey, Long> reservations, MEStorage storage, IActionSource src) {
        if (amount <= 0L) {
            return true;
        }
        long reserved = reservations.getOrDefault(key, 0L);
        long neededWithReservation = saturatedAdd(reserved, amount);
        long available = instrumentedExtract(storage, key, neededWithReservation, Actionable.SIMULATE, src);
        if (available < neededWithReservation) {
            return false;
        }
        reservations.put(key, neededWithReservation);
        return true;
    }

    private boolean extractReservation(ResourceReservation reservation, MEStorage storage, IActionSource src) {
        Map<AEItemKey, Long> committedItems = new HashMap<>();
        for (var entry : reservation.itemReservations().entrySet()) {
            long extracted = this.itemInputPorts.extractTransactional(entry.getKey(), entry.getValue());
            if (extracted > 0L) {
                committedItems.put(entry.getKey(), extracted);
            }
            if (extracted < entry.getValue()) {
                refundCommittedItems(committedItems, storage, src);
                return false;
            }
        }
        for (var entry : reservation.fluidReservations().entrySet()) {
            instrumentedExtract(storage, entry.getKey(), entry.getValue(), Actionable.MODULATE, src);
        }
        return true;
    }

    private void refundCommittedItems(Map<AEItemKey, Long> committedItems, MEStorage fallbackStorage, IActionSource src) {
        for (var entry : committedItems.entrySet()) {
            long refunded = this.itemInputPorts.insertTransactional(entry.getKey(), entry.getValue());
            if (refunded < entry.getValue()) {
                long remaining = entry.getValue() - refunded;
                long inserted = instrumentedInsert(fallbackStorage, entry.getKey(), remaining, src);
                remaining -= Math.max(0L, Math.min(remaining, inserted));
                if (remaining > 0L && this.level != null) {
                    UfoMod.LOGGER.warn(
                            "Item-input rollback at {} could not return {} x {}; ejecting a recovery package",
                            this.worldPosition, remaining, entry.getKey());
                    Containers.dropItemStack(
                            this.level,
                            this.worldPosition.getX() + 0.5D,
                            this.worldPosition.getY() + 0.5D,
                            this.worldPosition.getZ() + 0.5D,
                            GenericStack.wrapInItemStack(new GenericStack(entry.getKey(), remaining)));
                }
            }
        }
    }

    private long saturatedAdd(long a, long b) {
        if (b > 0L && a > Long.MAX_VALUE - b) {
            return Long.MAX_VALUE;
        }
        return a + b;
    }

    private long simulateExtractItem(IngredientStack.Item req) {
        long extracted = 0;
        long needed = req.getAmount();
        for (ItemStack match : req.getIngredient().getItems()) {
            long ext = this.itemInputPorts.extract(AEItemKey.of(match), needed, true);
            extracted += ext;
            needed -= ext;
            if (needed <= 0)
                break;
        }
        return extracted;
    }

    private long simulateExtractFluid(IngredientStack.Fluid req, MEStorage storage, IActionSource src) {
        long extracted = 0;
        long needed = req.getAmount();
        for (FluidStack match : req.getIngredient().getStacks()) {
            long ext = instrumentedExtract(storage, AEFluidKey.of(match.getFluid()), needed, Actionable.SIMULATE, src);
            extracted += ext;
            needed -= ext;
            if (needed <= 0)
                break;
        }
        return extracted;
    }

    private void modulateExtractFluid(IngredientStack.Fluid req, MEStorage storage, IActionSource src) {
        long needed = req.getAmount();
        for (FluidStack match : req.getIngredient().getStacks()) {
            long ext = instrumentedExtract(storage, AEFluidKey.of(match.getFluid()), needed, Actionable.MODULATE, src);
            needed -= ext;
            if (needed <= 0)
                break;
        }
    }

    private void prepareOutputs(StellarSimulationRecipe recipe) {
        List<TransactionalAmountLedger.Entry<AEKey>> outputs = new ArrayList<>();
        for (GenericStack out : recipe.getItemOutputs()) {
            if (out != null) {
                outputs.add(new TransactionalAmountLedger.Entry<>(out.what(), out.amount()));
            }
        }
        for (GenericStack out : recipe.getFluidOutputs()) {
            if (out != null) {
                outputs.add(new TransactionalAmountLedger.Entry<>(out.what(), out.amount()));
            }
        }
        this.pendingOutputs.prepare(outputs);
    }

    private boolean flushPendingOutputs(@Nullable AENetworkedBlockEntity nodeBE) {
        MEStorage storage = null;
        IActionSource src = IActionSource.empty();
        if (nodeBE != null && nodeBE.getActionableNode() != null) {
            IGridNode node = nodeBE.getActionableNode();
            if (node.getGrid() != null && node.isActive() && node.isPowered()) {
                storage = node.getGrid().getStorageService().getInventory();
                src = IActionSource.ofMachine(nodeBE);
            }
        }
        return flushPendingOutputs(storage, src);
    }

    private boolean flushPendingOutputs(@Nullable MEStorage storage, IActionSource src) {
        return this.pendingOutputs.drain((key, requested) -> {
            if (key instanceof AEItemKey itemKey) {
                return this.itemOutputPorts.insertTransactional(itemKey, requested);
            }
            return storage != null ? instrumentedInsert(storage, key, requested, src) : 0L;
        }) > 0L;
    }

    public int getItemInputPortCount() {
        return this.itemInputPorts.size();
    }

    public int getItemOutputPortCount() {
        return this.itemOutputPorts.size();
    }

    public boolean hasPendingOutputs() {
        return this.pendingOutputs.hasPending();
    }

    public long getPendingOutputAmount() {
        long total = 0L;
        for (var output : this.pendingOutputs.snapshot()) {
            total = total > Long.MAX_VALUE - output.amount() ? Long.MAX_VALUE : total + output.amount();
        }
        return total;
    }

    public EnergyDebugSnapshot getEnergyDebugSnapshot() {
        long configuredRate = this.fieldLevel >= 1 && this.fieldLevel <= 3
                ? ENERGY_RATE_BY_TIER[this.fieldLevel]
                : 0L;
        long activeRecipeCost = 0L;
        if (this.level != null && this.activeRecipeId != null) {
            var recipe = this.level.getRecipeManager().byKey(this.activeRecipeId);
            if (recipe.isPresent() && recipe.get().value() instanceof StellarSimulationRecipe stellarRecipe) {
                activeRecipeCost = StellarEnergyMath.effectiveCost(
                        stellarRecipe.getEnergyCost(), this.safeMode, this.isOverclocked);
            }
        }

        return new EnergyDebugSnapshot(
                this.energyBuffer,
                this.energyCapacity,
                configuredRate,
                this.lastEnergyRequested,
                this.lastEnergyAccepted,
                this.energyPorts.size(),
                activeRecipeCost,
                StellarEnergyMath.ticksToReach(this.energyBuffer, activeRecipeCost, configuredRate),
                StellarEnergyMath.ticksToReach(this.energyBuffer, activeRecipeCost, this.lastEnergyAccepted));
    }

    public List<com.raishxn.ufo.diagnostic.CoolantStatus> getCoolantStatus() {
        List<com.raishxn.ufo.diagnostic.CoolantStatus> result = new ArrayList<>();
        long flow = StellarCoolantMath.targetFlow(COOLANT_CONSUMPTION_PER_TICK,
                this.running && !this.paused, this.safeMode, this.isOverclocked);
        for (MassiveOutputHatchBE hatch : this.coolantHatches) {
            if (hatch.isRemoved() || this.level == null || !this.level.hasChunkAt(hatch.getBlockPos())) continue;
            var fluid = hatch.getStoredCoolant();
            long efficiency = 0;
            if (!fluid.isEmpty()) for (var definition : getCoolantDefinitions()) {
                if (definition.key().getFluid() == fluid.getFluid()) efficiency = definition.efficiency();
            }
            var profile = StellarCoolantMath.profile(efficiency, this.fieldLevel + 1L, flow);
            result.add(new com.raishxn.ufo.diagnostic.CoolantStatus(hatch.getBlockPos().asLong(),
                    fluid.isEmpty() ? "minecraft:empty" : BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString(),
                    fluid.getAmount(), MassiveOutputHatchBE.COOLANT_CAPACITY,
                    profile.heatNumerator(), profile.millibucketDenominator(), profile.maxFlowPerTick()));
        }
        return List.copyOf(result);
    }

    public CoolantDebugSnapshot getCoolantDebugSnapshot() {
        return new CoolantDebugSnapshot(
                this.heatLevel,
                MAX_HEAT,
                this.coolantPorts.size(),
                this.lastCoolantRequested,
                this.lastCoolantAccepted);
    }

    public record CoolantDebugSnapshot(
            int heat,
            int maxHeat,
            int portCount,
            long lastRequested,
            long lastAccepted) {
    }

    public record EnergyDebugSnapshot(
            long stored,
            long capacity,
            long configuredRate,
            long lastRequested,
            long lastAccepted,
            int portCount,
            long activeRecipeCost,
            long configuredTicksToRecipe,
            long observedTicksToRecipe) {
    }

    private record ResourceReservation(Map<AEItemKey, Long> itemReservations, Map<AEFluidKey, Long> fluidReservations) {
    }

    public void markStructureDirty() {
        this.structureDirty = true;
        this.scanCooldown = 0;
        this.energyPorts = EnergyPortGroup.empty();
        this.coolantPorts = FluidPortGroup.empty();
        this.coolantHatches = List.of();
        this.itemInputPorts = ItemPortGroup.empty();
        this.itemOutputPorts = ItemPortGroup.empty();
        this.networkNodeCandidates = List.of();
    }

    public void onControllerBroken() {
        if (this.level == null)
            return;
        recoverPendingOutputsBeforeRemoval();
        for (BlockPos partPos : this.parts) {
            if (this.level.getBlockEntity(partPos) instanceof IMultiblockPart part) {
                part.unlinkFromController();
            }
        }
        this.parts.clear();
        this.energyPorts = EnergyPortGroup.empty();
        this.coolantPorts = FluidPortGroup.empty();
        this.coolantHatches = List.of();
        this.itemInputPorts = ItemPortGroup.empty();
        this.itemOutputPorts = ItemPortGroup.empty();
        this.networkNodeCandidates = List.of();
        this.assembled = false;
    }

    private void recoverPendingOutputsBeforeRemoval() {
        if (this.level == null || this.level.isClientSide() || !this.pendingOutputs.hasPending()) {
            return;
        }

        flushPendingOutputs(getConnectedNetworkNode());
        for (var output : this.pendingOutputs.snapshot()) {
            Containers.dropItemStack(
                    this.level,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D,
                    GenericStack.wrapInItemStack(new GenericStack(output.key(), output.amount())));
        }
        this.pendingOutputs.clear();
    }

    // ──────────────────── Utility ────────────────────

    private static String formatAmount(long amount) {
        if (amount >= 1_000_000_000)
            return String.format("%.1fB", amount / 1_000_000_000.0);
        if (amount >= 1_000_000)
            return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000)
            return String.format("%.1fK", amount / 1_000.0);
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
        if (path.startsWith("source_"))
            path = path.substring(7);
        if (path.startsWith("flowing_"))
            path = path.substring(8);
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
        writeClientState(tag);

        ListTag pendingOutputTags = new ListTag();
        for (var output : this.pendingOutputs.snapshot()) {
            pendingOutputTags.add(GenericStack.writeTag(
                    registries, new GenericStack(output.key(), output.amount())));
        }
        tag.put("pendingOutputs", pendingOutputTags);
        tag.putBoolean("outputsPrepared", this.pendingOutputs.isPrepared());

        ListTag partsList = new ListTag();
        for (BlockPos pos : this.parts) {
            partsList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("parts", partsList);
    }

    /** Bounded visual state; structure membership and resource ledgers stay server-side. */
    private void writeClientState(CompoundTag tag) {
        tag.putBoolean("assembled", this.assembled);
        tag.putBoolean("running", this.running);
        tag.putBoolean("paused", this.paused);
        tag.putBoolean("safeMode", this.safeMode);

        tag.putInt("progress", this.progress);
        tag.putInt("heatLevel", this.heatLevel);
        tag.putInt("cooldownTimer", this.cooldownTimer);
        tag.putLong("energyBuffer", this.energyBuffer);
        tag.putLong("energyCapacity", this.energyCapacity);
        
        tag.putBoolean("autoStart", this.autoStart);
        tag.putBoolean("simulationLocked", this.simulationLocked);

        if (this.activeRecipeId != null) {
            tag.putString("activeRecipeId", this.activeRecipeId.toString());
        }

    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.assembled = tag.getBoolean("assembled");
        this.running = tag.getBoolean("running");
        this.paused = this.running && tag.getBoolean("paused");
        this.safeMode = tag.getBoolean("safeMode");

        this.progress = tag.getInt("progress");
        this.heatLevel = tag.getInt("heatLevel");
        this.cooldownTimer = tag.getInt("cooldownTimer");
        
        this.autoStart = tag.getBoolean("autoStart");
        this.simulationLocked = tag.getBoolean("simulationLocked");

        List<TransactionalAmountLedger.Entry<AEKey>> restoredOutputs = new ArrayList<>();
        if (tag.contains("pendingOutputs", Tag.TAG_LIST)) {
            ListTag pendingOutputTags = tag.getList("pendingOutputs", Tag.TAG_COMPOUND);
            for (int i = 0; i < pendingOutputTags.size(); i++) {
                GenericStack output = GenericStack.readTag(registries, pendingOutputTags.getCompound(i));
                if (output != null && output.amount() > 0L) {
                    restoredOutputs.add(new TransactionalAmountLedger.Entry<>(output.what(), output.amount()));
                }
            }
        }
        this.pendingOutputs.restore(restoredOutputs, tag.getBoolean("outputsPrepared"));

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
        this.energyPorts = EnergyPortGroup.empty();
        this.coolantPorts = FluidPortGroup.empty();
        this.coolantHatches = List.of();
        this.itemInputPorts = ItemPortGroup.empty();
        this.itemOutputPorts = ItemPortGroup.empty();
        this.networkNodeCandidates = List.of();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        writeClientState(tag);
        if (this.level != null && !this.level.isClientSide()) {
            MachinePerformanceRegistry.INSTANCE.recordSync(performanceMetricKey(), tag.sizeInBytes(), this.level.getGameTime());
        }
        return tag;
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}

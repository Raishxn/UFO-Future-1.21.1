package com.raishxn.ufo.block.entity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.api.multiblock.MultiblockTierScaling;
import com.raishxn.ufo.api.multiblock.MultiblockControllerDefinitions;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.api.multiblock.MultiblockRuntimeStateResolver;
import com.raishxn.ufo.api.multiblock.Ae2NodeAvailability;
import com.raishxn.ufocore.api.port.ChemicalPort;
import com.raishxn.ufocore.api.port.ChemicalPortGroup;
import com.raishxn.ufocore.api.port.FluidInputPort;
import com.raishxn.ufocore.api.port.FluidPortGroup;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.block.entity.processing.AutocraftingOutputPolicy;
import com.raishxn.ufo.block.entity.processing.KeyedTransferBatch;
import com.raishxn.ufo.block.entity.processing.MultiblockProcessingRecipe;
import com.raishxn.ufo.block.entity.processing.ParallelProcessState;
import com.raishxn.ufo.block.entity.processing.ParallelRuntimeCadence;
import com.raishxn.ufo.block.entity.processing.ThermalSystem;
import com.raishxn.ufo.compat.mekanism.MekanismChemicalCompat;
import com.raishxn.ufo.compat.mekanism.MekanismChemicalStorage;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.init.ModSounds;
import com.raishxn.ufo.item.custom.BaseCatalystItem;
import com.raishxn.ufo.item.custom.DimensionalCatalystItem;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractParallelMultiblockControllerBE extends AbstractSimpleMultiblockControllerBE implements ICraftingMachine {
    protected static final int MAX_PARALLEL_THREADS = 27;
    protected static final int SAFE_MODE_PARALLEL_THREADS = 9;
    protected static final int OVERCLOCK_SPEED_MULTIPLIER = 5;
    private static final int RECIPE_CACHE_REFRESH_TICKS = 100;
    private static final int THERMAL_MAX = 10000;
    private static final int OVERLOAD_TICKS = 100;
    private static final float THERMAL_EXPLOSION_POWER = 30.0F;
    private static final long LEGACY_MAXIMUM_BONUS_ROLLS = 3L;
    private static final ThermalSystem.CoolantProfile TEMPORAL_COOLANT_PROFILE =
            new ThermalSystem.CoolantProfile(100L, 1L, 10L);
    private static final ThermalSystem.CoolantProfile STABLE_COOLANT_PROFILE =
            new ThermalSystem.CoolantProfile(50L, 1L, 10L);
    private static final ThermalSystem.CoolantProfile GELID_COOLANT_PROFILE =
            new ThermalSystem.CoolantProfile(1L, 120L, 1_000L);
    private static final ThermalSystem.CoolantProfile FALLBACK_COOLANT_PROFILE =
            new ThermalSystem.CoolantProfile(15L, 1L, 10L);
    protected final List<ParallelProcessState> processStates = new ArrayList<>();
    private AEFluidKey[][] coolantPriorityByTier;
    private long lastClientSyncEvaluationTick = Long.MIN_VALUE;
    private int lastClientSyncHash = Integer.MIN_VALUE;
    private int thermalTicker = 0;
    private int overloadTimer = -1;
    @Nullable
    private PatternContainerGroup cachedCraftingMachineInfo;
    private int cachedCraftingMachineTier = Integer.MIN_VALUE;
    @Nullable
    private List<MultiblockProcessingRecipe> cachedAvailableRecipes;
    @Nullable
    private Map<ResourceLocation, MultiblockProcessingRecipe> cachedRecipeIndex;
    private long lastRecipeCacheRefreshTick = Long.MIN_VALUE;
    private ChemicalPortGroup<ResourceLocation> chemicalPorts = ChemicalPortGroup.empty();
    private FluidPortGroup<AEFluidKey> coolantPorts = FluidPortGroup.empty();
    private List<AENetworkedBlockEntity> networkNodeCandidates = List.of();
    @Nullable
    private CatalystProfile cachedCatalystProfile;

    protected AbstractParallelMultiblockControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.maxTemperature = THERMAL_MAX;
        for (int i = 0; i < MAX_PARALLEL_THREADS; i++) {
            this.processStates.add(new ParallelProcessState());
        }
    }

    @Override
    public void scanStructure(Level level) {
        super.scanStructure(level);
        rebuildChemicalPorts(level);
        rebuildCoolantPorts(level);
        rebuildNetworkNodeCandidates(level);
    }

    @Override
    public void markStructureDirty() {
        this.chemicalPorts = ChemicalPortGroup.empty();
        this.coolantPorts = FluidPortGroup.empty();
        this.networkNodeCandidates = List.of();
        super.markStructureDirty();
    }

    private void rebuildCoolantPorts(Level level) {
        if (!this.assembled) {
            this.coolantPorts = FluidPortGroup.empty();
            return;
        }
        List<FluidInputPort<AEFluidKey>> detectedPorts = new ArrayList<>();
        for (BlockPos partPos : this.parts) {
            if (level.getBlockEntity(partPos) instanceof MassiveOutputHatchBE hatch
                    && hatch.supportsFluidInput()) {
                detectedPorts.add(hatch);
            }
        }
        this.coolantPorts = new FluidPortGroup<>(detectedPorts);
    }

    public int getCoolantPortCount() {
        return this.coolantPorts.size();
    }

    @Override
    protected boolean validateMatchedStructure(Level level, MultiblockPattern.MatchResult result, Direction facing) {
        if (this instanceof QuantumCryoforgeControllerBE) {
            return true;
        }
        int patternHatches = 0;
        for (BlockPos partPos : result.partPositions()) {
            if (level.getBlockState(partPos).is(MultiblockBlocks.QUANTUM_PATTERN_HATCH.get())) {
                patternHatches++;
            }
        }
        return patternHatches == 1;
    }

    private void rebuildChemicalPorts(Level level) {
        if (!this.assembled) {
            this.chemicalPorts = ChemicalPortGroup.empty();
            return;
        }

        List<ChemicalPort<ResourceLocation>> detectedPorts = new ArrayList<>();
        for (BlockPos partPos : this.parts) {
            if (level.getBlockEntity(partPos) instanceof MekanismChemicalStorage storage
                    && storage.supportsChemicalIO()) {
                detectedPorts.add(storage);
            }
        }
        this.chemicalPorts = new ChemicalPortGroup<>(detectedPorts);
    }

    public int getChemicalPortCount() {
        return this.chemicalPorts.size();
    }

    private void rebuildNetworkNodeCandidates(Level level) {
        if (!this.assembled) {
            this.networkNodeCandidates = List.of();
            return;
        }
        List<AENetworkedBlockEntity> candidates = new ArrayList<>();
        for (BlockPos partPos : this.parts) {
            if (level.getBlockEntity(partPos) instanceof AENetworkedBlockEntity nodeBE) {
                candidates.add(nodeBE);
            }
        }
        this.networkNodeCandidates = List.copyOf(candidates);
    }

    public long getPendingPromisedOutputAmount() {
        long total = 0L;
        for (ParallelProcessState state : this.processStates) {
            for (GenericStack output : state.getPendingOutputs()) {
                total = saturatedAdd(total, output.amount());
            }
        }
        return total;
    }

    public long getPendingByproductAmount() {
        long total = 0L;
        for (ParallelProcessState state : this.processStates) {
            for (GenericStack byproduct : state.getPendingByproducts()) {
                total = saturatedAdd(total, byproduct.amount());
            }
        }
        return total;
    }

    @Override
    protected void machineTick() {
        if (!this.assembled || this.level == null) {
            updateRuntimeState(false, false, 0, 0, 0, 0);
            this.running = false;
            this.progress = 0;
            this.maxProgress = 0;
            this.storedEnergy = 0L;
            this.maxStoredEnergy = 0L;
            this.displayedRecipes.clear();
            updateTemperature(0, null, null, CatalystProfile.DEFAULT);
            syncClientState(false);
            return;
        }

        RecipeSnapshot recipes = getRecipeSnapshot();
        List<MultiblockProcessingRecipe> availableRecipes = recipes.recipes();
        Map<ResourceLocation, MultiblockProcessingRecipe> recipeIndex = recipes.index();

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null || nodeBE.getActionableNode() == null) {
            updateRuntimeState(true, false, getActiveProcessCount(), 0, countBlockedOutputs(), 0);
            this.running = false;
            this.progress = 0;
            this.maxProgress = 0;
            this.storedEnergy = 0L;
            this.maxStoredEnergy = 0L;
            updateTemperature(0, null, null, CatalystProfile.DEFAULT);
            if (shouldEvaluateClientState(true)) {
                rebuildDisplayedRecipes(recipeIndex);
                syncClientState(false);
            }
            return;
        }

        IGridNode node = nodeBE.getActionableNode();
        IGrid grid = node.getGrid();
        if (grid == null) {
            updateRuntimeState(true, false, getActiveProcessCount(), 0, countBlockedOutputs(), 0);
            this.running = false;
            this.progress = 0;
            this.maxProgress = 0;
            this.storedEnergy = 0L;
            this.maxStoredEnergy = 0L;
            updateTemperature(0, null, null, CatalystProfile.DEFAULT);
            if (shouldEvaluateClientState(true)) {
                rebuildDisplayedRecipes(recipeIndex);
                syncClientState(false);
            }
            return;
        }

        IEnergyService energyService = grid.getEnergyService();
        IStorageService storageService = grid.getStorageService();
        MEStorage inventory = storageService.getInventory();
        IActionSource src = IActionSource.ofMachine(nodeBE);
        refreshProcessStates(recipeIndex);
        CatalystProfile catalystProfile = getCatalystProfile();
        boolean persistentActivityBefore = hasPersistentRuntimeActivity();

        boolean anyRunning = false;
        int hottestMaxProgress = 0;
        int hottestProgress = 0;
        int runningThreads = 0;
        boolean thermalLocked = this.safeMode && this.temperature >= this.maxTemperature;
        int parallelLimit = getParallelThreadLimit();
        List<PreparedProcess> preparedProcesses = new ArrayList<>();
        List<ParallelProcessState> invalidProcesses = new ArrayList<>();

        for (ParallelProcessState processState : this.processStates) {
            if (!processState.isActive()) {
                continue;
            }

            if (processState.isOutputsPrepared()) {
                continue;
            }

            MultiblockProcessingRecipe recipe = recipeIndex.get(processState.getRecipeId());
            if (recipe == null) {
                invalidProcesses.add(processState);
                continue;
            }

            int scaledMaxProgress = getAdjustedProcessingTime(recipe, catalystProfile);
            if (scaledMaxProgress > hottestMaxProgress) {
                hottestMaxProgress = scaledMaxProgress;
                hottestProgress = processState.getProgress();
            }

            if (!MultiblockTierScaling.canRunRecipe(this.machineTier, recipe.requiredTier()) || thermalLocked) {
                continue;
            }

            if (processState.isPaused()) {
                continue;
            }

            long scaledEnergy = getAdjustedEnergyCost(recipe, catalystProfile);
            preparedProcesses.add(new PreparedProcess(processState, recipe, scaledEnergy, scaledMaxProgress));
        }

        cancelInvalidProcesses(invalidProcesses, inventory, energyService, src);

        KeyedTransferBatch<AEKey, IngredientTarget> inputBatch = new KeyedTransferBatch<>();
        Map<AEKey, Long> simulatedAvailability = new HashMap<>();
        int plannedRunningThreads = 0;
        for (PreparedProcess prepared : preparedProcesses) {
            if (plannedRunningThreads >= parallelLimit) {
                break;
            }
            ParallelProcessState processState = prepared.state();
            MultiblockProcessingRecipe recipe = prepared.recipe();
            processState.resizeBuffers(recipe.itemInputs().size(), recipe.fluidInputs().size(), recipe.chemicalInputs().size());
            chargeEnergy(processState, energyService, prepared.scaledEnergy());
            boolean materialsPlanned = planIngredientPulls(
                    processState, recipe, inventory, src, simulatedAvailability, inputBatch);
            if (materialsPlanned && processState.getEnergyBuffer() >= prepared.scaledEnergy()) {
                plannedRunningThreads++;
            }
        }
        applyIngredientPulls(inputBatch, inventory, src);

        for (PreparedProcess prepared : preparedProcesses) {
            if (runningThreads >= parallelLimit) {
                break;
            }
            ParallelProcessState processState = prepared.state();
            if (!hasAllIngredients(processState, prepared.recipe())
                    || processState.getEnergyBuffer() < prepared.scaledEnergy()) {
                continue;
            }

            runningThreads++;
            anyRunning = true;
            processState.setProgress(processState.getProgress() + getProgressPerTick());
            if (processState.getProgress() >= prepared.scaledMaxProgress()) {
                finishRecipe(processState, prepared.recipe());
            }
        }

        flushPendingOutputs(this.processStates, inventory, src);
        clearDeliveredProcesses();

        this.running = anyRunning;
        this.maxProgress = hottestMaxProgress;
        this.progress = hottestProgress;
        updateRuntimeState(true, true, getActiveProcessCount(), runningThreads,
                countBlockedOutputs(), countInvalidRecipes(recipeIndex), thermalLocked);
        updateTemperature(runningThreads, inventory, src, catalystProfile);
        if (persistentActivityBefore || hasPersistentRuntimeActivity()) {
            this.setChanged();
        }
        if (shouldEvaluateClientState(true)) {
            updateDisplayedEnergy(recipeIndex, catalystProfile);
            rebuildDisplayedRecipes(recipeIndex, catalystProfile);
            syncClientState(false);
        }
    }

    private Map<ResourceLocation, MultiblockProcessingRecipe> indexRecipes(List<MultiblockProcessingRecipe> availableRecipes) {
        Map<ResourceLocation, MultiblockProcessingRecipe> recipeIndex = new HashMap<>(availableRecipes.size());
        for (MultiblockProcessingRecipe recipe : availableRecipes) {
            recipeIndex.put(recipe.id(), recipe);
        }
        return recipeIndex;
    }

    private RecipeSnapshot getRecipeSnapshot() {
        long gameTime = this.level != null ? this.level.getGameTime() : 0L;
        if (this.cachedAvailableRecipes == null
                || this.cachedRecipeIndex == null
                || this.lastRecipeCacheRefreshTick == Long.MIN_VALUE
                || gameTime - this.lastRecipeCacheRefreshTick >= RECIPE_CACHE_REFRESH_TICKS) {
            refreshRecipeCache(gameTime);
        }
        return new RecipeSnapshot(this.cachedAvailableRecipes, this.cachedRecipeIndex);
    }

    private void refreshRecipeCache(long gameTime) {
        List<MultiblockProcessingRecipe> recipes = List.copyOf(getAvailableRecipes());
        this.cachedAvailableRecipes = recipes;
        this.cachedRecipeIndex = indexRecipes(recipes);
        this.lastRecipeCacheRefreshTick = gameTime;
    }

    private void invalidateRecipeCache() {
        this.cachedAvailableRecipes = null;
        this.cachedRecipeIndex = null;
        this.lastRecipeCacheRefreshTick = Long.MIN_VALUE;
    }

    private void refreshProcessStates(Map<ResourceLocation, MultiblockProcessingRecipe> recipeIndex) {
        for (ParallelProcessState state : this.processStates) {
            if (!state.isActive()) {
                continue;
            }

            MultiblockProcessingRecipe recipe = recipeIndex.get(state.getRecipeId());
            if (recipe == null) {
                continue;
            }

            state.resizeBuffers(recipe.itemInputs().size(), recipe.fluidInputs().size(), recipe.chemicalInputs().size());
            if (!state.isPatternPushed() && !state.hasBufferedWork()
                    && MultiblockTierScaling.canRunRecipe(this.machineTier, recipe.requiredTier())) {
                state.clear();
            }
        }
    }

    protected int getParallelThreadLimit() {
        return this.safeMode ? SAFE_MODE_PARALLEL_THREADS : MAX_PARALLEL_THREADS;
    }

    protected int getActiveProcessCount() {
        int count = 0;
        for (ParallelProcessState state : this.processStates) {
            if (state.isActive()) {
                count++;
            }
        }
        return count;
    }

    private int countBlockedOutputs() {
        int count = 0;
        for (ParallelProcessState state : this.processStates) {
            if (state.isOutputsPrepared() && state.hasPendingOutputs()) count++;
        }
        return count;
    }

    private int countInvalidRecipes(Map<ResourceLocation, MultiblockProcessingRecipe> recipeIndex) {
        int count = 0;
        for (ParallelProcessState state : this.processStates) {
            if (state.isActive() && state.getRecipeId() != null && !recipeIndex.containsKey(state.getRecipeId())) count++;
        }
        return count;
    }

    private void updateRuntimeState(boolean formed, boolean gridConnected, int active, int running,
                                    int blocked, int invalid) {
        updateRuntimeState(formed, gridConnected, active, running, blocked, invalid, false);
    }

    private void updateRuntimeState(boolean formed, boolean gridConnected, int active, int running,
                                    int blocked, int invalid, boolean overheated) {
        setRuntimeState(MultiblockRuntimeStateResolver.resolve(new MultiblockRuntimeStateResolver.Signals(
                formed, gridConnected, overheated, active, running, blocked, invalid)));
    }

    protected int getProgressPerTick() {
        return this.overclocked ? OVERCLOCK_SPEED_MULTIPLIER : 1;
    }

    protected double getHeatGenerationMultiplier() {
        return 1.0D;
    }

    private void chargeEnergy(ParallelProcessState state, IEnergyService energyService, long targetEnergy) {
        if (state.getEnergyBuffer() >= targetEnergy) {
            return;
        }
        long needed = targetEnergy - state.getEnergyBuffer();
        long chargeRate = 5_000_000L;
        double extracted = energyService.extractAEPower(Math.min(needed, chargeRate), Actionable.MODULATE, PowerMultiplier.CONFIG);
        state.setEnergyBuffer(state.getEnergyBuffer() + (long) extracted);
    }

    private boolean planIngredientPulls(ParallelProcessState state,
                                        MultiblockProcessingRecipe recipe,
                                        MEStorage inventory,
                                        IActionSource src,
                                        Map<AEKey, Long> simulatedAvailability,
                                        KeyedTransferBatch<AEKey, IngredientTarget> inputBatch) {
        boolean materialsFulfilled = true;

        for (int i = 0; i < recipe.itemInputs().size(); i++) {
            var requirement = recipe.itemInputs().get(i);
            if (state.getItemBuffers()[i] >= requirement.amount()) {
                continue;
            }
            long needed = requirement.amount() - state.getItemBuffers()[i];
            long toExtract = Math.min(needed, 100_000L);
            long planned = 0L;
            for (ItemStack match : requirement.ingredient().getItems()) {
                AEItemKey key = AEItemKey.of(match);
                long reserved = reserveSimulatedAvailability(
                        key, toExtract, inventory, src, simulatedAvailability);
                inputBatch.add(key, new IngredientTarget(state, BufferKind.ITEM, i), reserved);
                planned += reserved;
                toExtract -= reserved;
                if (toExtract <= 0) {
                    break;
                }
            }
            if (state.getItemBuffers()[i] + planned < requirement.amount()) {
                materialsFulfilled = false;
            }
        }

        for (int i = 0; i < recipe.fluidInputs().size(); i++) {
            var requirement = recipe.fluidInputs().get(i);
            if (state.getFluidBuffers()[i] >= requirement.amount()) {
                continue;
            }
            long needed = requirement.amount() - state.getFluidBuffers()[i];
            AEFluidKey key = AEFluidKey.of(requirement.fluid().getFluid());
            long reserved = reserveSimulatedAvailability(
                    key, Math.min(needed, 1_000_000L), inventory, src, simulatedAvailability);
            inputBatch.add(key, new IngredientTarget(state, BufferKind.FLUID, i), reserved);
            if (state.getFluidBuffers()[i] + reserved < requirement.amount()) {
                materialsFulfilled = false;
            }
        }

        for (int i = 0; i < recipe.chemicalInputs().size(); i++) {
            var requirement = recipe.chemicalInputs().get(i);
            if (state.getChemicalBuffers()[i] >= requirement.amount()) {
                continue;
            }
            long needed = requirement.amount() - state.getChemicalBuffers()[i];
            long extracted = extractChemicalFromHatches(requirement.chemicalId(), Math.min(needed, 1_000_000L));
            state.getChemicalBuffers()[i] += extracted;
            if (extracted > 0L) {
                AEKey key = MekanismChemicalCompat.createAeKey(requirement.chemicalId(), extracted);
                state.recordBufferedInput(key, extracted);
            }
            if (state.getChemicalBuffers()[i] < requirement.amount()) {
                materialsFulfilled = false;
            }
        }

        return materialsFulfilled;
    }

    private long reserveSimulatedAvailability(AEKey key,
                                              long requested,
                                              MEStorage inventory,
                                              IActionSource src,
                                              Map<AEKey, Long> simulatedAvailability) {
        if (requested <= 0L) {
            return 0L;
        }
        long available = simulatedAvailability.computeIfAbsent(key, ignored -> {
            long simulated = inventory.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, src);
            recordStorageOperation();
            return Math.max(0L, simulated);
        });
        long reserved = Math.min(requested, available);
        simulatedAvailability.put(key, available - reserved);
        return reserved;
    }

    private void applyIngredientPulls(KeyedTransferBatch<AEKey, IngredientTarget> inputBatch,
                                      MEStorage inventory,
                                      IActionSource src) {
        for (var allocation : inputBatch.execute((key, requested) -> {
            long extracted = inventory.extract(key, requested, Actionable.MODULATE, src);
            recordStorageOperation();
            return extracted;
        })) {
            IngredientTarget target = allocation.target();
            long[] buffers = target.kind() == BufferKind.ITEM
                    ? target.state().getItemBuffers()
                    : target.state().getFluidBuffers();
            buffers[target.index()] = saturatedAdd(buffers[target.index()], allocation.amount());
            target.state().recordBufferedInput(allocation.key(), allocation.amount());
        }
    }

    private boolean hasAllIngredients(ParallelProcessState state, MultiblockProcessingRecipe recipe) {
        for (int i = 0; i < recipe.itemInputs().size(); i++) {
            if (state.getItemBuffers()[i] < recipe.itemInputs().get(i).amount()) {
                return false;
            }
        }
        for (int i = 0; i < recipe.fluidInputs().size(); i++) {
            if (state.getFluidBuffers()[i] < recipe.fluidInputs().get(i).amount()) {
                return false;
            }
        }
        for (int i = 0; i < recipe.chemicalInputs().size(); i++) {
            if (state.getChemicalBuffers()[i] < recipe.chemicalInputs().get(i).amount()) {
                return false;
            }
        }
        return true;
    }

    private long extractChemicalFromHatches(ResourceLocation chemicalId, long amount) {
        if (amount <= 0L) {
            return 0L;
        }

        return this.chemicalPorts.extractTransactional(chemicalId, amount);
    }

    private void finishRecipe(ParallelProcessState state, MultiblockProcessingRecipe recipe) {
        if (!state.isOutputsPrepared()) {
            CatalystProfile catalystProfile = getCatalystProfile();
            List<GenericStack> outputs = new ArrayList<>();
            List<GenericStack> byproducts = new ArrayList<>();
            for (var output : recipe.outputs()) {
                if (!output.item().isEmpty()) {
                    prepareOutputAmounts(state, outputs, byproducts,
                            AEItemKey.of(output.item()), output.amount(), catalystProfile);
                }
                if (!output.fluid().isEmpty()) {
                    prepareOutputAmounts(state, outputs, byproducts,
                            AEFluidKey.of(output.fluid().getFluid()), output.amount(), catalystProfile);
                }
            }
            state.clearBuffers();
            state.setEnergyBuffer(0L);
            state.prepareOutputs(outputs, byproducts);
        }

    }

    private void prepareOutputAmounts(ParallelProcessState state,
                                      List<GenericStack> outputs,
                                      List<GenericStack> byproducts,
                                      AEKey key,
                                      long baseAmount,
                                      CatalystProfile catalystProfile) {
        if (state.getOutputPolicyVersion() < AutocraftingOutputPolicy.DETERMINISTIC_BASE) {
            long legacySafeAmount = AutocraftingOutputPolicy.legacySafeTotal(
                    baseAmount, LEGACY_MAXIMUM_BONUS_ROLLS);
            outputs.add(new GenericStack(key, legacySafeAmount));
            return;
        }

        AutocraftingOutputPolicy.OutputPlan plan = AutocraftingOutputPolicy.plan(
                baseAmount,
                catalystProfile.bonusDropChance(),
                () -> this.level != null ? this.level.random.nextDouble() : 1.0D);
        outputs.add(new GenericStack(key, plan.promisedAmount()));
        if (plan.byproductAmount() > 0L) {
            byproducts.add(new GenericStack(key, plan.byproductAmount()));
        }
    }

    private void flushPendingOutputs(List<ParallelProcessState> states, MEStorage inventory, IActionSource src) {
        KeyedTransferBatch<AEKey, OutputTarget> outputBatch = new KeyedTransferBatch<>();
        for (ParallelProcessState state : states) {
            if (!state.isActive() || !state.isOutputsPrepared()) {
                continue;
            }
            for (GenericStack output : state.getPendingOutputs()) {
                outputBatch.add(output.what(), new OutputTarget(state, false), output.amount());
            }
            for (GenericStack byproduct : state.getPendingByproducts()) {
                outputBatch.add(byproduct.what(), new OutputTarget(state, true), byproduct.amount());
            }
        }

        for (var allocation : outputBatch.execute((key, requested) -> {
            long inserted = inventory.insert(key, requested, Actionable.MODULATE, src);
            recordStorageOperation();
            return inserted;
        })) {
            if (allocation.target().byproduct()) {
                allocation.target().state().consumePendingByproduct(allocation.key(), allocation.amount());
            } else {
                allocation.target().state().consumePendingOutput(allocation.key(), allocation.amount());
            }
        }
    }

    private void clearDeliveredProcesses() {
        for (ParallelProcessState state : this.processStates) {
            if (state.isActive() && state.isOutputsPrepared() && !state.hasPendingOutputs()) {
                state.clear();
            }
        }
    }

    private void cancelInvalidProcesses(List<ParallelProcessState> states,
                                        MEStorage inventory,
                                        IEnergyService energyService,
                                        IActionSource src) {
        if (states.isEmpty()) {
            return;
        }
        Map<ParallelProcessState, Boolean> hadTrackedInputs = new HashMap<>();
        for (ParallelProcessState state : states) {
            hadTrackedInputs.put(state, state.hasTrackedInputs());
        }
        refundTrackedInputs(states, inventory, src);
        for (ParallelProcessState state : states) {
            refundEnergy(state, energyService);
            if (state.hasTrackedInputs() || state.getEnergyBuffer() > 0L) {
                continue;
            }
            if (!hadTrackedInputs.get(state) && state.hasLegacyMaterialBuffers()) {
                continue;
            }
            state.clear();
        }
    }

    private void refundTrackedInputs(List<ParallelProcessState> states, MEStorage inventory, IActionSource src) {
        KeyedTransferBatch<AEKey, ParallelProcessState> refundBatch = new KeyedTransferBatch<>();
        for (ParallelProcessState state : states) {
            for (GenericStack input : state.getBufferedInputs()) {
                refundBatch.add(input.what(), state, input.amount());
            }
        }
        for (var allocation : refundBatch.execute((key, requested) -> {
            long inserted = inventory.insert(key, requested, Actionable.MODULATE, src);
            recordStorageOperation();
            return inserted;
        })) {
            allocation.target().consumeBufferedInput(allocation.key(), allocation.amount());
        }

        for (ParallelProcessState state : states) {
            for (GenericStack input : state.getBufferedInputs()) {
                ResourceLocation chemicalId = MekanismChemicalCompat.getChemicalId(input.what());
                if (chemicalId != null) {
                    long inserted = insertChemicalIntoHatches(chemicalId, input.amount());
                    state.consumeBufferedInput(input.what(), inserted);
                }
            }
        }
    }

    private void refundEnergy(ParallelProcessState state, IEnergyService energyService) {
        long buffered = state.getEnergyBuffer();
        if (buffered <= 0L) {
            return;
        }
        double overflow = energyService.injectPower(buffered, Actionable.MODULATE);
        long remaining = overflow <= 0.0D ? 0L : Math.min(buffered, (long) Math.ceil(overflow));
        state.setEnergyBuffer(remaining);
    }

    private long insertChemicalIntoHatches(ResourceLocation chemicalId, long amount) {
        if (amount <= 0L) {
            return 0L;
        }

        return this.chemicalPorts.insertTransactional(chemicalId, amount);
    }

    private void rebuildDisplayedRecipes(Map<ResourceLocation, MultiblockProcessingRecipe> recipeIndex) {
        rebuildDisplayedRecipes(recipeIndex, getCatalystProfile());
    }

    private void rebuildDisplayedRecipes(Map<ResourceLocation, MultiblockProcessingRecipe> recipeIndex, CatalystProfile catalystProfile) {
        this.displayedRecipes.clear();
        for (int processIndex = 0; processIndex < this.processStates.size(); processIndex++) {
            ParallelProcessState processState = this.processStates.get(processIndex);
            if (!processState.isActive()) {
                continue;
            }
            MultiblockProcessingRecipe recipe = recipeIndex.get(processState.getRecipeId());
            if (recipe == null) {
                continue;
            }
            var primaryOutput = recipe.primaryOutput();
            int scaledMaxProgress = getAdjustedProcessingTime(recipe, catalystProfile);
            int displayedMaxProgress = getDisplayedTicks(scaledMaxProgress);
            int displayedProgress = Math.min(displayedMaxProgress, getDisplayedTicks(processState.getProgress()));
            Component label = primaryOutput.item().isEmpty()
                    ? (primaryOutput.fluid().isEmpty() ? Component.literal(recipe.name()) : primaryOutput.fluid().getHoverName())
                    : primaryOutput.item().getHoverName();
            if (!MultiblockTierScaling.canRunRecipe(this.machineTier, recipe.requiredTier())) {
                label = label.copy().append(Component.literal(" [Locked: MK" + recipe.requiredTier() + "]"));
            }
            this.displayedRecipes.add(new UniversalDisplayedRecipe(
                    primaryOutput.item(),
                    primaryOutput.fluid(),
                    label,
                    primaryOutput.item().isEmpty() ? primaryOutput.amount() : getMaximumAdjustedItemOutputAmount(primaryOutput.amount(), catalystProfile),
                    displayedProgress,
                    displayedMaxProgress,
                    processIndex,
                    processState.isPaused()));
        }
    }

    @Override
    public void toggleProcessPaused(int processIndex) {
        if (processIndex < 0 || processIndex >= this.processStates.size()) {
            return;
        }
        ParallelProcessState processState = this.processStates.get(processIndex);
        if (!processState.isActive()) {
            return;
        }
        processState.togglePaused();
        rebuildDisplayedRecipes(getRecipeSnapshot().index());
        this.setChanged();
        syncClientState(false);
    }

    protected int getDisplayedTicks(int rawTicks) {
        int divisor = getProgressPerTick();
        return Math.max(0, (rawTicks + divisor - 1) / divisor);
    }

    private void updateDisplayedEnergy(Map<ResourceLocation, MultiblockProcessingRecipe> recipeIndex, CatalystProfile catalystProfile) {
        long bufferedEnergy = 0L;
        long targetEnergy = 0L;
        for (ParallelProcessState processState : this.processStates) {
            if (!processState.isActive()) {
                continue;
            }

            bufferedEnergy += Math.max(0L, processState.getEnergyBuffer());
            MultiblockProcessingRecipe recipe = recipeIndex.get(processState.getRecipeId());
            if (recipe != null) {
                targetEnergy += Math.max(0L, getAdjustedEnergyCost(recipe, catalystProfile));
            }
        }

        this.storedEnergy = bufferedEnergy;
        this.maxStoredEnergy = targetEnergy;
    }

    private void updateTemperature(int activeThreads, @Nullable MEStorage inventory, @Nullable IActionSource src, CatalystProfile catalystProfile) {
        this.thermalTicker++;

        if (catalystProfile.creative()) {
            if (this.temperature > 0 && inventory != null && src != null) {
                this.temperature -= consumeCoolant(inventory, src);
            }
        } else if (activeThreads > 0) {
            if (this.thermalTicker % 2 == 0) {
                int baseHeat = Math.max(1, activeThreads) * (this.overclocked ? 5 : 1);
                int heatToAdd = Math.max(0, (int) Math.ceil(baseHeat * getHeatGenerationMultiplier() * catalystProfile.heatMultiplier()));
                this.temperature = Math.min(this.maxTemperature, this.temperature + heatToAdd);
            }
        } else if (this.temperature > 0 && this.thermalTicker % 40 == 0) {
            this.temperature -= 1;
        }

        if (this.temperature > 0 && inventory != null && src != null) {
            this.temperature -= consumeCoolant(inventory, src);
        }

        if (this.temperature < 0) {
            this.temperature = 0;
        }

        if (this.safeMode) {
            this.overloadTimer = -1;
            return;
        }

        if (this.temperature >= this.maxTemperature) {
            if (this.overloadTimer == -1) {
                this.overloadTimer = OVERLOAD_TICKS;
            }
        } else {
            this.overloadTimer = -1;
        }

        if (this.overloadTimer > 0) {
            if (this.level != null && this.overloadTimer % 20 == 0) {
                this.level.playSound(null, this.worldPosition, ModSounds.DMA_ALARM.get(),
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 0.8f);
            }

            this.overloadTimer--;
            if (this.overloadTimer == 0) {
                triggerThermalExplosion();
            }
        }
    }

    private int consumeCoolant(MEStorage inventory, IActionSource src) {
        for (AEFluidKey coolantKey : getCoolantPriority()) {
            if (coolantKey == null || coolantKey.getFluid() == Fluids.EMPTY) {
                continue;
            }

            ThermalSystem.CoolantProfile profile = getCoolantProfile(coolantKey.getFluid());
            long simulatedAvailable = this.coolantPorts.extract(coolantKey, profile.maxFlowPerTick(), true);
            if (simulatedAvailable < profile.maxFlowPerTick()) {
                long fallback = inventory.extract(coolantKey,
                        profile.maxFlowPerTick() - simulatedAvailable, Actionable.SIMULATE, src);
                recordStorageOperation();
                simulatedAvailable = saturatedAdd(simulatedAvailable, Math.max(0L, fallback));
            }
            if (simulatedAvailable <= 0L) {
                continue;
            }

            ThermalSystem.CoolingPlan plan = ThermalSystem.planCooling(
                    this.temperature, simulatedAvailable, profile);
            if (plan.requestedMillibuckets() <= 0L || plan.heatRemoved() <= 0L) {
                continue;
            }

            long extracted = this.coolantPorts.extract(coolantKey, plan.requestedMillibuckets(), false);
            if (extracted < plan.requestedMillibuckets()) {
                long fallback = inventory.extract(coolantKey,
                        plan.requestedMillibuckets() - extracted, Actionable.MODULATE, src);
                recordStorageOperation();
                extracted = saturatedAdd(extracted, Math.max(0L, fallback));
            }
            if (extracted <= 0L) {
                continue;
            }

            return (int) Math.min(
                    this.temperature, ThermalSystem.coolingFromExtracted(extracted, profile));
        }

        return 0;
    }

    private AEFluidKey[] getCoolantPriority() {
        if (this.coolantPriorityByTier == null) {
            AEFluidKey tier1 = AEFluidKey.of(ModFluids.SOURCE_GELID_CRYOTHEUM.get());
            AEFluidKey tier2 = AEFluidKey.of(ModFluids.SOURCE_STABLE_COOLANT.get());
            AEFluidKey tier3 = AEFluidKey.of(ModFluids.SOURCE_TEMPORAL_FLUID.get());
            this.coolantPriorityByTier = new AEFluidKey[][]{
                    {tier1, tier2, tier3},
                    {tier1, tier2, tier3},
                    {tier2, tier3, tier1},
                    {tier3, tier2, tier1}
            };
        }
        return this.coolantPriorityByTier[Math.max(1, Math.min(3, this.machineTier))];
    }

    private ThermalSystem.CoolantProfile getCoolantProfile(Fluid fluid) {
        if (fluid == ModFluids.SOURCE_TEMPORAL_FLUID.get() || fluid == ModFluids.FLOWING_TEMPORAL_FLUID.get()) {
            return TEMPORAL_COOLANT_PROFILE;
        }
        if (fluid == ModFluids.SOURCE_STABLE_COOLANT.get() || fluid == ModFluids.FLOWING_STABLE_COOLANT.get()) {
            return STABLE_COOLANT_PROFILE;
        }
        if (fluid == ModFluids.SOURCE_GELID_CRYOTHEUM.get() || fluid == ModFluids.FLOWING_GELID_CRYOTHEUM.get()) {
            return GELID_COOLANT_PROFILE;
        }
        return FALLBACK_COOLANT_PROFILE;
    }

    private void triggerThermalExplosion() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        Level level = this.level;
        level.explode(null,
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5,
                THERMAL_EXPLOSION_POWER,
                Level.ExplosionInteraction.BLOCK);
        onControllerBroken();
        removeControllerBlockAfterExplosion();
        this.temperature = 0;
        this.overloadTimer = -1;
        this.running = false;
        this.progress = 0;
        this.maxProgress = 0;
        updateDisplayedEnergy(Map.of(), CatalystProfile.DEFAULT);
        this.displayedRecipes.clear();
        saveChanges();
    }

    private void removeControllerBlockAfterExplosion() {
        if (this.level == null) {
            return;
        }

        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return;
        }

        this.level.removeBlock(this.worldPosition, false);
    }

    private AENetworkedBlockEntity getConnectedNetworkNode() {
        if (this.level == null) {
            return null;
        }
        for (AENetworkedBlockEntity nodeBE : this.networkNodeCandidates) {
            if (nodeBE.isRemoved()) {
                continue;
            }
            IGridNode node = nodeBE.getActionableNode();
            if (Ae2NodeAvailability.isUsable(
                    node != null,
                    node != null && node.getGrid() != null,
                    node != null && node.isActive(),
                    node != null && node.isPowered())) {
                return nodeBE;
            }
        }
        return null;
    }

    private CatalystProfile getCatalystProfile() {
        if (this.cachedCatalystProfile != null) {
            return this.cachedCatalystProfile;
        }

        double heatMultiplier = 1.0D;
        double speedMultiplier = 1.0D;
        double energyMultiplier = 1.0D;
        double bonusDropChance = 0.0D;
        boolean creative = false;
        int identicalCount = 0;
        BaseCatalystItem firstCatalyst = null;
        boolean synergyPossible = true;

        for (int i = 0; i < this.upgrades.size(); i++) {
            ItemStack upgradeStack = this.upgrades.getStackInSlot(i);
            if (upgradeStack.isEmpty()) {
                synergyPossible = false;
                continue;
            }

            if (upgradeStack.getItem() instanceof DimensionalCatalystItem) {
                creative = true;
                synergyPossible = false;
                continue;
            }

            if (upgradeStack.getItem() instanceof BaseCatalystItem catalyst) {
                heatMultiplier += catalyst.getStaticHeat() / 100.0D;
                speedMultiplier *= catalyst.getSpeedMultiplier();
                energyMultiplier *= catalyst.getPowerMultiplier();
                bonusDropChance += catalyst.getBonusDropChance();

                if (firstCatalyst == null) {
                    firstCatalyst = catalyst;
                    identicalCount++;
                } else if (firstCatalyst == catalyst) {
                    identicalCount++;
                } else {
                    synergyPossible = false;
                }
                continue;
            }

            synergyPossible = false;
        }

        if (synergyPossible && identicalCount == 4 && firstCatalyst != null) {
            heatMultiplier *= 1.5D;
            if ("chrono".equals(firstCatalyst.getFamily())) {
                speedMultiplier *= 2.0D;
            } else if ("matterflow".equals(firstCatalyst.getFamily())) {
                energyMultiplier *= 0.5D;
            } else if ("quantum".equals(firstCatalyst.getFamily())) {
                bonusDropChance += 0.5D;
            } else if ("overflux".equals(firstCatalyst.getFamily())) {
                heatMultiplier *= 0.5D;
            }
        }

        if (creative) {
            this.cachedCatalystProfile = CatalystProfile.CREATIVE;
            return this.cachedCatalystProfile;
        }

        this.cachedCatalystProfile = new CatalystProfile(
                false,
                Math.max(0.0D, heatMultiplier),
                Math.max(0.01D, speedMultiplier),
                Math.max(0.0D, energyMultiplier),
                Math.max(0.0D, bonusDropChance));
        return this.cachedCatalystProfile;
    }

    @Override
    public void saveChanges() {
        this.cachedCatalystProfile = null;
        super.saveChanges();
    }

    private int getAdjustedProcessingTime(MultiblockProcessingRecipe recipe, CatalystProfile catalystProfile) {
        if (catalystProfile.creative()) {
            return 1;
        }
        int tierAdjustedTime = MultiblockTierScaling.adjustedTime(recipe.time(), this.machineTier, recipe.requiredTier());
        return Math.max(1, (int) Math.ceil(tierAdjustedTime / catalystProfile.speedMultiplier()));
    }

    private long getAdjustedEnergyCost(MultiblockProcessingRecipe recipe, CatalystProfile catalystProfile) {
        if (catalystProfile.creative()) {
            return 0L;
        }
        long tierAdjustedEnergy = MultiblockTierScaling.adjustedEnergy(recipe.energy(), this.machineTier, recipe.requiredTier());
        return Math.max(1L, (long) Math.ceil(tierAdjustedEnergy * catalystProfile.energyMultiplier()));
    }

    private long getMaximumAdjustedItemOutputAmount(long baseAmount, CatalystProfile catalystProfile) {
        return AutocraftingOutputPolicy.maximumTotal(baseAmount, catalystProfile.bonusDropChance());
    }

    protected abstract List<MultiblockProcessingRecipe> getAvailableRecipes();

    protected MultiblockProcessingRecipe findRecipe(List<MultiblockProcessingRecipe> availableRecipes, ResourceLocation recipeId) {
        for (MultiblockProcessingRecipe recipe : availableRecipes) {
            if (recipe.id().equals(recipeId)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        if (this.cachedCraftingMachineInfo == null || this.cachedCraftingMachineTier != this.machineTier) {
            this.cachedCraftingMachineTier = this.machineTier;
            this.cachedCraftingMachineInfo = new PatternContainerGroup(
                    AEItemKey.of(this.getBlockState().getBlock().asItem()),
                    Component.translatable(getControllerTranslationKey()),
                    List.of(Component.literal("MK" + this.machineTier)));
        }
        return this.cachedCraftingMachineInfo;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs, net.minecraft.core.Direction ejectionDirection) {
        if (!this.assembled) {
            return false;
        }

        if (getActiveProcessCount() >= getParallelThreadLimit()) {
            return false;
        }

        MultiblockProcessingRecipe recipe = resolvePatternRecipe(patternDetails, inputs);
        if (recipe == null || !MultiblockTierScaling.canRunRecipe(this.machineTier, recipe.requiredTier())) {
            return false;
        }

        ParallelProcessState state = findInactiveState();
        if (state == null) {
            return false;
        }

        state.clear();
        state.setRecipeId(recipe.id());
        state.setPatternPushed(true);
        state.setOutputPolicyVersion(AutocraftingOutputPolicy.DETERMINISTIC_BASE);
        state.resizeBuffers(recipe.itemInputs().size(), recipe.fluidInputs().size(), recipe.chemicalInputs().size());
        // Item ownership remains with the processing-pattern delivery. Populate
        // from the exact counters AE2 handed to this job; an item port must not
        // charge the same recipe requirements a second time.
        populatePatternItemBuffers(state, recipe, flattenInputs(inputs));
        for (int i = 0; i < recipe.fluidInputs().size(); i++) {
            state.getFluidBuffers()[i] = recipe.fluidInputs().get(i).amount();
        }
        populatePatternChemicalBuffers(state, recipe, flattenInputs(inputs));
        state.setEnergyBuffer(0L);
        state.setProgress(0);

        for (PatternStack input : flattenInputs(inputs)) {
            state.recordBufferedInput(input.key(), input.amount());
        }
        for (KeyCounter input : inputs) {
            input.clear();
        }

        rebuildDisplayedRecipes(getRecipeSnapshot().index());
        saveChanges();
        return true;
    }

    @Override
    public boolean acceptsPlans() {
        return this.assembled
                && getActiveProcessCount() < getParallelThreadLimit()
                && findInactiveState() != null;
    }

    private ParallelProcessState findInactiveState() {
        for (ParallelProcessState state : this.processStates) {
            if (!state.isActive()) {
                return state;
            }
        }
        return null;
    }

    private MultiblockProcessingRecipe resolvePatternRecipe(IPatternDetails patternDetails, KeyCounter[] inputs) {
        List<MultiblockProcessingRecipe> outputMatches = new ArrayList<>();
        for (MultiblockProcessingRecipe recipe : getRecipeSnapshot().recipes()) {
            if (MultiblockTierScaling.canRunRecipe(this.machineTier, recipe.requiredTier())
                    && patternMatchesOutputs(patternDetails.getOutputs(), recipe.outputs())) {
                outputMatches.add(recipe);
            }
        }

        if (outputMatches.isEmpty()) {
            return null;
        }

        for (MultiblockProcessingRecipe recipe : outputMatches) {
            if (patternMatchesInputs(inputs, recipe)) {
                return recipe;
            }
        }

        return null;
    }

    private boolean patternMatchesInputs(KeyCounter[] inputs, MultiblockProcessingRecipe recipe) {
        List<PatternStack> availableStacks = flattenInputs(inputs);
        if (availableStacks.isEmpty() && (!recipe.itemInputs().isEmpty() || !recipe.fluidInputs().isEmpty())) {
            return false;
        }

        List<PatternStack> remaining = new ArrayList<>(availableStacks);
        for (var requirement : recipe.itemInputs()) {
            if (!removeMatchingItemRequirement(remaining, requirement)) {
                return false;
            }
        }
        for (var requirement : recipe.fluidInputs()) {
            if (!removeMatchingFluidRequirement(remaining, requirement)) {
                return false;
            }
        }
        // AE2 may deliver chemicals encoded in the processing pattern. They are
        // optional here because a formed ChemicalPort can supply any missing
        // amount. Wrong chemicals and amounts above the recipe requirement stay
        // in remaining and make the pattern fail closed.
        for (var requirement : recipe.chemicalInputs()) {
            consumeMatchingChemicalRequirement(remaining, requirement);
        }
        return remaining.isEmpty();
    }

    private void populatePatternChemicalBuffers(ParallelProcessState state,
                                                MultiblockProcessingRecipe recipe,
                                                List<PatternStack> deliveredInputs) {
        List<PatternStack> remaining = new ArrayList<>(deliveredInputs);
        for (int i = 0; i < recipe.chemicalInputs().size(); i++) {
            state.getChemicalBuffers()[i] = consumeMatchingChemicalRequirement(
                    remaining, recipe.chemicalInputs().get(i));
        }
    }

    private void populatePatternItemBuffers(ParallelProcessState state,
                                            MultiblockProcessingRecipe recipe,
                                            List<PatternStack> deliveredInputs) {
        List<PatternStack> remaining = new ArrayList<>(deliveredInputs);
        for (int i = 0; i < recipe.itemInputs().size(); i++) {
            state.getItemBuffers()[i] = consumeMatchingItemRequirement(
                    remaining, recipe.itemInputs().get(i));
        }
    }

    private long consumeMatchingChemicalRequirement(List<PatternStack> remaining,
                                                     MultiblockProcessingRecipe.ChemicalRequirement requirement) {
        for (int i = 0; i < remaining.size(); i++) {
            PatternStack stack = remaining.get(i);
            ResourceLocation chemicalId = MekanismChemicalCompat.getChemicalId(stack.key);
            if (chemicalId == null || !chemicalId.equals(requirement.chemicalId())) {
                continue;
            }

            long consumed = Math.min(stack.amount, requirement.amount());
            long leftover = stack.amount - consumed;
            if (leftover > 0L) {
                remaining.set(i, new PatternStack(stack.key, leftover));
            } else {
                remaining.remove(i);
            }
            return consumed;
        }
        return 0L;
    }

    private boolean patternMatchesOutputs(List<GenericStack> outputs, List<MultiblockProcessingRecipe.OutputStack> recipeOutputs) {
        if (outputs.size() != recipeOutputs.size()) {
            return false;
        }

        List<PatternStack> remaining = new ArrayList<>();
        for (GenericStack output : outputs) {
            remaining.add(new PatternStack(output.what(), output.amount()));
        }

        for (var output : recipeOutputs) {
            AEKey expectedKey = !output.item().isEmpty()
                    ? AEItemKey.of(output.item())
                    : AEFluidKey.of(output.fluid().getFluid());
            if (expectedKey == null) {
                return false;
            }

            boolean matched = false;
            for (int i = 0; i < remaining.size(); i++) {
                PatternStack candidate = remaining.get(i);
                if (candidate.key.equals(expectedKey) && patternOutputAmountMatches(candidate.amount, output.amount())) {
                    remaining.remove(i);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }

        return remaining.isEmpty();
    }

    private boolean patternOutputAmountMatches(long patternAmount, long baseAmount) {
        return AutocraftingOutputPolicy.matchesDeterministicPromise(patternAmount, baseAmount);
    }

    private List<PatternStack> flattenInputs(KeyCounter[] inputs) {
        Map<AEKey, Long> totals = new LinkedHashMap<>();
        for (KeyCounter counter : inputs) {
            for (var entry : counter) {
                totals.merge(entry.getKey(), entry.getLongValue(), this::saturatedAdd);
            }
        }
        List<PatternStack> stacks = new ArrayList<>(totals.size());
        totals.forEach((key, amount) -> stacks.add(new PatternStack(key, amount)));
        return stacks;
    }

    private long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private boolean removeMatchingItemRequirement(List<PatternStack> remaining, MultiblockProcessingRecipe.ItemRequirement requirement) {
        return consumeMatchingItemRequirement(remaining, requirement) == requirement.amount();
    }

    private long consumeMatchingItemRequirement(List<PatternStack> remaining, MultiblockProcessingRecipe.ItemRequirement requirement) {
        for (int i = 0; i < remaining.size(); i++) {
            PatternStack stack = remaining.get(i);
            if (stack.key instanceof AEItemKey itemKey
                    && stack.amount >= requirement.amount()
                    && requirement.ingredient().test(itemKey.toStack(1))) {
                long leftover = stack.amount - requirement.amount();
                if (leftover > 0L) {
                    remaining.set(i, new PatternStack(stack.key, leftover));
                } else {
                    remaining.remove(i);
                }
                return requirement.amount();
            }
        }
        return 0L;
    }

    private boolean removeMatchingFluidRequirement(List<PatternStack> remaining, MultiblockProcessingRecipe.FluidRequirement requirement) {
        for (int i = 0; i < remaining.size(); i++) {
            PatternStack stack = remaining.get(i);
            if (stack.key instanceof AEFluidKey fluidKey
                    && stack.amount >= requirement.amount()
                    && fluidKey.getFluid() == requirement.fluid().getFluid()) {
                long leftover = stack.amount - requirement.amount();
                if (leftover > 0L) {
                    remaining.set(i, new PatternStack(stack.key, leftover));
                } else {
                    remaining.remove(i);
                }
                return true;
            }
        }
        return false;
    }

    private record PatternStack(AEKey key, long amount) {
    }

    private enum BufferKind {
        ITEM,
        FLUID
    }

    private record IngredientTarget(
            ParallelProcessState state,
            BufferKind kind,
            int index) {
    }

    private record OutputTarget(
            ParallelProcessState state,
            boolean byproduct) {
    }

    private record PreparedProcess(
            ParallelProcessState state,
            MultiblockProcessingRecipe recipe,
            long scaledEnergy,
            int scaledMaxProgress) {
    }

    private record RecipeSnapshot(
            List<MultiblockProcessingRecipe> recipes,
            Map<ResourceLocation, MultiblockProcessingRecipe> index) {
    }

    private record CatalystProfile(
            boolean creative,
            double heatMultiplier,
            double speedMultiplier,
            double energyMultiplier,
            double bonusDropChance) {
        private static final CatalystProfile DEFAULT = new CatalystProfile(false, 1.0D, 1.0D, 1.0D, 0.0D);
        private static final CatalystProfile CREATIVE = new CatalystProfile(true, 0.0D, 1000.0D, 0.0D, 1.0D);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag processTags = new ListTag();
        for (ParallelProcessState state : this.processStates) {
            processTags.add(state.save(registries));
        }
        tag.put("processStates", processTags);
        tag.putInt("thermalTicker", this.thermalTicker);
        tag.putInt("overloadTimer", this.overloadTimer);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("processStates", Tag.TAG_LIST)) {
            ListTag processTags = tag.getList("processStates", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(processTags.size(), this.processStates.size()); i++) {
                this.processStates.get(i).load(processTags.getCompound(i), registries);
            }
        }
        this.thermalTicker = tag.getInt("thermalTicker");
        this.overloadTimer = tag.contains("overloadTimer") ? tag.getInt("overloadTimer") : -1;
    }

    @Override
    public void onControllerBroken() {
        recoverProcessesBeforeRemoval();
        super.onControllerBroken();
        this.chemicalPorts = ChemicalPortGroup.empty();
        this.coolantPorts = FluidPortGroup.empty();
        this.networkNodeCandidates = List.of();
        this.cachedCatalystProfile = null;
        invalidateRecipeCache();
        for (ParallelProcessState processState : this.processStates) {
            processState.clear();
        }
        this.storedEnergy = 0L;
        this.maxStoredEnergy = 0L;
        this.overloadTimer = -1;
    }

    private void recoverProcessesBeforeRemoval() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        IGridNode node = nodeBE != null ? nodeBE.getActionableNode() : null;
        IGrid grid = node != null ? node.getGrid() : null;
        MEStorage inventory = grid != null ? grid.getStorageService().getInventory() : null;
        IEnergyService energyService = grid != null ? grid.getEnergyService() : null;
        IActionSource src = nodeBE != null ? IActionSource.ofMachine(nodeBE) : IActionSource.empty();

        List<ParallelProcessState> activeStates = this.processStates.stream()
                .filter(ParallelProcessState::isActive)
                .toList();
        if (inventory != null) {
            flushPendingOutputs(activeStates, inventory, src);
            refundTrackedInputs(activeStates, inventory, src);
        }

        for (ParallelProcessState state : activeStates) {
            if (!state.isActive()) {
                continue;
            }
            if (energyService != null) {
                refundEnergy(state, energyService);
            }

            dropRecoveryStacks(state.getBufferedInputs());
            dropRecoveryStacks(state.getPendingOutputs());
            dropRecoveryStacks(state.getPendingByproducts());
            if (!state.hasTrackedInputs() && !state.isOutputsPrepared() && state.hasLegacyMaterialBuffers()) {
                UfoMod.LOGGER.warn("Could not recover legacy untracked process buffers while removing controller at {}", this.worldPosition);
            }
            state.clear();
        }
    }

    private void dropRecoveryStacks(List<GenericStack> stacks) {
        if (this.level == null) {
            return;
        }
        for (GenericStack stack : stacks) {
            if (stack.amount() <= 0L) {
                continue;
            }
            Containers.dropItemStack(
                    this.level,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D,
                    GenericStack.wrapInItemStack(stack));
        }
    }

    @Override
    protected int resolveMachineTier(com.raishxn.ufo.api.multiblock.MultiblockPattern.MatchResult result) {
        if (this.level == null) {
            return com.raishxn.ufo.api.multiblock.MultiblockMachineTier.MK1.level();
        }

        BlockState controllerState = this.level.getBlockState(this.worldPosition);
        Direction facing = MultiblockControllerDefinitions.getPatternFacing(this, controllerState);

        int resolvedTier = com.raishxn.ufo.api.multiblock.MultiblockMachineTier.MK3.level();
        boolean foundField = false;
        for (BlockPos fieldPos : getControllerPattern().getExpectedPositions(this.worldPosition, facing, 'F')) {
            BlockState fieldState = this.level.getBlockState(fieldPos);
            if (fieldState.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get())) {
                resolvedTier = Math.min(resolvedTier, 1);
                foundField = true;
            } else if (fieldState.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get())) {
                resolvedTier = Math.min(resolvedTier, 2);
                foundField = true;
            } else if (fieldState.is(MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get())) {
                foundField = true;
            }
        }

        return foundField ? resolvedTier : com.raishxn.ufo.api.multiblock.MultiblockMachineTier.MK1.level();
    }

    @Override
    protected boolean hasOngoingWork() {
        for (ParallelProcessState state : this.processStates) {
            if (state.isActive()) {
                return true;
            }
        }
        return super.hasOngoingWork();
    }

    @Override
    public int getGuiActiveParallels() {
        return getActiveProcessCount();
    }

    @Override
    public int getGuiMaxParallels() {
        return getParallelThreadLimit();
    }

    private void syncClientState(boolean throttle) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        if (!shouldEvaluateClientState(throttle)) {
            return;
        }

        long gameTime = this.level.getGameTime();
        this.lastClientSyncEvaluationTick = gameTime;

        int syncHash = computeClientSyncHash();
        if (syncHash == this.lastClientSyncHash) {
            return;
        }

        this.lastClientSyncHash = syncHash;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
    }

    private boolean shouldEvaluateClientState(boolean throttle) {
        return this.level != null && ParallelRuntimeCadence.shouldEvaluateClientState(
                this.level.getGameTime(), this.lastClientSyncEvaluationTick, throttle);
    }

    private boolean hasPersistentRuntimeActivity() {
        return ParallelRuntimeCadence.hasPersistentActivity(
                getActiveProcessCount(), this.temperature, this.overloadTimer)
                || this.running
                || this.progress > 0
                || this.maxProgress > 0
                || this.storedEnergy > 0L
                || this.maxStoredEnergy > 0L;
    }

    private void recordStorageOperation() {
        if (this.level != null && !this.level.isClientSide()) {
            com.raishxn.ufo.diagnostic.MachinePerformanceRegistry.INSTANCE.recordStorageOperation(
                    performanceMetricKey(), this.level.getGameTime());
        }
    }

    private int computeClientSyncHash() {
        int hash = Boolean.hashCode(this.assembled);
        hash = 31 * hash + Boolean.hashCode(this.running);
        hash = 31 * hash + this.progress;
        hash = 31 * hash + this.maxProgress;
        hash = 31 * hash + this.temperature;
        hash = 31 * hash + this.machineTier;
        hash = 31 * hash + Boolean.hashCode(this.safeMode);
        hash = 31 * hash + Boolean.hashCode(this.overclocked);
        hash = 31 * hash + Long.hashCode(this.storedEnergy);
        hash = 31 * hash + Long.hashCode(this.maxStoredEnergy);
        hash = 31 * hash + getActiveProcessCount();
        hash = 31 * hash + computeDisplayedRecipesHash();
        return hash;
    }

    private int computeDisplayedRecipesHash() {
        int hash = 1;
        for (UniversalDisplayedRecipe recipe : this.displayedRecipes) {
            hash = 31 * hash + recipe.label().getString().hashCode();
            hash = 31 * hash + recipe.progress();
            hash = 31 * hash + recipe.maxProgress();
            hash = 31 * hash + recipe.processIndex();
            hash = 31 * hash + Boolean.hashCode(recipe.paused());
            hash = 31 * hash + Long.hashCode(recipe.outputAmount());
            hash = 31 * hash + java.util.Objects.hashCode(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(recipe.itemIcon().getItem()));
            hash = 31 * hash + java.util.Objects.hashCode(net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(recipe.fluidIcon().getFluid()));
        }
        return hash;
    }
}

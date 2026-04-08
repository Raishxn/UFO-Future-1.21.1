package com.raishxn.ufo.block.entity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import com.raishxn.ufo.api.multiblock.MultiblockTierScaling;
import com.raishxn.ufo.block.entity.processing.MultiblockProcessingRecipe;
import com.raishxn.ufo.block.entity.processing.ParallelProcessState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParallelMultiblockControllerBE extends AbstractSimpleMultiblockControllerBE {
    protected static final int MAX_PARALLEL_THREADS = 8;
    protected final List<ParallelProcessState> processStates = new ArrayList<>();

    protected AbstractParallelMultiblockControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        for (int i = 0; i < MAX_PARALLEL_THREADS; i++) {
            this.processStates.add(new ParallelProcessState());
        }
    }

    @Override
    protected void machineTick() {
        if (!this.assembled || this.level == null) {
            this.running = false;
            this.progress = 0;
            this.maxProgress = 0;
            this.displayedRecipes.clear();
            coolDown();
            return;
        }

        List<MultiblockProcessingRecipe> availableRecipes = getAvailableRecipes();
        bindProcessStates(availableRecipes);

        AENetworkedBlockEntity nodeBE = getConnectedNetworkNode();
        if (nodeBE == null || nodeBE.getActionableNode() == null) {
            rebuildDisplayedRecipes(availableRecipes);
            coolDown();
            return;
        }

        IGridNode node = nodeBE.getActionableNode();
        IGrid grid = node.getGrid();
        if (grid == null) {
            rebuildDisplayedRecipes(availableRecipes);
            coolDown();
            return;
        }

        IEnergyService energyService = grid.getEnergyService();
        IStorageService storageService = grid.getStorageService();
        MEStorage inventory = storageService.getInventory();
        IActionSource src = IActionSource.ofMachine(nodeBE);

        boolean anyRunning = false;
        int hottestMaxProgress = 0;
        int hottestProgress = 0;
        int activeThreads = 0;

        for (ParallelProcessState processState : this.processStates) {
            if (!processState.isActive()) {
                continue;
            }

            MultiblockProcessingRecipe recipe = findRecipe(availableRecipes, processState.getRecipeId());
            if (recipe == null) {
                processState.clear();
                continue;
            }

            activeThreads++;
            int scaledMaxProgress = MultiblockTierScaling.adjustedTime(recipe.time(), this.machineTier, recipe.requiredTier());
            if (scaledMaxProgress > hottestMaxProgress) {
                hottestMaxProgress = scaledMaxProgress;
                hottestProgress = processState.getProgress();
            }

            if (!MultiblockTierScaling.canRunRecipe(this.machineTier, recipe.requiredTier())) {
                continue;
            }

            processState.resizeBuffers(recipe.itemInputs().size(), recipe.fluidInputs().size());
            long scaledEnergy = MultiblockTierScaling.adjustedEnergy(recipe.energy(), this.machineTier, recipe.requiredTier());
            chargeEnergy(processState, energyService, scaledEnergy);
            boolean materialsFulfilled = pullIngredients(processState, recipe, inventory, src);
            if (!materialsFulfilled || processState.getEnergyBuffer() < scaledEnergy) {
                continue;
            }

            anyRunning = true;
            processState.setProgress(processState.getProgress() + (this.overclocked ? 5 : 1));
            if (processState.getProgress() >= scaledMaxProgress) {
                finishRecipe(processState, recipe, inventory, src);
            }
        }

        this.running = anyRunning;
        this.maxProgress = hottestMaxProgress;
        this.progress = hottestProgress;
        rebuildDisplayedRecipes(availableRecipes);
        updateTemperature(activeThreads);
        this.setChanged();
    }

    private void bindProcessStates(List<MultiblockProcessingRecipe> availableRecipes) {
        for (int i = 0; i < this.processStates.size(); i++) {
            ParallelProcessState state = this.processStates.get(i);
            if (i < availableRecipes.size()) {
                ResourceLocation targetId = availableRecipes.get(i).id();
                if (!targetId.equals(state.getRecipeId())) {
                    state.clear();
                    state.setRecipeId(targetId);
                }
            } else {
                state.clear();
            }
        }
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

    private boolean pullIngredients(ParallelProcessState state, MultiblockProcessingRecipe recipe, MEStorage inventory, IActionSource src) {
        boolean materialsFulfilled = true;

        for (int i = 0; i < recipe.itemInputs().size(); i++) {
            var requirement = recipe.itemInputs().get(i);
            if (state.getItemBuffers()[i] >= requirement.amount()) {
                continue;
            }
            long needed = requirement.amount() - state.getItemBuffers()[i];
            long toExtract = Math.min(needed, 100_000L);
            for (ItemStack match : requirement.ingredient().getItems()) {
                long extracted = inventory.extract(AEItemKey.of(match), toExtract, Actionable.MODULATE, src);
                state.getItemBuffers()[i] += extracted;
                toExtract -= extracted;
                if (toExtract <= 0) {
                    break;
                }
            }
            if (state.getItemBuffers()[i] < requirement.amount()) {
                materialsFulfilled = false;
            }
        }

        for (int i = 0; i < recipe.fluidInputs().size(); i++) {
            var requirement = recipe.fluidInputs().get(i);
            if (state.getFluidBuffers()[i] >= requirement.amount()) {
                continue;
            }
            long needed = requirement.amount() - state.getFluidBuffers()[i];
            long extracted = inventory.extract(AEFluidKey.of(requirement.fluid().getFluid()), Math.min(needed, 1_000_000L), Actionable.MODULATE, src);
            state.getFluidBuffers()[i] += extracted;
            if (state.getFluidBuffers()[i] < requirement.amount()) {
                materialsFulfilled = false;
            }
        }

        return materialsFulfilled;
    }

    private void finishRecipe(ParallelProcessState state, MultiblockProcessingRecipe recipe, MEStorage inventory, IActionSource src) {
        for (var output : recipe.outputs()) {
            if (!output.item().isEmpty()) {
                inventory.insert(AEItemKey.of(output.item()), output.amount(), Actionable.MODULATE, src);
            }
            if (!output.fluid().isEmpty()) {
                inventory.insert(AEFluidKey.of(output.fluid().getFluid()), output.amount(), Actionable.MODULATE, src);
            }
        }

        state.setProgress(0);
        state.setEnergyBuffer(0L);
        state.clearBuffers();
    }

    private void rebuildDisplayedRecipes(List<MultiblockProcessingRecipe> availableRecipes) {
        this.displayedRecipes.clear();
        for (ParallelProcessState processState : this.processStates) {
            if (!processState.isActive()) {
                continue;
            }
            MultiblockProcessingRecipe recipe = findRecipe(availableRecipes, processState.getRecipeId());
            if (recipe == null) {
                continue;
            }
            var primaryOutput = recipe.primaryOutput();
            int scaledMaxProgress = MultiblockTierScaling.adjustedTime(recipe.time(), this.machineTier, recipe.requiredTier());
            String label = recipe.name();
            if (!MultiblockTierScaling.canRunRecipe(this.machineTier, recipe.requiredTier())) {
                label += " [Locked: MK" + recipe.requiredTier() + "]";
            }
            this.displayedRecipes.add(new UniversalDisplayedRecipe(
                    primaryOutput.item(),
                    primaryOutput.fluid(),
                    net.minecraft.network.chat.Component.literal(label),
                    primaryOutput.amount(),
                    processState.getProgress(),
                    scaledMaxProgress));
        }
    }

    private void updateTemperature(int activeThreads) {
        if (this.running && activeThreads > 0) {
            int heatPerTick = this.overclocked ? 5 * activeThreads : activeThreads;
            this.temperature = Math.min(this.maxTemperature, this.temperature + heatPerTick);
            if (this.safeMode && this.temperature >= this.maxTemperature) {
                this.running = false;
                for (ParallelProcessState processState : this.processStates) {
                    processState.setProgress(0);
                }
            }
        } else {
            coolDown();
        }
    }

    private void coolDown() {
        if (this.temperature > 0) {
            this.temperature = Math.max(0, this.temperature - 2);
        }
    }

    private AENetworkedBlockEntity getConnectedNetworkNode() {
        if (this.level == null) {
            return null;
        }
        for (BlockPos position : this.parts) {
            if (this.level.getBlockEntity(position) instanceof AENetworkedBlockEntity nodeBE) {
                if (nodeBE.getActionableNode() != null && nodeBE.getActionableNode().getGrid() != null) {
                    return nodeBE;
                }
            }
        }
        return null;
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
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag processTags = new ListTag();
        for (ParallelProcessState state : this.processStates) {
            processTags.add(state.save(registries));
        }
        tag.put("processStates", processTags);
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
    }

    @Override
    public void onControllerBroken() {
        super.onControllerBroken();
        for (ParallelProcessState processState : this.processStates) {
            processState.clear();
        }
    }
}

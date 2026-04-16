package com.raishxn.ufo.block.entity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.raishxn.ufo.api.multiblock.FieldTieredCubeValidator;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.screen.EntropicAssemblerMatrixMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EntropicAssemblerMatrixBE extends AbstractEntropicMachineBE implements ICraftingMachine {
    private static final int MAX_JOBS = 256;
    private final List<JobState> jobs = new ArrayList<>();

    public EntropicAssemblerMatrixBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENTROPIC_ASSEMBLER_MATRIX_BE.get(), pos, state);
    }

    @Override
    protected void tickMachine() {
        if (!this.assembled || !isNetworkConnected()) {
            this.running = false;
            this.progress = 0;
            this.maxProgress = 0;
            this.storedEnergy = 0L;
            this.maxStoredEnergy = 0L;
            this.jobs.clear();
            this.displayedRecipes.clear();
            return;
        }

        var grid = getActionableNode().getGrid();
        if (grid == null) {
            return;
        }

        MEStorage inventory = grid.getStorageService().getInventory();
        var source = appeng.api.networking.security.IActionSource.ofMachine(this);
        this.running = !this.jobs.isEmpty();
        this.progress = this.jobs.isEmpty() ? 0 : this.jobs.get(0).progress;
        this.maxProgress = this.jobs.isEmpty() ? 0 : this.jobs.get(0).maxProgress;
        this.displayedRecipes.clear();

        Iterator<JobState> iterator = this.jobs.iterator();
        while (iterator.hasNext()) {
            JobState job = iterator.next();
            job.progress += getProgressPerTick();

            this.displayedRecipes.add(new UniversalDisplayedRecipe(
                    job.itemIcon,
                    job.fluidIcon,
                    job.label,
                    job.totalOutputAmount,
                    Math.min(job.progress, job.maxProgress),
                    job.maxProgress));

            if (job.progress < job.maxProgress) {
                continue;
            }

            for (GenericStack output : job.outputs) {
                inventory.insert(output.what(), output.amount(), Actionable.MODULATE, source);
            }
            iterator.remove();
        }

        this.running = !this.jobs.isEmpty();
        this.progress = this.jobs.isEmpty() ? 0 : this.jobs.get(0).progress;
        this.maxProgress = this.jobs.isEmpty() ? 0 : this.jobs.get(0).maxProgress;
        this.storedEnergy = this.jobs.size();
        this.maxStoredEnergy = getParallelThreadLimit();
        this.temperature = Math.min(this.maxTemperature, this.jobs.size() * (this.overclocked ? 6 : 2));
        setChanged();
    }

    @Override
    protected FieldTieredCubeValidator.ShellPredicate getShellPredicate() {
        return (state, level, pos) -> state.is(MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get())
                || state.is(MultiblockBlocks.ENTROPIC_ASSEMBLER_MATRIX.get())
                || state.is(MultiblockBlocks.QUANTUM_PATTERN_HATCH.get());
    }

    @Override
    protected String getMachineNameKey() {
        return "block.ufo.entropic_assembler_matrix";
    }

    @Override
    public int getGuiActiveParallels() {
        return this.jobs.size();
    }

    @Override
    public int getGuiMaxParallels() {
        return getParallelThreadLimit();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new EntropicAssemblerMatrixMenu(id, playerInventory, this);
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs, net.minecraft.core.Direction ejectionDirection) {
        if (!this.assembled || !isNetworkConnected() || this.jobs.size() >= getParallelThreadLimit()) {
            return false;
        }

        List<GenericStack> outputs = patternDetails.getOutputs();
        if (outputs.isEmpty()) {
            return false;
        }

        GenericStack primary = outputs.getFirst();
        var itemIcon = primary.what() instanceof AEItemKey itemKey ? itemKey.toStack() : net.minecraft.world.item.ItemStack.EMPTY;
        var fluidIcon = primary.what() instanceof AEFluidKey fluidKey
                ? new FluidStack(fluidKey.getFluid(), (int) Math.min(Integer.MAX_VALUE, primary.amount()))
                : FluidStack.EMPTY;

        this.jobs.add(new JobState(
                List.copyOf(outputs),
                primary.what().getDisplayName(),
                itemIcon,
                fluidIcon,
                outputs.stream().mapToLong(GenericStack::amount).sum(),
                getJobDuration()));

        for (KeyCounter input : inputs) {
            input.clear();
        }

        setChanged();
        return true;
    }

    @Override
    public boolean acceptsPlans() {
        return this.assembled && isNetworkConnected() && this.jobs.size() < getParallelThreadLimit();
    }

    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        return new PatternContainerGroup(
                AEItemKey.of(MultiblockBlocks.ENTROPIC_ASSEMBLER_MATRIX.get().asItem()),
                Component.translatable(getMachineNameKey()),
                List.of(Component.literal("MK" + this.machineTier))
        );
    }

    private int getParallelThreadLimit() {
        return switch (this.machineTier) {
            case 3 -> 256;
            case 2 -> 128;
            default -> 64;
        };
    }

    private int getProgressPerTick() {
        int base = switch (this.machineTier) {
            case 3 -> 400;
            case 2 -> 100;
            default -> 25;
        };
        return this.overclocked ? base * 4 : base;
    }

    private int getJobDuration() {
        return switch (this.machineTier) {
            case 3 -> 1;
            case 2 -> 10;
            default -> 40;
        };
    }

    private static final class JobState {
        private final List<GenericStack> outputs;
        private final Component label;
        private final net.minecraft.world.item.ItemStack itemIcon;
        private final FluidStack fluidIcon;
        private final long totalOutputAmount;
        private final int maxProgress;
        private int progress;

        private JobState(List<GenericStack> outputs, Component label, net.minecraft.world.item.ItemStack itemIcon,
                         FluidStack fluidIcon, long totalOutputAmount, int maxProgress) {
            this.outputs = outputs;
            this.label = label;
            this.itemIcon = itemIcon;
            this.fluidIcon = fluidIcon;
            this.totalOutputAmount = totalOutputAmount;
            this.maxProgress = Math.max(1, maxProgress);
        }
    }
}

package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.multiblock.FieldTieredCubeValidator;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.screen.EntropicConvergenceEngineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntropicConvergenceCasingBE extends AbstractEntropicMachineBE {
    private static final long[] STORAGE_BY_TIER = {
            0L,
            4_611_686_018_427_387_904L,
            8_646_911_284_551_352_320L,
            Long.MAX_VALUE
    };
    private static final int[] COPROCESSORS_BY_TIER = {
            0,
            250_000_000,
            1_000_000_000,
            Integer.MAX_VALUE
    };

    public EntropicConvergenceCasingBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENTROPIC_CONVERGENCE_CASING_BE.get(), pos, state);
    }

    @Override
    protected void tickMachine() {
        this.running = this.assembled && isNetworkConnected();
        this.progress = 0;
        this.maxProgress = 1;
        this.temperature = 0;
        this.storedEnergy = getTierStorage();
        this.maxStoredEnergy = getTierStorage();
        this.displayedRecipes.clear();
    }

    @Override
    protected FieldTieredCubeValidator.ShellPredicate getShellPredicate() {
        return (state, level, pos) -> state.is(MultiblockBlocks.ENTROPIC_CONVERGENCE_CASING.get());
    }

    @Override
    protected String getMachineNameKey() {
        return "block.ufo.entropic_convergence_engine";
    }

    @Override
    public int getGuiActiveParallels() {
        return this.assembled ? getTierCoProcessors() : 0;
    }

    @Override
    public int getGuiMaxParallels() {
        return this.assembled ? getTierCoProcessors() : 1;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new EntropicConvergenceEngineMenu(id, playerInventory, this);
    }

    @Override
    public List<UniversalDisplayedRecipe> getDisplayedRecipes() {
        return List.of();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ufo.entropic_convergence_engine");
    }

    private long getTierStorage() {
        return STORAGE_BY_TIER[Math.max(1, Math.min(3, this.machineTier))];
    }

    private int getTierCoProcessors() {
        return COPROCESSORS_BY_TIER[Math.max(1, Math.min(3, this.machineTier))];
    }
}

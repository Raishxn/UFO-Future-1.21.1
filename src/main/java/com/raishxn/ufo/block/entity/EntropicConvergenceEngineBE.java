package com.raishxn.ufo.block.entity;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.crafting.CraftingBlockEntity;
import com.raishxn.ufo.api.multiblock.FieldTieredCubeValidator;
import com.raishxn.ufo.api.multiblock.IEntropicMachineController;
import com.raishxn.ufo.api.multiblock.MultiblockMachineTier;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntropicConvergenceEngineBE extends CraftingBlockEntity
        implements IEntropicMachineController, IUniversalMultiblockController, IUpgradeableObject, MenuProvider {

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

    private final IUpgradeInventory upgrades;
    private final List<BlockPos> parts = new ArrayList<>();
    private final Set<BlockPos> partSet = new HashSet<>();
    private boolean structureAssembled;
    private boolean structureDirty = true;
    private int scanCooldown;
    private int machineTier = MultiblockMachineTier.MK1.level();

    public EntropicConvergenceEngineBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENTROPIC_CONVERGENCE_ENGINE_BE.get(), pos, state);
        this.upgrades = UpgradeInventories.forMachine(state.getBlock().asItem(), 4, this::saveChanges);
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        if (this.structureDirty || --this.scanCooldown <= 0) {
            scanStructure(this.level);
            this.scanCooldown = 40;
            this.structureDirty = false;
        }
    }

    @Override
    public void scanStructure(Level level) {
        boolean wasAssembled = this.structureAssembled;
        var result = FieldTieredCubeValidator.findMatchingCube(level, this.worldPosition,
                (state, testLevel, pos) -> state.is(MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get())
                        || state.is(MultiblockBlocks.ENTROPIC_CONVERGENCE_ENGINE.get()));

        this.parts.clear();
        this.partSet.clear();

        if (result.isPresent() && result.get().valid() && result.get().shellPositions().contains(this.worldPosition)) {
            this.structureAssembled = true;
            this.machineTier = result.get().machineTier();
            for (BlockPos pos : result.get().shellPositions()) {
                if (!pos.equals(this.worldPosition)) {
                    BlockPos immutable = pos.immutable();
                    this.parts.add(immutable);
                    this.partSet.add(immutable);
                }
            }
            for (BlockPos pos : result.get().interiorPositions()) {
                BlockPos immutable = pos.immutable();
                this.parts.add(immutable);
                this.partSet.add(immutable);
            }
        } else {
            this.structureAssembled = false;
            this.machineTier = MultiblockMachineTier.MK1.level();
        }

        if (wasAssembled != this.structureAssembled && level != null) {
            level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
        setChanged();
    }

    @Override
    public long getStorageBytes() {
        if (!this.structureAssembled) {
            return 0L;
        }
        return STORAGE_BY_TIER[Math.max(1, Math.min(3, this.machineTier))];
    }

    @Override
    public int getAcceleratorThreads() {
        if (!this.structureAssembled) {
            return 0;
        }
        return COPROCESSORS_BY_TIER[Math.max(1, Math.min(3, this.machineTier))];
    }

    @Override
    public boolean isAssembled() {
        return this.structureAssembled;
    }

    @Override
    public boolean canProxyInteract(BlockPos pos) {
        return pos.equals(this.worldPosition) || this.partSet.contains(pos);
    }

    @Override
    public boolean isNetworkConnected() {
        return getActionableNode() != null && getActionableNode().getGrid() != null && getActionableNode().isActive();
    }

    @Override
    public void markStructureDirty() {
        this.structureDirty = true;
        this.scanCooldown = 0;
    }

    @Override
    public void addPart(BlockPos partPos) {
        BlockPos immutable = partPos.immutable();
        if (this.partSet.add(immutable)) {
            this.parts.add(immutable);
        }
    }

    @Override
    public void removePart(BlockPos partPos) {
        this.partSet.remove(partPos);
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
    public Component getDisplayName() {
        return Component.translatable("block.ufo.entropic_convergence_engine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new com.raishxn.ufo.screen.EntropicConvergenceEngineMenu(id, playerInventory, this);
    }

    @Override
    public boolean isGuiAssembled() {
        return this.structureAssembled;
    }

    @Override
    public boolean isGuiRunning() {
        return this.isFormed() && isNetworkConnected();
    }

    @Override
    public int getGuiProgress() {
        return 0;
    }

    @Override
    public int getGuiMaxProgress() {
        return 1;
    }

    @Override
    public int getGuiTemperature() {
        return 0;
    }

    @Override
    public int getGuiMaxTemperature() {
        return 1;
    }

    @Override
    public int getGuiMachineTier() {
        return this.machineTier;
    }

    @Override
    public long getGuiStoredEnergy() {
        return getStorageBytes();
    }

    @Override
    public long getGuiMaxEnergy() {
        return getStorageBytes();
    }

    @Override
    public int getGuiActiveParallels() {
        return this.structureAssembled ? getAcceleratorThreads() : 0;
    }

    @Override
    public int getGuiMaxParallels() {
        return this.structureAssembled ? getAcceleratorThreads() : 1;
    }

    @Override
    public boolean isGuiSafeMode() {
        return true;
    }

    @Override
    public boolean isGuiOverclocked() {
        return false;
    }

    @Override
    public void toggleSafeMode() {
    }

    @Override
    public void toggleOverclock() {
    }

    @Override
    public List<UniversalDisplayedRecipe> getDisplayedRecipes() {
        return List.of();
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("StructureAssembled", this.structureAssembled);
        tag.putBoolean("StructureDirty", this.structureDirty);
        tag.putInt("ScanCooldown", this.scanCooldown);
        tag.putInt("MachineTier", this.machineTier);

        ListTag partsTag = new ListTag();
        for (BlockPos partPos : this.parts) {
            partsTag.add(NbtUtils.writeBlockPos(partPos));
        }
        tag.put("EntropicParts", partsTag);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        this.structureAssembled = tag.getBoolean("StructureAssembled");
        this.structureDirty = tag.getBoolean("StructureDirty");
        this.scanCooldown = tag.getInt("ScanCooldown");
        this.machineTier = Math.max(MultiblockMachineTier.MK1.level(), tag.getInt("MachineTier"));
        this.parts.clear();
        this.partSet.clear();
        ListTag partsTag = tag.getList("EntropicParts", Tag.TAG_COMPOUND);
        for (Tag partTag : partsTag) {
            NbtUtils.readBlockPos((CompoundTag) partTag, "").ifPresent(pos -> {
                BlockPos immutable = pos.immutable();
                this.parts.add(immutable);
                this.partSet.add(immutable);
            });
        }
    }
}

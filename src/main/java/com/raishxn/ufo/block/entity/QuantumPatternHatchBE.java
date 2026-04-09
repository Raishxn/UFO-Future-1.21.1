package com.raishxn.ufo.block.entity;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.screen.QuantumPatternHatchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumPatternHatchBE extends PatternProviderBlockEntity implements IMultiblockPart, MenuProvider {
    public static final int PATTERN_CAPACITY = 64;

    @Nullable
    private BlockPos controllerPos;

    public QuantumPatternHatchBE(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.QUANTUM_PATTERN_HATCH_BE.get(), pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(this.getMainNode(), this, PATTERN_CAPACITY);
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(MultiblockBlocks.QUANTUM_PATTERN_HATCH.get());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(MultiblockBlocks.QUANTUM_PATTERN_HATCH.get());
    }

    @Override
    public void linkToController(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        setChanged();
    }

    @Override
    public void unlinkFromController() {
        this.controllerPos = null;
        setChanged();
    }

    @Override
    public @Nullable BlockPos getControllerPos() {
        return this.controllerPos;
    }

    @Override
    public @NotNull net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.ufo.quantum_pattern_hatch");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new QuantumPatternHatchMenu(containerId, playerInventory, this);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.controllerPos != null) {
            tag.put("controllerPos", NbtUtils.writeBlockPos(this.controllerPos));
        }
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains("controllerPos")) {
            NbtUtils.readBlockPos(tag.getCompound("controllerPos"), "").ifPresent(pos -> this.controllerPos = pos);
        } else {
            this.controllerPos = null;
        }
    }
}

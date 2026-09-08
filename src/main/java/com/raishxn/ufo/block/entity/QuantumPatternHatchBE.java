package com.raishxn.ufo.block.entity;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.block.crafting.PatternProviderBlock;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockCasingStyle;
import com.raishxn.ufo.block.QuantumPatternHatchBlock;
import com.raishxn.ufo.block.MultiblockBlocks;
import com.raishxn.ufo.init.ModBlockEntities;
import com.raishxn.ufo.init.ModMenus;
import com.raishxn.ufo.screen.QuantumPatternHatchMenu;
import net.minecraft.core.Direction;
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

public class QuantumPatternHatchBE extends PatternProviderBlockEntity implements IMultiblockPart, MenuProvider, com.raishxn.ufo.wireless.QuantumWirelessHost {
    private final com.raishxn.ufo.wireless.QuantumWirelessLinks wirelessLinks = new com.raishxn.ufo.wireless.QuantumWirelessLinks();

    @Override
    public com.raishxn.ufo.wireless.QuantumWirelessLinks wirelessLinks() { return wirelessLinks; }
    public static final int PATTERN_CAPACITY = 72;
    public final com.raishxn.ufo.wireless.QuantumBonusProfile bonusProfile = new com.raishxn.ufo.wireless.QuantumBonusProfile();

    @Nullable
    private BlockPos controllerPos;

    public QuantumPatternHatchBE(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.QUANTUM_PATTERN_HATCH_BE.get(), pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new QuantumPatternProviderLogic(this, PATTERN_CAPACITY);
    }

    @Override
    public void onReady() {
        super.onReady();
        com.raishxn.ufo.wireless.QuantumWirelessActivity.register(this);
    }

    @Override
    public void setRemoved() {
        com.raishxn.ufo.wireless.QuantumWirelessActivity.unregister(this);
        super.setRemoved();
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
        if (controllerPos.equals(this.controllerPos)) {
            // Clear the temporary L-0038 casing skin from worlds that were
            // saved during its short-lived use. Pattern Hatches deliberately
            // retain their native texture even when a multiblock is formed.
            updateCasingStyle(MultiblockCasingStyle.DEFAULT);
            return;
        }
        this.controllerPos = controllerPos;
        updateCasingStyle(MultiblockCasingStyle.DEFAULT);
        setChanged();
    }

    @Override
    public void unlinkFromController() {
        if (this.controllerPos == null) {
            return;
        }
        this.controllerPos = null;
        updateCasingStyle(MultiblockCasingStyle.DEFAULT);
        setChanged();
    }

    /**
     * Detaches during physical removal without changing the block state.
     * A block-state update from {@code Block#onRemove} re-enters Minecraft's
     * removal path and can restore the hatch instead of letting the break
     * complete.
     */
    public void unlinkForRemoval() {
        if (this.controllerPos == null) {
            return;
        }
        this.controllerPos = null;
        setChanged();
    }

    @Override
    public @Nullable BlockPos getControllerPos() {
        return this.controllerPos;
    }

    private void updateCasingStyle(MultiblockCasingStyle style) {
        if (this.level == null || this.level.isClientSide()
                || !this.getBlockState().hasProperty(QuantumPatternHatchBlock.CASING_STYLE)
                || this.getBlockState().getValue(QuantumPatternHatchBlock.CASING_STYLE) == style) {
            return;
        }
        this.level.setBlock(this.worldPosition,
                this.getBlockState().setValue(QuantumPatternHatchBlock.CASING_STYLE, style),
                net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
    }

    public Direction getPushDirectionForController() {
        var pushDirection = getBlockState().getValue(PatternProviderBlock.PUSH_DIRECTION).getDirection();
        return pushDirection != null ? pushDirection : Direction.NORTH;
    }

    @Override
    public @NotNull net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.ufo.quantum_pattern_hatch");
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(ModMenus.QUANTUM_PATTERN_HATCH_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(ModMenus.QUANTUM_PATTERN_HATCH_MENU.get(), player, subMenu.getLocator());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new QuantumPatternHatchMenu(containerId, playerInventory, this);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        wirelessLinks.save(tag);
        if (this.controllerPos != null) {
            tag.put("controllerPos", NbtUtils.writeBlockPos(this.controllerPos));
        }
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        wirelessLinks.load(tag);
        if (tag.contains("controllerPos")) {
            NbtUtils.readBlockPos(tag.getCompound("controllerPos"), "").ifPresent(pos -> this.controllerPos = pos);
        } else {
            this.controllerPos = null;
        }
    }
}

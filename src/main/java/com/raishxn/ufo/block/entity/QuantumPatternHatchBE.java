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
    private int patternDispatchCursor;

    public QuantumPatternHatchBE(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.QUANTUM_PATTERN_HATCH_BE.get(), pos, blockState);
    }

    /** The Buffer is the multiblock pattern part; the legacy Hatch is standalone. */
    public boolean isPatternBuffer() {
        return this.getBlockState().is(MultiblockBlocks.QUANTUM_PATTERN_BUFFER.get());
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new QuantumPatternProviderLogic(this, PATTERN_CAPACITY);
    }

    @Override
    public void onReady() {
        super.onReady();
        com.raishxn.ufo.wireless.QuantumWirelessActivity.register(this);
        bindLinkedPatternProxies();
    }

    private void bindLinkedPatternProxies() {
        if (this.level == null || this.level.isClientSide() || !isPatternBuffer()) {
            return;
        }
        for (var target : this.wirelessLinks.targets()) {
            if (!this.level.hasChunkAt(target.pos())) {
                continue;
            }
            var targetBe = this.level.getBlockEntity(target.pos());
            if (targetBe instanceof QuantumPatternProxyBE proxy
                    && com.raishxn.ufo.wireless.QuantumWirelessLinks.matches(proxy, target.identity())) {
                proxy.bindPatternBuffer(this);
            }
        }
    }

    @Override
    public void setRemoved() {
        com.raishxn.ufo.wireless.QuantumWirelessActivity.unregister(this);
        super.setRemoved();
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(this.getBlockState().getBlock());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(this.getBlockState().getBlock());
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

    int getPatternDispatchStart(int routeCount) {
        return routeCount > 0 ? Math.floorMod(this.patternDispatchCursor, routeCount) : 0;
    }

    void completePatternDispatch(int selectedRoute, int routeCount) {
        this.patternDispatchCursor = routeCount > 0
                ? Math.floorMod(selectedRoute + 1, routeCount)
                : 0;
        setChanged();
    }

    @Override
    public @NotNull net.minecraft.network.chat.Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(isPatternBuffer() ? ModMenus.QUANTUM_PATTERN_BUFFER_MENU.get()
                : ModMenus.QUANTUM_PATTERN_HATCH_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(isPatternBuffer() ? ModMenus.QUANTUM_PATTERN_BUFFER_MENU.get()
                : ModMenus.QUANTUM_PATTERN_HATCH_MENU.get(), player, subMenu.getLocator());
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
        tag.putInt("ufoPatternDispatchCursor", this.patternDispatchCursor);
        if (this.controllerPos != null) {
            tag.put("controllerPos", NbtUtils.writeBlockPos(this.controllerPos));
        }
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        wirelessLinks.load(tag);
        this.patternDispatchCursor = Math.max(0, tag.getInt("ufoPatternDispatchCursor"));
        if (tag.contains("controllerPos")) {
            NbtUtils.readBlockPos(tag.getCompound("controllerPos"), "").ifPresent(pos -> this.controllerPos = pos);
        } else {
            this.controllerPos = null;
        }
    }
}

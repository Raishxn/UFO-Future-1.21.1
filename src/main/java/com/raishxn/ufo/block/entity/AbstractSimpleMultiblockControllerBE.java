package com.raishxn.ufo.block.entity;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSimpleMultiblockControllerBE extends BlockEntity implements IMultiblockController, MenuProvider {

    protected boolean assembled = false;
    protected boolean structureDirty = true;
    protected int scanCooldown = 0;
    protected final List<BlockPos> parts = new ArrayList<>();
    protected boolean running = false;
    protected int progress = 0;
    protected int maxProgress = 0;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AbstractSimpleMultiblockControllerBE.this.assembled ? 1 : 0;
                case 1 -> AbstractSimpleMultiblockControllerBE.this.running ? 1 : 0;
                case 2 -> AbstractSimpleMultiblockControllerBE.this.progress;
                case 3 -> AbstractSimpleMultiblockControllerBE.this.maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> AbstractSimpleMultiblockControllerBE.this.assembled = value == 1;
                case 1 -> AbstractSimpleMultiblockControllerBE.this.running = value == 1;
                case 2 -> AbstractSimpleMultiblockControllerBE.this.progress = value;
                case 3 -> AbstractSimpleMultiblockControllerBE.this.maxProgress = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    protected AbstractSimpleMultiblockControllerBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract MultiblockPattern getControllerPattern();

    protected abstract String getControllerTranslationKey();

    public ContainerData getContainerData() {
        return this.data;
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        this.scanCooldown--;
        if (this.structureDirty || this.scanCooldown <= 0) {
            scanStructure(this.level);
            this.scanCooldown = 40;
            this.structureDirty = false;
        }
    }

    @Override
    public boolean isAssembled() {
        return this.assembled;
    }

    @Override
    public void scanStructure(Level level) {
        Direction facing = Direction.NORTH;
        BlockState state = level.getBlockState(this.worldPosition);
        if (state.hasProperty(DirectionalBlock.FACING)) {
            facing = state.getValue(DirectionalBlock.FACING);
        }

        MultiblockPattern.MatchResult result = getControllerPattern().match(level, this.worldPosition, facing);
        boolean wasAssembled = this.assembled;
        this.assembled = result.isValid();

        for (BlockPos existingPart : new ArrayList<>(this.parts)) {
            if (!result.partPositions().contains(existingPart)
                    && level.getBlockEntity(existingPart) instanceof IMultiblockPart part) {
                part.unlinkFromController();
            }
        }

        this.parts.clear();
        if (this.assembled) {
            for (BlockPos partPos : result.partPositions()) {
                if (!partPos.equals(this.worldPosition)) {
                    this.parts.add(partPos);
                    if (level.getBlockEntity(partPos) instanceof IMultiblockPart part) {
                        part.linkToController(this.worldPosition);
                    }
                }
            }
        } else {
            this.running = false;
            this.progress = 0;
            this.maxProgress = 0;
        }

        updateControllerBlockState(level.getBlockState(this.worldPosition), wasAssembled);
        this.setChanged();
    }

    private void updateControllerBlockState(BlockState currentState, boolean wasAssembled) {
        if (this.level == null || wasAssembled == this.assembled) {
            return;
        }

        for (var property : currentState.getProperties()) {
            if (property instanceof BooleanProperty booleanProperty && "active".equals(booleanProperty.getName())) {
                this.level.setBlock(this.worldPosition, currentState.setValue(booleanProperty, this.assembled), Block.UPDATE_CLIENTS);
                return;
            }
        }

        this.level.sendBlockUpdated(this.worldPosition, currentState, currentState, Block.UPDATE_ALL);
    }

    public void markStructureDirty() {
        this.structureDirty = true;
        this.scanCooldown = 0;
    }

    public void onControllerBroken() {
        if (this.level == null) {
            return;
        }

        for (BlockPos partPos : this.parts) {
            if (this.level.getBlockEntity(partPos) instanceof IMultiblockPart part) {
                part.unlinkFromController();
            }
        }

        this.parts.clear();
        this.assembled = false;
        this.running = false;
        this.progress = 0;
        this.maxProgress = 0;
        this.setChanged();
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
    public @NotNull Component getDisplayName() {
        return Component.translatable(getControllerTranslationKey());
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("assembled", this.assembled);
        tag.putBoolean("running", this.running);
        tag.putInt("progress", this.progress);
        tag.putInt("maxProgress", this.maxProgress);

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
        this.progress = tag.getInt("progress");
        this.maxProgress = tag.getInt("maxProgress");

        this.parts.clear();
        if (tag.contains("parts", Tag.TAG_LIST)) {
            ListTag partsList = tag.getList("parts", Tag.TAG_COMPOUND);
            for (int i = 0; i < partsList.size(); i++) {
                NbtUtils.readBlockPos(partsList.getCompound(i), "").ifPresent(this.parts::add);
            }
        }

        this.structureDirty = true;
        this.scanCooldown = 0;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

package com.raishxn.ufo.block.entity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A proxy carries no patterns or ingredients. It only exposes the crafting
 * machine of the multiblock it belongs to, allowing a remote Pattern Buffer to
 * dispatch the job atomically into that controller.
 */
public final class QuantumPatternProxyBE extends BlockEntity implements IMultiblockPart, ICraftingMachine {
    @Nullable
    private BlockPos controllerPos;
    @Nullable
    private BlockPos patternBufferPos;
    @Nullable
    private UUID patternBufferIdentity;

    public QuantumPatternProxyBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void linkToController(BlockPos controllerPos) {
        BlockPos immutable = controllerPos.immutable();
        if (!immutable.equals(this.controllerPos)) {
            this.controllerPos = immutable;
            setChanged();
        }
    }

    @Override
    public void unlinkFromController() {
        if (controllerPos != null) {
            controllerPos = null;
            setChanged();
        }
    }

    public void unlinkForRemoval() {
        controllerPos = null;
        setChanged();
    }

    @Nullable
    @Override
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Nullable
    public BlockEntity getControllerBlockEntity() {
        if (level == null || controllerPos == null || !level.hasChunkAt(controllerPos)) {
            return null;
        }
        return level.getBlockEntity(controllerPos);
    }

    @Nullable
    private ICraftingMachine controller() {
        BlockEntity entity = getControllerBlockEntity();
        return entity instanceof ICraftingMachine machine ? machine : null;
    }

    /** Binds this proxy to the Buffer whose AE grid it relays to its controller. */
    public boolean bindPatternBuffer(QuantumPatternHatchBE buffer) {
        if (level == null || buffer.getLevel() != level || !buffer.isPatternBuffer()) {
            return false;
        }
        BlockPos sourcePos = buffer.getBlockPos().immutable();
        UUID sourceIdentity = com.raishxn.ufo.wireless.QuantumWirelessLinks.identity(buffer);
        if (!sourcePos.equals(this.patternBufferPos) || !sourceIdentity.equals(this.patternBufferIdentity)) {
            this.patternBufferPos = sourcePos;
            this.patternBufferIdentity = sourceIdentity;
            setChanged();
        }
        return true;
    }

    public void unbindPatternBuffer(QuantumPatternHatchBE buffer) {
        if (this.patternBufferPos != null && this.patternBufferPos.equals(buffer.getBlockPos())) {
            this.patternBufferPos = null;
            this.patternBufferIdentity = null;
            setChanged();
        }
    }

    /** Returns a live, reciprocal Buffer link without loading either chunk. */
    @Nullable
    public QuantumPatternHatchBE getLinkedPatternBuffer() {
        if (level == null || patternBufferPos == null || patternBufferIdentity == null
                || !level.hasChunkAt(patternBufferPos)) {
            return null;
        }
        if (!(level.getBlockEntity(patternBufferPos) instanceof QuantumPatternHatchBE buffer)
                || !buffer.isPatternBuffer()
                || !com.raishxn.ufo.wireless.QuantumWirelessLinks.matches(buffer, patternBufferIdentity)
                || !buffer.wirelessLinks().enabled()) {
            return null;
        }
        boolean reciprocal = buffer.wirelessLinks().targets().stream().anyMatch(target ->
                target.pos().equals(worldPosition)
                        && com.raishxn.ufo.wireless.QuantumWirelessLinks.matches(this, target.identity()));
        return reciprocal ? buffer : null;
    }

    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        ICraftingMachine controller = controller();
        return controller != null ? controller.getCraftingMachineInfo() : new PatternContainerGroup(
                AEItemKey.of(MultiblockBlocks.QUANTUM_PATTERN_PROXY.get()),
                Component.translatable("block.ufo.quantum_pattern_proxy"), List.of());
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs, Direction ejectionDirection) {
        ICraftingMachine controller = controller();
        return controller != null && controller.acceptsPlans()
                && controller.pushPattern(patternDetails, inputs, ejectionDirection);
    }

    @Override
    public boolean acceptsPlans() {
        ICraftingMachine controller = controller();
        return controller != null && controller.acceptsPlans();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) {
            tag.put("controllerPos", NbtUtils.writeBlockPos(controllerPos));
        }
        if (patternBufferPos != null && patternBufferIdentity != null) {
            tag.putLong("patternBufferPos", patternBufferPos.asLong());
            tag.putUUID("patternBufferIdentity", patternBufferIdentity);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        controllerPos = tag.contains("controllerPos")
                ? NbtUtils.readBlockPos(tag.getCompound("controllerPos"), "").orElse(null)
                : null;
        if (tag.hasUUID("patternBufferIdentity")) {
            patternBufferPos = BlockPos.of(tag.getLong("patternBufferPos"));
            patternBufferIdentity = tag.getUUID("patternBufferIdentity");
        } else {
            patternBufferPos = null;
            patternBufferIdentity = null;
        }
    }
}

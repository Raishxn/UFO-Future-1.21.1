package com.raishxn.ufo.block.entity;

import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import com.raishxn.ufo.api.multiblock.IMultiblockPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntropicMachinePartBE extends AENetworkedBlockEntity implements IMultiblockPart {
    @Nullable
    private BlockPos controllerPos;

    public EntropicMachinePartBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.getMainNode()
                .setExposedOnSides(java.util.EnumSet.allOf(Direction.class))
                .setIdlePowerUsage(0);
    }

    @Override
    public void linkToController(BlockPos controllerPos) {
        this.controllerPos = controllerPos.immutable();
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
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.controllerPos != null) {
            tag.put("ControllerPos", NbtUtils.writeBlockPos(this.controllerPos));
        }
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains("ControllerPos")) {
            NbtUtils.readBlockPos(tag.getCompound("ControllerPos"), "").ifPresent(pos -> this.controllerPos = pos.immutable());
        } else {
            this.controllerPos = null;
        }
    }
}

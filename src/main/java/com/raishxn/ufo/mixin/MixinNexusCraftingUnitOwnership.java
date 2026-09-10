package com.raishxn.ufo.mixin;

import appeng.blockentity.crafting.CraftingBlockEntity;
import com.raishxn.ufo.api.ae.NexusCraftingUnitOwnership;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingBlockEntity.class, priority = 3000, remap = false)
public abstract class MixinNexusCraftingUnitOwnership implements NexusCraftingUnitOwnership {
    @Unique private static final String UFO_NEXUS_OWNER_TAG = "ufoNexusController";
    @Unique @Nullable private BlockPos ufo$nexusController;

    @Override
    public @Nullable BlockPos ufo$getNexusController() {
        return ufo$nexusController;
    }

    @Override
    public void ufo$setNexusController(@Nullable BlockPos controllerPos) {
        BlockPos normalized = controllerPos == null ? null : controllerPos.immutable();
        if (java.util.Objects.equals(this.ufo$nexusController, normalized)) return;
        this.ufo$nexusController = normalized;
        CraftingBlockEntity unit = (CraftingBlockEntity) (Object) this;
        unit.setChanged();
        unit.markForUpdate();
    }

    @Inject(
            method = "onReady",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/me/cluster/implementations/CraftingCPUCalculator;calculateMultiblock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V"),
            cancellable = true)
    private void ufo$deferVanillaFormationForNexus(CallbackInfo ci) {
        if (NexusCraftingUnitOwnership.deferVanillaFormation((CraftingBlockEntity) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void ufo$saveNexusOwner(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (ufo$nexusController != null) tag.put(UFO_NEXUS_OWNER_TAG, NbtUtils.writeBlockPos(ufo$nexusController));
    }

    @Inject(method = "loadTag", at = @At("TAIL"))
    private void ufo$loadNexusOwner(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.ufo$nexusController = null;
        if (tag.contains(UFO_NEXUS_OWNER_TAG)) {
            NbtUtils.readBlockPos(tag.getCompound(UFO_NEXUS_OWNER_TAG), "")
                    .ifPresent(pos -> this.ufo$nexusController = pos.immutable());
        }
    }
}

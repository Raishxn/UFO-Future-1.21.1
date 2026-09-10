package com.raishxn.ufo.api.ae;

import appeng.blockentity.crafting.CraftingBlockEntity;
import com.raishxn.ufo.block.entity.QuantumComputationNexusControllerBE;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/** Persistent ownership marker that keeps Nexus modules out of AE2's contiguous CPU scanner. */
public interface NexusCraftingUnitOwnership {
    @Nullable BlockPos ufo$getNexusController();

    void ufo$setNexusController(@Nullable BlockPos controllerPos);

    static boolean deferVanillaFormation(CraftingBlockEntity unit) {
        NexusCraftingUnitOwnership ownership = (NexusCraftingUnitOwnership) unit;
        BlockPos controllerPos = ownership.ufo$getNexusController();
        if (controllerPos == null || unit.getLevel() == null) return false;
        if (!unit.getLevel().isLoaded(controllerPos)) return true;
        if (unit.getLevel().getBlockEntity(controllerPos) instanceof QuantumComputationNexusControllerBE controller
                && controller.canOwnModuleAt(unit.getBlockPos())) {
            return true;
        }
        ownership.ufo$setNexusController(null);
        return false;
    }
}

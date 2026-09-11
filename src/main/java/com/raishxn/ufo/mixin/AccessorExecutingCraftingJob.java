package com.raishxn.ufo.mixin;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.inv.ListCraftingInventory;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "appeng.crafting.execution.ExecutingCraftingJob", remap = false)
public interface AccessorExecutingCraftingJob {
    @Accessor("tasks")
    Map<IPatternDetails, ?> ufo$getTasks();

    @Accessor("waitingFor")
    ListCraftingInventory ufo$getWaitingFor();

    @Accessor("timeTracker")
    ElapsedTimeTracker ufo$getTimeTracker();
}

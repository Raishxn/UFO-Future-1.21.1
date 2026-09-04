package com.raishxn.ufo.client.ctm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/** Defines which neighbouring blocks visually join a connected-texture model. */
interface ConnectionPredicate {
    boolean connects(BlockAndTintGetter level, BlockPos pos, BlockState self, Direction direction);

    boolean connects(BlockAndTintGetter level, BlockPos pos, BlockState self, BlockPos neighbourPos);
}

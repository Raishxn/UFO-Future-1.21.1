package com.raishxn.ufo.client.ctm;

import com.raishxn.ufo.UfoMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

final class ConnectionPredicates {
    static final ResourceLocation SAME_BLOCK_ID = UfoMod.id("same_block");

    private static final ConnectionPredicate SAME_BLOCK = new ConnectionPredicate() {
        @Override
        public boolean connects(BlockAndTintGetter level, BlockPos pos, BlockState self, Direction direction) {
            return level.getBlockState(pos.relative(direction)).is(self.getBlock());
        }

        @Override
        public boolean connects(BlockAndTintGetter level, BlockPos pos, BlockState self, BlockPos neighbourPos) {
            return level.getBlockState(neighbourPos).is(self.getBlock());
        }
    };

    private static final Map<ResourceLocation, ConnectionPredicate> PREDICATES = Map.of(SAME_BLOCK_ID, SAME_BLOCK);

    private ConnectionPredicates() {
    }

    static ConnectionPredicate get(ResourceLocation id) {
        ConnectionPredicate predicate = PREDICATES.get(id);
        if (predicate == null) {
            throw new IllegalArgumentException("Unknown UFO connected-texture predicate: " + id);
        }
        return predicate;
    }
}

package com.raishxn.ufo.block;

import com.mojang.serialization.MapCodec;
import com.raishxn.ufo.block.entity.EntropicConvergenceCasingBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class EntropicConvergenceCasingBlock extends AbstractEntropicMachineBlock<EntropicConvergenceCasingBE> {
    public static final MapCodec<EntropicConvergenceCasingBlock> CODEC = simpleCodec(EntropicConvergenceCasingBlock::new);

    public EntropicConvergenceCasingBlock() {
        this(BlockBehaviour.Properties.of()
                .strength(30.0f, 1200.0f)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(FORMED) ? 10 : 0));
    }

    private EntropicConvergenceCasingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends AbstractEntropicMachineBlock<EntropicConvergenceCasingBE>> codec() {
        return (MapCodec) CODEC;
    }

    @Override
    public EntropicConvergenceCasingBE newBlockEntity(BlockPos pos, BlockState state) {
        return new EntropicConvergenceCasingBE(pos, state);
    }
}

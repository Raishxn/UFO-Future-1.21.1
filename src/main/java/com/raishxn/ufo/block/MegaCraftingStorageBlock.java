package com.raishxn.ufo.block;

import appeng.block.crafting.CraftingUnitBlock;
import appeng.block.crafting.ICraftingUnitType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class MegaCraftingStorageBlock extends CraftingUnitBlock {
    public MegaCraftingStorageBlock(ICraftingUnitType type) {
        super(type);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
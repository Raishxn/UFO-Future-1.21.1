package com.raishxn.ufo.fluid.custom;

import com.raishxn.ufo.block.ModBlocks;
import com.raishxn.ufo.fluid.ModFluidTypes;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.item.ModItems;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class BoseEinsteinCondensateFluid extends BaseFlowingFluid {
    protected BoseEinsteinCondensateFluid() {
        super(new Properties(
                ModFluidTypes.BOSE_EINSTEIN_CONDENSATE_TYPE,
                ModFluids.SOURCE_BOSE_EINSTEIN_CONDENSATE,
                ModFluids.FLOWING_BOSE_EINSTEIN_CONDENSATE)
                .bucket(ModItems.BOSE_EINSTEIN_CONDENSATE_BUCKET)
                .block(ModBlocks.BOSE_EINSTEIN_CONDENSATE_BLOCK));
    }

    public static class Source extends BoseEinsteinCondensateFluid {
        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return true;
        }
    }

    public static class Flowing extends BoseEinsteinCondensateFluid {
        public Flowing() {
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        @Override
        protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, net.minecraft.world.level.material.FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return false;
        }
    }
}

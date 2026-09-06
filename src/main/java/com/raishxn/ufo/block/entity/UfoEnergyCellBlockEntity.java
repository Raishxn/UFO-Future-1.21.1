package com.raishxn.ufo.block.entity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.block.networking.EnergyCellBlock;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import com.raishxn.ufo.util.AdjacentEnergyExporter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class UfoEnergyCellBlockEntity extends EnergyCellBlockEntity {
    private final IEnergyStorage exposedEnergy = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0) {
                return 0;
            }
            int offered = Math.min(maxReceive, getForgeExportRate());
            double offeredAE = PowerUnit.FE.convertTo(PowerUnit.AE, offered);
            double overflow = UfoEnergyCellBlockEntity.this.injectAEPower(offeredAE,
                    simulate ? Actionable.SIMULATE : Actionable.MODULATE);
            return (int) Math.max(0, Math.min(offered,
                    Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, offeredAE - overflow))));
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (maxExtract <= 0) {
                return 0;
            }

            return (int) Math.min(Integer.MAX_VALUE,
                    PowerUnit.AE.convertTo(PowerUnit.FE, UfoEnergyCellBlockEntity.this.extractAEPower(
                            PowerUnit.FE.convertTo(PowerUnit.AE, maxExtract),
                            simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                            PowerMultiplier.ONE)));
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE,
                    Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, UfoEnergyCellBlockEntity.this.getAECurrentPower())));
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE,
                    Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, UfoEnergyCellBlockEntity.this.getAEMaxPower())));
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public UfoEnergyCellBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        int totalBudget = getForgeExportRate();
        AdjacentEnergyExporter.pushEnergy(this.level, this.worldPosition, this.exposedEnergy, totalBudget, totalBudget);
    }

    public IEnergyStorage getExposedEnergy() {
        return this.exposedEnergy;
    }

    private int getForgeExportRate() {
        if (!(this.getBlockState().getBlock() instanceof EnergyCellBlock energyCellBlock)) {
            return 0;
        }

        return (int) Math.max(1,
                Math.min(Integer.MAX_VALUE, Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, energyCellBlock.getChargeRate()))));
    }
}

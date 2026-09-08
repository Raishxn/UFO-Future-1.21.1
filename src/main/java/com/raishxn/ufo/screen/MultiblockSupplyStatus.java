package com.raishxn.ufo.screen;

import appeng.menu.guisync.PacketWritable;
import com.raishxn.ufo.block.entity.AbstractParallelMultiblockControllerBE;
import com.raishxn.ufo.diagnostic.CoolantStatus;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.List;

/** Bounded menu-only snapshot; never includes structure positions or process inventories. */
public record MultiblockSupplyStatus(List<CoolantStatus> coolants, long capacity, int hatches,
                                    double speed, double energy, double heat, double bonus,
                                    boolean creative, boolean recipeFactorsLocked) implements PacketWritable {
    public static final MultiblockSupplyStatus EMPTY = new MultiblockSupplyStatus(
            List.of(), 0, 0, 1, 1, 1, 0, false, false);

    public MultiblockSupplyStatus {
        coolants = List.copyOf(coolants);
        if (coolants.size() > 3) throw new IllegalArgumentException("At most three coolant types");
    }

    public static MultiblockSupplyStatus from(AbstractParallelMultiblockControllerBE controller) {
        var tanks = controller.getCoolantStatus();
        var upgrades = controller.getUpgradeStatus();
        return new MultiblockSupplyStatus(controller.getCoolantDisplayStatus(),
                tanks.stream().mapToLong(CoolantStatus::capacityMillibuckets).sum(), tanks.size(),
                upgrades.catalystSpeedMultiplier(), upgrades.catalystEnergyMultiplier(),
                upgrades.heatMultiplier(), upgrades.bonusDropChance(), upgrades.creative(), controller.hasWirelessBonusSnapshot());
    }

    public MultiblockSupplyStatus(RegistryFriendlyByteBuf buffer) {
        this(readCoolants(buffer), buffer.readLong(), buffer.readVarInt(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean(), buffer.readBoolean());
    }

    private static List<CoolantStatus> readCoolants(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > 3) throw new IllegalArgumentException("Invalid coolant count");
        var result = new java.util.ArrayList<CoolantStatus>(count);
        for (int i = 0; i < count; i++) {
            result.add(new CoolantStatus(0, buffer.readUtf(128), buffer.readLong(), 0,
                    buffer.readLong(), buffer.readLong(), buffer.readLong()));
        }
        return List.copyOf(result);
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(coolants.size());
        for (var coolant : coolants) {
            buffer.writeUtf(coolant.fluidId(), 128);
            buffer.writeLong(coolant.storedMillibuckets());
            buffer.writeLong(coolant.heatNumerator());
            buffer.writeLong(coolant.millibucketDenominator());
            buffer.writeLong(coolant.maxMillibucketsPerTick());
        }
        buffer.writeLong(capacity);
        buffer.writeVarInt(hatches);
        buffer.writeDouble(speed);
        buffer.writeDouble(energy);
        buffer.writeDouble(heat);
        buffer.writeDouble(bonus);
        buffer.writeBoolean(creative);
        buffer.writeBoolean(recipeFactorsLocked);
    }
}

package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.network.MachinePacketGuard;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleStellarLock(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketToggleStellarLock> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("ufo", "toggle_stellar_lock"));

    public static final StreamCodec<FriendlyByteBuf, PacketToggleStellarLock> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketToggleStellarLock::pos,
            PacketToggleStellarLock::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            StellarNexusControllerBE controller = MachinePacketGuard.requireStellar(
                    context, pos, MachinePacketGuard.Action.TOGGLE_LOCK);
            if (controller != null) {
                controller.toggleSimulationLock();
            }
        });
    }
}

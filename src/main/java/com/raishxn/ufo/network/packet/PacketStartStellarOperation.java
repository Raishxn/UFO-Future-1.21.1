package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.block.entity.StellarNexusControllerBE;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketStartStellarOperation(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketStartStellarOperation> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("ufo", "start_stellar_operation"));

    public static final StreamCodec<FriendlyByteBuf, PacketStartStellarOperation> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketStartStellarOperation::pos,
            PacketStartStellarOperation::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.level().isLoaded(pos)) {
                if (player.level().getBlockEntity(pos) instanceof StellarNexusControllerBE controller) {
                    controller.startOperation();
                }
            }
        });
    }
}

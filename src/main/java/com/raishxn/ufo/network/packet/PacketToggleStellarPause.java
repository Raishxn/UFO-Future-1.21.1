package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.network.MachinePacketGuard;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-authoritative pause/resume control for the Stellar Nexus. */
public record PacketToggleStellarPause(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketToggleStellarPause> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "toggle_stellar_pause"));
    public static final StreamCodec<ByteBuf, PacketToggleStellarPause> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketToggleStellarPause::pos, PacketToggleStellarPause::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketToggleStellarPause packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            StellarNexusControllerBE controller = MachinePacketGuard.requireStellar(
                    context, packet.pos(), MachinePacketGuard.Action.TOGGLE_STELLAR_PAUSE);
            if (controller != null) {
                controller.togglePause();
            }
        });
    }
}

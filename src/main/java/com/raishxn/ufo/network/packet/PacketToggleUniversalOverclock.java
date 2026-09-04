package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.IUniversalMultiblockController;
import com.raishxn.ufo.network.MachinePacketGuard;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleUniversalOverclock(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketToggleUniversalOverclock> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "toggle_universal_overclock"));

    public static final StreamCodec<ByteBuf, PacketToggleUniversalOverclock> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketToggleUniversalOverclock::pos,
            PacketToggleUniversalOverclock::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketToggleUniversalOverclock packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            IUniversalMultiblockController controller = MachinePacketGuard.requireUniversal(
                    context, packet.pos(), MachinePacketGuard.Action.TOGGLE_OVERCLOCK);
            if (controller != null) {
                controller.toggleOverclock();
            }
        });
    }
}

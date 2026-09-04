package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.IUniversalMultiblockController;
import com.raishxn.ufo.network.MachinePacketGuard;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleUniversalProcessPaused(BlockPos pos, int processIndex)
        implements CustomPacketPayload {
    public static final Type<PacketToggleUniversalProcessPaused> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "toggle_universal_process_paused"));

    public static final StreamCodec<ByteBuf, PacketToggleUniversalProcessPaused> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PacketToggleUniversalProcessPaused::pos,
                    ByteBufCodecs.VAR_INT, PacketToggleUniversalProcessPaused::processIndex,
                    PacketToggleUniversalProcessPaused::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketToggleUniversalProcessPaused packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            IUniversalMultiblockController controller = MachinePacketGuard.requireUniversal(
                    context, packet.pos(), MachinePacketGuard.Action.TOGGLE_PROCESS_PAUSE);
            if (controller != null) {
                controller.toggleProcessPaused(packet.processIndex());
            }
        });
    }
}

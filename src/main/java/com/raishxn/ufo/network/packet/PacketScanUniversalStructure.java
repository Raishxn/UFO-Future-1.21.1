package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.api.multiblock.IMultiblockController;
import com.raishxn.ufo.network.MachinePacketGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketScanUniversalStructure(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketScanUniversalStructure> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("ufo", "scan_universal_structure"));

    public static final StreamCodec<FriendlyByteBuf, PacketScanUniversalStructure> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            PacketScanUniversalStructure::pos,
            PacketScanUniversalStructure::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var universal = MachinePacketGuard.requireUniversal(
                    context, pos, MachinePacketGuard.Action.SCAN_STRUCTURE);
            if (universal instanceof IMultiblockController controller
                    && context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                controller.scanStructure(player.level());
            }
        });
    }
}

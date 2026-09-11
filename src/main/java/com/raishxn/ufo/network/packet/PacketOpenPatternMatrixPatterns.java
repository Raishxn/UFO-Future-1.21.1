package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.network.MachinePacketGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Opens the Matrix library only from its authenticated controller dashboard. */
public record PacketOpenPatternMatrixPatterns(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketOpenPatternMatrixPatterns> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "open_pattern_matrix_patterns"));
    public static final StreamCodec<FriendlyByteBuf, PacketOpenPatternMatrixPatterns> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, PacketOpenPatternMatrixPatterns::pos,
                    PacketOpenPatternMatrixPatterns::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketOpenPatternMatrixPatterns packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            var matrix = MachinePacketGuard.requirePatternMatrix(context, packet.pos(),
                    MachinePacketGuard.Action.OPEN_PATTERN_MANAGEMENT);
            if (matrix != null) matrix.openPatternManagement(player);
        });
    }
}

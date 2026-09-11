package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.crafting.SingularityCraftingMode;
import com.raishxn.ufo.network.MachinePacketGuard;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSetSingularityMode(BlockPos pos, int mode) implements CustomPacketPayload {
    public static final Type<PacketSetSingularityMode> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "set_singularity_mode"));
    public static final StreamCodec<ByteBuf, PacketSetSingularityMode> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketSetSingularityMode::pos,
            ByteBufCodecs.VAR_INT, PacketSetSingularityMode::mode,
            PacketSetSingularityMode::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSetSingularityMode packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var singularity = MachinePacketGuard.requireFabricationSingularity(
                    context, packet.pos(), MachinePacketGuard.Action.SET_CRAFTING_MODE);
            if (singularity != null) {
                singularity.setCraftingMode(SingularityCraftingMode.byOrdinal(packet.mode()));
            }
        });
    }
}

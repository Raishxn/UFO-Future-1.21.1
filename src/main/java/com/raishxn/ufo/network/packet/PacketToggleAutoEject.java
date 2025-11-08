package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
import com.raishxn.ufo.util.ConfigType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleAutoEject(BlockPos pos, int typeOrdinal) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketToggleAutoEject> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "toggle_auto_eject"));

    public static final StreamCodec<ByteBuf, PacketToggleAutoEject> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketToggleAutoEject::pos,
            ByteBufCodecs.INT, PacketToggleAutoEject::typeOrdinal,
            PacketToggleAutoEject::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketToggleAutoEject payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity be = player.level().getBlockEntity(payload.pos());
                if (be instanceof DimensionalMatterAssemblerBlockEntity dma) {
                    // Garante que o ordinal é válido para evitar crashes
                    if (payload.typeOrdinal >= 0 && payload.typeOrdinal < ConfigType.values().length) {
                        ConfigType type = ConfigType.values()[payload.typeOrdinal];
                        dma.setAutoEject(type, !dma.isAutoEject(type));
                    }
                }
            }
        });
    }
}
package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.util.ConfigType;
import com.raishxn.ufo.util.IOMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Adicionado 'nextModeOrdinal' ao record
public record PacketChangeSideConfig(BlockPos pos, Direction side, int typeOrdinal, int nextModeOrdinal) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketChangeSideConfig> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "change_side_config"));

    public static final StreamCodec<ByteBuf, PacketChangeSideConfig> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketChangeSideConfig::pos,
            Direction.STREAM_CODEC, PacketChangeSideConfig::side,
            ByteBufCodecs.INT, PacketChangeSideConfig::typeOrdinal,
            ByteBufCodecs.INT, PacketChangeSideConfig::nextModeOrdinal, // Novo campo
            PacketChangeSideConfig::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
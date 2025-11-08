package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.entity.DimensionalMatterAssemblerBlockEntity;
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

public record PacketChangeSideConfig(BlockPos pos, Direction side, int typeOrdinal) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketChangeSideConfig> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "change_side_config"));

    public static final StreamCodec<ByteBuf, PacketChangeSideConfig> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketChangeSideConfig::pos,
            Direction.STREAM_CODEC, PacketChangeSideConfig::side,
            ByteBufCodecs.INT, PacketChangeSideConfig::typeOrdinal,
            PacketChangeSideConfig::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketChangeSideConfig payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity be = player.level().getBlockEntity(payload.pos());
                if (be instanceof DimensionalMatterAssemblerBlockEntity dma) {
                    if (payload.typeOrdinal >= 0 && payload.typeOrdinal < ConfigType.values().length) {
                        ConfigType type = ConfigType.values()[payload.typeOrdinal];
                        IOMode current = dma.getSideConfig(type, payload.side());

                        // Lógica de ciclo: Energia só tem NONE ou ENERGY. Outros ciclam tudo.
                        IOMode next = current.next();
                        if (type == ConfigType.ENERGY) {
                            next = (current == IOMode.NONE) ? IOMode.ENERGY : IOMode.NONE;
                        }
                        // Adicione mais filtros aqui se Coolant só puder ser Input, etc.

                        dma.setSideConfig(type, payload.side(), next);
                    }
                }
            }
        });
    }
}
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

public record PacketToggleAutoInput(BlockPos pos, int typeOrdinal) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketToggleAutoInput> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "toggle_auto_input"));

    public static final StreamCodec<ByteBuf, PacketToggleAutoInput> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketToggleAutoInput::pos,
            ByteBufCodecs.INT, PacketToggleAutoInput::typeOrdinal,
            PacketToggleAutoInput::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketToggleAutoInput payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Verifica se a posição está carregada para evitar crashes em áreas não carregadas
                if (player.level().isLoaded(payload.pos)) {
                    BlockEntity be = player.level().getBlockEntity(payload.pos());
                    if (be instanceof DimensionalMatterAssemblerBlockEntity dma) {
                        // Garante que o ordinal do tipo é válido
                        if (payload.typeOrdinal >= 0 && payload.typeOrdinal < ConfigType.values().length) {
                            ConfigType type = ConfigType.values()[payload.typeOrdinal];
                            // Alterna o estado atual (se estava true vira false, e vice-versa)
                            dma.setAutoInput(type, !dma.isAutoInput(type));
                        }
                    }
                }
            }
        });
    }
}
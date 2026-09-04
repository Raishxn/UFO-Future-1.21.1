package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.api.multiblock.MultiblockAutoBuildService;
import com.raishxn.ufo.network.MachinePacketGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketAutoBuildMultiblock(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketAutoBuildMultiblock> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("ufo", "auto_build_multiblock"));
    public static final StreamCodec<FriendlyByteBuf, PacketAutoBuildMultiblock> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketAutoBuildMultiblock::pos, PacketAutoBuildMultiblock::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            BlockEntity target = MachinePacketGuard.requireStellar(context, pos, MachinePacketGuard.Action.AUTO_BUILD);
            if (target == null) {
                var universal = MachinePacketGuard.requireUniversal(context, pos, MachinePacketGuard.Action.AUTO_BUILD);
                if (universal instanceof BlockEntity blockEntity) target = blockEntity;
            }
            if (target != null) MultiblockAutoBuildService.start(player, target);
        });
    }
}

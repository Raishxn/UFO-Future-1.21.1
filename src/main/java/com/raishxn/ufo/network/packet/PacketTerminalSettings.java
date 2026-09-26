package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.StructureScannerItem;
import com.raishxn.ufo.item.StructureTerminalSettings;
import com.raishxn.ufo.network.MachinePacketGuard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketTerminalSettings(int handOrdinal, boolean replace, boolean dismantle, boolean ae,
                                     int tier) implements CustomPacketPayload {

    public static final Type<PacketTerminalSettings> TYPE = new Type<>(UfoMod.id("terminal_settings"));

    public static final StreamCodec<FriendlyByteBuf, PacketTerminalSettings> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.handOrdinal);
                buf.writeBoolean(p.replace);
                buf.writeBoolean(p.dismantle);
                buf.writeBoolean(p.ae);
                buf.writeVarInt(p.tier);
            },
            buf -> new PacketTerminalSettings(
                    buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                    buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTerminalSettings packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!MachinePacketGuard.allow(context, MachinePacketGuard.Action.CONFIGURE_SCANNER)) return;
            if (packet.handOrdinal < 0 || packet.handOrdinal >= InteractionHand.values().length) return;
            ItemStack stack = context.player().getItemInHand(InteractionHand.values()[packet.handOrdinal]);
            if (!(stack.getItem() instanceof StructureScannerItem)) return;
            StructureTerminalSettings.setReplaceMode(stack, packet.replace());
            StructureTerminalSettings.setDismantleMode(stack, packet.dismantle());
            StructureTerminalSettings.setAeMode(stack, packet.ae());
            StructureTerminalSettings.setFieldTier(stack, packet.tier());
        });
    }
}

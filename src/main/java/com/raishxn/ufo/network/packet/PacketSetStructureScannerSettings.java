package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.StructureScannerItem;
import com.raishxn.ufo.item.StructureScannerSettings;
import com.raishxn.ufo.network.MachinePacketGuard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSetStructureScannerSettings(int handOrdinal, int modeOrdinal, boolean hatchMode,
                                                int fieldTier, boolean useAeNetwork)
        implements CustomPacketPayload {
    public static final Type<PacketSetStructureScannerSettings> TYPE =
            new Type<>(UfoMod.id("set_structure_scanner_settings"));
    public static final StreamCodec<FriendlyByteBuf, PacketSetStructureScannerSettings> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, PacketSetStructureScannerSettings::handOrdinal,
                    ByteBufCodecs.VAR_INT, PacketSetStructureScannerSettings::modeOrdinal,
                    ByteBufCodecs.BOOL, PacketSetStructureScannerSettings::hatchMode,
                    ByteBufCodecs.VAR_INT, PacketSetStructureScannerSettings::fieldTier,
                    ByteBufCodecs.BOOL, PacketSetStructureScannerSettings::useAeNetwork,
                    PacketSetStructureScannerSettings::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSetStructureScannerSettings packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!MachinePacketGuard.allow(context, MachinePacketGuard.Action.CONFIGURE_SCANNER)) return;
            if (packet.handOrdinal < 0 || packet.handOrdinal >= InteractionHand.values().length) return;
            ItemStack stack = context.player().getItemInHand(InteractionHand.values()[packet.handOrdinal]);
            if (!(stack.getItem() instanceof StructureScannerItem)) return;
            new StructureScannerSettings(
                    StructureScannerSettings.Mode.byOrdinal(packet.modeOrdinal), packet.hatchMode,
                    Math.clamp(packet.fieldTier, 1, 3), packet.useAeNetwork).write(stack);
        });
    }
}

package com.raishxn.ufo.network;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.IHasCycleableModes;
import com.raishxn.ufo.item.custom.IEnergyTool;
import com.raishxn.ufo.network.packet.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(UfoMod.MOD_ID).versioned("1.0");

        registrar.playToServer(CycleToolKeyPacket.TYPE, CycleToolKeyPacket.STREAM_CODEC,
                ModPackets::handleCycleToolKey);
        registrar.playToServer(CycleModeKeyPacket.TYPE, CycleModeKeyPacket.STREAM_CODEC,
                ModPackets::handleCycleModeKey);

        registrar.playToServer(
                PacketToggleAutoEject.TYPE,
                PacketToggleAutoEject.STREAM_CODEC,
                PacketToggleAutoEject::handle
        );
        registrar.playToServer(
                PacketToggleAutoInput.TYPE,
                PacketToggleAutoInput.STREAM_CODEC,
                PacketToggleAutoInput::handle
        );
        registrar.playToServer(
                PacketChangeSideConfig.TYPE,
                PacketChangeSideConfig.STREAM_CODEC,
                PacketChangeSideConfig::handle
        );
        registrar.playToServer(
                ToggleAutoSmeltPacket.TYPE,
                ToggleAutoSmeltPacket.STREAM_CODEC,
                ToggleAutoSmeltPacket::handle
        );
    }

    private static void handleCycleToolKey(final CycleToolKeyPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player.getMainHandItem().getItem() instanceof IEnergyTool tool) {
                tool.transformTool(player.level(), player, InteractionHand.MAIN_HAND, packet.forward());
            }
        });
    }

    private static void handleCycleModeKey(final CycleModeKeyPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof IHasCycleableModes tool) {
                tool.cycleMode(stack, player);
            }
        });
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
}